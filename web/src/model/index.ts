export namespace Model {

  export interface ChatMessage {
    uuid: string;
    content: string;
    author: Human;
    sentAt: Time | null;
  }

  export interface Human {
    nickname: string;
  }

  export interface Time {
    timestamp: number;
  }
}
