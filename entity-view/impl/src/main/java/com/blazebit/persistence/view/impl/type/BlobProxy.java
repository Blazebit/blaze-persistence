/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.type;

import com.blazebit.persistence.view.spi.type.BasicDirtyTracker;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Blob;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class BlobProxy implements InvocationHandler {

    private static final Class<?>[] PROXY_INTERFACES = new Class[] { Blob.class, LobImplementor.class };

    private final Blob delegate;
    private BasicDirtyTracker parent;
    private int parentIndex = -1;
    private boolean dirty;

    public BlobProxy(Blob delegate) {
        this.delegate = delegate;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //CHECKSTYLE:OFF: MissingSwitchDefault
        switch (method.getName()) {
            case "getWrapped":
                return delegate;
            case "$$_isDirty":
                return dirty;
            case "$$_unmarkDirty":
                dirty = false;
                return null;
            case "$$_setParent":
                if (this.parent != null) {
                    throw new IllegalStateException("Parent object for " + this.toString() + " is already set to " + this.parent.toString() + " and can't be set to:" + parent.toString());
                }
                parent = (BasicDirtyTracker) args[0];
                parentIndex = (int) args[1];
                return null;
            case "$$_unsetParent":
                parent = null;
                parentIndex = -1;
                return null;
            case "$$_markDirty":
                dirty = true;
                return null;
            case "setBytes":
            case "setBinaryStream":
            case "truncate":
                dirty = true;
                if (parent != null) {
                    parent.$$_markDirty(parentIndex);
                }
                break;
            case "toString":
                return this.toString();
            case "equals":
                return this == args[0];
            case "hashCode":
                return this.hashCode();
        }
        //CHECKSTYLE:ON: MissingSwitchDefault

        return method.invoke(delegate, args);
    }

    public static Blob generateProxy(Blob delegate) {
        if (delegate == null) {
            return null;
        }
        return (Blob) Proxy.newProxyInstance(BlobProxy.class.getClassLoader(), PROXY_INTERFACES, new BlobProxy(delegate));
    }
}
