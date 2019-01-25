DROP TABLE chatroom_user;
DROP TABLE chatroom_channel;
DROP TABLE chatroom_chat_message;

CREATE TABLE chatroom_user (
  uuid UUID PRIMARY KEY,
  name varchar(100),
  created_at timestamp with time zone
);

CREATE TABLE chatroom_chat_message (
  uuid UUID PRIMARY KEY,
  user_uuid UUID NOT NULL,
  channel_name text NOT NULL,
  text text NOT NULL,
  created_at timestamp with time zone NOT NULL
);

CREATE INDEX ON chatroom_chat_message (created_at);