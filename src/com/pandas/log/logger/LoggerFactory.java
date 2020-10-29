package com.pandas.log.logger;

import com.pandas.log.Logger;

public interface LoggerFactory {
    public Logger makeNewLoggerInstance(String name);
}
