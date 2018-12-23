import { action, observable, runInAction } from "mobx";
import { Model } from "../model";
import { getLogger } from "../util";
import { WsEventSource } from "../realworld/ws-event-source";
import { WsEventSink } from "../realworld/ws-event-sink";
import { WsConnection } from "../realworld/ws-connection";
import { ChatroomChatMessage, ChatroomUserInfo, ServerChannelBroadcast, ServerJoinedChannel } from "../../src-gen";
import { Debug } from "../util/debug";

const logger = getLogger(__filename);

export interface ChannelStore {
  state: ChannelState;
  messageUuids: ReadonlySet<string>;
  latestMessageUuid?: string;
  userUuids: ReadonlyArray<string>;
  userPool: ReadonlyMap<string, Model.User>;
}

export enum ChannelState {
  joining = 1,
  joined = 2,
  left = 3,
}

/**
 * mobx-observable repo
 */
export class ChannelRepo implements ChannelStore {

  @observable
  state = ChannelState.left;

  @observable
  latestMessageUuid?: string = undefined;

  // FIXME: change to sorted (by time) keys of messagePool.keys()
  messageUuids = new Set<string>();

  readonly messagePool = new Map<string, Model.ChatMessage>();

  @observable.ref
  userUuids: string[] = [];

  private uuid?: string;

  constructor(readonly name: string,
              readonly userPool: Map<string, ChatroomUserInfo>,
              private readonly conn: WsConnection,
              private readonly eventSrc: WsEventSource,
              private readonly eventSink: WsEventSink) {
  }

  @action
  async join() {
    this.assertState(ChannelState.left, "expected ChannelState.left");
    this.state = ChannelState.joining;
    try {
      const joined = await this.eventSink.joinChannel(name);
      this.onJoined(joined);
    } catch (e) {
      runInAction(() => this.state = ChannelState.left);
    }
  }

  @action
  onChannelBroadcast(b: ServerChannelBroadcast) {
    Debug.assert(this.uuid === b.channelUuid, "channel uuid mismatch");

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
      // FIXME: should prevent duplicate
      this.messagePool.set(m.uuid, mapChatMessage(m));
      this.messageUuids.add(m.uuid);
      this.latestMessageUuid = m.uuid;
    }
  }

  @action
  leave() {
    this.assertState(ChannelState.joined, "not joined");
    const leave = this.eventSink.leaveChannel(this.uuid!);
    try {
      this.conn.sendMessage(leave);
      runInAction(() => {
        this.state = ChannelState.left;
        this.uuid = undefined;
      });
    } catch (e) {
      runInAction(() => this.state = ChannelState.left);
    }
  }

  @action
  private onJoined(joined: ServerJoinedChannel) {
    this.assertState(ChannelState.joining, "expected ChannelState.joining");
    const { channel, users, history } = joined;

    this.state = ChannelState.joined;
    this.uuid = channel.uuid;

    for (const u of users) {
      this.userPool.set(u.uuid, u);
    }

    this.userUuids = users.map(u => u.uuid);

    for (const m of history) {
      if (!this.messagePool.has(m.uuid)) {
        this.messagePool.set(m.uuid, mapChatMessage(m));
        this.messageUuids.add(m.uuid);
        this.latestMessageUuid = m.uuid;
      }
    }

    this.state = ChannelState.joined;
  }

  private assertState(expectedState: ChannelState, message: string) {
    Debug.assert(this.state === expectedState, message);
  }
}

function mapChatMessage(m: ChatroomChatMessage): Model.ChatMessage {
  return ({
    userUuid: m.userUuid,
    channelUuid: m.channelUuid,
    text: m.text,
    sent: {
      uuid: m.uuid,
      timestamp: m.timestamp,
    },
  });
}
