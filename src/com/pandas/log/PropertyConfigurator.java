package com.pandas.log;

import com.pandas.log.config.Configurator;
import com.pandas.log.helper.LogLog;
import com.pandas.log.helper.OptionConverter;
import com.pandas.log.logger.LoggerFactory;
import com.pandas.log.logger.LoggerRepository;

import javax.swing.text.html.Option;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Hashtable;
import java.util.Properties;


//进行properties的解析
public class PropertyConfigurator implements Configurator {
    protected Hashtable registry = new Hashtable(11);
    private LoggerRepository repository;
    protected LoggerFactory loggerFactory = new DefaultCategoryFactory();
    static final String      CATEGORY_PREFIX = "log4j.category.";
    static final String      LOGGER_PREFIX   = "log4j.logger.";
    static final String       FACTORY_PREFIX = "log4j.factory";
    static final String    ADDITIVITY_PREFIX = "log4j.additivity.";
    static final String ROOT_CATEGORY_PREFIX = "log4j.rootCategory";
    static final String ROOT_LOGGER_PREFIX   = "log4j.rootLogger";
    static final String      APPENDER_PREFIX = "log4j.appender.";
    static final String      RENDERER_PREFIX = "log4j.renderer.";
    static final String      THRESHOLD_PREFIX = "log4j.threshold";
    private static final String      THROWABLE_RENDERER_PREFIX = "log4j.throwableRenderer";
    private static final String LOGGER_REF	= "logger-ref";
    private static final String ROOT_REF		= "root-ref";
    private static final String APPENDER_REF_TAG 	= "appender-ref";

    public static final String LOGGER_FACTORY_KEY = "log4j.loggerFactory";
    private static final String RESET_KEY = "log4j.reset";

    static final private String INTERNAL_ROOT_NAME = "root";

    @Override
    public void doConfigure(InputStream inputStream, LoggerRepository repository) {

    }

    @Override
    public void doConfigure(URL url, LoggerRepository repository) {
        Properties props = new Properties();
        LogLog.debug("Reading configuration from URL " +url);
        InputStream istream = null;
        URLConnection uConn = null;
        try{
            uConn = url.openConnection();
            uConn.setUseCaches(false);
            istream = uConn.getInputStream();
            props.load(istream);
        }catch (Exception e){
            if (e instanceof InterruptedIOException || e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            LogLog.error("Could not read configuration file from URL [" + url
                    + "].", e);
            LogLog.error("Ignoring configuration file [" + url +"].");
            return;
        }finally {
            if(istream != null){
                try{
                    istream.close();
                }catch (InterruptedIOException ignore){
                    Thread.currentThread().interrupt();
                }catch(IOException ignore) {
                } catch(RuntimeException ignore) {
                }
            }
        }
        doConfigure(props, repository);
    }

    public void doConfigure(Properties properties, LoggerRepository hierarchy) {
        repository = hierarchy;
        String value = properties.getProperty(LogLog.DEBUG_KEY);
        if(value == null){
            value = properties.getProperty("log4j.configDebug");
            if(value != null){
                LogLog.warn("[log4j.configDebug] is deprecated. Use [log4j.debug] instead.");
            }
        }
        if(value != null) {
            LogLog.setInternalDebugging(OptionConverter.toBoolean(value, true));
        }
        String reset = properties.getProperty(RESET_KEY);
        if(reset != null && OptionConverter.toBoolean(reset, false)){
            hierarchy.resetConfiguration();
        }
        //取出thresholdStr
        String thresholdStr = OptionConverter.findAndSubst(THRESHOLD_PREFIX, properties);
        if(thresholdStr != null){
            hierarchy.setThreshold(OptionConverter.toLevel(thresholdStr, Level.ALL));
            LogLog.debug("Hierarchy threshold set to ["+hierarchy.getThreshold()+"].");
        }
        //配置根信息
        configureRootCategory(properties, hierarchy);
        //配置logger工厂
        configureLoggerFactory(properties);
        //配置基本信息
        parseCatsAndRenderers(properties, hierarchy);

        LogLog.debug("Finished configuring.");
        registry.clear();
    }

    void parseCatsAndRenderers(Properties properties, LoggerRepository hierarchy) {
    }

    void configureLoggerFactory(Properties properties) {
    }

    void configureRootCategory(Properties properties, LoggerRepository hierarchy) {
        String effectiveFrefix = ROOT_LOGGER_PREFIX;
        String value = OptionConverter.findAndSubst(ROOT_LOGGER_PREFIX, properties);
        if(value == null){
            value = OptionConverter.findAndSubst(ROOT_CATEGORY_PREFIX, properties);
            effectiveFrefix = ROOT_CATEGORY_PREFIX;
        }
        if(value == null){
            LogLog.debug("Could not find root logger information. Is this OK?");
        }else{
            Logger root = hierarchy.getRootLogger();
            synchronized (root){
                parseCategory(properties, root, effectiveFrefix, INTERNAL_ROOT_NAME, value);
            }
        }
    }

    void parseCategory(Properties properties, Logger root, String effectiveFrefix, String internalRootName, String value) {
    }
}
