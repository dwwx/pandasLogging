package com.pandas.log.appender;

import com.pandas.log.event.LoggingEvent;

public interface Appender {
    //添加filter的先不管
    public void close();
    public void doAppend(LoggingEvent event);
    public String getName();
    public void setName(String name);
    //异常处理的也暂时不考虑
    //输出布局也暂不考虑，后面再加
}
