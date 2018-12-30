import * as React from "react";
import { isEqual, isEqualWith } from 'lodash-es';

/**
 * PureComponent that re-renders when
 * @param p
 */
export function lazyComponent<P>(p: React.ComponentClass<P> | React.FunctionComponent<P>): React.ComponentClass<P> {

  class ComponentWithDeep extends React.Component<P> {
    shouldComponentUpdate(nextProps: Readonly<P>, nextState: Readonly<{}>, nextContext: any): boolean {
      return !isEqual(this.props, nextProps);
    }
    render(): React.ReactNode {
      return React.createElement(p, this.props);
    }
  }

  return ComponentWithDeep;
}
