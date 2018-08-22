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

package com.blazebit.persistence.impl;

import com.blazebit.persistence.KeysetPage;
import com.blazebit.persistence.PagedArrayList;
import com.blazebit.persistence.PagedList;
import com.blazebit.persistence.PaginatedTypedQuery;
import com.blazebit.persistence.impl.builder.object.KeysetExtractionObjectBuilder;
import com.blazebit.persistence.impl.keyset.KeysetMode;
import com.blazebit.persistence.impl.keyset.KeysetPageImpl;
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
import java.io.Serializable;
import java.util.*;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class PaginatedTypedQueryImpl<X> implements PaginatedTypedQuery<X> {

    private final boolean withCount;
    private final int highestOffset;
    private final TypedQuery<?> countQuery;
    private final TypedQuery<?> idQuery;
    private final TypedQuery<X> objectQuery;
    private final KeysetExtractionObjectBuilder<X> objectBuilder;
    private final Map<String, Parameter<?>> parameters;
    private final Map<String, ParameterLocation> parameterToQuery;
    private final Object entityId;
    private int firstResult;
    private int pageSize;

    private final int identifierCount;
    private final boolean needsNewIdList;
    private final int[] keysetToSelectIndexMapping;
    private final int keysetSuffix;
    private final KeysetMode keysetMode;
    private final KeysetPage keysetPage;

    public PaginatedTypedQueryImpl(boolean withCount, int highestOffset, TypedQuery<?> countQuery, TypedQuery<?> idQuery, TypedQuery<X> objectQuery, KeysetExtractionObjectBuilder<X> objectBuilder, Set<Parameter<?>> parameters,
                                   Object entityId, int firstResult, int pageSize, int identifierCount, boolean needsNewIdList, int[] keysetToSelectIndexMapping, KeysetMode keysetMode, KeysetPage keysetPage) {
        this.withCount = withCount;
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

        Map<String, Parameter<?>> params = new HashMap<>(parameters.size());
        for (Parameter<?> parameter : parameters) {
            params.put(getParameterName(parameter), parameter);
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
        String name = parameter.getName();
        if (name == null) {
            return parameter.getPosition().toString();
        }
        return name;
    }

    @Override
    public long getTotalCount() {
        return (Long) countQuery.getSingleResult();
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
        if (withCount) {
            if (entityId == null) {
                totalSize = (Long) countQuery.getSingleResult();
            } else {
                Object[] result = (Object[]) countQuery.getSingleResult();
                totalSize = (Long) result[0];

                if (result[1] == null) {
                    // If the reference entity id is not contained (i.e. has no position), we return this special value
                    queryFirstResult = -1;
                    firstRow = 0;
                } else {
                    // The page position is numbered from 1 so we need to correct this here
                    int position = ((Long) result[1]).intValue() - 1;
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

            if (keysetMode == KeysetMode.NONE) {
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

                return new PagedArrayList<X>(newKeysetPage, totalSize, queryFirstResult, pageSize);
            }

            Serializable[] lowest = null;
            Serializable[] highest = null;

            if (needsNewIdList) {
                if (keysetToSelectIndexMapping != null) {
                    int keysetPageSize = pageSize - highestOffset;
                    if (ids.get(0) instanceof Object[]) {
                        lowest = KeysetPaginationHelper.extractKey((Object[]) ids.get(0), keysetToSelectIndexMapping, keysetSuffix);
                        highest = KeysetPaginationHelper.extractKey((Object[]) (ids.size() >= keysetPageSize ? ids.get(keysetPageSize - 1) : ids.get(ids.size() - 1)), keysetToSelectIndexMapping, keysetSuffix);
                    } else {
                        lowest = new Serializable[]{ (Serializable) ids.get(0) };
                        highest = new Serializable[]{ (Serializable) (ids.size() >= keysetPageSize ? ids.get(keysetPageSize - 1) : ids.get(ids.size() - 1)) };
                    }

                    // Swap keysets as we have inverse ordering when going to the previous page
                    if (keysetMode == KeysetMode.PREVIOUS) {
                        Serializable[] tmp = lowest;
                        lowest = highest;
                        highest = tmp;
                    }
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
            }

            if (identifierCount > 1) {
                StringBuilder parameterNameBuilder = new StringBuilder(AbstractCommonQueryBuilder.ID_PARAM_NAME.length() + 10);
                parameterNameBuilder.append(AbstractCommonQueryBuilder.ID_PARAM_NAME).append('_');
                int start = parameterNameBuilder.length();
                for (int i = 0; i < ids.size(); i++) {
                    Object[] tuple = (Object[]) ids.get(i);
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
                newKeyset = new KeysetPageImpl(firstRow, pageSize, lowest, highest);
            }

            List<X> queryResultList = objectQuery.getResultList();

            PagedList<X> pagedResultList = new PagedArrayList<X>(queryResultList, newKeyset, totalSize, queryFirstResult, pageSize);
            return pagedResultList;
        } else {
            objectQuery.setMaxResults(pageSize);

            if (keysetMode == KeysetMode.NONE) {
                objectQuery.setFirstResult(firstRow);
            } else {
                objectQuery.setFirstResult(0);
            }

            List<X> result = objectQuery.getResultList();

            if (result.isEmpty()) {
                KeysetPage newKeysetPage = null;
                if (keysetMode == KeysetMode.NEXT) {
                    // When we scroll over the last page to a non existing one, we reuse the current keyset
                    newKeysetPage = keysetPage;
                }

                return new PagedArrayList<X>(newKeysetPage, totalSize, queryFirstResult, pageSize);
            }

            if (keysetMode == KeysetMode.PREVIOUS) {
                Collections.reverse(result);
            }

            KeysetPage newKeyset = null;

            if (keysetToSelectIndexMapping != null) {
                Serializable[] lowest = objectBuilder.getLowest();
                Serializable[] highest = objectBuilder.getHighest();
                newKeyset = new KeysetPageImpl(firstRow, pageSize, lowest, highest);
            }

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
        if (param.getName() == null) {
            List<Query> queries = parameterToQuery.get(Integer.toString(param.getPosition())).getQueries(countQuery, idQuery, objectQuery);
            for (Query query : queries) {
                query.setParameter(param.getPosition(), value);
            }
        } else if (Character.isDigit(param.getName().charAt(0))) {
            List<Query> queries = parameterToQuery.get(param.getName()).getQueries(countQuery, idQuery, objectQuery);
            for (Query query : queries) {
                query.setParameter(Integer.parseInt(param.getName()), value);
            }
        } else {
            List<Query> queries = parameterToQuery.get(param.getName()).getQueries(countQuery, idQuery, objectQuery);
            for (Query query : queries) {
                query.setParameter(param.getName(), value);
            }
        }
        return this;
    }

    @Override
    public TypedQuery<X> setParameter(Parameter<Calendar> param, Calendar value, TemporalType temporalType) {
        if (param.getName() == null) {
            List<Query> queries = parameterToQuery.get(Integer.toString(param.getPosition())).getQueries(countQuery, idQuery, objectQuery);
            for (Query query : queries) {
                query.setParameter(param.getPosition(), value, temporalType);
            }
        } else if (Character.isDigit(param.getName().charAt(0))) {
            List<Query> queries = parameterToQuery.get(param.getName()).getQueries(countQuery, idQuery, objectQuery);
            for (Query query : queries) {
                query.setParameter(Integer.parseInt(param.getName()), value, temporalType);
            }
        } else {
            List<Query> queries = parameterToQuery.get(param.getName()).getQueries(countQuery, idQuery, objectQuery);
            for (Query query : queries) {
                query.setParameter(param.getName(), value, temporalType);
            }
        }
        return this;
    }

    @Override
    public TypedQuery<X> setParameter(Parameter<Date> param, Date value, TemporalType temporalType) {
        if (param.getName() == null) {
            List<Query> queries = parameterToQuery.get(Integer.toString(param.getPosition())).getQueries(countQuery, idQuery, objectQuery);
            for (Query query : queries) {
                query.setParameter(param.getPosition(), value, temporalType);
            }
        } else if (Character.isDigit(param.getName().charAt(0))) {
            List<Query> queries = parameterToQuery.get(param.getName()).getQueries(countQuery, idQuery, objectQuery);
            for (Query query : queries) {
                query.setParameter(Integer.parseInt(param.getName()), value, temporalType);
            }
        } else {
            List<Query> queries = parameterToQuery.get(param.getName()).getQueries(countQuery, idQuery, objectQuery);
            for (Query query : queries) {
                query.setParameter(param.getName(), value, temporalType);
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
        Parameter<?> param = parameters.get(Integer.toString(position));
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
