/*
 * Copyright 2014 - 2017 Blazebit.
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
import com.blazebit.persistence.PagedList;
import com.blazebit.persistence.impl.builder.object.KeysetExtractionObjectBuilder;
import com.blazebit.persistence.impl.keyset.KeysetMode;
import com.blazebit.persistence.impl.keyset.KeysetPageImpl;
import com.blazebit.persistence.impl.keyset.KeysetPaginationHelper;

import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Parameter;
import javax.persistence.PersistenceException;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import java.io.Serializable;
import java.util.*;

public class PaginatedTypedQuery<X> implements TypedQuery<X> {

    private final TypedQuery<?> countQuery;
    private final TypedQuery<?> idQuery;
    private final TypedQuery<X> objectQuery;
    private final KeysetExtractionObjectBuilder<X> objectBuilder;
    private final Object entityId;
    private int firstResult;
    private int pageSize;

    private final boolean needsNewIdList;
    private final boolean keysetExtraction;
    private final KeysetMode keysetMode;
    private final KeysetPage keysetPage;

    public PaginatedTypedQuery(TypedQuery<?> countQuery, TypedQuery<?> idQuery, TypedQuery<X> objectQuery, KeysetExtractionObjectBuilder<X> objectBuilder, Object entityId, int firstResult, int pageSize, boolean needsNewIdList, boolean keysetExtraction, KeysetMode keysetMode, KeysetPage keysetPage) {
        this.countQuery = countQuery;
        this.idQuery = idQuery;
        this.objectQuery = objectQuery;
        this.objectBuilder = objectBuilder;
        this.entityId = entityId;
        this.firstResult = firstResult;
        this.pageSize = pageSize;
        this.needsNewIdList = needsNewIdList;
        this.keysetExtraction = keysetExtraction;
        this.keysetMode = keysetMode;
        this.keysetPage = keysetPage;
    }

    @Override
    @SuppressWarnings("unchecked")
    public PagedList<X> getResultList() {
        int queryFirstResult = firstResult;
        int firstRow = firstResult;
        long totalSize;
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

        if (totalSize == 0L) {
            return new PagedListImpl<X>(null, totalSize, queryFirstResult, pageSize);
        }

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

                return new PagedListImpl<X>(newKeysetPage, totalSize, queryFirstResult, pageSize);
            }

            Serializable[] lowest = null;
            Serializable[] highest = null;

            if (needsNewIdList) {
                if (keysetExtraction) {
                    lowest = KeysetPaginationHelper.extractKey((Object[]) ids.get(0), 1);
                    highest = KeysetPaginationHelper.extractKey((Object[]) ids.get(ids.size() - 1), 1);
                }

                List<Object> newIds = new ArrayList<Object>(ids.size());

                for (int i = 0; i < ids.size(); i++) {
                    newIds.add(((Object[]) ids.get(i))[0]);
                }

                ids = newIds;
            }

            objectQuery.setParameter(AbstractCommonQueryBuilder.ID_PARAM_NAME, ids);

            KeysetPage newKeyset = null;

            if (keysetExtraction) {
                newKeyset = new KeysetPageImpl(firstRow, pageSize, lowest, highest);
            }

            List<X> queryResultList = objectQuery.getResultList();

            PagedList<X> pagedResultList = new PagedListImpl<X>(queryResultList, newKeyset, totalSize, queryFirstResult, pageSize);
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

                return new PagedListImpl<X>(newKeysetPage, totalSize, queryFirstResult, pageSize);
            }

            if (keysetMode == KeysetMode.PREVIOUS) {
                Collections.reverse(result);
            }

            KeysetPage newKeyset = null;

            if (keysetExtraction) {
                Serializable[] lowest = objectBuilder.getLowest();
                Serializable[] highest = objectBuilder.getHighest();
                newKeyset = new KeysetPageImpl(firstRow, pageSize, lowest, highest);
            }

            PagedList<X> pagedResultList = new PagedListImpl<X>(result, newKeyset, totalSize, queryFirstResult, pageSize);
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
        if (objectQuery.getParameter(param.getName()) != null) {
            objectQuery.setParameter(param, value);
        }
        if (idQuery != null && idQuery.getParameter(param.getName()) != null) {
            idQuery.setParameter(param, value);
        }
        if (countQuery.getParameter(param.getName()) != null) {
            countQuery.setParameter(param, value);
        }
        return this;
    }

    @Override
    public TypedQuery<X> setParameter(Parameter<Calendar> param, Calendar value, TemporalType temporalType) {
        if (objectQuery.getParameter(param.getName()) != null) {
            objectQuery.setParameter(param, value, temporalType);
        }
        if (idQuery != null && idQuery.getParameter(param.getName()) != null) {
            idQuery.setParameter(param, value, temporalType);
        }
        if (countQuery.getParameter(param.getName()) != null) {
            countQuery.setParameter(param, value, temporalType);
        }
        return this;
    }

    @Override
    public TypedQuery<X> setParameter(Parameter<Date> param, Date value, TemporalType temporalType) {
        if (objectQuery.getParameter(param.getName()) != null) {
            objectQuery.setParameter(param, value, temporalType);
        }
        if (idQuery != null && idQuery.getParameter(param.getName()) != null) {
            idQuery.setParameter(param, value, temporalType);
        }
        if (countQuery.getParameter(param.getName()) != null) {
            countQuery.setParameter(param, value, temporalType);
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
        throw new IllegalArgumentException("Positional parameters unsupported!");
    }

    @Override
    public TypedQuery<X> setParameter(int position, Calendar value, TemporalType temporalType) {
        throw new IllegalArgumentException("Positional parameters unsupported!");
    }

    @Override
    public TypedQuery<X> setParameter(int position, Date value, TemporalType temporalType) {
        throw new IllegalArgumentException("Positional parameters unsupported!");
    }

    @Override
    public Set<Parameter<?>> getParameters() {
        Set<Parameter<?>> parameters = new HashSet<Parameter<?>>();
        parameters.addAll(objectQuery.getParameters());

        if (idQuery != null) {
            parameters.addAll(idQuery.getParameters());
        }

        parameters.addAll(countQuery.getParameters());
        return parameters;
    }

    @Override
    public Parameter<?> getParameter(String name) {
        Parameter<?> parameter = objectQuery.getParameter(name);
        if (parameter != null) {
            return parameter;
        }
        if (idQuery != null) {
            parameter = idQuery.getParameter(name);
            if (parameter != null) {
                return parameter;
            }
        }
        return countQuery.getParameter(name);
    }

    @Override
    public <T> Parameter<T> getParameter(String name, Class<T> type) {
        Parameter<T> parameter = objectQuery.getParameter(name, type);
        if (parameter != null) {
            return parameter;
        }
        if (idQuery != null) {
            parameter = idQuery.getParameter(name, type);
            if (parameter != null) {
                return parameter;
            }
        }
        return countQuery.getParameter(name, type);
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
        T value = objectQuery.getParameterValue(param);
        if (value != null) {
            return value;
        }
        if (idQuery != null) {
            value = idQuery.getParameterValue(param);
            if (value != null) {
                return value;
            }
        }
        return countQuery.getParameterValue(param);
    }

    @Override
    public Object getParameterValue(String name) {
        Object value = objectQuery.getParameterValue(name);
        if (value != null) {
            return value;
        }
        if (idQuery != null) {
            value = idQuery.getParameterValue(name);
            if (value != null) {
                return value;
            }
        }
        return countQuery.getParameterValue(name);
    }

    @Override
    public Object getParameterValue(int position) {
        throw new IllegalArgumentException("Positional parameters unsupported!");
    }

    @Override
    public Parameter<?> getParameter(int position) {
        throw new IllegalArgumentException("Positional parameters unsupported!");
    }

    @Override
    public <T> Parameter<T> getParameter(int position, Class<T> type) {
        throw new IllegalArgumentException("Positional parameters unsupported!");
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
}
