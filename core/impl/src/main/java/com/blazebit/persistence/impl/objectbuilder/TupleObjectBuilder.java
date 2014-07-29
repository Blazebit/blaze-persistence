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
package com.blazebit.persistence.impl.objectbuilder;

import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.impl.SelectManager;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Tuple;
import javax.persistence.TupleElement;

/**
 *
 * @author Moritz Becker
 */
public class TupleObjectBuilder implements ObjectBuilder<Tuple> {

    private final SelectManager<?> selectManager;

    public TupleObjectBuilder(SelectManager<?> selectManager) {
        this.selectManager = selectManager;
    }

    @Override
    public Tuple build(Object[] tuple, String[] aliases) {
        return new TupleImpl(tuple, aliases);
    }

    @Override
    public List<Tuple> buildList(List<Tuple> list) {
        return list;
    }

    @Override
    public String[][] getExpressions() {
        return null;
    }

    class TupleImpl implements Tuple {

        private final Object[] tuple;
        private final String[] aliases;
        private List<TupleElement<?>> tupleElements;

        private TupleImpl(Object[] tuple, String[] aliases) {
            if (tuple.length != selectManager.getSelectInfoCount()) {
                throw new IllegalArgumentException(
                        "Size mismatch between tuple result [" + tuple.length
                        + "] and expected tuple elements [" + selectManager.getSelectAbsolutePathToInfoMap().size() + "]"
                );
            }
            this.tuple = tuple;
            this.aliases = aliases;
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
                    index = selectManager.getSelectAliasToPositionMap().get(alias);
                }
            }
            if (index == null) {
                throw new IllegalArgumentException(
                        "Given alias [" + alias + "] did not correspond to an element in the result tuple"
                );
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
            if (i >= tuple.length) {
                throw new IllegalArgumentException(
                        "Given index [" + i + "] was outside the range of result tuple size [" + tuple.length + "] "
                );
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
                tupleElements = new ArrayList<TupleElement<?>>(tuple.length);
                for (int i = 0; i < tuple.length; i++) {
                    tupleElements.add(new TupleElementImpl<Object>(i));
                }
            }
            
            return tupleElements;
        }
        
        private class TupleElementImpl<X> implements TupleElement<X> {
            
            private final int index;

            public TupleElementImpl(int index) {
                this.index = index;
            }

            @Override
            public Class<? extends X> getJavaType() {
                return (Class<? extends X>) tuple[index];
            }

            @Override
            public String getAlias() {
                return aliases[index];
            }
        }

    }
    
}
