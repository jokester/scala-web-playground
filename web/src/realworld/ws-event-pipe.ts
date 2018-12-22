import { WsConnection } from "./ws-connection";
import { WsEventSource } from "./ws-event-source";
import { WsEventSink } from "./ws-event-sink";

export function createEventPipe(wsUrl: string) {
  const conn = new WsConnection(wsUrl);
  const source = new WsEventSource();
  const sink = new WsEventSink(conn, source);
  return { conn, source, sink };
}
