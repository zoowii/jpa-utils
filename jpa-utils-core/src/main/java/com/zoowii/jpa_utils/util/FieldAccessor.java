package com.zoowii.jpa_utils.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * java bean property(field/get-method/set-method)'s wrapper of java bean
 */
public class FieldAccessor {
    private Field field;
    private Method getMethod;
    private Method setMethod;
    private final Class<?> cls;
    private final String name;

    private static final Map<String, FieldAccessor> fieldAccessorCache = new HashMap<String, FieldAccessor>();

    public static FieldAccessor getFieldAccessor(Class<?> cls, String name) {
        String key = cls.getCanonicalName() + "@" + name;
        if(!fieldAccessorCache.containsKey(key)) {
            synchronized (fieldAccessorCache) {
                if(!fieldAccessorCache.containsKey(key)) {
                    fieldAccessorCache.put(key, new FieldAccessor(cls, name));
                }
            }
        }
        return fieldAccessorCache.get(key);
    }

    @SuppressWarnings("unchecked")
    public FieldAccessor(Class<?> cls, String name) {
        this.cls = cls;
        this.name = name;
        try {
            field = cls.getDeclaredField(name);
            field.setAccessible(true);
        } catch (NoSuchFieldException e) {
            field = null;
        }
        try {
            getMethod = cls.getMethod(getGetMethodName());
        } catch (NoSuchMethodException e) {
            getMethod = null;
        }
        try {
            setMethod = cls.getMethod(getSetMethodName());
        } catch (NoSuchMethodException e) {
            setMethod = null;
        }
    }

    public Class getPropertyType() {
        if (getMethod != null) {
            return getMethod.getReturnType();
        }
        if (field != null) {
            return field.getType();
        } else {
            return null;
        }
    }

    /**
     * get annotation of this property, first find on field, then find on get-field-method, then on set-field-method
     * @param annoCls
     * @param <T>
     * @return
     */
    public <T extends Annotation> T getPropertyAnnotation(Class<T> annoCls) {
        if(field != null) {
            T annoOnField = field.getAnnotation(annoCls);
            if (annoOnField != null) {
                return annoOnField;
            }
        }
        if(getMethod != null) {
            T annoOnGetMethod = getMethod.getAnnotation(annoCls);
            if (annoOnGetMethod != null) {
                return annoOnGetMethod;
            }
        }
        if(setMethod != null) {
            return setMethod.getAnnotation(annoCls);
        }
        return null;
    }

    public Object getProperty(Object obj) {
        if (getMethod != null) {
            try {
                return getMethod.invoke(obj);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        if (field != null) {
            if (Proxy.isProxyClass(obj.getClass())) {
                // You may need process this, maybe you can make all proxy instance implement a special interface
            }
            try {
                return field.get(obj);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        throw new RuntimeException(String.format("Can't find accessor of get property value of %s", name));
    }

    public void setProperty(Object obj, Object value) {
        if (setMethod != null) {
            try {
                setMethod.invoke(obj, value);
                return;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        if (field != null) {
            if (Proxy.isProxyClass(obj.getClass())) {
                // You may need process this, maybe you can make all proxy instance implement a special interface
            }
            try {
                field.set(obj, value);
                return;
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        throw new RuntimeException(String.format("Can't find accessor of set property %s", name));
    }

    private String upperFirstChar(String str) {
        if (str == null) {
            return null;
        }
        String s = str.trim();
        if (s.length() < 1) {
            return s;
        }
        return String.valueOf(s.charAt(0)).toUpperCase() + s.substring(1);
    }

    /**
     * getter method of the property's name
     *
     * @return
     */
    private String getGetMethodName() {
        assert name != null;
        if (field != null && (field.getType() == Boolean.class
                    || "boolean".equals(field.getType().getName()))) {
            return "is" + upperFirstChar(name);
        }
        return "get" + upperFirstChar(name);
    }

    /**
     * setter method of the property's name
     *
     * @return
     */
    private String getSetMethodName() {
        assert name != null;
        return "set" + upperFirstChar(name);
    }
}