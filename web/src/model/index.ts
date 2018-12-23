export namespace Model {
  const nonnull = 0;

  export interface ChatMessage {
    userUuid: string;
    channelUuid: string;
    text: string;
    sent?: {
      uuid: string;
      timestamp: string;
    };
    // true when failed to send messages
    failed?: boolean;
  }

  export interface User {
    uuid: string;
    name: string;
  }
}
