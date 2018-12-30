import { getWebpackEnv } from "../util";
import { AppRepo, createRepo } from "./app-repo";
import { wait } from "../commonutil/async";
import { createEventPipe } from "../realworld";

export async function tryRepo() {
  const appRepo = createRepo();
  await appRepo.startConnect();

  await appRepo.auth("tryRepo", "otp");

  const channelRepo = appRepo.getChannelRepo("chan1");

  await channelRepo.join();

  await channelRepo.sendMessage("chan1-msg1");

  await wait(3e3);

  await channelRepo.sendMessage("chan1-msg2");

  await wait(3e3);

  await channelRepo.leave();
}

export async function tryConnection() {
  const buildEnv = getWebpackEnv<{ REACT_APP_WS_URL: string }>();
  const pipe = createEventPipe(buildEnv.REACT_APP_WS_URL);
  const appRepo = new AppRepo(pipe);
  await appRepo.startConnect();
  await appRepo.auth("nick", "otp");

  const chan1 = await pipe.sink.joinChannel('chan1');
  await pipe.sink.sendChat(chan1.channel.uuid, "mesg1");
  await wait(3e3);
  await pipe.sink.sendChat(chan1.channel.uuid, "mesg3");
  await wait(3e3);
  // await pipe.sink.leaveChannel(chan1.channel.uuid);
}
