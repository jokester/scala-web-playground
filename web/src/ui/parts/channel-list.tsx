import * as React from "react";

interface ChannelListProps {
  channels: string[];
  currentChannel?: string;
  onChannelSelect(channel: string): void;
}

interface ChannelListState {

}

export class ChannelList extends React.Component<ChannelList> {

}
