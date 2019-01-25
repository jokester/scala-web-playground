import { getWebpackEnv } from "../util";
import { AppRepo, createRepo } from "./app-repo";
import { wait } from "../commonutil/async";
import { createEventPipe } from "../realworld";

export async function defaultRoutine(appRepo: AppRepo) {
  await wait(1e3);

  await appRepo.startConnect();

  const userName = (prompt("Input a username", "Anonymous User") || '').trim();

  if (!userName) {
    alert("Username cannot be empty. Refresh page to try again");
    return;
  }

  await appRepo.auth(userName, "otp");

  const channelRepo = appRepo.createChannelRepo("general");

  await channelRepo.join();
}

export async function tryRepoRoutine(appRepo: AppRepo) {

  await wait(1e3);
  await appRepo.startConnect();

  await appRepo.auth("tryRepoRoutine", "otp");

  const channelRepo = appRepo.createChannelRepo("chan1");

  await channelRepo.join();

  for (let i = 0; i < 1e4; ++i) {
    await channelRepo.sendMessage(`chan1-msg${i}`);
    await wait(3e3);
  }
}

export async function tryConnection() {
  const buildEnv = getWebpackEnv<{ REACT_APP_WS_URL: string }>();
  const pipe = createEventPipe(buildEnv.REACT_APP_WS_URL);
  const appRepo = new AppRepo(pipe);
  await appRepo.startConnect();
  await appRepo.auth("nick", "otp");

  const chan1 = await pipe.sink.joinChannel('chan1');
  await pipe.sink.sendChat(chan1.channel.name, "mesg1");
  await wait(3e3);
  await pipe.sink.sendChat(chan1.channel.name, "mesg3");
  await wait(3e3);
  // await pipe.sink.leaveChannel(chan1.channel.uuid);
}
