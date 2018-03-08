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

package com.blazebit.persistence.view.impl.objectbuilder;

import java.util.Arrays;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class TupleRest {

    private static final Object[] ANY_OFFSET_TUPLE = new Object[0];

    private final Object[] tuple;
    // We need to keep a separate reference to that element since it will be replaced by a collection during flattening
    private final Object[] offsetTuple;
    private final int index;

    public TupleRest(Object[] tuple, int index, int offset) {
        Object[] offsetTuple = new Object[offset];
        System.arraycopy(tuple, index, offsetTuple, 0, offset);
        this.tuple = tuple;
        this.offsetTuple = offsetTuple;
        this.index = index + offset;
    }

    public TupleRest(Object[] tuple, int index) {
        this.tuple = tuple;
        this.offsetTuple = ANY_OFFSET_TUPLE;
        this.index = index;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        int result = 1;
        // We ignore the offset tuple in the hash code to be able to match tuples after the offset
        for (int i = index; i < tuple.length; i++) {
            Object element = tuple[i];
            result = 31 * result + elementHashCode(element);
        }
        hash = 53 * hash + result;
        return hash;
    }

    private static int elementHashCode(Object element) {
        if (element instanceof Object[]) {
            return Arrays.deepHashCode((Object[]) element);
        } else if (element instanceof byte[]) {
            return Arrays.hashCode((byte[]) element);
        } else if (element instanceof short[]) {
            return Arrays.hashCode((short[]) element);
        } else if (element instanceof int[]) {
            return Arrays.hashCode((int[]) element);
        } else if (element instanceof long[]) {
            return Arrays.hashCode((long[]) element);
        } else if (element instanceof char[]) {
            return Arrays.hashCode((char[]) element);
        } else if (element instanceof float[]) {
            return Arrays.hashCode((float[]) element);
        } else if (element instanceof double[]) {
            return Arrays.hashCode((double[]) element);
        } else if (element instanceof boolean[]) {
            return Arrays.hashCode((boolean[]) element);
        } else if (element != null) {
            return element.hashCode();
        }

        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final TupleRest other = (TupleRest) obj;

        if (!deepEquals(other)) {
            return false;
        }
        return true;
    }

    public boolean deepEquals(TupleRest other) {
        Object[] otherTuple = other.tuple;
        Object[] otherOffsetTuple = other.offsetTuple;
        int otherIndex = other.index;
        boolean checkOffsetTuple = offsetTuple != ANY_OFFSET_TUPLE && otherOffsetTuple != ANY_OFFSET_TUPLE;

        if (checkOffsetTuple) {
            if (offsetTuple == null || otherOffsetTuple == null) {
                return false;
            }
            if (offsetTuple.length != other.offsetTuple.length) {
                return false;
            }
        } else if (tuple == otherTuple) {
            // We do this simply to avoid matching the original tuple
            // since in the ANY_OFFSET_TUPLE mode, we want to match the tuple rest of a different tuple
            return false;
        }

        if (tuple == null || otherTuple == null) {
            return false;
        }
        int length = tuple.length - index;
        if (otherTuple.length - otherIndex != length) {
            return false;
        }

        if (checkOffsetTuple) {
            for (int i = 0; i < offsetTuple.length; i++) {
                Object e1 = offsetTuple[i];
                Object e2 = otherOffsetTuple[i];

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
        }

        for (int i = 0; i < length; i++) {
            Object e1 = tuple[index + i];
            Object e2 = otherTuple[otherIndex + i];

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

    private static boolean deepEquals0(Object e1, Object e2) {
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
