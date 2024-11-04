/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.type;

import com.blazebit.persistence.view.spi.type.BasicUserType;

import java.util.Collection;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractCollectionUserTypeWrapper<C extends Collection<V>, V> extends AbstractPluralUserTypeWrapper<C, V> {

    public AbstractCollectionUserTypeWrapper(BasicUserType<V> elementUserType) {
        super(elementUserType);
    }

    protected abstract C createCollection(int size);

    @Override
    public C deepClone(C object) {
        C clone = createCollection(object.size());

        for (V element : object) {
            clone.add(elementUserType.deepClone(element));
        }

        return clone;
    }
}
