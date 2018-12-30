import { action, observable, runInAction } from "mobx";
import { Model } from "../model";
import { getLogger } from "../util";
import { WsEventSource, WsEventSink, WsConnection } from "../realworld";
import { ChatroomChatMessage, ServerChannelBroadcast, ServerJoinedChannel } from "../../src-gen";
import { Debug } from "../util/debug";
import { nonce8 } from "../util/string-util";

const logger = getLogger('chan-repo');

export interface ChannelStore {
  state: ChannelState;
  messages: ReadonlyMap<string, Model.ChatMessage>;
  userUuids: ReadonlyArray<string>;
  userPool: ReadonlyMap<string, Readonly<Model.User>>;
}

export enum ChannelState {
  joining = "ChannelState.joining",
  joined = "ChannelState.joined",
  left = "ChannelState.left",
}

/**
 * mobx-observable repo
 */
export class ChannelRepo implements ChannelStore {

  @observable
  state = ChannelState.left;

  @observable
  latestMessageUuid?: string = undefined;

  @observable
  readonly messages = new Map<string, Model.ChatMessage>();

  @observable.ref
  userUuids: string[] = [];

  private uuid?: string;

  constructor(readonly channelName: string,
              private readonly userIdentity: Readonly<Model.User>,
              readonly userPool: Map<string, Readonly<Model.User>>,
              private readonly conn: WsConnection,
              private readonly eventSrc: WsEventSource,
              private readonly eventSink: WsEventSink) {
  }

  @action
  async join() {
    this.assertState(ChannelState.left, "expected ChannelState.left");
    this.state = ChannelState.joining;
    try {
      const joined = await this.eventSink.joinChannel(this.channelName);
      this.onJoined(joined);
    } catch (e) {
      runInAction(() => this.state = ChannelState.left);
    }
  }

  @action
  async sendMessage(text: string) {
    this.assertState(ChannelState.joined);

    const tmpMessage: Model.ChatMessage = {
      text,
      uuid: nonce8(),
      userUuid: this.userIdentity.uuid,
      channelUuid: this.uuid!,
    };

    this.messages.set(tmpMessage.uuid, tmpMessage);

    try {
      const { msg } = await this.eventSink.sendChat(this.uuid!, text);
      runInAction(() => {
        this.messages.delete(tmpMessage.uuid);
        this.messages.set(msg.uuid, mapChatMessage(msg));
      });
    } catch (e) {
      runInAction(() => {
        tmpMessage.failed = true;
      });
    }

  }

  @action
  onChannelBroadcast(b: ServerChannelBroadcast) {
    Debug.assert(this.uuid === b.channelUuid && this.channelName === b.channelName, "channel uuid or channelName mismatch");

    if (b.leftUsers.length || b.joinedUsers.length) {
      // merge users
      const newUserSet = new Set<string>(this.userUuids);
      for (const u of b.joinedUsers) {
        newUserSet.add(u);
      }
      for (const u of b.leftUsers) {
        newUserSet.delete(u);
      }
      this.userUuids = Array.from(newUserSet);
    }

    for (const m of b.newMessages) {
      // merge messages
      this.messages.set(m.uuid, mapChatMessage(m));
      this.latestMessageUuid = m.uuid;
    }
  }

  @action
  async leave() {
    this.assertState(ChannelState.joined);
    try {
      const { reason } = await this.eventSink.leaveChannel(this.uuid!);
      logger.debug(`left channel=${this.channelName}: ${reason}`);
    } finally {
      runInAction(() => {
        this.uuid = undefined;
        this.state = ChannelState.left;
      });
    }
  }

  @action
  private onJoined(joined: ServerJoinedChannel) {
    this.assertState(ChannelState.joining);
    const { channel, users, history } = joined;

    this.state = ChannelState.joined;
    this.uuid = channel.uuid;

    for (const u of users) {
      this.userPool.set(u.uuid, u);
    }

    this.userUuids = users.map(u => u.uuid);

    for (const m of history) {
      if (!this.messages.has(m.uuid)) {
        this.messages.set(m.uuid, mapChatMessage(m));
        this.latestMessageUuid = m.uuid;
      }
    }

    this.state = ChannelState.joined;
  }

  private assertState(expectedState: ChannelState, message = `expected ChannelState to be ${expectedState}`) {
    Debug.assert(this.state === expectedState, message);
  }
}

function mapChatMessage(m: ChatroomChatMessage): Model.ChatMessage {
  return ({
    uuid: m.uuid,
    userUuid: m.userUuid,
    channelUuid: m.channelUuid,
    text: m.text,
    timestamp: m.timestamp,
  });
}
