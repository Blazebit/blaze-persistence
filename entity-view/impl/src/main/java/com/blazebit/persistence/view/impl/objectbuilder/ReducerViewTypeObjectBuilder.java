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

import com.blazebit.persistence.ObjectBuilder;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class ReducerViewTypeObjectBuilder<T> extends DelegatingObjectBuilder<T> {

    private final int start;
    private final int suffix;
    private final int length;
    private final boolean keepTuplePrefix;
    private final FastArrayList newTuple;

    public ReducerViewTypeObjectBuilder(ObjectBuilder<T> delegate, int start, int suffix, int length, boolean keepTuplePrefix) {
        super(delegate);
        this.start = start;
        this.suffix = suffix;
        this.length = length;
        this.keepTuplePrefix = keepTuplePrefix;
        this.newTuple = new FastArrayList(length);
    }

    @Override
    public T build(Object[] tuple) {
        newTuple.clear();
        for (int i = start; i < start + length; i++) {
            if (tuple[i] != TupleReuse.CONSUMED) {
                newTuple.add(tuple[i]);
            }
        }

        // We can return the actual array here because we know that the only possible delegate
        // is the ViewTypeObjectBuilder which consumes the elements of the array
        T result = buildObject(tuple, newTuple.getArray());
        if (keepTuplePrefix) {
            // Create a new array and put in the prefix parts as well as the result into it
            Object[] tupleWithPrefix = new Object[start + suffix + 1];
            System.arraycopy(tuple, 0, tupleWithPrefix, 0, start);
            System.arraycopy(tuple, tuple.length - suffix, tupleWithPrefix, start + 1, suffix);
            tupleWithPrefix[start] = result;
            result = (T) tupleWithPrefix;
        }

        return result;
    }

    protected T buildObject(Object[] originalTuple, Object[] tuple) {
        return delegate.build(tuple);
    }

    /**
     * @author Christian Beikov
     * @since 1.0.0
     */
    private static class FastArrayList {

        /**
         * The maximum size of array to allocate.
         * Some VMs reserve some header words in an array.
         * Attempts to allocate larger arrays may result in
         * OutOfMemoryError: Requested array size exceeds VM limit
         */
        private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

        private Object[] array;
        private int size;

        public FastArrayList(int initialSize) {
            this.size = 0;
            this.array = new Object[initialSize];
        }

        public void clear() {
            size = 0;
        }

        public Object[] getArray() {
            if (array.length == size) {
                return array;
            }

            array = Arrays.copyOf(array, size);
            return array;
        }

        public void add(Object value) {
            if (size < array.length) {
                array[size] = value;
            } else {
                grow(size + 1);
                array[size] = value;
            }

            size++;
        }

        /*
         * The following is copied from java.util.ArrayList
         */

        /**
         * Increases the capacity to ensure that it can hold at least the
         * number of elements specified by the minimum capacity argument.
         *
         * @param minCapacity the desired minimum capacity
         */
        private void grow(int minCapacity) {
            // overflow-conscious code
            int oldCapacity = array.length;
            int newCapacity = oldCapacity + (oldCapacity >> 1);
            if (newCapacity - minCapacity < 0) {
                newCapacity = minCapacity;
            }
            if (newCapacity - MAX_ARRAY_SIZE > 0) {
                newCapacity = hugeCapacity(minCapacity);
            }
            // minCapacity is usually close to size, so this is a win:
            array = Arrays.copyOf(array, newCapacity);
        }

        private static int hugeCapacity(int minCapacity) {
            if (minCapacity < 0) { // overflow
                throw new OutOfMemoryError();
            }
            return (minCapacity > MAX_ARRAY_SIZE)
                    ? Integer.MAX_VALUE
                    : MAX_ARRAY_SIZE;
        }
    }
}
