import { ChannelStore, ChannelRepo } from "./channel-repo";
import { action, observable, runInAction } from "mobx";
import { WsState } from "../realworld/ws-connection";
import { createEventPipe } from "../realworld/ws-event-pipe";

export interface AppStore {
  connStatus: WsState;
  nickname?: string;
  channels: Map<string, ChannelStore>;
}

export class AppRepo {

  @observable
  readonly appState: AppStore = {
    connStatus: WsState.inited,
    channels: observable(new Map<string, ChannelStore>()),
  };

  private readonly channelRepos = new Map<string, ChannelRepo>();

  /**
   * pipe: flow of WS messages  [repo -> pipe.sink -> pipe.conn -> pipe.source -> repo]
   * @param pipe
   */
  constructor(private readonly pipe: ReturnType<typeof createEventPipe>) {

    const { appState } = this;
    const { conn, source } = this.pipe;
    conn.on("statusChange", (newStatus: WsState) => {
      runInAction(() => appState.connStatus = newStatus);
    });
    conn.on("message", (m: unknown) => {
      source.feedMsg(m);
    });
  }

  @action
  startConnect(nickname: string) {
    const { conn } = this.pipe;
    this.appState.nickname = nickname;
    conn.startConnect();

    return conn.waitConnect();
  }

  async auth(otp: string) {
    const { conn, sink } = this.pipe;
    await conn.waitConnect();
    await sink.userAuth(this.appState.nickname!, otp);
  }

  @action
  getChannelRepo(channel: string) {
    if (!this.channelRepos.has(channel)) {
      const { conn, source, sink } = this.pipe;
      const newRepo = new ChannelRepo(channel, conn, source, sink);
      this.channelRepos.set(channel, newRepo);
      this.appState.channels.set(channel, newRepo);
    }
    return this.channelRepos.get(channel)!;
  }
}
