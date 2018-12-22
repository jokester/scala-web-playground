import * as React from "react";
import Button from '@material-ui/core/Button';
import { PropsOf } from "@material-ui/core";

export function DemoButton1(props: PropsOf<typeof Button>) {
  return (
    <Button variant="contained" color="primary" {...props}>
      Hello World
    </Button>
  );
}
