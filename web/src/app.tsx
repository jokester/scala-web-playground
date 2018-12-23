import * as React from "react";
import { DemoLayout } from "./ui/sketch/demo-layout";
import { getWebpackEnv } from "./util";
import { AppRepo } from "./repo/app-repo";
import { createEventPipe } from "./realworld/ws-event-pipe";
import { wait } from "./commonutil/async";

export class App extends React.Component {
  render(): React.ReactNode {
    return <DemoLayout/>;
  }
}

export function createRepo() {
  const buildEnv = getWebpackEnv<{ REACT_APP_WS_URL: string }>();
  const pipe = createEventPipe(buildEnv.REACT_APP_WS_URL);
  const repo = new AppRepo(pipe);


  return repo;
}

export async function tryRepo() {
  const repo = createRepo();
  await repo.startConnect("nick");

  const channelRepo = repo.getChannelRepo("chan1");


}

export async function tryConnection() {
  const buildEnv = getWebpackEnv<{ REACT_APP_WS_URL: string }>();
  const pipe = createEventPipe(buildEnv.REACT_APP_WS_URL);
  const repo = new AppRepo(pipe);
  await repo.startConnect("nick");
  await repo.auth("otp");
  const chan1 = await pipe.sink.joinChannel('chan1');
  await pipe.sink.sendChat(chan1.channel.uuid, "mesg1");
  await wait(3e3);
  await pipe.sink.sendChat(chan1.channel.uuid, "mesg3");
  await wait(3e3);
  // await pipe.sink.leaveChannel(chan1.channel.uuid);
}
