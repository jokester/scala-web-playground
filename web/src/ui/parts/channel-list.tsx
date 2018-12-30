import * as React from "react";
import { Avatar, Grid, List, ListItem, ListItemText } from "@material-ui/core";
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

  onSwitchChannel?(channel: string): void;

  onAddChannel?(channel: string): void;
}

interface ChannelListState {
  newChannel: string;
}

const JoinedChannel = lazyComponent((props: ChannelView & { selected: boolean }) => (
  <ListItem button selected={props.selected}>
    <Avatar>
      <GroupIcon/>
    </Avatar>
    <ListItemText primary={props.name} secondary={`${props.userCount} users`}/>
  </ListItem>
));

@observer
export class ChannelList extends React.Component<ChannelListProps, ChannelListState> {

  state: ChannelListState = {
    newChannel: '',
  };

  renderJoinedChannels() {
    const { appRepo, currentChannel } = this.props;
    const channels = Array.from(appRepo.appState.channels.keys());

    return channels.map(
      c => <JoinedChannel key={c} selected={c === currentChannel} name={c} userCount={20}/>);
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
