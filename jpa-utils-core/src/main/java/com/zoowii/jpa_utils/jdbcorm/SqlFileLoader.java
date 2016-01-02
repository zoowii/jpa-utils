package com.zoowii.jpa_utils.jdbcorm;

import clojure.java.api.Clojure;
import clojure.lang.IMapIterable;
import clojure.lang.PersistentArrayMap;
import clojure.lang.Var;
import com.zoowii.jpa_utils.util.StringUtil;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by zoowii on 16/1/2.
 */
public class SqlFileLoader {
    private static final SqlFileLoader instance;

    static {
        instance = new SqlFileLoader();
    }

    private SqlFileLoader() {

    }

    public static SqlFileLoader getInstance() {
        return instance;
    }

    private ConcurrentHashMap<String, String> namedSqls = new ConcurrentHashMap<String, String>();

    public static SqlFileLoader loadSqlFile(String resourcePath, String ns) throws IOException {
        return getInstance()._loadSqlFile(resourcePath, ns);
    }

    @SuppressWarnings(value = "all")
    public SqlFileLoader _loadSqlFile(String resourcePath, String ns) throws IOException {
        if (resourcePath == null) {
            return this;
        }
        if (resourcePath.startsWith("classpath:")) {
            resourcePath = resourcePath.substring("classpath:".length());
        }
        if (StringUtil.isEmpty(ns)) {
            ns = resourcePath.replace('/', '.').replace('\\', '.');
        }
        if (ns.endsWith(".clj")) {
            ns = ns.substring(0, ns.length() - ".clj".length());
        }
        String scriptBase = resourcePath;
        if (scriptBase.endsWith(".clj")) {
            scriptBase = scriptBase.substring(0, scriptBase.length() - ".clj".length());
        }
        try {
            clojure.lang.RT.load(scriptBase);
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        }
        Var sqlsVar = (Var) Clojure.var(ns, "sqls");
        IMapIterable sqlsVarVal = (PersistentArrayMap) sqlsVar.get();
        Iterator<String> keys = sqlsVarVal.keyIterator();
        Iterator<String> vals = sqlsVarVal.valIterator();
        while (keys.hasNext()) {
            String key = keys.next();
            String val = vals.next();
            if (StringUtil.isEmpty(key) || StringUtil.isEmpty(val)) {
                continue;
            }
            namedSqls.put(key, val);
        }
        return this;
    }

    public String _getSqlByName(String name) {
        return namedSqls.get(name);
    }

    public static String getSqlByName(String name) {
        return getInstance()._getSqlByName(name);
    }
}
