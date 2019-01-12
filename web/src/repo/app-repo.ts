import { ChannelRepo, ChannelState, ChannelStore } from "./channel-repo";
import { action, observable, runInAction } from "mobx";
import { createEventPipe, WsState } from "../realworld";
import { ServerBroadcast } from "../../src-gen";
import { Model } from "../model";
import { getLogger, getWebpackEnv } from "../util";
import { Debug } from "../util/debug";
import { DeepReadonly } from "../commonutil/type/freeze";

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

// read-only obj pools for UI
export type UserPool = ReadonlyMap<string, DeepReadonly<Model.User>>;

const logger = getLogger('app-repo.ts');

export class AppRepo {

  @observable
  readonly appState: AppStore = {
    connStatus: WsState.inited,
    identity: undefined,
    channels: observable(new Map<string, ChannelStore>()),
  };

  get userPool(): UserPool {
    return this._userPool;
  }

  private readonly _userPool = new Map<string, Model.User>();

  private readonly _channelRepos = new Map<string, ChannelRepo>();

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
        this._userPool.set(u.uuid, u);
      }
      for (const c of m.channels) {
        if (this._channelRepos.has(c.channelName)) {
          const channelRepo = this._channelRepos.get(c.channelName)!;
          channelRepo.onChannelBroadcast(c);
        } else {
          logger.warn("got broadcast from not joined channel", c);
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
  createChannelRepo(channelName: string) {
    this.assertAuthed();
    if (!this._channelRepos.has(channelName)) {
      const { conn, source, sink } = this.pipe;
      const newRepo = new ChannelRepo(channelName, this.appState.identity!, this._userPool, conn, source, sink);
      this._channelRepos.set(channelName, newRepo);
      this.appState.channels.set(channelName, newRepo);
      return newRepo;
    }
    return this._channelRepos.get(channelName)!;
  }

  getChannelRepo(channelName: string) {
    this.assertAuthed();
    return this._channelRepos.get(channelName)!;
  }

  @action
  leaveChannel(channelName: string) {
    this.assertAuthed();
    const c = this._channelRepos.get(channelName)!;
    if (c && c.state === ChannelState.joined) {
      c.leave();
      this._channelRepos.delete(channelName);
      this.appState.channels.delete(channelName);
      return;
    }
    throw new Error('attempted to leave a not-joined channel');
  }

  private assertAuthed() {
    Debug.assert(
      this.appState.connStatus === WsState.connected && this.appState.identity,
      "getChannelRepo() required auth");
  }
}
