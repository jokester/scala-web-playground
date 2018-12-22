import { action, observable, runInAction } from "mobx";
import { Model } from "../model";
import { getLogger } from "../util";
import { WsEventSource } from "../realworld/ws-event-source";
import { WsEventSink } from "../realworld/ws-event-sink";
import { WsConnection } from "../realworld/ws-connection";

const logger = getLogger(__filename);

export interface ChannelStore {
  state: ChannelState;
  messages: Model.ChatMessage[];
  userUuids: string[];
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
  messages: Model.ChatMessage[] = [];

  @observable
  userUuids: string[] = [];

  private uuid?: string;

  constructor(readonly name: string,
              private readonly conn: WsConnection,
              private readonly eventSrc: WsEventSource,
              private readonly eventSink: WsEventSink) {
  }

  @action
  async join() {
    this.assertState(ChannelState.left, "already joining");
    this.state = ChannelState.joining;
    try {
      const { channel } = await this.eventSink.joinChannel(name);
      runInAction(() => {
        this.state = ChannelState.joined;
        this.uuid = channel.uuid;
        // FIXME: merge history
      });
    } catch (e) {
      runInAction(() => this.state = ChannelState.left);
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

  private assertState(expectedState: ChannelState, message: string) {
    if (expectedState !== this.state) {
      throw new Error(message);
    }
  }
}
