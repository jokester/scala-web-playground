import { ChannelStore, ChannelRepo } from "./channel-repo";
import { action, observable, runInAction } from "mobx";
import { WsState } from "../realworld/ws-connection";
import { createEventPipe } from "../realworld/ws-event-pipe";
import { ServerBroadcast } from "../../src-gen";
import { Model } from "../model";
import { getLogger } from "../util";

export interface AppStore {
  connStatus: WsState;
  nickname?: string;
  channels: Map<string, ChannelStore>;
}

const logger = getLogger(__filename);

export class AppRepo {

  @observable
  readonly appState: AppStore = {
    connStatus: WsState.inited,
    channels: observable(new Map<string, ChannelStore>()),
  };

  private readonly userPool = new Map<string, Model.User>();

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
    source.on("broadcast", (m: ServerBroadcast) => {
      for (const u of m.newUsers) {
        this.userPool.set(u.uuid, u);
      }
      for (const c of m.channels) {
        if (this.channelRepos.has(c.channelName)) {
          const channelRepo = this.channelRepos.get(c.channelName)!;
          channelRepo.onChannelBroadcast(c);
        }
      }
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
  getChannelRepo(channelName: string) {
    if (!this.channelRepos.has(channelName)) {
      const { conn, source, sink } = this.pipe;
      const newRepo = new ChannelRepo(channelName, this.userPool, conn, source, sink);
      this.channelRepos.set(channelName, newRepo);
      this.appState.channels.set(channelName, newRepo);
    }
    return this.channelRepos.get(channelName)!;
  }
}
