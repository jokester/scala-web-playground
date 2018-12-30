import { getLogger as getLogLevelLogger, Logger, LogLevelDesc } from "loglevel";
import { noop } from 'lodash-es';

type OurLogger = Pick<Logger, "trace" | "debug" | "info" | "warn" | "error" | 'getLevel'>;

const isProd = typeof process === 'object' && process.env.NODE_ENV === 'production';

export function getLogger(name: string, level: LogLevelDesc = "WARN"): OurLogger {
  if (isProd) return dummyLogger;
  if (!(name && name.trim()) || name.startsWith('/index'))
    throw new Error('logger must have a nonempty name. do not use __filename');
  const logger = getLogLevelLogger(name);
  logger.setLevel(level, false);
  return logger;
}

const dummyLogger: OurLogger = {
  trace: noop,
  debug: noop,
  info: noop,
  warn: noop,
  error: noop,
  getLevel: () => 5,
};
