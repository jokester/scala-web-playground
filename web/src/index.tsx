import React from 'react';
import ReactDOM from 'react-dom';
import './index.scss';
import { App } from './app';
import * as serviceWorker from './serviceWorker';
import { getLogger, isProdBuild } from "./util";
import { createRepo } from "./repo/app-repo";
import { tryRepo } from "./repo/dummy";

const logger = getLogger('src/index.tsx', "debug");

function registerHMR() {
  type ModuleHMR = typeof module & {
    hot?: {
      accept(dependencies: string | string[], callback: (updatedDependencies: any[]) => void): void
    }
  };

  if ((module as ModuleHMR).hot) {
    (module as ModuleHMR).hot!.accept('./app', render);
  }
}

const appRepo = createRepo();

function render() {
  ReactDOM.render(
    <App appRepo={appRepo}/>,
    document.getElementById('root') as HTMLElement
  );
  logger.debug("rendered");
}

if (isProdBuild) {
  // TODO: should enable SW in future
  // If you want your app to work offline and load faster, you can change
  // unregister() to register() below. Note this comes with some pitfalls.
  // Learn more about service workers: http://bit.ly/CRA-PWA
  serviceWorker.unregister();
} else {
  registerHMR();
  tryRepo(appRepo);
}

render();
