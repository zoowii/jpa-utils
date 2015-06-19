package com.zoowii.jpa_utils.util;

import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

/**
 * Created by zoowii on 15/6/18.
 */
public class Logger {
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger("jpautils");

    public static void logSql(String sql) {
        debug(sql);
    }

    public static String getName() {
        return LOG.getName();
    }

    public static boolean isTraceEnabled() {
        return LOG.isTraceEnabled();
    }

    public static void trace(String var1) {
        LOG.trace(var1);
    }

    public static void trace(String var1, Object var2) {
        LOG.trace(var1, var2);
    }

    public static void trace(String var1, Object var2, Object var3) {
        LOG.trace(var1, var2, var3);
    }

    public static void trace(String var1, Object... var2) {
        LOG.trace(var1, var2);
    }

    public static void trace(String var1, Throwable var2) {
        LOG.trace(var1, var2);
    }

    public static boolean isDebugEnabled() {
        return LOG.isDebugEnabled();
    }

    public static void debug(String var1) {
        LOG.debug(var1);
    }

    public static void debug(String var1, Object var2) {
        LOG.debug(var1, var2);
    }

    public static void debug(String var1, Object var2, Object var3) {
        LOG.debug(var1, var2, var3);
    }

    public static void debug(String var1, Object... var2) {
        LOG.debug(var1, var2);
    }

    public static void debug(String var1, Throwable var2) {
        LOG.debug(var1, var2);
    }

    public static boolean isInfoEnabled() {
        return LOG.isInfoEnabled();
    }

    public static void info(String var1) {
        LOG.info(var1);
    }

    public static void info(String var1, Object var2) {
        LOG.info(var1, var2);
    }

    public static void info(String var1, Object var2, Object var3) {
        LOG.info(var1, var2, var3);
    }

    public static void info(String var1, Object... var2) {
        LOG.info(var1, var2);
    }

    public static void info(String var1, Throwable var2) {
        LOG.info(var1, var2);
    }

    public static boolean isWarnEnabled() {
        return LOG.isWarnEnabled();
    }

    public static void warn(String var1) {
        LOG.warn(var1);
    }

    public static void warn(String var1, Object var2) {
        LOG.warn(var1, var2);
    }

    public static void warn(String var1, Object... var2) {
        LOG.warn(var1, var2);
    }

    public static void warn(String var1, Object var2, Object var3) {
        LOG.warn(var1, var2, var3);
    }

    public static void warn(String var1, Throwable var2) {
        LOG.warn(var1, var2);
    }

    public static boolean isErrorEnabled() {
        return LOG.isErrorEnabled();
    }

    public static void error(String var1) {
        LOG.error(var1);
    }

    public static void error(String var1, Object var2) {
        LOG.error(var1, var2);
    }

    public static void error(String var1, Object var2, Object var3) {
        LOG.error(var1, var2, var3);
    }

    public static void error(String var1, Object... var2) {
        LOG.error(var1, var2);
    }

    public static void error(String var1, Throwable var2) {
        LOG.error(var1, var2);
    }
}
