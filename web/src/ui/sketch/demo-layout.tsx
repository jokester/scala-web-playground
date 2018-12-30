import * as React from 'react';
import { Button, Grid, Icon, Paper, Typography, withWidth } from "@material-ui/core";
import { injectMuiTheme } from "../theme";
import { AppTitle } from "../parts/app-title";
import { ChannelList } from "../parts";

const MyWidth = withWidth()((props: { width: string }) => {
  return <span>Current width: {props.width}</span>;
});

export const DemoLayout = injectMuiTheme(DemoLayoutContent);

function DemoLayoutContent(props: {}) {
  const open = false;
  return (
    <div className="" style={{ height: 'calc(100vh)', display: 'flex', }}>
      <AppTitle connecting={true}/>
      <Grid container className="grow-1" style={{ marginTop: 48, padding: 8, }}>
        <Grid item xs={12} container>
          <ChannelList channels={[]}/>
          <JoinedRoom/>
        </Grid>
      </Grid>
    </div>
  );
}

function JoinedRoom() {
  return (
    <Grid container item xs direction="column" style={{ marginTop: 8 }}>
      <div className="grow-1 chat-history">
        <Paper elevation={1} style={{ padding: 8 }}>
          <Typography>
            line1
          </Typography>
        </Paper>
      </div>
      <div style={{ display: 'flex', }}>
        <textarea className="grow-1"
                  style={{ height: 64, resize: 'none', }}
                  defaultValue="tttt"/>
        <Button variant="contained" color="primary">
          <Icon>send</Icon>
          Send
        </Button>
      </div>
    </Grid>
  );
}

function UnjoinedRoom() {
  return (
    <Grid container item xs direction="column" style={{ marginTop: 8 }}>
      <Paper className="grow-1 chat-history">
      </Paper>
    </Grid>
  );
}

