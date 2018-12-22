import * as React from "react";
import { DemoLayout } from "./ui/sketch/demo-layout";
import { getWebpackEnv } from "./util/webpack-env";
import { AppRepo } from "./repo/app-repo";
import { createEventPipe } from "./realworld/ws-event-pipe";

export class App extends React.Component {
  render(): React.ReactNode {
    return <DemoLayout/>;
  }
}

export function createRepo() {
  const buildEnv = getWebpackEnv<{ REACT_APP_WS_URL: string }>();
  const pipe = createEventPipe(buildEnv.REACT_APP_WS_URL);
  const repo = new AppRepo(pipe);

  repo.startConnect("nick");

  return repo;
}

export async function tryConnection() {
  const buildEnv = getWebpackEnv<{ REACT_APP_WS_URL: string }>();
  const pipe = createEventPipe(buildEnv.REACT_APP_WS_URL);
  const repo = new AppRepo(pipe);
  await repo.startConnect("nick");
  await repo.auth("otp");
  const chan1 = await pipe.sink.joinChannel('chan1');
  await pipe.sink.sendChat(chan1.channel.uuid, "mesg1");
}
