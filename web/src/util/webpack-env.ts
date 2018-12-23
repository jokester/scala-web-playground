// ENV injected by CRA
interface WebpackEnv {
  NODE_ENV: 'production' | 'development';
  PUBLIC_URL: string;
}

const DefaultWebpackEnv = {
  REACT_APP_WS_URL: 'ws://127.0.0.1:18080/chatroom/ws',
};

export function getWebpackEnv<ExtraEnv extends {} = {}>() {
  return {
    ...DefaultWebpackEnv,
    ... process.env,
  } as any as (WebpackEnv & ExtraEnv);
}

export const isProdBuild = getWebpackEnv().NODE_ENV === 'production';
export const isDevBuild = !isProdBuild;
