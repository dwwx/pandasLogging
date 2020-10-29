package com.pandas.log.appender;

import java.util.Enumeration;

//attaching appenders to objects
//对appender的集中管理
public interface AppenderAttachable {
    public void addAppender(Appender newAppender);
    public Enumeration getAllAppenders();
    public Appender getAppender(String name);
    public boolean isAttached(Appender appender);
    void removeAllAppenders();
    void removeAppender(Appender appender);
    void removeAppender(String name);
}
