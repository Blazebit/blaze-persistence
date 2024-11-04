/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.criteria.impl;

import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.SelectBuilder;
import com.blazebit.persistence.parser.util.TypeUtils;

import javax.persistence.Tuple;
import javax.persistence.TupleElement;
import javax.persistence.criteria.Selection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class JpaTupleObjectBuilder implements ObjectBuilder<Tuple> {

    private final Set<String> supportedEnumTypes;
    private final List<Selection<?>> selectionItems;
    private Map<String, Integer> selectAliasToPositionMap;
    private Map<Selection<?>, Integer> selectionToPositionMap;

    public JpaTupleObjectBuilder(BlazeCriteriaBuilderImpl criteriaBuilder, List<Selection<?>> selectionItems) {
        this.supportedEnumTypes = criteriaBuilder.getEntityMetamodel().getEnumTypes().keySet();
        this.selectionItems = selectionItems;
    }

    private Map<String, Integer> getSelectAliasToPositionMap() {
        if (selectAliasToPositionMap == null) {
            selectAliasToPositionMap = new HashMap<String, Integer>(selectionItems.size());
            int index = 0;
            for (Selection<?> s : selectionItems) {
                final String alias = s.getAlias();
                if (alias != null) {
                    selectAliasToPositionMap.put(alias, index);
                }
                index++;
            }
        }

        return selectAliasToPositionMap;
    }

    private Map<Selection<?>, Integer> getSelectionToPositionMap() {
        if (selectionToPositionMap == null) {
            selectionToPositionMap = new HashMap<Selection<?>, Integer>(selectionItems.size());
            int index = 0;
            for (Selection<?> s : selectionItems) {
                selectionToPositionMap.put(s, index);
                index++;
            }
        }

        return selectionToPositionMap;
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
        for (Selection<?> s : selectionItems) {
            renderSelection(queryBuilder, s);
        }
    }

    protected abstract void renderSelection(SelectBuilder<?> cb, Selection<?> s);

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    private class TupleImpl implements Tuple {

        private final Object[] tuple;

        private TupleImpl(Object[] tuple) {
            this.tuple = tuple;
        }

        @Override
        public <X> X get(TupleElement<X> tupleElement) {
            return get(getSelectionToPositionMap().get(tupleElement), tupleElement.getJavaType());
        }

        @Override
        public Object get(String alias) {
            Integer index = null;
            if (alias != null) {
                alias = alias.trim();
                if (alias.length() > 0) {
                    index = getSelectAliasToPositionMap().get(alias);
                }
            }
            if (index == null) {
                throw new IllegalArgumentException("Could not find an element with alias '" + alias + "' in the result tuple");
            }
            return tuple[index];
        }

        @Override
        public <X> X get(String alias, Class<X> type) {
            Object tupleElement = get(alias);
            return TypeUtils.convert(tupleElement, type, supportedEnumTypes);
        }

        @Override
        public Object get(int i) {
            if (i >= tuple.length || i < 0) {
                throw new IllegalArgumentException("Invalid index " + i + "! The result tuple size is " + tuple.length);
            }
            return tuple[i];
        }

        @Override
        public <X> X get(int i, Class<X> type) {
            Object tupleElement = get(i);
            return TypeUtils.convert(tupleElement, type, supportedEnumTypes);
        }

        @Override
        public Object[] toArray() {
            return tuple.clone();
        }

        @Override
        @SuppressWarnings({"unchecked"})
        public List<TupleElement<?>> getElements() {
            return (List<TupleElement<?>>) (List<? extends TupleElement<?>>) selectionItems;
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
            return Arrays.deepEquals(this.tuple, other.tuple);
        }

    }

}