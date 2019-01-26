// ENV injected by CRA
interface WebpackEnv {
  NODE_ENV: 'production' | 'development';
  PUBLIC_URL: string;
  REACT_APP_WS_ORIGIN: string;
  REACT_APP_WS_PATH: string;
}

const DefaultWebpackEnv = {
};

export function getWebpackEnv<ExtraEnv extends {} = {}>() {
  return {
    ...DefaultWebpackEnv,
    ... process.env,
  } as any as (WebpackEnv & ExtraEnv);
}

export const isProdBuild = getWebpackEnv().NODE_ENV === 'production';
export const isDevBuild = !isProdBuild;
