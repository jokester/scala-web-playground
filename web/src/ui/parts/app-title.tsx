import { AppBar, CircularProgress, Toolbar, Typography } from "@material-ui/core";
import * as React from "react";

export function AppTitle(props: { connecting: boolean }) {
  const connectingIndicator = props.connecting
    ? <CircularProgress color="secondary"/>
    : null;
  return (
    <AppBar>
      <Toolbar>
        <Typography variant="h6" color="inherit">
          Yet another chatroom
        </Typography>
        {connectingIndicator}
      </Toolbar>
    </AppBar>
  );
}
