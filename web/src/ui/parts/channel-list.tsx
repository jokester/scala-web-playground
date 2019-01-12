import * as React from "react";
import { Avatar, Button, Grid, Input, List, ListItem, ListItemText } from "@material-ui/core/es";
import { Group as GroupIcon, GroupAdd as GroupAddIcon } from "@material-ui/icons";
import { lazyComponent } from "../util/lazy-component";
import { AppRepo } from "../../repo";
import { observer } from "mobx-react";
import { ChannelState } from "../../repo/channel-repo";

interface ChannelView {
  name: string;
  userCount: number;
  selected: boolean;
  state: ChannelState;
}

interface ChannelListProps {
  appRepo: AppRepo;
  currentChannel?: string;

  onSwitchChannel(channelName: string): void;

  onLeaveChannel(channelName: string): void;

  onJoinChannel(channelName: string): void;
}

const JoinedChannel = lazyComponent((props: ChannelView & Pick<ChannelListProps, "onSwitchChannel" | "onLeaveChannel">) => (
  <ListItem button selected={props.selected} onClick={() => props.onSwitchChannel(props.name)}>
    <Avatar>
      <GroupIcon/>
    </Avatar>
    <ListItemText>
      <div> {`${props.name} / ${props.userCount} user (s)`} </div>
      <Button variant="contained" color="primary" disabled={props.state !== ChannelState.joined} onClick={() => props.onLeaveChannel(props.name)}>Leave</Button>
    </ListItemText>
  </ListItem>
));

class NewChannel extends React.Component<Pick<ChannelListProps, "onJoinChannel">, { channelName: string }> {
  state = {
    channelName: '',
  };

  onChange = (ev: { target: { value: string } }) => {
    this.setState({ channelName: (ev.target.value || '').trim() });
  };

  onSubmit = (ev: unknown) => {
    if (this.state.channelName) {
      this.props.onJoinChannel(this.state.channelName);
      this.setState({ channelName: '' });
    }
  };

  render() {
    return (
      <ListItem button divider>
        <Avatar>
          <GroupAddIcon/>
        </Avatar>
        <ListItemText>
          <Input placeholder="Channel name" onChange={this.onChange} value={this.state.channelName}/>
          <br/>
          <Button variant="contained" color="primary" disabled={!this.state.channelName} onClick={this.onSubmit}>Join</Button>
        </ListItemText>
      </ListItem>
    );
  }
}

@observer
export class ChannelList extends React.Component<ChannelListProps, never> {

  renderJoinedChannels() {
    const { appRepo, currentChannel } = this.props;
    const channels = Array.from(appRepo.appState.channels.keys());

    return channels.map(c => (
      <JoinedChannel
        key={c}
        selected={c === currentChannel}
        name={c}
        state={appRepo.getChannelRepo(c)!.state}
        userCount={appRepo.appState.channels.get(c)!.userUuids.length}
        onSwitchChannel={this.props.onSwitchChannel}
        onLeaveChannel={this.props.onLeaveChannel}
      />
    ));
  }

  render() {
    return (
      <Grid item xs={3}>
        <List>
          {this.renderJoinedChannels()}
          <NewChannel onJoinChannel={this.props.onJoinChannel}/>
        </List>
      </Grid>
    );
  }
}
