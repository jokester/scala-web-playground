import { ChannelRepo, ChannelStore } from "./channel-repo";
import { action, observable, runInAction } from "mobx";
import { WsState, createEventPipe } from "../realworld";
import { ServerBroadcast } from "../../src-gen";
import { Model } from "../model";
import { getLogger, getWebpackEnv } from "../util";
import { Debug } from "../util/debug";

export function createRepo() {
  const buildEnv = getWebpackEnv<{ REACT_APP_WS_URL: string }>();
  const pipe = createEventPipe(buildEnv.REACT_APP_WS_URL);
  return new AppRepo(pipe);
}

export interface AppStore {
  connStatus: WsState;
  identity?: Model.User;
  channels: Map<string, ChannelStore>;
}

const logger = getLogger(__filename);

export class AppRepo {

  @observable
  readonly appState: AppStore = {
    connStatus: WsState.inited,
    identity: undefined,
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
  startConnect() {
    Debug.assert(this.appState.connStatus === WsState.inited, "already started connection");
    const { conn } = this.pipe;
    conn.startConnect();

    return conn.waitConnect();
  }

  async auth(name: string, otp: string) {
    const { conn, sink } = this.pipe;
    await conn.waitConnect();
    const authed = await sink.userAuth(name, otp);
    runInAction(() => {
      this.appState.identity = authed.identity;
    });
    return { ...authed.identity };
  }

  @action
  getChannelRepo(channelName: string) {
    Debug.assert(
      this.appState.connStatus === WsState.connected && this.appState.identity,
      "getChannelRepo() required auth");
    if (!this.channelRepos.has(channelName)) {
      const { conn, source, sink } = this.pipe;
      const newRepo = new ChannelRepo(channelName, this.appState.identity!, this.userPool, conn, source, sink);
      this.channelRepos.set(channelName, newRepo);
      this.appState.channels.set(channelName, newRepo);
    }
    return this.channelRepos.get(channelName)!;
  }
}
