/*
 * Copyright 2014 Blazebit.
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

package com.blazebit.persistence.impl;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class KeySetPaginationHelper {
    
    public static Serializable[] extractKey(Object[] tuple, int offset) {
        Serializable[] key = new Serializable[tuple.length - offset];
        System.arraycopy(tuple, offset, key, 0, key.length);
        return key;
    }
    
    public static KeySetMode getKeySetMode(boolean extractKeySet, KeySetImpl keySet, int firstRow, int pageSize, List<OrderByExpression> orderByExpressions) {
        // a key set must be given
        if (!extractKeySet || keySet == null) {
            return KeySetMode.NONE;
        }
        // The last page size must equal the current page size
        if (keySet.getMaxResults() != pageSize) {
            return KeySetMode.NONE;
        }
        // Ordering has changed
        if (!keySet.getOrderByExpressions().equals(orderByExpressions)) {
            return KeySetMode.NONE;
        }

        int offset = keySet.getFirstResult() - firstRow;

        if (offset == pageSize) {
            // We went to the previous page
            if (isValidKey(keySet.getLowest(), orderByExpressions)) {
                return KeySetMode.PREVIOUS;
            } else {
                return KeySetMode.NONE;
            }
        } else if (offset == -pageSize) {
            // We went to the next page
            if (isValidKey(keySet.getHighest(), orderByExpressions)) {
                return KeySetMode.NEXT;
            } else {
                return KeySetMode.NONE;
            }
        } else if (offset == 0) {
            // Same page again
            if (isValidKey(keySet.getLowest(), orderByExpressions)) {
                return KeySetMode.SAME;
            } else {
                return KeySetMode.NONE;
            }
        } else {
            // The last key set is away more than one page
            return KeySetMode.NONE;
        }
    }

    private static boolean isValidKey(Serializable[] key, List<OrderByExpression> orderByExpressions) {
        return key != null && key.length == orderByExpressions.size();
    }
}
