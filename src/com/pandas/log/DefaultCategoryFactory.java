package com.pandas.log;


import com.pandas.log.Logger;
import com.pandas.log.logger.LoggerFactory;

public class DefaultCategoryFactory implements LoggerFactory {
    DefaultCategoryFactory(){}
    @Override
    public Logger makeNewLoggerInstance(String name) {
        return new Logger(name);
    }
}
