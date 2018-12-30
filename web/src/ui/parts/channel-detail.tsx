import * as React from "react";
import { Button, Grid, Icon, Paper, Typography } from "@material-ui/core";
import { ChannelRepo, ChannelState } from "../../repo/channel-repo";
import { lazyComponent } from "../util/lazy-component";
import { observer } from "mobx-react";
import { Model } from "../../model";
import { UserPool } from "../../repo/app-repo";
import { ChangeEvent } from "react";

interface ChannelDetailProps {
  channelRepo: ChannelRepo;
  userPool: UserPool;
}

@observer
export class ChannelDetail extends React.Component<ChannelDetailProps> {

  render() {
    const { channelRepo, userPool } = this.props;

    if (channelRepo.state !== ChannelState.joined) {
      return "joining ... ";
    }

    const messages = Array.from(channelRepo.messages.values());

    return (
      <Grid container item xs direction="column" style={{ marginTop: 8 }}>
        <div className="grow-1 chat-history">
          <MessageList messages={messages} userPool={userPool}/>
        </div>
        <MessageDraft/>
      </Grid>
    );
  }
}

class MessageDraft extends React.Component<{ submitDisabled?: boolean, onSubmit?(text: string): void }, { draft: string }> {
  state = {
    draft: '',
  };

  onSubmit() {
    if (this.props.onSubmit) {
      this.props.onSubmit(this.state.draft);
      this.setState({ draft: '' });
    }
  }

  onChange(ev: ChangeEvent<HTMLTextAreaElement>) {
    this.setState({ draft: ev.target.value });
  }

  render() {
    const { draft } = this.state;
    const submitDisabled = this.props.submitDisabled || !draft.trim();
    return (
      <div style={{ display: 'flex', }}>
        <textarea className="grow-1"
                  style={{ height: 64, resize: 'none', }}
                  onChange={this.onChange}
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
  (p1, p2) => p1.messages.length === p2.messages.length);

const MessageListItem = lazyComponent(
  (props: { msg: Model.ChatMessage, userPool: UserPool }) => {
    const u = props.userPool.get(props.msg.userUuid);

    if (!u) {
      throw new Error('user not in userPool');
    }
    return (
      <Paper elevation={1} style={{ padding: 8 }}>
        <Typography>
          {props.msg.text}
        </Typography>
      </Paper>
    );
  },
  (p1, p2) => p1.msg.uuid === p2.msg.uuid);
