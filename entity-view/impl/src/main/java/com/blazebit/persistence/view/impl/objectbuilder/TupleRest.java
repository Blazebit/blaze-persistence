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
package com.blazebit.persistence.view.impl.objectbuilder;

import java.util.Arrays;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class TupleRest {

    private final Object[] tuple;
    private final int index;

    public TupleRest(Object[] tuple, int index) {
        this.tuple = tuple;
        this.index = index;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        int result = 1;
        for (int i = index; i < tuple.length; i++) {
            Object element = tuple[i];
            int elementHash = 0;
            if (element instanceof Object[]) {
                elementHash = Arrays.deepHashCode((Object[]) element);
            } else if (element instanceof byte[]) {
                elementHash = Arrays.hashCode((byte[]) element);
            } else if (element instanceof short[]) {
                elementHash = Arrays.hashCode((short[]) element);
            } else if (element instanceof int[]) {
                elementHash = Arrays.hashCode((int[]) element);
            } else if (element instanceof long[]) {
                elementHash = Arrays.hashCode((long[]) element);
            } else if (element instanceof char[]) {
                elementHash = Arrays.hashCode((char[]) element);
            } else if (element instanceof float[]) {
                elementHash = Arrays.hashCode((float[]) element);
            } else if (element instanceof double[]) {
                elementHash = Arrays.hashCode((double[]) element);
            } else if (element instanceof boolean[]) {
                elementHash = Arrays.hashCode((boolean[]) element);
            } else if (element != null) {
                elementHash = element.hashCode();
            }

            result = 31 * result + elementHash;
        }
        hash = 53 * hash + result;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TupleRest other = (TupleRest) obj;
        if (!deepEquals(other.tuple, other.index)) {
            return false;
        }
        return true;
    }

    public boolean deepEquals(Object[] other, int otherIndex) {
        if (tuple == other) {
            return true;
        }
        if (tuple == null || other == null) {
            return false;
        }
        int length = tuple.length - index;
        if (other.length - otherIndex != length ) {
            return false;
        }

        for (int i = 0; i < length; i++) {
            Object e1 = tuple[index + i];
            Object e2 = other[otherIndex + i];

            if (e1 == e2) {
                continue;
            }
            if (e1 == null) {
                return false;
            }

            // Figure out whether the two elements are equal
            boolean eq = deepEquals0(e1, e2);

            if (!eq) {
                return false;
            }
        }
        return true;
    }

    static boolean deepEquals0(Object e1, Object e2) {
        assert e1 != null;
        boolean eq;
        if (e1 instanceof Object[] && e2 instanceof Object[]) {
            eq = Arrays.deepEquals((Object[]) e1, (Object[]) e2);
        } else if (e1 instanceof byte[] && e2 instanceof byte[]) {
            eq = Arrays.equals((byte[]) e1, (byte[]) e2);
        } else if (e1 instanceof short[] && e2 instanceof short[]) {
            eq = Arrays.equals((short[]) e1, (short[]) e2);
        } else if (e1 instanceof int[] && e2 instanceof int[]) {
            eq = Arrays.equals((int[]) e1, (int[]) e2);
        } else if (e1 instanceof long[] && e2 instanceof long[]) {
            eq = Arrays.equals((long[]) e1, (long[]) e2);
        } else if (e1 instanceof char[] && e2 instanceof char[]) {
            eq = Arrays.equals((char[]) e1, (char[]) e2);
        } else if (e1 instanceof float[] && e2 instanceof float[]) {
            eq = Arrays.equals((float[]) e1, (float[]) e2);
        } else if (e1 instanceof double[] && e2 instanceof double[]) {
            eq = Arrays.equals((double[]) e1, (double[]) e2);
        } else if (e1 instanceof boolean[] && e2 instanceof boolean[]) {
            eq = Arrays.equals((boolean[]) e1, (boolean[]) e2);
        } else {
            eq = e1.equals(e2);
        }
        return eq;
    }
}
