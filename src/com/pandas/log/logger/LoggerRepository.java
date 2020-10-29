package com.pandas.log.logger;

import com.pandas.log.Level;
import com.pandas.log.Logger;

public interface LoggerRepository {
    public
    abstract
    void resetConfiguration();
    public
    void setThreshold(Level level);
    public
    Level getThreshold();
    public Logger getRootLogger();
}
