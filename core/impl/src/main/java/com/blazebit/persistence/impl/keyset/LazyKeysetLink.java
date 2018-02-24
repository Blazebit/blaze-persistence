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

package com.blazebit.persistence.impl.keyset;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.blazebit.persistence.Keyset;
import com.blazebit.persistence.impl.OrderByExpression;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class LazyKeysetLink extends AbstractKeysetLink {

    private final Map<String, Object> keysetValues;
    private Keyset keyset;

    public LazyKeysetLink(Map<String, Object> keysetValues, KeysetMode keysetMode) {
        super(keysetMode);
        this.keysetValues = keysetValues;
    }

    @Override
    public void initialize(List<OrderByExpression> orderByExpressions) {
        Serializable[] tuple = new Serializable[orderByExpressions.size()];

        for (int i = 0; i < tuple.length; i++) {
            String expressionString = orderByExpressions.get(0).getExpression().toString();
            Object value = keysetValues.get(expressionString);

            if (value == null) {
                if (!keysetValues.containsKey(expressionString)) {
                    throw new IllegalArgumentException("The keyset provided [" + keysetValues
                        + "] does not contain an entry for the order by expression: " + expressionString);
                }

                tuple[i] = null;
            } else {
                if (!(value instanceof Serializable)) {
                    throw new IllegalArgumentException("The keyset value [" + value + "] provided for the order by expression [" + expressionString
                        + "] is not an instance of java.io.Serializable!");
                }

                tuple[i] = (Serializable) value;
            }
        }

        keyset = new KeysetImpl(tuple);
        validate(keyset, orderByExpressions);
    }

    @Override
    public Keyset getKeyset() {
        return keyset;
    }
}
