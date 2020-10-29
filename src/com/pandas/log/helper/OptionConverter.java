package com.pandas.log.helper;

import com.pandas.log.Level;
import com.pandas.log.PropertyConfigurator;
import com.pandas.log.config.Configurator;
import com.pandas.log.logger.LoggerRepository;

import java.io.InterruptedIOException;
import java.net.URL;
import java.util.Properties;

public class OptionConverter {
    static String DELIM_START = "${";
    static char   DELIM_STOP  = '}';
    static int DELIM_START_LEN = 2;
    static int DELIM_STOP_LEN  = 1;

    public static String getSystemProperty(String key, String def){
        try{
            return System.getProperty(key, def);
        }catch (Throwable e){
            LogLog.debug("Was not allowed to read system property \""+key+"\".");
            return def;
        }
    }
    public
    static
    boolean toBoolean(String value, boolean dEfault) {
        if(value == null) {
            return dEfault;
        }
        String trimmedVal = value.trim();
        if("true".equalsIgnoreCase(trimmedVal)) {
            return true;
        }
        if("false".equalsIgnoreCase(trimmedVal)) {
            return false;
        }
        return dEfault;
    }
    static
    public
    void selectAndConfigure(URL url, String clazz, LoggerRepository hierarchy) {
        Configurator configurator = null;
        String filename = url.getFile();

        if(clazz == null && filename != null && filename.endsWith(".xml")) {
            clazz = "org.apache.log4j.xml.DOMConfigurator";
        }

        if(clazz != null) {
            //如果是xml就进行xml的解析
            LogLog.debug("Preferred configurator class: " + clazz);
            configurator = (Configurator) instantiateByClassName(clazz,
                    Configurator.class,
                    null);
            if(configurator == null) {
                LogLog.error("Could not instantiate configurator ["+clazz+"].");
                return;
            }
        } else {
            //否则就进行properties的解析
            System.out.println("call new PropertyConfigurator();");
            configurator = new PropertyConfigurator();
        }

        configurator.doConfigure(url, hierarchy);
    }
    public
    static
    Object instantiateByClassName(String className, Class superClass,
                                  Object defaultValue) {
        if(className != null) {
            try {
                Class classObj = Loader.loadClass(className);
                if(!superClass.isAssignableFrom(classObj)) {
                    LogLog.error("A \""+className+"\" object is not assignable to a \""+
                            superClass.getName() + "\" variable.");
                    LogLog.error("The class \""+ superClass.getName()+"\" was loaded by ");
                    LogLog.error("["+superClass.getClassLoader()+"] whereas object of type ");
                    LogLog.error("\"" +classObj.getName()+"\" was loaded by ["
                            +classObj.getClassLoader()+"].");
                    return defaultValue;
                }
                return classObj.newInstance();
            } catch (ClassNotFoundException e) {
                LogLog.error("Could not instantiate class [" + className + "].", e);
            } catch (IllegalAccessException e) {
                LogLog.error("Could not instantiate class [" + className + "].", e);
            } catch (InstantiationException e) {
                LogLog.error("Could not instantiate class [" + className + "].", e);
            } catch (RuntimeException e) {
                LogLog.error("Could not instantiate class [" + className + "].", e);
            }
        }
        return defaultValue;
    }
    public
    static
    String findAndSubst(String key, Properties props) {
        String value = props.getProperty(key);
        if(value == null) {
            return null;
        }

        try {
            return substVars(value, props);
        } catch(IllegalArgumentException e) {
            LogLog.error("Bad option value ["+value+"].", e);
            return value;
        }
    }
    public static
    String substVars(String val, Properties props) throws
            IllegalArgumentException {

        StringBuffer sbuf = new StringBuffer();

        int i = 0;
        int j, k;

        while(true) {
            j=val.indexOf(DELIM_START, i);
            if(j == -1) {
                // no more variables
                if(i==0) { // this is a simple string
                    return val;
                } else { // add the tail string which contails no variables and return the result.
                    sbuf.append(val.substring(i, val.length()));
                    return sbuf.toString();
                }
            } else {
                sbuf.append(val.substring(i, j));
                k = val.indexOf(DELIM_STOP, j);
                if(k == -1) {
                    throw new IllegalArgumentException('"'+val+
                            "\" has no closing brace. Opening brace at position " + j
                            + '.');
                } else {
                    j += DELIM_START_LEN;
                    String key = val.substring(j, k);
                    // first try in System properties
                    String replacement = getSystemProperty(key, null);
                    // then try props parameter
                    if(replacement == null && props != null) {
                        replacement =  props.getProperty(key);
                    }

                    if(replacement != null) {
                        // Do variable substitution on the replacement string
                        // such that we can solve "Hello ${x2}" as "Hello p1"
                        // the where the properties are
                        // x1=p1
                        // x2=${x1}
                        String recursiveReplacement = substVars(replacement, props);
                        sbuf.append(recursiveReplacement);
                    }
                    i = k + DELIM_STOP_LEN;
                }
            }
        }
    }
    public
    static
    Level toLevel(String value, Level defaultValue) {
        if(value == null) {
            return defaultValue;
        }

        value = value.trim();

        int hashIndex = value.indexOf('#');
        if (hashIndex == -1) {
            if("NULL".equalsIgnoreCase(value)) {
                return null;
            } else {
                // no class name specified : use standard Level class
                return Level.toLevel(value, defaultValue);
            }
        }

        Level result = defaultValue;

        String clazz = value.substring(hashIndex+1);
        String levelName = value.substring(0, hashIndex);

        // This is degenerate case but you never know.
        if("NULL".equalsIgnoreCase(levelName)) {
            return null;
        }

        LogLog.debug("toLevel" + ":class=[" + clazz + "]"
                + ":pri=[" + levelName + "]");

        try {
            Class customLevel = Loader.loadClass(clazz);

            // get a ref to the specified class' static method
            // toLevel(String, org.apache.log4j.Level)
            Class[] paramTypes = new Class[] { String.class,
                    com.pandas.log.Level.class
            };
            java.lang.reflect.Method toLevelMethod =
                    customLevel.getMethod("toLevel", paramTypes);

            // now call the toLevel method, passing level string + default
            Object[] params = new Object[] {levelName, defaultValue};
            Object o = toLevelMethod.invoke(null, params);

            result = (Level) o;
        } catch(ClassNotFoundException e) {
            LogLog.warn("custom level class [" + clazz + "] not found.");
        } catch(NoSuchMethodException e) {
            LogLog.warn("custom level class [" + clazz + "]"
                    + " does not have a class function toLevel(String, Level)", e);
        } catch(java.lang.reflect.InvocationTargetException e) {
            if (e.getTargetException() instanceof InterruptedException
                    || e.getTargetException() instanceof InterruptedIOException) {
                Thread.currentThread().interrupt();
            }
            LogLog.warn("custom level class [" + clazz + "]"
                    + " could not be instantiated", e);
        } catch(ClassCastException e) {
            LogLog.warn("class [" + clazz
                    + "] is not a subclass of org.apache.log4j.Level", e);
        } catch(IllegalAccessException e) {
            LogLog.warn("class ["+clazz+
                    "] cannot be instantiated due to access restrictions", e);
        } catch(RuntimeException e) {
            LogLog.warn("class ["+clazz+"], level ["+levelName+
                    "] conversion failed.", e);
        }
        return result;
    }
}
