import { getWebpackEnv, isProdBuild } from "./util";
import { Debug } from "./util/debug";

interface BuildConfig {
  wsUrl: string;
}

const webpackEnv = getWebpackEnv();
const wsProtocol = /^https/i.test(location.protocol) ? 'wss' : 'ws';

function createDevBuildConfig(): Readonly<BuildConfig> {
  Debug.assert(webpackEnv.REACT_APP_WS_ORIGIN, "REACT_APP_WS_ORIGIN not defined");
  Debug.assert(webpackEnv.REACT_APP_WS_PATH, "REACT_APP_WS_PATH not defined");
  const wsOrigin = webpackEnv.REACT_APP_WS_ORIGIN;
  const wsUrl = [
    wsOrigin ,
    webpackEnv.REACT_APP_WS_PATH,
  ].join("");
  return { wsUrl };
}

function createBuildConfig(): Readonly<BuildConfig> {
  Debug.assert(webpackEnv.REACT_APP_WS_ORIGIN, "REACT_APP_WS_ORIGIN not defined");
  Debug.assert(webpackEnv.REACT_APP_WS_PATH, "REACT_APP_WS_PATH not defined");
  const wsUrl = [
    webpackEnv.REACT_APP_WS_ORIGIN,
    webpackEnv.REACT_APP_WS_PATH,
  ].join("");
  console.log("wsUrl", wsUrl);
  return { wsUrl };
}

export const buildConfig = isProdBuild ? createBuildConfig() : createDevBuildConfig();
