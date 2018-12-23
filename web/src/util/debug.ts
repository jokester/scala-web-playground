import { deepFreeze } from "../commonutil/type/freeze";

const _debug = {
  assert(expectedTruthy: any, errorMessage: string) {
    if (!expectedTruthy) throw new Error(`assertion failed: ${errorMessage}`);
  },
};

export const Debug = deepFreeze(_debug);
