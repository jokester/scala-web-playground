import * as React from "react";
import { ChangeEvent } from "react";
import { last } from 'lodash-es';

import { Button, Grid, Icon, Paper, Typography } from "@material-ui/core";
import { ChannelRepo, ChannelState } from "../../repo/channel-repo";
import { lazyComponent } from "../util/lazy-component";
import { observer } from "mobx-react";
import { Model } from "../../model";
import { UserPool } from "../../repo/app-repo";
import { getLogger } from "../../util";
import { KeyboardEventHandler } from "react";

interface ChannelDetailProps {
  channelRepo: ChannelRepo;
  userPool: UserPool;
}

const logger = getLogger('channel-detail');

@observer
export class ChannelDetail extends React.Component<ChannelDetailProps> {

  onSubmit = (text: string) => {
    const { channelRepo, userPool } = this.props;
    channelRepo.sendMessage(text);
  };

  render() {
    const { channelRepo, userPool } = this.props;

    if (!channelRepo) {
      return "leaving ...";
    }

    if (channelRepo.state !== ChannelState.joined) {
      return "joining ...";
    }

    const messages = Array.from(channelRepo.messages.values());

    return (
      <Grid container item xs direction="column" style={{ marginTop: 8, flexWrap: 'nowrap' }}>
        <div className="grow-1 chat-history">
          <MessageList messages={messages} userPool={userPool}/>
        </div>
        <MessageDraft onSubmit={this.onSubmit}/>
      </Grid>
    );
  }
}

class MessageDraft extends React.Component<{ submitDisabled?: boolean, onSubmit?(text: string): void }, { draft: string }> {
  state = {
    draft: '',
  };

  onSubmit = () => {
    if (this.state.draft && this.props.onSubmit) {
      this.props.onSubmit(this.state.draft);
      this.setState({ draft: '' });
    }
  };

  onChange = (ev: ChangeEvent<HTMLTextAreaElement>) => {
    this.setState({ draft: ev.target.value });
  };

  onKeyPress = (ev: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (ev.keyCode === 13 && !ev.altKey && !ev.ctrlKey && !ev.shiftKey) {
      // FIXME: likely to have bugs. should test with IMEs and stuff.
      ev.stopPropagation();
      ev.preventDefault();
      this.onSubmit();
    } else {
    }
  };

  render() {
    const { draft } = this.state;
    const submitDisabled = this.props.submitDisabled || !draft.trim();
    return (
      <div style={{ display: 'flex', flexShrink: 0 }}>
        <textarea className="grow-1"
                  style={{ height: 64, resize: 'none', }}
                  onChange={this.onChange}
                  onKeyDown={this.onKeyPress}
                  value={draft}
                  placeholder="content here"
        />
        <Button
          variant="contained"
          color="primary"
          disabled={submitDisabled}
          onClick={this.onSubmit}>
          <Icon>send</Icon>
          Send
        </Button>
      </div>
    );
  }
}

const MessageList = lazyComponent(
  (props: { messages: Model.ChatMessage[], userPool: UserPool }) => {
    const { messages, userPool } = props;

    return (
      <>
        {messages.map(msg => <MessageListItem key={msg.uuid} msg={msg} userPool={userPool}/>)}
      </>
    );
  },
  (p1, p2) => {
    if (p1.messages.length !== p2.messages.length) return false;
    if (p1.messages.length) {
      const m1 = last(p1.messages)!;
      const m2 = last(p2.messages)!;
      if (m1.uuid !== m2.uuid) return false;
    }
    return true;
  });

const MessageListItem = lazyComponent(
  (props: { msg: Model.ChatMessage, userPool: UserPool }) => {
    const { msg, userPool } = props;
    const u = userPool.get(msg.userUuid);
    if (!u) {
      throw new Error('user not in userPool');
    }
    const date = msg.timestamp ? new Date(msg.timestamp!).toString() : 'sending ...';
    return (
      <Paper elevation={1} className="message-list-item">
        <Typography variant="subtitle2">
          {`${date} / ${u.name}:`}
        </Typography>
        <Typography component="pre">
          {msg.text}
        </Typography>
      </Paper>
    );
  },
  (p1, p2) => p1.msg.uuid === p2.msg.uuid);
