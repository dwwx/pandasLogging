package com.pandas.log.config;

import com.pandas.log.logger.LoggerRepository;

import java.io.InputStream;
import java.net.URL;

public interface Configurator {
    public static final String INHERITED = "inherited";
    public static final String NULL = "null";
    void doConfigure(InputStream inputStream, LoggerRepository repository);
    void doConfigure(URL url, LoggerRepository repository);
}
