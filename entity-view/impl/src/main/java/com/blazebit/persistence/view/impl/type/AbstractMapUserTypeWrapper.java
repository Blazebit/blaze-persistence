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

import com.blazebit.persistence.view.spi.type.BasicUserType;

import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractMapUserTypeWrapper<C extends Map<K, V>, K, V> extends AbstractPluralUserTypeWrapper<C, V> {

    protected final BasicUserType<K> keyUserType;

    public AbstractMapUserTypeWrapper(BasicUserType<K> keyUserType, BasicUserType<V> elementUserType) {
        super(elementUserType);
        this.keyUserType = keyUserType;
    }

    protected abstract C createCollection(int size);

    @Override
    public C deepClone(C object) {
        C clone = createCollection(object.size());

        if (keyUserType == null) {
            for (Map.Entry<K, V> entry : object.entrySet()) {
                clone.put(entry.getKey(), elementUserType.deepClone(entry.getValue()));
            }
        } else if (elementUserType == null) {
            for (Map.Entry<K, V> entry : object.entrySet()) {
                clone.put(keyUserType.deepClone(entry.getKey()), entry.getValue());
            }
        } else {
            for (Map.Entry<K, V> entry : object.entrySet()) {
                clone.put(keyUserType.deepClone(entry.getKey()), elementUserType.deepClone(entry.getValue()));
            }
        }

        return clone;
    }
}
