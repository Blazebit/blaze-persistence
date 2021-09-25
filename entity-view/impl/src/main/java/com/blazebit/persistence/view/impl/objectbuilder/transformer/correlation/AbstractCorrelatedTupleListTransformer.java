/*
 * Copyright 2014 - 2021 Blazebit.
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

package com.blazebit.persistence.view.impl.objectbuilder.transformer.correlation;

import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.ExpressionFactory;
import com.blazebit.persistence.parser.util.JpaMetamodelUtils;
import com.blazebit.persistence.spi.JpaProvider;
import com.blazebit.persistence.view.CorrelationProviderFactory;
import com.blazebit.persistence.view.impl.CorrelationProviderHelper;
import com.blazebit.persistence.view.impl.EntityViewConfiguration;
import com.blazebit.persistence.view.impl.ExpressionUtils;
import com.blazebit.persistence.view.impl.PrefixingQueryGenerator;
import com.blazebit.persistence.view.impl.metamodel.ManagedViewTypeImplementor;
import com.blazebit.persistence.view.impl.objectbuilder.ContainerAccumulator;
import com.blazebit.persistence.view.impl.objectbuilder.Limiter;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.TupleListTransformer;

import javax.persistence.Parameter;
import javax.persistence.metamodel.IdentifiableType;
import javax.persistence.metamodel.ManagedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractCorrelatedTupleListTransformer extends TupleListTransformer implements TupleResultCopier {

    protected static final String[] EMPTY = new String[0];

    protected final JpaProvider jpaProvider;
    protected final Correlator correlator;
    protected final ContainerAccumulator<Object> containerAccumulator;
    protected final ManagedViewTypeImplementor<?> viewRootType;
    protected final ManagedViewTypeImplementor<?> embeddingViewType;
    protected final int viewRootIndex;
    protected final int embeddingViewIndex;
    protected final String correlationAlias;
    protected final String correlationExternalAlias;
    protected final String correlationResult;
    protected final CorrelationProviderFactory correlationProviderFactory;
    protected final Class<?> correlationBasisType;
    protected final Class<?> correlationBasisEntity;
    protected final String attributePath;
    protected final String[] fetches;
    protected final String[] indexFetches;
    protected final String indexExpression;
    protected final Correlator indexCorrelator;
    protected final Limiter limiter;

    protected final EntityViewConfiguration entityViewConfiguration;

    public AbstractCorrelatedTupleListTransformer(ExpressionFactory ef, Correlator correlator, ContainerAccumulator<?> containerAccumulator, ManagedViewTypeImplementor<?> viewRootType, ManagedViewTypeImplementor<?> embeddingViewType, Expression correlationResult, CorrelationProviderFactory correlationProviderFactory, String attributePath, String[] fetches,
                                                  String[] indexFetches, Expression indexExpression, Correlator indexCorrelator, int viewRootIndex, int embeddingViewIndex, int tupleIndex, Class<?> correlationBasisType, Class<?> correlationBasisEntity, Limiter limiter, EntityViewConfiguration entityViewConfiguration) {
        super(tupleIndex);
        this.containerAccumulator = (ContainerAccumulator<Object>) containerAccumulator;
        this.jpaProvider = entityViewConfiguration.getCriteriaBuilder().getService(JpaProvider.class);
        this.correlator = correlator;
        this.viewRootType = viewRootType;
        this.embeddingViewType = embeddingViewType;
        this.correlationProviderFactory = correlationProviderFactory;
        this.viewRootIndex = viewRootIndex;
        this.embeddingViewIndex = embeddingViewIndex;
        this.correlationBasisType = correlationBasisType;
        this.correlationBasisEntity = correlationBasisEntity;
        this.attributePath = attributePath;
        this.limiter = limiter;
        this.entityViewConfiguration = entityViewConfiguration;
        this.correlationAlias = CorrelationProviderHelper.getDefaultCorrelationAlias(attributePath);
        this.indexCorrelator = indexCorrelator;
        if (limiter == null) {
            this.correlationExternalAlias = correlationAlias;
        } else {
            this.correlationExternalAlias = CorrelationProviderHelper.getDefaultExternalCorrelationAlias(attributePath);
        }
        if (ExpressionUtils.isEmptyOrThis(correlationResult)) {
            this.correlationResult = correlationExternalAlias;
        } else {
            this.correlationResult = PrefixingQueryGenerator.prefix(ef, correlationResult, correlationExternalAlias, viewRootType.getEntityViewRootTypes().keySet(), true);
        }
        this.indexExpression = indexExpression == null ? null : PrefixingQueryGenerator.prefix(ef, indexExpression, this.correlationResult, viewRootType.getEntityViewRootTypes().keySet(), true);
        this.fetches = prefix(correlationAlias, fetches);
        this.indexFetches = prefix(this.indexExpression, indexFetches);
    }

    private static String[] prefix(String prefix, String[] fetches) {
        if (fetches == null || fetches.length == 0) {
            return fetches;
        }
        String[] newFetches = new String[fetches.length];
        for (int i = 0; i < fetches.length; i++) {
            newFetches[i] = (prefix  + "." + fetches[i]).intern();
        }

        return newFetches;
    }

    protected String getEntityIdName(Class<?> entityClass) {
        ManagedType<?> managedType = entityViewConfiguration.getCriteriaBuilder().getMetamodel().managedType(entityClass);
        if (JpaMetamodelUtils.isIdentifiable(managedType)) {
            return JpaMetamodelUtils.getSingleIdAttribute((IdentifiableType<?>) managedType).getName();
        } else {
            return null;
        }
    }

    protected void fillDefaultValues(Map<Object, Map<Object, TuplePromise>> promiseMap) {
        for (Map.Entry<Object, Map<Object, TuplePromise>> entry : promiseMap.entrySet()) {
            for (Map.Entry<Object, TuplePromise> promiseEntry : entry.getValue().entrySet()) {
                TuplePromise promise = promiseEntry.getValue();
                if (!promise.hasResult()) {
                    promise.onResult(createDefaultResult(), this);
                }
            }
        }
    }

    @Override
    public int getConsumableIndex() {
        return -1;
    }

    @Override
    public Object copy(Object o) {
        return containerAccumulator == null ? o : createContainer(o);
    }

    protected boolean isRecording() {
        return false;
    }

    protected Object postConstruct(Object value) {
        containerAccumulator.postConstruct(value);
        return value;
    }

    protected Object createDefaultResult() {
        return containerAccumulator == null ? null : containerAccumulator.createContainer(isRecording(), 0);
    }

    protected Object createContainer(Object container) {
        Object result = containerAccumulator.createContainer(isRecording(), 0);
        containerAccumulator.addAll(result, container, isRecording());
        return result;
    }

    protected void populateParameters(FullQueryBuilder<?, ?> queryBuilder) {
        FullQueryBuilder<?, ?> mainBuilder = entityViewConfiguration.getCriteriaBuilder();
        for (Parameter<?> paramEntry : mainBuilder.getParameters()) {
            if (queryBuilder.containsParameter(paramEntry.getName()) && !queryBuilder.isParameterSet(paramEntry.getName())) {
                queryBuilder.setParameter(paramEntry.getName(), mainBuilder.getParameterValue(paramEntry.getName()));
            }
        }
        for (Map.Entry<String, Object> paramEntry : entityViewConfiguration.getOptionalParameters().entrySet()) {
            if (queryBuilder.containsParameter(paramEntry.getKey()) && !queryBuilder.isParameterSet(paramEntry.getKey())) {
                queryBuilder.setParameter(paramEntry.getKey(), paramEntry.getValue());
            }
        }
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    protected static class TuplePromise {

        private final int index;
        private Object result;
        private TupleResultCopier copier;
        private boolean hasResult;
        private List<Object[]> tuples = new ArrayList<Object[]>();

        public TuplePromise(int index) {
            this.index = index;
        }

        public void add(Object[] tuple) {
            if (hasResult) {
                tuple[index] = copier.copy(result);
            } else {
                tuples.add(tuple);
            }
        }

        public void onResult(Object result, TupleResultCopier copier) {
            hasResult = true;
            this.result = result;
            this.copier = copier;
            // Every tuple promies must at least have one tuple
            tuples.get(0)[index] = result;
            for (int i = 1; i < tuples.size(); i++) {
                tuples.get(i)[index] = copier.copy(result);
            }
        }

        public boolean hasResult() {
            return hasResult;
        }
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    protected static final class FixedArrayList implements List<Object> {

        private final Object[] array;
        private int size;

        public FixedArrayList(int size) {
            this.array = new Object[size];
        }

        public Object get(int index) {
            return array[index];
        }

        public Object set(int index, Object value) {
            array[index] = value;
            return null;
        }

        public boolean add(Object value) {
            array[size++] = value;
            return true;
        }

        public int size() {
            return array.length;
        }

        public int realSize() {
            return size;
        }

        public void reset() {
            size = 0;
        }

        public void clearRest() {
            for (int i = size; i < array.length; i++) {
                array[i] = null;
            }
        }

        /* List implementation */

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean contains(Object o) {
            return indexOf(o) != -1;
        }

        @Override
        public Iterator<Object> iterator() {
            return listIterator();
        }

        @Override
        public Object[] toArray() {
            return array;
        }

        @Override
        public <T> T[] toArray(T[] a) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean addAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean addAll(int index, Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(int index, Object element) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object remove(int index) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int indexOf(Object o) {
            for (int i = 0; i < array.length; i++) {
                if (o.equals(array[i])) {
                    return i;
                }
            }
            return -1;
        }

        @Override
        public int lastIndexOf(Object o) {
            for (int i = array.length - 1; i > -1; i--) {
                if (o.equals(array[i])) {
                    return i;
                }
            }
            return -1;
        }

        @Override
        public ListIterator<Object> listIterator() {
            return listIterator(0);
        }

        @Override
        public ListIterator<Object> listIterator(final int index) {
            return new ListIterator<Object>() {

                private int cursor = index;

                @Override
                public boolean hasNext() {
                    return cursor < array.length;
                }

                @Override
                public Object next() {
                    return array[cursor++];
                }

                @Override
                public boolean hasPrevious() {
                    return cursor > 0;
                }

                @Override
                public Object previous() {
                    return array[--cursor];
                }

                @Override
                public int nextIndex() {
                    return cursor;
                }

                @Override
                public int previousIndex() {
                    return cursor - 1;
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void set(Object o) {
                    array[cursor - 1] = 0;
                }

                @Override
                public void add(Object o) {
                    throw new UnsupportedOperationException();
                }
            };
        }

        @Override
        public List<Object> subList(int fromIndex, int toIndex) {
            throw new UnsupportedOperationException();
        }
    }
}
