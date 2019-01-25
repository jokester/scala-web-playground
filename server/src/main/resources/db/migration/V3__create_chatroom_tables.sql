CREATE TABLE chatroom_user (
  uuid UUID PRIMARY KEY,
  name varchar(100),
  created_at timestamp with time zone
);

CREATE TABLE chatroom_channel (
  uuid UUID PRIMARY KEY,
  name varchar(100),
  created_at timestamp with time zone
);

CREATE TABLE chatroom_chat_message (
  uuid UUID PRIMARY KEY,
  userUuid UUID,
  channelUuid UUID,
  text text,
  created_at timestamp with time zone
);

CREATE INDEX ON chatroom_chat_message (created_at);