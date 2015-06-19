package com.zoowii.jpa_utils.core;

/**
 * Created by zoowii on 14-12-23.
 */
public interface SessionFactory {

    public Session getThreadScopeSession();

    public Session currentSession();

    public abstract Session createSession();

    public abstract void close();
}
