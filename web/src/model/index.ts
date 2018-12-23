export namespace Model {
  const nonnull = 0;

  export interface ChatMessage {
    uuid: string;
    userUuid: string;
    channelUuid: string;
    text: string;
    timestamp?: string;
    // true when failed to send messages
    failed?: boolean;
  }

  export interface User {
    uuid: string;
    name: string;
  }
}
