import React from 'react';
import ReactDOM from 'react-dom';
import './index.scss';
import { App, createRepo, tryConnection } from './app';
import * as serviceWorker from './serviceWorker';
import { getLogger, isProdBuild } from "./util";

const logger = getLogger(__filename, "debug");

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

function render() {
  ReactDOM.render(
    <App/>,
    document.getElementById('root') as HTMLElement
  );
  logger.debug("rendered {}", { a: 1 }, process.env);
}

if (isProdBuild) {
  // TODO: should enable SW in future
  // If you want your app to work offline and load faster, you can change
  // unregister() to register() below. Note this comes with some pitfalls.
  // Learn more about service workers: http://bit.ly/CRA-PWA
  serviceWorker.unregister();
} else {
  registerHMR();
}

registerHMR();
render();

tryConnection();
