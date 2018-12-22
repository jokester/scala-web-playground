import { getLogger } from "../util";
import { TypedEventEmitter } from "../util/typed-event-emitter";

const logger = getLogger(__filename);
const MaxConnectionCount = 5;

interface WsEventMap {
  statusChange: WsState;
  tooManyDisconnect: void;
  message: unknown;
}

export enum WsState {
  inited = "WsState.inited",
  disconnected = "WsState.disconnected",
  connecting = "WsState.connecting",
  connected = "WsState.connected",
}

export class WsConnection extends TypedEventEmitter<WsEventMap> {
  private state = WsState.inited;
  private connectCount = 0;

  constructor(private readonly wsUrl: string) {
    super();
    this.setState(WsState.disconnected);
  }

  ///////////////

  private socket: WebSocket | null = null;

  startConnect() {
    if (this.socket) {
      throw new Error("socket already exists");
    }

    const newSocket = new WebSocket(this.wsUrl);
    this.socket = newSocket;
    this.setState(WsState.connecting);

    newSocket.onopen = (ev) => {
      this.setState(WsState.connected);
    };

    newSocket.onclose = (ev) => {
      this.setState(WsState.disconnected);
      this.socket = null;
      if (++this.connectCount < MaxConnectionCount) {
        setTimeout(() => this.startConnect());
      } else {
        logger.debug("not auto-reconnecting after", this.connectCount, "attempts");
        this.emit("tooManyDisconnect", undefined);
      }
    };

    newSocket.onerror = (ev) => {
      logger.error("ws error occurred", ev);
      // error event should always be followed by a close event. reconnecting in close handler.
    };

    newSocket.onmessage = (ev) => {
      logger.debug("received ws message", ev.data);
      this.emit("message", ev.data);
    };
  }

  waitConnect() {
    if (this.state === WsState.connected) {
      return Promise.resolve();
    }
    return new Promise<void>(f => {
      this.smartOnce("statusChange", (newStatus: WsState) => {
        if (newStatus === WsState.connected) {
          f();
          return true;
        }
        return false;
      });
    });
  }

  sendMessage(msgObj: {}) {
    if (this.state === WsState.connected) {
      logger.debug("sending ws message", msgObj);
      const msg = JSON.stringify(msgObj);
      this.socket!.send(msg);
    } else {
      throw new Error("ws not connected");
    }
  }

  private setState(newState: WsState, forceNotifyChange = false) {
    if (newState !== this.state || forceNotifyChange) {
      this.state = newState;
      this.emit("statusChange", newState);
    }
  }
}
