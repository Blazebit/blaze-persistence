/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.objectbuilder;

/**
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
public interface ContainerAccumulator<T>  {

    T createContainer(boolean recording, int size);

    void add(T container, Object index, Object value, boolean recording);

    void addAll(T container, T value, boolean recording);

    boolean requiresPostConstruct();

    void postConstruct(T collection);

}
