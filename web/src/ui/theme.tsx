import { createMuiTheme, createStyles, MuiThemeProvider, Theme } from "@material-ui/core";
import purple from "@material-ui/core/colors/purple";
import green from "@material-ui/core/colors/green";
import * as React from "react";
import CssBaseline from '@material-ui/core/CssBaseline';
import withStyles from '@material-ui/core/styles/withStyles';

export const theme = createMuiTheme(
  {
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

export function injectMuiTheme<P>(uiRoot: React.Factory<P>): React.Factory<P> {

  const styles = (theme: Theme) =>
    createStyles(
      {
        root: {
          textAlign: 'center',
          paddingTop: theme.spacing.unit * 20,
        },
      });

  const StyledContent = withStyles(styles)(uiRoot as any);
  return (props: undefined | (React.Attributes & P), ...children: React.ReactNode[]) => (
    <MuiThemeProvider theme={theme}>
      <CssBaseline/>
      <StyledContent {...props} children={children}/>
    </MuiThemeProvider>
  );
}
