package com.pandas.log;

import com.pandas.log.helper.Loader;
import com.pandas.log.helper.LogLog;
import com.pandas.log.helper.OptionConverter;
import com.pandas.log.logger.DefaultRepositorySelector;
import com.pandas.log.logger.LoggerRepository;
import com.pandas.log.logger.NOPLoggerRepository;
import com.pandas.log.logger.RepositorySelector;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;

//第一步先进行配置文件的读取
public class LogManager {
    static public final String DEFAULT_CONFIGURATION_FILE = "log4j.properties";

    static final String DEFAULT_XML_CONFIGURATION_FILE = "log4j.xml";

    static final public String DEFAULT_CONFIGURATION_KEY="log4j.configuration";

    static final public String CONFIGURATOR_CLASS_KEY="log4j.configuratorClass";

    public static final String DEFAULT_INIT_OVERRIDE_KEY =
            "log4j.defaultInitOverride";

    static private Object guard = null;
    static private RepositorySelector repositorySelector;

    //static的静态代码块是什么时候执行的
    static {
        String override = OptionConverter.getSystemProperty(DEFAULT_INIT_OVERRIDE_KEY,null);
        System.out.println(override);
        if(override == null || "false".equalsIgnoreCase(override)){
            //读文件和得到资源主要是利用System和ClassLoader的反射机制获取相应的资源
            String configurationOptionStr = OptionConverter.getSystemProperty(DEFAULT_CONFIGURATION_KEY,null);
            String configuratorClassName = OptionConverter.getSystemProperty(CONFIGURATOR_CLASS_KEY, null);

            URL url = null;
            if(configurationOptionStr == null){
                url = Loader.getResource(DEFAULT_XML_CONFIGURATION_FILE);
                if(url == null){
                    url = Loader.getResource(DEFAULT_CONFIGURATION_FILE);
                }
            }else{
                try{
                    url = new URL(configurationOptionStr);
                }catch (Exception e){
                    url = Loader.getResource(configurationOptionStr);
                }
            }
            if(url != null) {
                System.out.println(url.getFile());
                LogLog.debug("Using URL ["+url+"] for automatic log4j configuration.");
                try {
                    //这里是在获取文件路径和相关的URL之后利用OptionConverter进行相关的设置与配置
                    //
                    OptionConverter.selectAndConfigure(url, configuratorClassName,
                            LogManager.getLoggerRepository());
                } catch (NoClassDefFoundError e) {
                    LogLog.warn("Error during default initialization", e);
                }
            } else {
                LogLog.debug("Could not find resource: ["+configurationOptionStr+"].");
            }
        }else{
            LogLog.debug("Default initialization of overridden by " +
                    DEFAULT_INIT_OVERRIDE_KEY + "property.");
        }
    }

    static
    public LoggerRepository getLoggerRepository() {
        if (repositorySelector == null) {
            repositorySelector = new DefaultRepositorySelector(new NOPLoggerRepository());
            guard = null;
            Exception ex = new IllegalStateException("Class invariant violation");
            String msg =
                    "log4j called after unloading, see http://logging.apache.org/log4j/1.2/faq.html#unload.";
            if (isLikelySafeScenario(ex)) {
                LogLog.debug(msg, ex);
            } else {
                LogLog.error(msg, ex);
            }
        }
        return repositorySelector.getLoggerRepository();
    }
    private static boolean isLikelySafeScenario(final Exception ex) {
        StringWriter stringWriter = new StringWriter();
        ex.printStackTrace(new PrintWriter(stringWriter));
        String msg = stringWriter.toString();
        System.out.println(msg);
        return msg.indexOf("org.apache.catalina.loader.WebappClassLoader.stop") != -1;
    }
}
