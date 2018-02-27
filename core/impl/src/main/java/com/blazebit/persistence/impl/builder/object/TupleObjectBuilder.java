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

package com.blazebit.persistence.impl.builder.object;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.persistence.Tuple;
import javax.persistence.TupleElement;

import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.SelectBuilder;
import com.blazebit.persistence.impl.SelectInfo;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public class TupleObjectBuilder implements ObjectBuilder<Tuple> {

    private final Map<String, Integer> selectAliasToPositionMap;
    private String[] aliases;

    public TupleObjectBuilder(List<SelectInfo> selectInfos, Map<String, Integer> selectAliasToPositionMap) {
        aliases = new String[selectInfos.size()];
        for (int i = 0; i < aliases.length; i++) {
            aliases[i] = selectInfos.get(i).getAlias();
        }
        this.selectAliasToPositionMap = selectAliasToPositionMap;
    }

    @Override
    public Tuple build(Object[] tuple) {
        return new TupleImpl(tuple);
    }

    @Override
    public List<Tuple> buildList(List<Tuple> list) {
        return list;
    }

    @Override
    public <X extends SelectBuilder<X>> void applySelects(X queryBuilder) {
    }

    private String[] getSelectAliases() {
        return aliases;
    }

    /**
     * @author Christian Beikov
     * @since 1.0.0
     */
    private class TupleImpl implements Tuple {

        private final Object[] tuple;
        private List<TupleElement<?>> tupleElements;

        private TupleImpl(Object[] tuple) {
            this.tuple = tuple;
        }

        @Override
        public <X> X get(TupleElement<X> tupleElement) {
            return get(tupleElement.getAlias(), tupleElement.getJavaType());
        }

        @Override
        public Object get(String alias) {
            Integer index = null;
            if (alias != null) {
                alias = alias.trim();
                if (alias.length() > 0) {
                    index = selectAliasToPositionMap.get(alias);
                }
            }
            if (index == null) {
                throw new IllegalArgumentException("Given alias [" + alias + "] did not correspond to an element in the result tuple");
            }
            // index should be "in range" by nature of size check in ctor
            return tuple[index];
        }

        @Override
        public <X> X get(String alias, Class<X> type) {
            return type.cast(get(alias));
        }

        @Override
        public Object get(int i) {
            if (i >= tuple.length || i < 0) {
                throw new IllegalArgumentException("Given index [" + i + "] was outside the range of result tuple size [" + tuple.length + "] ");
            }
            return tuple[i];
        }

        @Override
        public <X> X get(int i, Class<X> type) {
            return type.cast(get(i));
        }

        @Override
        public Object[] toArray() {
            return tuple.clone();
        }

        @Override
        public List<TupleElement<?>> getElements() {
            if (tupleElements == null) {
                if (aliases == null) {
                    aliases = getSelectAliases();
                }

                tupleElements = new ArrayList<TupleElement<?>>(tuple.length);
                for (int i = 0; i < tuple.length; i++) {
                    tupleElements.add(new TupleElementImpl<Object>(i));
                }
            }

            return tupleElements;
        }

        /**
         * @author Christian Beikov
         * @since 1.0.0
         */
        private class TupleElementImpl<X> implements TupleElement<X> {

            private final int index;

            public TupleElementImpl(int index) {
                this.index = index;
            }

            @Override
            @SuppressWarnings("unchecked")
            public Class<? extends X> getJavaType() {
                return tuple[index] == null ? null : (Class<? extends X>) tuple[index].getClass();
            }

            @Override
            public String getAlias() {
                return aliases[index];
            }
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 29 * hash + Arrays.deepHashCode(this.tuple);
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
            final TupleImpl other = (TupleImpl) obj;
            if (!Arrays.deepEquals(this.tuple, other.tuple)) {
                return false;
            }
            return true;
        }

    }

}
