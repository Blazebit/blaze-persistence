/*
 * Copyright 2014 - 2016 Blazebit.
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

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.view.CorrelationProvider;
import com.blazebit.persistence.view.impl.CorrelationProviderFactory;
import com.blazebit.persistence.view.impl.EntityViewConfiguration;
import com.blazebit.persistence.view.impl.macro.CorrelatedSubqueryViewRootJpqlMacro;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.TupleListTransformer;
import com.blazebit.persistence.view.metamodel.ManagedViewType;

import javax.persistence.metamodel.IdentifiableType;
import javax.persistence.metamodel.ManagedType;
import java.util.*;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractCorrelatedTupleListTransformer extends TupleListTransformer {

    private static final String CORRELATION_PARAM_PREFIX = "correlationParam_";

    protected final Correlator correlator;
    protected final Class<?> criteriaBuilderRoot;
    protected final ManagedViewType<?> viewRootType;
    protected final String correlationResult;
    protected final CorrelationProviderFactory correlationProviderFactory;
    protected final String attributePath;
    protected final Class<?> correlationBasisEntity;

    protected final EntityViewConfiguration entityViewConfiguration;
    protected final int batchSize;

    protected final String correlationParamName;
    protected final CriteriaBuilder<?> criteriaBuilder;
    protected final CorrelatedSubqueryViewRootJpqlMacro viewRootJpqlMacro;
    protected final String correlationRoot;

    public AbstractCorrelatedTupleListTransformer(Correlator correlator, Class<?> criteriaBuilderRoot, ManagedViewType<?> viewRootType, String correlationResult, CorrelationProviderFactory correlationProviderFactory, String attributePath, int tupleIndex, int batchSize, Class<?> correlationBasisEntity, EntityViewConfiguration entityViewConfiguration) {
        super(tupleIndex);
        this.correlator = correlator;
        this.criteriaBuilderRoot = criteriaBuilderRoot;
        this.viewRootType = viewRootType;
        this.correlationResult = correlationResult;
        this.correlationProviderFactory = correlationProviderFactory;
        this.attributePath = attributePath;
        this.correlationBasisEntity = correlationBasisEntity;
        this.entityViewConfiguration = entityViewConfiguration;
        this.batchSize = entityViewConfiguration.getBatchSize(attributePath, batchSize);

        Class<?> viewRootEntityClass = viewRootType.getEntityClass();
        ManagedType<?> managedType = entityViewConfiguration.getCriteriaBuilder().getMetamodel().managedType(viewRootEntityClass);
        String idAttributePath;
        if (managedType instanceof IdentifiableType<?>) {
            IdentifiableType<?> identifiableType = (IdentifiableType<?>) managedType;
            idAttributePath = identifiableType.getId(identifiableType.getIdType().getJavaType()).getName();
        } else {
            idAttributePath = null;
        }

        this.correlationParamName = generateCorrelationParamName();
        FullQueryBuilder<?, ?> queryBuilder = entityViewConfiguration.getCriteriaBuilder();
        Map<String, Object> optionalParameters = entityViewConfiguration.getOptionalParameters();

        this.criteriaBuilder = queryBuilder.getCriteriaBuilderFactory().create(queryBuilder.getEntityManager(), criteriaBuilderRoot);
        this.viewRootJpqlMacro = new CorrelatedSubqueryViewRootJpqlMacro(criteriaBuilder, optionalParameters, viewRootEntityClass, idAttributePath);
        this.criteriaBuilder.registerMacro("view_root", viewRootJpqlMacro);
        // TODO: take special care when handling parameters. some must be copied, others should probably be moved to optional parameters

        SubqueryCorrelationBuilder correlationBuilder = new SubqueryCorrelationBuilder(criteriaBuilder, correlationResult);
        CorrelationProvider provider = correlationProviderFactory.create(entityViewConfiguration.getCriteriaBuilder(), entityViewConfiguration.getOptionalParameters());
        provider.applyCorrelation(correlationBuilder, ':' + correlationParamName);
        this.correlationRoot = correlationBuilder.getCorrelationRoot();
    }

    private String generateCorrelationParamName() {
        final FullQueryBuilder<?, ?> queryBuilder = entityViewConfiguration.getCriteriaBuilder();
        final Map<String, Object> optionalParameters = entityViewConfiguration.getOptionalParameters();
        int paramNumber = 0;
        String paramName;
        while (true) {
            paramName = CORRELATION_PARAM_PREFIX + paramNumber;
            if (queryBuilder.getParameter(paramName) != null) {
                paramNumber++;
            } else if (optionalParameters.containsKey(paramName)) {
                paramNumber++;
            } else {
                return paramName;
            }
        }
    }

    @Override
    public List<Object[]> transform(List<Object[]> tuples) {
        FixedArrayList correlationParams = new FixedArrayList(batchSize);
        // Implementation detail: the tuple list is a LinkedList
        Iterator<Object[]> tupleListIter = tuples.iterator();

        // If view root is used, we have to decide whether we do batches for each view root id or correlation param
        if (viewRootJpqlMacro.usesViewRoot()) {
            tupleListIter = tuples.iterator();
            int totalSize = tuples.size();
            Map<Object, Map<Object, TuplePromise>> viewRoots = new HashMap<Object, Map<Object, TuplePromise>>(totalSize);
            Map<Object, Map<Object, TuplePromise>> correlationValues = new HashMap<Object, Map<Object, TuplePromise>>(totalSize);

            // Group tuples by view roots and correlation values and create tuple promises
            while (tupleListIter.hasNext()) {
                Object[] tuple = tupleListIter.next();
                Object viewRootKey = tuple[0];
                Object correlationValueKey = tuple[startIndex];

                Map<Object, TuplePromise> viewRootCorrelationValues = viewRoots.get(viewRootKey);
                if (viewRootCorrelationValues == null) {
                    viewRootCorrelationValues = new HashMap<Object, TuplePromise>();
                    viewRoots.put(viewRootKey, viewRootCorrelationValues);
                }
                TuplePromise viewRootPromise = viewRootCorrelationValues.get(correlationValueKey);
                if (viewRootPromise == null) {
                    viewRootPromise = new TuplePromise(startIndex);
                    viewRootCorrelationValues.put(correlationValueKey, viewRootPromise);
                }
                viewRootPromise.add(tuple);

                Map<Object, TuplePromise> correlationValueViewRoots = correlationValues.get(correlationValueKey);
                if (correlationValueViewRoots == null) {
                    correlationValueViewRoots = new HashMap<Object, TuplePromise>();
                    correlationValues.put(correlationValueKey, correlationValueViewRoots);
                }
                TuplePromise correlationValuePromise = correlationValueViewRoots.get(viewRootKey);
                if (correlationValuePromise == null) {
                    correlationValuePromise = new TuplePromise(startIndex);
                    correlationValueViewRoots.put(viewRootKey, correlationValuePromise);
                }
                correlationValuePromise.add(tuple);
            }

            boolean batchCorrelationValues = viewRoots.size() <= correlationValues.size();
            FixedArrayList viewRootIds = new FixedArrayList(batchSize);

            if (batchCorrelationValues) {
                if (batchSize > 1) {
                    // TODO: Implement
                    throw new UnsupportedOperationException("Not yet implemented!");
//                    criteriaBuilder.fromValues(entityClass, alias);
                }
                correlator.finish(criteriaBuilder, entityViewConfiguration, batchSize, correlationRoot);

                for (Map.Entry<Object, Map<Object, TuplePromise>> batchEntry : viewRoots.entrySet()) {
                    Map<Object, TuplePromise> batchValues = batchEntry.getValue();
                    for (Map.Entry<Object, TuplePromise> batchValueEntry : batchValues.entrySet()) {
                        if (correlationBasisEntity != null) {
                            correlationParams.add(criteriaBuilder.getEntityManager().getReference(correlationBasisEntity, batchValueEntry.getKey()));
                        } else {
                            correlationParams.add(batchValueEntry.getKey());
                        }

                        if (batchSize == correlationParams.realSize()) {
                            viewRootIds.add(batchEntry.getKey());
                            batchLoad(batchValues, correlationParams, viewRootIds, correlationParams.get(0));
                        }
                    }

                    if (correlationParams.realSize() > 0) {
                        viewRootIds.add(batchEntry.getKey());
                        batchLoad(batchValues, correlationParams, viewRootIds, null);
                    }
                }
            } else {
                if (batchSize > 1) {
                    // TODO: Implement
                    throw new UnsupportedOperationException("Not yet implemented!");
//                    criteriaBuilder.fromValues(entityClass, alias);
                }
                correlator.finish(criteriaBuilder, entityViewConfiguration, batchSize, correlationRoot);

                for (Map.Entry<Object, Map<Object, TuplePromise>> batchEntry : correlationValues.entrySet()) {
                    Map<Object, TuplePromise> batchValues = batchEntry.getValue();
                    for (Map.Entry<Object, TuplePromise> batchValueEntry : batchValues.entrySet()) {
                        viewRootIds.add(batchValueEntry.getKey());

                        if (batchSize == viewRootIds.realSize()) {
                            if (correlationBasisEntity != null) {
                                correlationParams.add(criteriaBuilder.getEntityManager().getReference(correlationBasisEntity, batchEntry.getKey()));
                            } else {
                                correlationParams.add(batchEntry.getKey());
                            }
                            batchLoad(batchValues, correlationParams, viewRootIds, viewRootIds.get(0));
                        }
                    }

                    if (viewRootIds.realSize() > 0) {
                        if (correlationBasisEntity != null) {
                            correlationParams.add(criteriaBuilder.getEntityManager().getReference(correlationBasisEntity, batchEntry.getKey()));
                        } else {
                            correlationParams.add(batchEntry.getKey());
                        }
                        batchLoad(batchValues, correlationParams, viewRootIds, null);
                    }
                }
            }
        } else {
            if (batchSize > 1) {
                // TODO: Implement
                throw new UnsupportedOperationException("Not yet implemented!");
//                    criteriaBuilder.fromValues(entityClass, alias);
            }
            correlator.finish(criteriaBuilder, entityViewConfiguration, batchSize, correlationRoot);

            Map<Object, TuplePromise> correlationValues = new HashMap<Object, TuplePromise>(tuples.size());
            while (tupleListIter.hasNext()) {
                Object[] tuple = tupleListIter.next();
                Object correlationValue = tuple[startIndex];
                TuplePromise tupleIndexValue = correlationValues.get(correlationValue);

                if (tupleIndexValue == null) {
                    tupleIndexValue = new TuplePromise(startIndex);
                    tupleIndexValue.add(tuple);
                    correlationValues.put(correlationValue, tupleIndexValue);

                    if (correlationBasisEntity != null) {
                        correlationParams.add(criteriaBuilder.getEntityManager().getReference(correlationBasisEntity, tuple[startIndex]));
                    } else {
                        correlationParams.add(tuple[startIndex]);
                    }

                    if (batchSize == correlationParams.realSize()) {
                        batchLoad(correlationValues, correlationParams, null, correlationParams.get(0));
                    }
                } else {
                    tupleIndexValue.add(tuple);
                }
            }

            if (correlationParams.realSize() > 0) {
                batchLoad(correlationValues, correlationParams, null, null);
            }
        }

        return tuples;
    }

    private void batchLoad(Map<Object, TuplePromise> correlationValues, FixedArrayList batchParameters, FixedArrayList viewRootIds, Object defaultKey) {
        batchParameters.clearRest();
        criteriaBuilder.setParameter(correlationParamName, batchParameters);
        if (viewRootIds != null) {
            viewRootIds.clearRest();
            viewRootJpqlMacro.setParameters(viewRootIds);
        }

        populateResult(correlationValues, defaultKey, (List<Object[]>) criteriaBuilder.getResultList());

        batchParameters.reset();
        if (viewRootIds != null) {
            viewRootIds.reset();
        }
    }

    protected abstract void populateResult(Map<Object, TuplePromise> correlationValues, Object defaultKey, List<Object[]> list);

    protected static class TuplePromise {

        private final int index;
        private Object result;
        private boolean hasResult;
        private List<Object[]> tuples = new ArrayList<Object[]>();

        public TuplePromise(int index) {
            this.index = index;
        }

        public void add(Object[] tuple) {
            if (hasResult) {
                tuple[index] = result;
            } else {
                tuples.add(tuple);
            }
        }

        public void onResult(Object result) {
            hasResult = true;
            this.result = result;
            for (int i = 0; i < tuples.size(); i++) {
                tuples.get(i)[index] = result;
            }
        }
    }

    private static final class FixedArrayList implements List<Object> {

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
