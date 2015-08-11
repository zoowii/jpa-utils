package com.zoowii.jpa_utils.core;

/**
 * Created by zoowii on 14-12-23.
 */
public interface SessionFactory {
    Session getThreadScopeSession();

    Session currentSession();

    abstract Session createSession();

    abstract void close();
}
