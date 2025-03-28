/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl;

import com.blazebit.persistence.KeysetPage;
import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.PagedArrayList;
import com.blazebit.persistence.PagedList;
import com.blazebit.persistence.PaginatedTypedQuery;
import com.blazebit.persistence.impl.builder.object.CountExtractionObjectBuilder;
import com.blazebit.persistence.impl.builder.object.KeysetExtractionObjectBuilder;
import com.blazebit.persistence.impl.keyset.KeysetMode;
import com.blazebit.persistence.DefaultKeysetPage;
import com.blazebit.persistence.impl.keyset.KeysetPaginationHelper;
import com.blazebit.persistence.impl.util.SetView;

import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Parameter;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.ParameterExpression;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class PaginatedTypedQueryImpl<X> implements PaginatedTypedQuery<X> {

    private final boolean withExtractAllKeysets;
    private final boolean withCount;
    private final boolean boundedCount;
    private final int highestOffset;
    private final TypedQuery<?> countQuery;
    private final Query idQuery;
    private final TypedQuery<X> objectQuery;
    private final ObjectBuilder<X> objectBuilder;
    private final Map<String, Parameter<?>> parameters;
    private final Map<String, ParameterLocation> parameterToQuery;
    private final Map<ParameterExpression<?>, String> criteriaNameMapping;
    private final Object entityId;
    private int firstResult;
    private int pageSize;

    private final int identifierCount;
    private final boolean needsNewIdList;
    private final int[] keysetToSelectIndexMapping;
    private final int keysetSuffix;
    private final KeysetMode keysetMode;
    private final KeysetPage keysetPage;
    private final boolean forceFirstResult;
    private final boolean inlinedIdQuery;
    private final boolean inlinedCountQuery;

    public PaginatedTypedQueryImpl(boolean withExtractAllKeysets, boolean withCount, boolean boundedCount, int highestOffset, TypedQuery<?> countQuery, Query idQuery, TypedQuery<X> objectQuery, ObjectBuilder<X> objectBuilder, Collection<ParameterManager.ParameterImpl<?>> parameters, Map<ParameterExpression<?>, String> criteriaNameMapping,
                                   Object entityId, int firstResult, int pageSize, int identifierCount, boolean needsNewIdList, int[] keysetToSelectIndexMapping, KeysetMode keysetMode, KeysetPage keysetPage, boolean forceFirstResult, boolean inlinedIdQuery, boolean inlinedCountQuery) {
        this.withExtractAllKeysets = withExtractAllKeysets;
        this.withCount = withCount;
        this.boundedCount = boundedCount;
        this.highestOffset = highestOffset;
        this.countQuery = countQuery;
        this.idQuery = idQuery;
        this.objectQuery = objectQuery;
        this.objectBuilder = objectBuilder;
        this.parameterToQuery = new HashMap<>(parameters.size());
        this.entityId = entityId;
        this.firstResult = firstResult;
        this.pageSize = pageSize;
        this.identifierCount = identifierCount;
        this.needsNewIdList = needsNewIdList;
        this.keysetToSelectIndexMapping = keysetToSelectIndexMapping;
        this.keysetMode = keysetMode;
        this.keysetPage = keysetPage;
        this.forceFirstResult = forceFirstResult;
        this.inlinedIdQuery = inlinedIdQuery;
        this.inlinedCountQuery = inlinedCountQuery;
        this.criteriaNameMapping = criteriaNameMapping;

        Map<String, Parameter<?>> params = new HashMap<>(parameters.size());
        for (ParameterManager.ParameterImpl<?> parameter : parameters) {
            String name = parameter.getName();
            if (name == null) {
                name = parameter.getPosition().toString();
            }
            if (parameter.getCriteriaParameter() == null) {
                params.put(name, parameter);
            } else {
                params.put(name, parameter.getCriteriaParameter());
            }
        }

        this.parameters = Collections.unmodifiableMap(params);

        for (Parameter<?> parameter : countQuery.getParameters()) {
            parameterToQuery.put(getParameterName(parameter), ParameterLocation.COUNT);
        }
        if (idQuery != null) {
            for (Parameter<?> parameter : idQuery.getParameters()) {
                String name = getParameterName(parameter);
                ParameterLocation parameterLocation = parameterToQuery.get(name);
                if (parameterLocation == null) {
                    parameterLocation = ParameterLocation.ID;
                } else {
                    parameterLocation = parameterLocation.andId();
                }
                parameterToQuery.put(name, parameterLocation);
            }
        }
        if (objectQuery != null) {
            for (Parameter<?> parameter : objectQuery.getParameters()) {
                String name = getParameterName(parameter);
                ParameterLocation parameterLocation = parameterToQuery.get(name);
                if (parameterLocation == null) {
                    parameterLocation = ParameterLocation.OBJECT;
                } else {
                    parameterLocation = parameterLocation.andObject();
                }
                parameterToQuery.put(name, parameterLocation);
            }
        }

        int suffix = 0;
        if (inlinedCountQuery) {
            suffix++;
        }
        if (keysetToSelectIndexMapping != null) {
            for (int i = 0; i < keysetToSelectIndexMapping.length; i++) {
                if (keysetToSelectIndexMapping[i] == -1) {
                    suffix++;
                }
            }
        }
        this.keysetSuffix = suffix;
    }

    private String getParameterName(Parameter<?> parameter) {
        if (criteriaNameMapping != null && parameter.getName() == null && parameter instanceof ParameterExpression<?>) {
            return criteriaNameMapping.get(parameter);
        }
        String name = parameter.getName();
        if (name == null) {
            return parameter.getPosition().toString();
        }
        return name;
    }

    @Override
    public long getTotalCount() {
        return ((Number) countQuery.getSingleResult()).longValue();
    }

    @Override
    public List<X> getPageResultList() {
        int queryFirstResult = firstResult;
        int firstRow = firstResult;
        return getResultList(queryFirstResult, firstRow, -1L);
    }

    @Override
    @SuppressWarnings("unchecked")
    public PagedList<X> getResultList() {
        int queryFirstResult = firstResult;
        int firstRow = firstResult;
        long totalSize = -1L;
        if (withCount && !inlinedCountQuery) {
            if (entityId == null) {
                totalSize = ((Number) countQuery.getSingleResult()).longValue();
            } else {
                Object[] result = (Object[]) countQuery.getSingleResult();
                totalSize = ((Number) result[0]).longValue();

                if (result[1] == null) {
                    // If the reference entity id is not contained (i.e. has no position), we return this special value
                    queryFirstResult = -1;
                    firstRow = 0;
                } else {
                    // The page position is numbered from 1 so we need to correct this here
                    int position = ((Number) result[1]).intValue() - 1;
                    queryFirstResult = firstRow = position == 0 ? 0 : position - (position % pageSize);
                }
            }
        }

        if (totalSize == 0L) {
            return new PagedArrayList<X>(null, totalSize, queryFirstResult, pageSize);
        }

        return getResultList(queryFirstResult, firstRow, totalSize);
    }

    private PagedList<X> getResultList(int queryFirstResult, int firstRow, long totalSize) {
        if (idQuery != null) {
            idQuery.setMaxResults(pageSize);

            if (forceFirstResult || keysetMode == KeysetMode.NONE) {
                idQuery.setFirstResult(firstRow);
            } else {
                idQuery.setFirstResult(0);
            }

            List<?> ids = idQuery.getResultList();

            if (ids.isEmpty()) {
                KeysetPage newKeysetPage = null;
                if (keysetMode == KeysetMode.NEXT) {
                    // When we scroll over the last page to a non existing one, we reuse the current keyset
                    newKeysetPage = keysetPage;
                }
                long size;
                if (withCount && totalSize == -1) {
                    size = getTotalCount();
                } else {
                    size = totalSize;
                }
                if (boundedCount) {
                    if (keysetMode == KeysetMode.NEXT) {
                        size = Math.max(size, keysetPage.getFirstResult() + keysetPage.getMaxResults());
                    } else if (forceFirstResult || keysetMode == KeysetMode.NONE) {
                        size = Math.max(size, firstRow);
                    }
                }
                return new PagedArrayList<X>(newKeysetPage, size, queryFirstResult, pageSize);
            }

            Serializable[] lowest = null;
            Serializable[] highest = null;
            Serializable[][] keysets = null;

            if (needsNewIdList) {
                if (keysetToSelectIndexMapping != null) {
                    int keysetPageSize = pageSize - highestOffset;
                    int size = Math.min(ids.size(), keysetPageSize);
                    int lowestIndex;
                    int highestIndex;
                    // Swap keysets as we have inverse ordering when going to the previous page
                    if (keysetMode == KeysetMode.PREVIOUS) {
                        lowestIndex = size - 1;
                        highestIndex = 0;
                    } else {
                        lowestIndex = 0;
                        highestIndex = size - 1;
                    }
                    if (ids.get(0) instanceof Object[]) {
                        if (withExtractAllKeysets) {
                            keysets = new Serializable[size][];
                            for (int i = 0; i < size; i++) {
                                keysets[i] = KeysetPaginationHelper.extractKey((Object[]) ids.get(i), keysetToSelectIndexMapping, keysetSuffix);
                            }
                            lowest = keysets[lowestIndex];
                            highest = keysets[highestIndex];
                        } else {
                            lowest = KeysetPaginationHelper.extractKey((Object[]) ids.get(lowestIndex), keysetToSelectIndexMapping, keysetSuffix);
                            highest = KeysetPaginationHelper.extractKey((Object[]) ids.get(highestIndex), keysetToSelectIndexMapping, keysetSuffix);
                        }
                    } else {
                        if (withExtractAllKeysets) {
                            keysets = new Serializable[size][];
                            for (int i = 0; i < size; i++) {
                                keysets[i] = new Serializable[]{ (Serializable) (ids.get(i)) };
                            }
                            lowest = keysets[lowestIndex];
                            highest = keysets[highestIndex];
                        } else {
                            lowest = new Serializable[]{ (Serializable) ids.get(lowestIndex) };
                            highest = new Serializable[]{ (Serializable) ids.get(highestIndex) };
                        }
                    }

                    // Swap keysets as we have inverse ordering when going to the previous page
                    if (keysetMode == KeysetMode.PREVIOUS) {
                        if (withExtractAllKeysets) {
                            // Reverse the keysets
                            Serializable[] tmp;
                            for (int i = 0, mid = size >> 1, j = size - 1; i < mid; i++, j--) {
                                tmp = keysets[i];
                                keysets[i] = keysets[j];
                                keysets[j] = tmp;
                            }
                        }
                    }
                }

                // extract count
                if (inlinedCountQuery) {
                    Object[] first = (Object[]) ids.get(0);
                    totalSize = (long) first[first.length - 1];
                }
                List<Object> newIds = new ArrayList<Object>(ids.size());
                if (identifierCount > 1) {
                    for (int i = 0; i < ids.size(); i++) {
                        Object[] tuple = (Object[]) ids.get(i);
                        Object newId = new Object[identifierCount];
                        System.arraycopy(tuple, 0, newId, 0, identifierCount);
                        newIds.add(newId);
                    }
                } else {
                    for (int i = 0; i < ids.size(); i++) {
                        Object o = ids.get(i);
                        if (o instanceof Object[]) {
                            newIds.add(((Object[]) o)[0]);
                        } else {
                            newIds.add(o);
                        }
                    }
                }

                ids = newIds;
            } else if (inlinedCountQuery) {
                Object[] first = (Object[]) ids.get(0);
                int newSize = first.length - 1;
                totalSize = (long) first[first.length - 1];
                // If this would have been a non-object array type without the count query, we must unwrap the result
                List<Object> newIds = new ArrayList<>(ids.size());
                if (newSize == 1) {
                    for (int i = 0; i < ids.size(); i++) {
                        newIds.add(((Object[]) ids.get(i))[0]);
                    }
                } else {
                    for (int i = 0; i < ids.size(); i++) {
                        Object[] tuple = (Object[]) ids.get(i);
                        Object newId = new Object[newSize];
                        System.arraycopy(tuple, 0, newId, 0, newSize);
                        newIds.add(newId);
                    }
                }
                ids = newIds;
            }

            if (identifierCount > 1) {
                StringBuilder parameterNameBuilder = new StringBuilder(AbstractCommonQueryBuilder.ID_PARAM_NAME.length() + 10);
                parameterNameBuilder.append(AbstractCommonQueryBuilder.ID_PARAM_NAME).append('_');
                int start = parameterNameBuilder.length();
                Object[] empty = ids.size() < pageSize ? new Object[identifierCount] : null;
                for (int i = 0; i < pageSize; i++) {
                    Object[] tuple;
                    if (ids.size() > i) {
                        tuple = (Object[]) ids.get(i);
                    } else {
                        tuple = empty;
                    }
                    for (int j = 0; j < identifierCount; j++) {
                        parameterNameBuilder.setLength(start);
                        parameterNameBuilder.append(j).append('_').append(i);
                        objectQuery.setParameter(parameterNameBuilder.toString(), tuple[j]);
                    }
                }
            } else {
                objectQuery.setParameter(AbstractCommonQueryBuilder.ID_PARAM_NAME, ids);
            }

            KeysetPage newKeyset = null;

            if (keysetToSelectIndexMapping != null) {
                newKeyset = new DefaultKeysetPage(firstRow, pageSize, lowest, highest, keysets);
            }

            totalSize = Math.max(totalSize, firstRow + ids.size());
            List<X> queryResultList = objectQuery.getResultList();

            PagedList<X> pagedResultList = new PagedArrayList<X>(queryResultList, newKeyset, totalSize, queryFirstResult, pageSize);
            return pagedResultList;
        } else {
            if (!inlinedIdQuery) {
                objectQuery.setMaxResults(pageSize);

                if (forceFirstResult || keysetMode == KeysetMode.NONE) {
                    objectQuery.setFirstResult(firstRow);
                } else {
                    objectQuery.setFirstResult(0);
                }
            }

            List<X> result = objectQuery.getResultList();

            if (result.isEmpty()) {
                KeysetPage newKeysetPage = null;
                if (keysetMode == KeysetMode.NEXT) {
                    // When we scroll over the last page to a non existing one, we reuse the current keyset
                    newKeysetPage = keysetPage;
                }

                if (totalSize == -1) {
                    if (inlinedCountQuery && firstRow == 0) {
                        totalSize = 0L;
                    } else if (withCount) {
                        totalSize = getTotalCount();
                    }
                }
                if (boundedCount) {
                    if (keysetMode == KeysetMode.NEXT) {
                        totalSize = Math.max(totalSize, keysetPage.getFirstResult() + keysetPage.getMaxResults());
                    } else if (forceFirstResult || keysetMode == KeysetMode.NONE) {
                        totalSize = Math.max(totalSize, firstRow);
                    }
                }

                return new PagedArrayList<X>(newKeysetPage, totalSize, queryFirstResult, pageSize);
            }

            if (keysetMode == KeysetMode.PREVIOUS) {
                Collections.reverse(result);
            }

            KeysetPage newKeyset = null;

            if (keysetToSelectIndexMapping != null) {
                if (objectBuilder == null) {
                    // extract count
                    if (inlinedCountQuery) {
                        Object[] first = (Object[]) result.get(0);
                        totalSize = (long) first[first.length - 1];
                        // If this would have been a non-object array type without the count query, we must unwrap the result
                        if (first.length == 2) {
                            List<X> newResult = new ArrayList<>(result.size());
                            for (int i = 0; i < result.size(); i++) {
                                newResult.add((X) ((Object[]) result.get(i))[0]);
                            }
                            result = newResult;
                        }
                    }
                } else if (objectBuilder instanceof KeysetExtractionObjectBuilder<?>) {
                    KeysetExtractionObjectBuilder<?> keysetExtractionObjectBuilder = (KeysetExtractionObjectBuilder<?>) objectBuilder;
                    Serializable[] lowest = keysetExtractionObjectBuilder.getLowest();
                    Serializable[] highest = keysetExtractionObjectBuilder.getHighest();
                    Serializable[][] keysets = keysetExtractionObjectBuilder.getKeysets();
                    // extract count
                    if (inlinedCountQuery) {
                        totalSize = keysetExtractionObjectBuilder.getCount();
                    }
                    newKeyset = new DefaultKeysetPage(firstRow, pageSize, lowest, highest, keysets);
                } else if (objectBuilder instanceof CountExtractionObjectBuilder<?>) {
                    totalSize = ((CountExtractionObjectBuilder<X>) objectBuilder).getCount();
                }
            }

            totalSize = Math.max(totalSize, firstRow + result.size());

            PagedList<X> pagedResultList = new PagedArrayList<X>(result, newKeyset, totalSize, queryFirstResult, pageSize);
            return pagedResultList;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public X getSingleResult() {
        List<X> result = getResultList();
        if (result.size() == 0) {
            throw new NoResultException("No entity found for query");
        } else if (result.size() > 1) {
            final Set<X> uniqueResult = new HashSet<X>(result);
            if (uniqueResult.size() > 1) {
                throw new NonUniqueResultException("result returns more than one element");
            } else {
                return uniqueResult.iterator().next();
            }
        } else {
            return result.get(0);
        }
    }

    @Override
    public int executeUpdate() {
        throw new IllegalArgumentException("Can not call executeUpdate on a select query!");
    }

    @Override
    public TypedQuery<X> setMaxResults(int maxResult) {
        throw new IllegalArgumentException("Updating max results is not supported on paginated query!");
    }

    @Override
    public int getMaxResults() {
        return pageSize;
    }

    @Override
    public TypedQuery<X> setFirstResult(int startPosition) {
        throw new IllegalArgumentException("Updating first result is not supported on paginated query!");
    }

    @Override
    public int getFirstResult() {
        return firstResult;
    }

    @Override
    public TypedQuery<X> setHint(String hintName, Object value) {
        // TODO: implement
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Map<String, Object> getHints() {
        // TODO: implement
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public <T> TypedQuery<X> setParameter(Parameter<T> param, T value) {
        String name = getParameterName(param);
        if (name == null) {
            List<Query> queries = parameterToQuery.get(Integer.toString(param.getPosition())).getQueries(countQuery, idQuery, objectQuery);
            for (Query query : queries) {
                query.setParameter(param.getPosition(), value);
            }
        } else if (Character.isDigit(name.charAt(0))) {
            List<Query> queries = parameterToQuery.get(name).getQueries(countQuery, idQuery, objectQuery);
            for (Query query : queries) {
                query.setParameter(Integer.parseInt(name), value);
            }
        } else {
            List<Query> queries = parameterToQuery.get(name).getQueries(countQuery, idQuery, objectQuery);
            for (Query query : queries) {
                query.setParameter(name, value);
            }
        }
        return this;
    }

    @Override
    public TypedQuery<X> setParameter(Parameter<Calendar> param, Calendar value, TemporalType temporalType) {
        String name = getParameterName(param);
        if (name == null) {
            List<Query> queries = parameterToQuery.get(Integer.toString(param.getPosition())).getQueries(countQuery, idQuery, objectQuery);
            for (Query query : queries) {
                query.setParameter(param.getPosition(), value, temporalType);
            }
        } else if (Character.isDigit(name.charAt(0))) {
            List<Query> queries = parameterToQuery.get(name).getQueries(countQuery, idQuery, objectQuery);
            for (Query query : queries) {
                query.setParameter(Integer.parseInt(name), value, temporalType);
            }
        } else {
            List<Query> queries = parameterToQuery.get(name).getQueries(countQuery, idQuery, objectQuery);
            for (Query query : queries) {
                query.setParameter(name, value, temporalType);
            }
        }
        return this;
    }

    @Override
    public TypedQuery<X> setParameter(Parameter<Date> param, Date value, TemporalType temporalType) {
        String name = getParameterName(param);
        if (name == null) {
            List<Query> queries = parameterToQuery.get(Integer.toString(param.getPosition())).getQueries(countQuery, idQuery, objectQuery);
            for (Query query : queries) {
                query.setParameter(param.getPosition(), value, temporalType);
            }
        } else if (Character.isDigit(name.charAt(0))) {
            List<Query> queries = parameterToQuery.get(name).getQueries(countQuery, idQuery, objectQuery);
            for (Query query : queries) {
                query.setParameter(Integer.parseInt(name), value, temporalType);
            }
        } else {
            List<Query> queries = parameterToQuery.get(name).getQueries(countQuery, idQuery, objectQuery);
            for (Query query : queries) {
                query.setParameter(name, value, temporalType);
            }
        }
        return this;
    }

    @Override
    public TypedQuery<X> setParameter(String name, Object value) {
        return setParameter((Parameter<Object>) getParameter(name), value);
    }

    @Override
    public TypedQuery<X> setParameter(String name, Calendar value, TemporalType temporalType) {
        return setParameter(getParameter(name, Calendar.class), value, temporalType);
    }

    @Override
    public TypedQuery<X> setParameter(String name, Date value, TemporalType temporalType) {
        return setParameter(getParameter(name, Date.class), value, temporalType);
    }

    @Override
    public TypedQuery<X> setParameter(int position, Object value) {
        return setParameter((Parameter<Object>) getParameter(position), value);
    }

    @Override
    public TypedQuery<X> setParameter(int position, Calendar value, TemporalType temporalType) {
        return setParameter(getParameter(position, Calendar.class), value, temporalType);
    }

    @Override
    public TypedQuery<X> setParameter(int position, Date value, TemporalType temporalType) {
        return setParameter(getParameter(position, Date.class), value, temporalType);
    }

    @Override
    public Set<Parameter<?>> getParameters() {
        return new SetView<>(parameters.values());
    }

    @Override
    public Parameter<?> getParameter(String name) {
        Parameter<?> param = parameters.get(name);
        if (param == null) {
            throw new IllegalArgumentException("Couldn't find parameter with name '" + name + "'!");
        }
        return param;
    }

    @Override
    public <T> Parameter<T> getParameter(String name, Class<T> type) {
        Parameter<?> param = getParameter(name);
        if (!param.getParameterType().isAssignableFrom(type)) {
            throw new IllegalArgumentException(
                    "The parameter with the name '" + name + "' has the type '" + param.getParameterType().getName() +
                            "' which is not assignable to requested type '" + type.getName() + "'"
            );
        }
        return (Parameter<T>) param;
    }

    @Override
    public boolean isBound(Parameter<?> param) {
        if (objectQuery.isBound(param)) {
            return true;
        }
        if (idQuery != null && idQuery.isBound(param)) {
            return true;
        }
        return countQuery.isBound(param);
    }

    @Override
    public <T> T getParameterValue(Parameter<T> param) {
        if (param.getName() == null) {
            Query query = parameterToQuery.get(Integer.toString(param.getPosition())).getQuery(countQuery, idQuery, objectQuery);
            return (T) query.getParameterValue(param.getPosition());
        } else if (Character.isDigit(param.getName().charAt(0))) {
            Query query = parameterToQuery.get(param.getName()).getQuery(countQuery, idQuery, objectQuery);
            return (T) query.getParameterValue(Integer.parseInt(param.getName()));
        } else {
            Query query = parameterToQuery.get(param.getName()).getQuery(countQuery, idQuery, objectQuery);
            return (T) query.getParameterValue(param.getName());
        }
    }

    @Override
    public Object getParameterValue(String name) {
        Query query = parameterToQuery.get(name).getQuery(countQuery, idQuery, objectQuery);
        return query.getParameterValue(name);
    }

    @Override
    public Object getParameterValue(int position) {
        Query query = parameterToQuery.get(Integer.toString(position)).getQuery(countQuery, idQuery, objectQuery);
        return query.getParameterValue(position);
    }

    @Override
    public Parameter<?> getParameter(int position) {
        String string = Integer.toString(position);
        Parameter<?> param = parameters.get(string);
        if (param == null) {
            throw new IllegalArgumentException("Couldn't find parameter with position '" + position + "'!");
        }
        return param;
    }

    @Override
    public <T> Parameter<T> getParameter(int position, Class<T> type) {
        Parameter<?> param = getParameter(position);
        if (!param.getParameterType().isAssignableFrom(type)) {
            throw new IllegalArgumentException(
                    "The parameter at position '" + position + "' has the type '" + param.getParameterType().getName() +
                    "' which is not assignable to requested type '" + type.getName() + "'"
            );
        }
        return (Parameter<T>) param;
    }

    @Override
    public TypedQuery<X> setFlushMode(FlushModeType flushMode) {
        objectQuery.setFlushMode(flushMode);
        return this;
    }

    @Override
    public FlushModeType getFlushMode() {
        return objectQuery.getFlushMode();
    }

    @Override
    public TypedQuery<X> setLockMode(LockModeType lockMode) {
        objectQuery.setLockMode(lockMode);
        return this;
    }

    @Override
    public LockModeType getLockMode() {
        return objectQuery.getLockMode();
    }

    @Override
    public <T> T unwrap(Class<T> cls) {
        throw new PersistenceException("Unsupported unwrap: " + cls.getName());
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    private static enum ParameterLocation {
        COUNT {
            @Override
            public ParameterLocation andId() {
                return COUNT_ID;
            }

            @Override
            public ParameterLocation andObject() {
                return COUNT_OBJECT;
            }

            @Override
            public Query getQuery(Query countQuery, Query idQuery, Query objectQuery) {
                return countQuery;
            }

            @Override
            public List<Query> getQueries(Query countQuery, Query idQuery, Query objectQuery) {
                return Collections.singletonList(countQuery);
            }
        },
        COUNT_ID {
            @Override
            public ParameterLocation andId() {
                return this;
            }

            @Override
            public ParameterLocation andObject() {
                return COUNT_ID_OBJECT;
            }

            @Override
            public Query getQuery(Query countQuery, Query idQuery, Query objectQuery) {
                return countQuery;
            }

            @Override
            public List<Query> getQueries(Query countQuery, Query idQuery, Query objectQuery) {
                return Arrays.asList(countQuery, idQuery);
            }
        },
        COUNT_ID_OBJECT {
            @Override
            public ParameterLocation andId() {
                return this;
            }

            @Override
            public ParameterLocation andObject() {
                return this;
            }

            @Override
            public Query getQuery(Query countQuery, Query idQuery, Query objectQuery) {
                return countQuery;
            }

            @Override
            public List<Query> getQueries(Query countQuery, Query idQuery, Query objectQuery) {
                return Arrays.asList(countQuery, idQuery, objectQuery);
            }
        },
        COUNT_OBJECT {
            @Override
            public ParameterLocation andId() {
                return COUNT_ID_OBJECT;
            }

            @Override
            public ParameterLocation andObject() {
                return this;
            }

            @Override
            public Query getQuery(Query countQuery, Query idQuery, Query objectQuery) {
                return countQuery;
            }

            @Override
            public List<Query> getQueries(Query countQuery, Query idQuery, Query objectQuery) {
                return Arrays.asList(countQuery, objectQuery);
            }
        },
        ID {
            @Override
            public ParameterLocation andId() {
                return this;
            }

            @Override
            public ParameterLocation andObject() {
                return ID_OBJECT;
            }

            @Override
            public Query getQuery(Query countQuery, Query idQuery, Query objectQuery) {
                return idQuery;
            }

            @Override
            public List<Query> getQueries(Query countQuery, Query idQuery, Query objectQuery) {
                return Collections.singletonList(idQuery);
            }
        },
        ID_OBJECT {
            @Override
            public ParameterLocation andId() {
                return this;
            }

            @Override
            public ParameterLocation andObject() {
                return this;
            }

            @Override
            public Query getQuery(Query countQuery, Query idQuery, Query objectQuery) {
                return idQuery;
            }

            @Override
            public List<Query> getQueries(Query countQuery, Query idQuery, Query objectQuery) {
                return Arrays.asList(idQuery, objectQuery);
            }
        },
        OBJECT {
            @Override
            public ParameterLocation andId() {
                return ID_OBJECT;
            }

            @Override
            public ParameterLocation andObject() {
                return this;
            }

            @Override
            public Query getQuery(Query countQuery, Query idQuery, Query objectQuery) {
                return objectQuery;
            }

            @Override
            public List<Query> getQueries(Query countQuery, Query idQuery, Query objectQuery) {
                return Collections.singletonList(objectQuery);
            }
        };

        public abstract ParameterLocation andId();

        public abstract ParameterLocation andObject();

        public abstract Query getQuery(Query countQuery, Query idQuery, Query objectQuery);

        public abstract List<Query> getQueries(Query countQuery, Query idQuery, Query objectQuery);
    }
}
