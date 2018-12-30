import * as React from "react";
import * as PcUI from './pc-layout';
import { AppRepo } from "../repo";

interface UiProps {
  appRepo: AppRepo;
}

interface UIState {
  channels: string[];
  // none for
  currentChannel?: string;
}

export class PcLayout extends React.Component<UiProps, UIState> {

  render(): React.ReactNode {
    return undefined;
  }
}

export class AppLayout extends React.Component<{}, {}> {
  render(): React.ReactNode {
    return (
      <>
        {
          /*
          mobile layout (narrow):
          TOP TAB (channels)
          MESSAGES
          DRAFT / SEND

          -------

          PC layout (wide):

          LEFT bar

           */
        }
      </>
    );
  }
}
