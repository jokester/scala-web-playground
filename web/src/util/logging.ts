import { getLogger as getLogLevelLogger, Logger, LogLevelDesc } from "loglevel";
import { noop } from 'lodash-es';

type OurLogger = Pick<Logger, "trace" | "debug" | "info" | "warn" | "error">;

const isProd = typeof process === 'object' && process.env.NODE_ENV === 'production';

export function getLogger(name: string, level: LogLevelDesc = "info"): OurLogger {
  if (isProd) return dummyLogger;
  const logger = getLogLevelLogger(name);
  logger.setLevel(level);
  return logger;
}

const dummyLogger: OurLogger = {
  trace: noop,
  debug: noop,
  info: noop,
  warn: noop,
  error: noop,
};
