import * as React from "react";
import { Avatar, Grid, List, ListItem, ListItemText } from "@material-ui/core";
import { Group as GroupIcon, GroupAdd as GroupAddIcon } from "@material-ui/icons";
import { lazyComponent } from "../util/lazy-component";

interface ChannelView {
  name: string;
  userCount: number;
}

interface ChannelListProps {
  channels: ChannelView[];
  currentChannel?: string;

  onChannelSelect?(channel: string): void;

  onJoinChannel?(channel: string): void;
}

interface ChannelListState {

}

const JoinedChannel = lazyComponent((props: ChannelView & { selected: boolean }) => (
  <ListItem button selected={props.selected}>
    <Avatar>
      <GroupIcon/>
    </Avatar>
    <ListItemText primary={props.name} secondary={`${props.userCount} users`}/>
  </ListItem>
));

export class ChannelList extends React.Component<ChannelListProps> {

  renderJoinedChannels() {
    const { channels, currentChannel } = this.props;
    return channels.map(
      c => <JoinedChannel key={c.name} selected={c.name === currentChannel} {...c}/>);
  }

  render() {
    return (
      <Grid item xs={3}>
        <List>
          {this.renderJoinedChannels()}

          <ListItem button divider>
            <Avatar>
              <GroupAddIcon/>
            </Avatar>
            <ListItemText primary="Join"/>
          </ListItem>
        </List>
      </Grid>
    );
  }
}
