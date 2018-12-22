
import { storiesOf } from '@storybook/react';
import { action } from '@storybook/addon-actions';
import { linkTo } from '@storybook/addon-links';

import * as React from "react";

import { DemoButton1 } from "../src/ui/sketch/demo-button";

storiesOf("DemoButton1", module)
  .add("variant 1", () => <DemoButton1 action={action("ACTION")}/>);
