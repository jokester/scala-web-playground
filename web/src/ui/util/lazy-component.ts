import * as React from "react";
import { isEqual, isEqualWith } from 'lodash-es';

/**
 * PureComponent that re-renders when
 * @param c
 * @param comparator
 */
export function lazyComponent<P>(
  c: React.ComponentClass<P> | React.FunctionComponent<P>,
  comparator: (currentProps: P, nextProps: P) => boolean = isEqual): React.ComponentClass<P> {

  class ComponentWithDeep extends React.Component<P> {
    shouldComponentUpdate(nextProps: Readonly<P>, nextState: Readonly<{}>, nextContext: any): boolean {
      return !comparator(this.props, nextProps);
    }
    render(): React.ReactNode {
      return React.createElement(c, this.props);
    }
  }

  return ComponentWithDeep;
}
