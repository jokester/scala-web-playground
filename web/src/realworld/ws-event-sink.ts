import { WsConnection, WsState } from './ws-connection';
import {
  ChatroomCommand,
  ServerAuthed,
  ServerJoinedChannel, ServerLeftChannel,
  ServerPong, ServerSentChat,
  UserAuth,
  UserJoinChannel,
  UserLeaveChannel,
  UserPing,
  UserSendChat,
} from "../../src-gen";
import { getLogger } from "../util";
import { WsEventSource } from "./ws-event-source";

const logger = getLogger(__filename);

export class WsEventSink {
  private readonly factory = new WsMessageFactory();
  private timer: ReturnType<typeof setInterval> | null = null;

  constructor(private readonly conn: WsConnection, private readonly eventSource: WsEventSource) {
    conn.on("statusChange", (state: WsState) => {
      if (state === WsState.connected) {
        this.timer = setInterval(() => this.ping(), 10e3);
      } else {
        if (this.timer !== null) {
          clearInterval(this.timer);
          this.timer = null;
        }
      }
    });
  }

  async ping() {
    const msg = this.factory.ping();
    const start = Date.now();
    this.conn.sendMessage(msg);
    await this.eventSource.nextOfCmd<ServerPong>("Pong");
    // an estimation of RTT
    return Date.now() - start;
  }

  async userAuth(name: string, otp: string) {
    const msg = this.factory.userAuth(name, otp);
    this.conn.sendMessage(msg);
    return this.eventSource.nextOfSeq<ServerAuthed>(msg.cmd.seq);
  }

  async joinChannel(name: string) {
    const msg = this.factory.joinChannel(name);
    this.conn.sendMessage(msg);
    return this.eventSource.nextOfSeq<ServerJoinedChannel>(msg.cmd.seq);
  }

  async leaveChannel(channelUuid: string) {
    const msg = this.factory.leaveChannel(channelUuid);
    this.conn.sendMessage(msg);
    return this.eventSource.nextOfSeq<ServerLeftChannel>(msg.cmd.seq);
  }

  sendChat(channelUuid: string, text: string) {
    const msg = this.factory.sendChat(channelUuid, text);
    this.conn.sendMessage(msg);
    return this.eventSource.nextOfSeq<ServerSentChat>(msg.cmd.seq);
  }
}

/**
 * stateless factory of ws messages
 */
class WsMessageFactory {
  private msgSeq = 0;

  ping(): UserPing {
    return {
      cmd: this.createCmd("Ping"),
    };
  }

  userAuth(name: string, otp: string): UserAuth {
    return {
      name,
      otp,
      cmd: this.createCmd('Auth'),
    };
  }

  joinChannel(name: string): UserJoinChannel {
    return {
      name,
      cmd: this.createCmd("JoinChannel"),
    };
  }

  leaveChannel(channelUuid: string): UserLeaveChannel {
    return {
      channelUuid,
      cmd: this.createCmd("LeaveChannel"),
    };
  }

  sendChat(channelUuid: string, text: string): UserSendChat {
    return {
      channelUuid,
      text,
      cmd: this.createCmd("SendChat"),
    };
  }

  private createCmd(method: string): ChatroomCommand {
    return {
      method,
      seq: ++this.msgSeq,
    };
  }
}
