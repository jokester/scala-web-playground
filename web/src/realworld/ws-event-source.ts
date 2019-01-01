import {
  ChatroomCommand,
  ServerAuthed,
  ServerBaseRes,
  ServerBroadcast,
  ServerJoinedChannel,
  ServerLeftChannel,
  ServerPong,
  ServerSentChat,
} from "../../src-gen";
import { TypedEventEmitter } from "../util/typed-event-emitter";
import { getLogger } from "../util";

const logger = getLogger('ws-event-src');

export interface DecodeEventMap {
  pong: ServerPong;

  fail: ServerBaseRes;

  authed: ServerAuthed;

  joinedChannel: ServerJoinedChannel;

  leftChannel: ServerLeftChannel;

  sentChat: ServerSentChat;
  broadcast: ServerBroadcast;
}

const internalEventOfSeq = (seq: number) => `waitBySeq_${seq}`;
const internalEventOfCmd = (cmd: string) => `waitByCmd_${cmd}`;
const haveError = (msg: ServerBaseRes) => msg.cmd.errors && msg.cmd.errors.length;

export class WsEventSource extends TypedEventEmitter<DecodeEventMap> {

  nextOfSeq<ExpectedType extends ServerBaseRes>(seq: number): Promise<ExpectedType> {
    return new Promise<ExpectedType>((fulfill, reject) => {
      this.onceInternal(internalEventOfSeq(seq), (msg: ExpectedType) => {
        if (haveError(msg)) reject(msg); else fulfill(msg);
      });
    });
  }

  nextOfCmd<ExpectedType extends ServerBaseRes>(cmd: string): Promise<ExpectedType> {
    return new Promise<ExpectedType>((fulfill, reject) => {
      this.onceInternal(internalEventOfCmd(cmd), (msg: ExpectedType) => {
        if (haveError(msg)) reject(msg); else fulfill(msg);
      });
    });
  }

  /**
   * @param raw ev.data of a websocket MessageEvent
   */
  feedMsg(raw: unknown) {
    try {
      logger.debug("handling message", raw);
      this.doOnMsg(raw);
    } catch (e) {
      // not really necessary: bottom of call stack is likely to be an event handler
      logger.error("error handling raw message", e);
    }
  }

  private doOnMsg(raw: unknown) {
    if (typeof raw !== 'string') {
      logger.warn("got non-string raw message");
      return;
    }

    const decoded: ServerBaseRes = JSON.parse(raw);
    if (typeof decoded !== 'object' || !decoded) {
      logger.warn("got non-object after JSON.parse", decoded);
      return;
    }

    const cmd: ChatroomCommand = decoded.cmd;

    if (!(cmd && cmd.seq && cmd.method)) {
      logger.warn("cannot dispatch by cmd");
      return;
    }

    this.emitInternal(internalEventOfCmd(cmd.method), decoded)
      .emitInternal(internalEventOfSeq(cmd.seq), decoded);

    switch (cmd.method) {
      case 'Pong':
        this.emit("pong", decoded);
        return;
      case 'Authed':
        this.emit("authed", decoded as any);
        return;
      case 'JoinedChannel':
        this.emit("joinedChannel", decoded as any);
        return;
      case 'LeftChannel':
        this.emit("leftChannel", decoded as any);
        return;
      case 'SentChat':
        this.emit("sentChat", decoded as any);
        return;
      case 'Broadcast':
        this.emit('broadcast', decoded as any);
        return;
      case 'Fail':
        this.emit('fail', decoded);
        return;
    }

    logger.warn('unhandled message', decoded);
  }
}
