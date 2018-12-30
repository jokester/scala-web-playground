import * as React from "react";
import { AppRepo } from "./repo";

interface UiProps {
  appRepo: AppRepo;
}

interface UIState {
  channels: string[];
  // none for
  currentChannel?: string;
}

export class App extends React.Component<UiProps, UIState> {

  render() {
    return (
      null
    );
  }
}
