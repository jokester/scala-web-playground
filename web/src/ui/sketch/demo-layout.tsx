import * as React from 'react';
import {
  AppBar,
  Avatar,
  Button,
  CircularProgress,
  Grid,
  Icon,
  List,
  ListItem,
  ListItemText,
  Paper,
  Toolbar,
  Typography,
  withWidth
} from "@material-ui/core";

import { Group as GroupIcon, GroupAdd as GroupAddIcon } from '@material-ui/icons';
import { injectMuiTheme } from "../theme";

const MyWidth = withWidth()((props: { width: string }) => {
  return <span>Current width: {props.width}</span>;
});

export const DemoLayout = injectMuiTheme(DemoLayoutContent);

function DemoLayoutContent(props: {}) {
  const open = false;
  return (
    <div className="" style={{ height: 'calc(100vh)', display: 'flex', }}>
      <TitleBar/>
      <Grid container className="grow-1" style={{ marginTop: 48, padding: 8, }}>
        <Grid item xs={12} container>
          <SideBar/>
          <JoinedRoom/>
        </Grid>
      </Grid>
    </div>
  );
}

function TitleBar() {
  return (
    <AppBar>
      <Toolbar>
        <Typography variant="h6" color="inherit">
          Talkative
        </Typography>
        <CircularProgress color="secondary"/>
        <Typography>
          <MyWidth/>
        </Typography>
      </Toolbar>
    </AppBar>
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

function SideBar() {
  return (
    <Grid item xs={3}>
      <List>
        <ListItem button selected>
          <Avatar>
            <GroupIcon/>
          </Avatar>
          <ListItemText primary="Group 1" secondary="1 person"/>
        </ListItem>
        <ListItem button divider>
          <Avatar>
            <GroupIcon/>
          </Avatar>
          <ListItemText primary="Group 2" secondary="1 person"/>
        </ListItem>
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
