import * as React from "react";
import { AppRepo, WsState } from "./repo";
import { injectMuiTheme } from "./ui/theme";
import { ChannelList } from "./ui/parts";
import { AppTitle } from "./ui/parts/app-title";
import { Grid } from "@material-ui/core";
import { ChannelDetail } from "./ui/parts/channel-detail";
import { computed } from "mobx";
import { Debug } from "./util/debug";
import { getLogger } from "./util";

interface UiProps {
  appRepo: AppRepo;
}

interface UIState {
  // none for
  currentChannel?: string;
}

const logger = getLogger('app.tsx');

class AppLayout extends React.Component<UiProps, UIState> {

  state: UIState = {};

  get appRepo() {
    return this.props.appRepo;
  }

  @computed
  get channels() {
    return Array.from(this.appRepo.appState.channels.keys());
  }

  onAddChannel = (channel: string) => {
    logger.debug('onAddChannel', channel);

    const trimmedChannel = channel.trim();
    if (!trimmedChannel) return;

    const { channels } = this;

    if (channels.indexOf(trimmedChannel) !== -1) {
      this.appRepo.getChannelRepo(trimmedChannel).join();
      this.setState(
        {
          currentChannel: trimmedChannel,
        });
    } else {
      this.onSwitchChannel(trimmedChannel);
    }
  }

  onSwitchChannel = (currentChannel: string) => {
    logger.debug('onSwitchChannel', currentChannel);
    this.setState({ currentChannel });
  }

  renderChannelList() {
    const { appRepo, channels } = this;
    const { currentChannel } = this.state;

    return (
      <ChannelList
        appRepo={appRepo}
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
      return <ChannelDetail channelRepo={cRepo} userPool={userPool}/>;
    }
    return null;
  }

  render() {
    const connecting = this.appRepo.appState.connStatus !== WsState.connected;
    return (
      <div className="" style={{ height: 'calc(100vh)', display: 'flex', }}>
        <AppTitle connecting={connecting}/>
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
