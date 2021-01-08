/*
 * Copyright 2014 - 2021 Blazebit.
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

package com.blazebit.persistence.view.impl;

import java.lang.reflect.Constructor;

/**
 * @author Christian
 * @since 1.4.0
 */
public final class SimpleEntityViewListenerFactory<T> implements EntityViewListenerFactory<T> {

    private final Class<? extends T> entityViewListenerClass;
    private final Constructor<? extends T> entityViewListenerConstructor;
    private final Class<? super T> listenerKind;

    public SimpleEntityViewListenerFactory(Class<? extends T> entityViewListenerClass, Class<? super T> listenerKind) {
        this.entityViewListenerClass = entityViewListenerClass;
        this.listenerKind = listenerKind;
        try {
            Constructor<? extends T> constructor = entityViewListenerClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            this.entityViewListenerConstructor = constructor;
        } catch (Exception e) {
            throw new IllegalArgumentException("Couldn't access empty constructor of entity view listener class: " + entityViewListenerClass, e);
        }
    }

    @Override
    public Class<? super T> getListenerKind() {
        return listenerKind;
    }

    @Override
    public Class<T> getListenerClass() {
        return (Class<T>) entityViewListenerClass;
    }

    @Override
    public T createListener() {
        try {
            return entityViewListenerConstructor.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Couldn't instantiate entity view listener", e);
        }
    }
}
