import * as React from "react";
import { AppRepo } from "./repo";
import { injectMuiTheme } from "./ui/theme";
import { ChannelList } from "./ui/parts";
import { AppTitle } from "./ui/parts/app-title";
import { Grid } from "@material-ui/core";
import { ChannelDetail } from "./ui/parts/channel-detail";

interface UiProps {
  appRepo: AppRepo;
}

interface UIState {
  channels: string[];
  // none for
  currentChannel?: string;
}

class AppLayout extends React.Component<UiProps, UIState> {

  state: UIState = {
    channels: [],
  };

  get appRepo() {
    return this.props.appRepo;
  }

  onAddChannel = (channel: string) => {
    const { channels } = this.state;

    const trimmedChannel = channel.trim();
    if (!trimmedChannel) return;

    if (channels.indexOf(trimmedChannel) !== -1) {
      this.appRepo.getChannelRepo(trimmedChannel).join();
      this.setState(
        {
          channels: channels.concat([trimmedChannel]),
          currentChannel: trimmedChannel,
        });
    } else {
      this.onSwitchChannel(trimmedChannel);
    }
  };

  onSwitchChannel = (currentChannel: string) => {
    this.setState({ currentChannel });
  };

  renderChannelList() {
    const { appRepo } = this.props;
    const { channels, currentChannel } = this.state;
    const views = channels.map(c => ({
      name: c,
      userCount: 50,
    }));

    return (
      <ChannelList
        channels={views}
        currentChannel={currentChannel}
        onAddChannel={this.onAddChannel}
        onSwitchChannel={this.onSwitchChannel}
      />
    );

  }

  renderChannelDetail() {
    if (this.state.currentChannel) {
      const cRepo = this.appRepo.getChannelRepo(this.state.currentChannel);
      const userPool = this.appRepo.userPool;
      return <ChannelDetail channelRepo={cRepo} userPool={userPool} />
    }
    return null;
  }

  render() {
    return (
      <div className="" style={{ height: 'calc(100vh)', display: 'flex', }}>
        <AppTitle connecting={true}/>
        <Grid container className="grow-1" style={{ marginTop: 48, padding: 8, }}>
          <Grid item xs={12} container>
            {this.renderChannelList()}
            {this.renderChannelDetail()}
          </Grid>
        </Grid>
      </div>
    );
  }
}

export const App = injectMuiTheme(AppLayout);
