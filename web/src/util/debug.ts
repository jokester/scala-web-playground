import { deepFreeze } from "../commonutil/type/freeze";

const _debug = {
  assert(expectedTruthy: any, errorMessage: string): expectedTruthy is true {
    if (!expectedTruthy) throw new Error(`assertion failed: ${errorMessage}`);
    return true;
  },
};

export const Debug = deepFreeze(_debug);
