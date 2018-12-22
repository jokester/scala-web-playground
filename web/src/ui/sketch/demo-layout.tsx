import * as React from 'react';
import { createMuiTheme, createStyles, MuiThemeProvider } from '@material-ui/core/styles';
import purple from '@material-ui/core/colors/purple';
import green from '@material-ui/core/colors/green';
import CssBaseline from '@material-ui/core/CssBaseline';
import withStyles from '@material-ui/core/styles/withStyles';
import {
  AppBar, Avatar, Button, Divider, Grid, Icon, List, ListItem, ListItemText, Toolbar, Typography, withWidth, Theme, Paper, CircularProgress
} from "@material-ui/core";

import { Work as WorkIcon, Group as GroupIcon, GroupAdd as GroupAddIcon } from '@material-ui/icons';

const theme = createMuiTheme({
  palette: {
    primary: purple,
    secondary: green,
  },
  typography: {
    useNextVariants: true,
  },
  breakpoints: {
    keys: [
      // xs: phone (portrait)
      // [0, 320] dp
      "xs",
      // md: phone ( ) / tablet
      // [320, 640] dp
      "md",
      // lg: wider than md
      // [640, inf] dp
      "lg",
    ],
    values: {
      xs: 0,
      sm: NaN,
      md: 320,
      lg: 640,
      xl: NaN,
    },
  },
});

const MyWidth = withWidth()((props: { width: string }) => {
  return <span>Current width: {props.width}</span>;
});

export function DemoLayout() {
  const styles = (theme: Theme) =>
    createStyles({
      root: {
        textAlign: 'center',
        paddingTop: theme.spacing.unit * 20,
      },
    });
  const StyledContent = withStyles(styles)(DemoLayoutContent);
  return (
    <MuiThemeProvider theme={theme}>
      <CssBaseline/>
      <StyledContent/>
    </MuiThemeProvider>
  );
}

function DemoLayoutContent() {
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
