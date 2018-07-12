/*
 * Copyright 2014 - 2018 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blazebit.persistence.view.impl.type;

import com.blazebit.persistence.view.spi.type.BasicDirtyTracker;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Clob;
import java.sql.NClob;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class ClobProxy implements InvocationHandler {

    private static final Class<?>[] PROXY_INTERFACES = new Class[] { Clob.class, LobImplementor.class };
    private static final Class<?>[] NCLOB_PROXY_INTERFACES = new Class[] { NClob.class, LobImplementor.class };

    private final Clob delegate;
    private BasicDirtyTracker parent;
    private int parentIndex = -1;
    private boolean dirty;

    public ClobProxy(Clob delegate) {
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
            case "setString":
            case "setAsciiStream":
            case "setCharacterStream":
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

    public static Clob generateProxy(Clob delegate) {
        if (delegate == null) {
            return null;
        }
        return (Clob) Proxy.newProxyInstance(ClobProxy.class.getClassLoader(), PROXY_INTERFACES, new ClobProxy(delegate));
    }

    public static NClob generateProxy(NClob delegate) {
        if (delegate == null) {
            return null;
        }
        return (NClob) Proxy.newProxyInstance(ClobProxy.class.getClassLoader(), NCLOB_PROXY_INTERFACES, new ClobProxy(delegate));
    }
}
