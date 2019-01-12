import * as React from "react";
import { Avatar, Grid, List, ListItem, ListItemText, Input , Button} from "@material-ui/core/es";
import { Group as GroupIcon, GroupAdd as GroupAddIcon } from "@material-ui/icons";
import { lazyComponent } from "../util/lazy-component";
import { AppRepo } from "../../repo";
import { observer } from "mobx-react";

interface ChannelView {
  name: string;
  userCount: number;
}

interface ChannelListProps {
  appRepo: AppRepo;
  currentChannel?: string;

  onSwitchChannel(channel: string): void;

  onJoinChannel(channel: string): void;
}

const JoinedChannel = lazyComponent((props: ChannelView & { selected: boolean; onClick?(c: string): void; }) => (
  <ListItem button selected={props.selected} onClick={props.onClick ? () => props.onClick!(props.name) : undefined}>
    <Avatar>
      <GroupIcon/>
    </Avatar>
    <ListItemText primary={props.name} secondary={props.userCount > 1 ? `${props.userCount} users online` : `${props.userCount} user online`}/>
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
          {" "}
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

    return channels.map(
      c => <JoinedChannel
        key={c}
        selected={c === currentChannel}
        name={c}
        userCount={appRepo.appState.channels.get(c)!.userUuids.length}
        onClick={this.props.onSwitchChannel}/>);
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
