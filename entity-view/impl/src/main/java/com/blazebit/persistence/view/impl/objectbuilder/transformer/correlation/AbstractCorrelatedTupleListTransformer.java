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
import com.blazebit.reflection.ReflectionUtils;

import javax.persistence.Query;
import javax.persistence.metamodel.IdentifiableType;
import javax.persistence.metamodel.ManagedType;
import java.util.*;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractCorrelatedTupleListTransformer extends TupleListTransformer {

    private static final String CORRELATION_KEY_ALIAS = "correlationKey";
    private static final String CORRELATION_PARAM_PREFIX = "correlationParam_";

    protected final Correlator correlator;
    protected final Class<?> criteriaBuilderRoot;
    protected final ManagedViewType<?> viewRootType;
    protected final String correlationResult;
    protected final CorrelationProviderFactory correlationProviderFactory;
    protected final Class<?> correlationBasisType;
    protected final Class<?> correlationBasisEntity;

    protected final EntityViewConfiguration entityViewConfiguration;
    protected final int batchSize;
    protected final boolean expectBatchCorrelationValues;

    protected String correlationParamName;
    protected String correlationKeyExpression;
    protected CriteriaBuilder<?> criteriaBuilder;
    protected CorrelatedSubqueryViewRootJpqlMacro viewRootJpqlMacro;
    protected Query query;

    public AbstractCorrelatedTupleListTransformer(Correlator correlator, Class<?> criteriaBuilderRoot, ManagedViewType<?> viewRootType, String correlationResult, CorrelationProviderFactory correlationProviderFactory, String attributePath, int tupleIndex, int defaultBatchSize, Class<?> correlationBasisType, Class<?> correlationBasisEntity, EntityViewConfiguration entityViewConfiguration) {
        super(tupleIndex);
        this.correlator = correlator;
        this.criteriaBuilderRoot = criteriaBuilderRoot;
        this.viewRootType = viewRootType;
        this.correlationResult = correlationResult;
        this.correlationProviderFactory = correlationProviderFactory;
        this.correlationBasisType = correlationBasisType;
        this.correlationBasisEntity = correlationBasisEntity;
        this.entityViewConfiguration = entityViewConfiguration;
        this.batchSize = entityViewConfiguration.getBatchSize(attributePath, defaultBatchSize);
        this.expectBatchCorrelationValues = entityViewConfiguration.getExpectBatchCorrelationValues(attributePath);
        // TODO: take special care when handling parameters. some must be copied, others should probably be moved to optional parameters
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

    private String getEntityIdName(Class<?> entityClass) {
        ManagedType<?> managedType = entityViewConfiguration.getCriteriaBuilder().getMetamodel().managedType(entityClass);
        if (managedType instanceof IdentifiableType<?>) {
            IdentifiableType<?> identifiableType = (IdentifiableType<?>) managedType;
            Class<?> idType = identifiableType.getIdType().getJavaType();
            try {
                return identifiableType.getId(idType).getName();
            } catch (IllegalArgumentException ex) {
                idType = ReflectionUtils.getPrimitiveClassOfWrapper(idType);
                if (idType == null) {
                    throw ex;
                }
                return identifiableType.getId(idType).getName();
            }
        } else {
            return null;
        }
    }

    private String applyAndGetCorrelationRoot(boolean batchCorrelationValues) {
        Class<?> viewRootEntityClass = viewRootType.getEntityClass();
        String idAttributePath = getEntityIdName(viewRootEntityClass);

        FullQueryBuilder<?, ?> queryBuilder = entityViewConfiguration.getCriteriaBuilder();
        Map<String, Object> optionalParameters = entityViewConfiguration.getOptionalParameters();

        Class<?> correlationBasisEntityType;
        String viewRootExpression;
        if (batchCorrelationValues) {
            correlationBasisEntityType = correlationBasisEntity;
            viewRootExpression = null;
        } else {
            correlationBasisEntityType = viewRootEntityClass;
            viewRootExpression = CORRELATION_KEY_ALIAS;
        }

        this.criteriaBuilder = queryBuilder.getCriteriaBuilderFactory().create(queryBuilder.getEntityManager(), Object[].class);
        this.viewRootJpqlMacro = new CorrelatedSubqueryViewRootJpqlMacro(criteriaBuilder, optionalParameters, viewRootEntityClass, idAttributePath, viewRootExpression);
        this.criteriaBuilder.registerMacro("view_root", viewRootJpqlMacro);

        SubqueryCorrelationBuilder correlationBuilder = new SubqueryCorrelationBuilder(criteriaBuilder, correlationResult, correlationBasisType, correlationBasisEntityType, CORRELATION_KEY_ALIAS, batchSize);
        CorrelationProvider provider = correlationProviderFactory.create(entityViewConfiguration.getCriteriaBuilder(), entityViewConfiguration.getOptionalParameters());

        if (batchSize > 1) {
            if (batchCorrelationValues) {
                this.correlationParamName = CORRELATION_KEY_ALIAS;
            } else {
                this.correlationParamName = generateCorrelationParamName();
            }
            if (correlationBasisEntityType != null) {
                if (batchCorrelationValues) {
                    correlationKeyExpression = CORRELATION_KEY_ALIAS + getEntityIdName(correlationBasisEntityType);
                } else {
                    correlationKeyExpression = CORRELATION_KEY_ALIAS + '.' + idAttributePath;
                }
            } else {
                // The correlation key is basic type
                correlationKeyExpression = CORRELATION_KEY_ALIAS + ".value";
            }
        } else {
            this.correlationParamName = generateCorrelationParamName();
            this.correlationKeyExpression = null;
        }

        if (batchSize > 1 && batchCorrelationValues) {
            provider.applyCorrelation(correlationBuilder, correlationKeyExpression);
        } else {
            provider.applyCorrelation(correlationBuilder, ':' + correlationParamName);
        }

        return correlationBuilder.getCorrelationRoot();
    }

    @Override
    public List<Object[]> transform(List<Object[]> tuples) {
        FixedArrayList correlationParams = new FixedArrayList(batchSize);
        // Implementation detail: the tuple list is a LinkedList
        Iterator<Object[]> tupleListIter = tuples.iterator();

        final String correlationRoot = applyAndGetCorrelationRoot(expectBatchCorrelationValues);

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
                    // If the expectation was wrong, we have to create a new criteria builder
                    if (!expectBatchCorrelationValues) {
                        applyAndGetCorrelationRoot(true);
                    }

                    criteriaBuilder.select(correlationKeyExpression);
                }
                correlator.finish(criteriaBuilder, entityViewConfiguration, batchSize, correlationRoot);
                query = criteriaBuilder.getQuery();

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
                            batchLoad(batchValues, correlationParams, viewRootIds, correlationParams.get(0), true);
                        }
                    }

                    if (correlationParams.realSize() > 0) {
                        viewRootIds.add(batchEntry.getKey());
                        batchLoad(batchValues, correlationParams, viewRootIds, null, true);
                    }
                }
            } else {
                if (batchSize > 1) {
                    // If the expectation was wrong, we have to create a new criteria builder
                    if (expectBatchCorrelationValues) {
                        applyAndGetCorrelationRoot(false);
                    }

                    criteriaBuilder.select(correlationKeyExpression);
                }
                correlator.finish(criteriaBuilder, entityViewConfiguration, batchSize, correlationRoot);
                query = criteriaBuilder.getQuery();

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
                            batchLoad(batchValues, correlationParams, viewRootIds, viewRootIds.get(0), false);
                        }
                    }

                    if (viewRootIds.realSize() > 0) {
                        if (correlationBasisEntity != null) {
                            correlationParams.add(criteriaBuilder.getEntityManager().getReference(correlationBasisEntity, batchEntry.getKey()));
                        } else {
                            correlationParams.add(batchEntry.getKey());
                        }
                        batchLoad(batchValues, correlationParams, viewRootIds, null, false);
                    }
                }
            }
        } else {
            if (batchSize > 1) {
                // If the expectation was wrong, we have to create a new criteria builder
                if (!expectBatchCorrelationValues) {
                    applyAndGetCorrelationRoot(true);
                }

                criteriaBuilder.select(correlationKeyExpression);
            }
            correlator.finish(criteriaBuilder, entityViewConfiguration, batchSize, correlationRoot);
            query = criteriaBuilder.getQuery();

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
                        batchLoad(correlationValues, correlationParams, null, correlationParams.get(0), true);
                    }
                } else {
                    tupleIndexValue.add(tuple);
                }
            }

            if (correlationParams.realSize() > 0) {
                batchLoad(correlationValues, correlationParams, null, null, true);
            }
        }

        return tuples;
    }

    private void batchLoad(Map<Object, TuplePromise> correlationValues, FixedArrayList batchParameters, FixedArrayList viewRootIds, Object defaultKey, boolean batchCorrelationValues) {
        batchParameters.clearRest();
        if (batchCorrelationValues) {
            query.setParameter(correlationParamName, batchParameters);
        } else {
            query.setParameter(correlationParamName, batchParameters.get(0));
        }

        if (viewRootIds != null) {
            viewRootIds.clearRest();
            viewRootJpqlMacro.setParameters(query, viewRootIds);
        }

        populateResult(correlationValues, defaultKey, (List<Object>) query.getResultList());

        batchParameters.reset();
        if (viewRootIds != null) {
            viewRootIds.reset();
        }
    }

    protected abstract void populateResult(Map<Object, TuplePromise> correlationValues, Object defaultKey, List<Object> list);

    protected static interface TupleResultCopier {

        public Object copy(Object o);

    }

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
