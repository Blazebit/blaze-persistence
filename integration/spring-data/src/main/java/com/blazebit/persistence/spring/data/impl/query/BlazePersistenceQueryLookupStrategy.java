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

package com.blazebit.persistence.spring.data.impl.query;

import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.view.EntityViewManager;
import org.springframework.data.jpa.provider.PersistenceProvider;
import org.springframework.data.jpa.provider.QueryExtractor;
import org.springframework.data.jpa.repository.query.JpaQueryMethod;
import org.springframework.data.jpa.repository.query.PartTreeEntityViewQuery;
import org.springframework.data.jpa.repository.query.PartTreeJpaQuery;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.EvaluationContextProvider;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.util.Assert;

import javax.persistence.EntityManager;
import java.lang.reflect.Method;

/**
 * @author Moritz Becker (moritz.becker@gmx.at)
 * @since 1.2.0
 */
public final class BlazePersistenceQueryLookupStrategy {

    /**
     * Private constructor to prevent instantiation.
     */
    private BlazePersistenceQueryLookupStrategy() { }

    /**
     * Base class for {@link QueryLookupStrategy} implementations that need access to an {@link EntityManager}.
     *
     * @author Oliver Gierke
     * @author Thomas Darimont
     */
    private abstract static class AbstractQueryLookupStrategy implements QueryLookupStrategy {

        private final EntityManager em;
        private final QueryExtractor provider;

        /**
         * Creates a new {@link JpaQueryLookupStrategy.AbstractQueryLookupStrategy}.
         *
         * @param em the entity manager
         * @param extractor the query extractor
         */
        public AbstractQueryLookupStrategy(EntityManager em, QueryExtractor extractor) {

            this.em = em;
            this.provider = extractor;
        }

        /*
         * (non-Javadoc)
         * @see org.springframework.data.repository.query.QueryLookupStrategy#resolveQuery(java.lang.reflect.Method, org.springframework.data.repository.core.RepositoryMetadata, org.springframework.data.projection.ProjectionFactory, org.springframework.data.repository.core.NamedQueries)
         */
        @Override
        public final RepositoryQuery resolveQuery(Method method, RepositoryMetadata metadata, ProjectionFactory factory,
                                                  NamedQueries namedQueries) {
            return resolveQuery(new EntityViewAwareJpaQueryMethod(method, metadata, factory, provider), em, namedQueries);
        }

        protected abstract RepositoryQuery resolveQuery(JpaQueryMethod method, EntityManager em, NamedQueries namedQueries);
    }

    /**
     * {@link QueryLookupStrategy} to create a query from the method name.
     *
     * @author Oliver Gierke
     * @author Thomas Darimont
     */
    private static class CreateQueryLookupStrategy extends BlazePersistenceQueryLookupStrategy.AbstractQueryLookupStrategy {

        private final PersistenceProvider persistenceProvider;
        private final CriteriaBuilderFactory cbf;
        private final EntityViewManager evm;

        public CreateQueryLookupStrategy(EntityManager em, QueryExtractor extractor, CriteriaBuilderFactory cbf, EntityViewManager evm) {
            super(em, extractor);
            this.persistenceProvider = PersistenceProvider.fromEntityManager(em);
            this.cbf = cbf;
            this.evm = evm;
        }

        @Override
        protected RepositoryQuery resolveQuery(JpaQueryMethod method, EntityManager em, NamedQueries namedQueries) {

            try {
                if (((EntityViewAwareJpaQueryMethod) method).isEntityViewQuery()) {
                    return new PartTreeEntityViewQuery(method, em, persistenceProvider, cbf, evm);
                } else {
                    return new PartTreeJpaQuery(method, em, persistenceProvider);
                }
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                        String.format("Could not create query metamodel for method %s!", method.toString()), e);
            }
        }
    }

    /**
     * Creates a {@link QueryLookupStrategy} for the given {@link EntityManager} and {@link QueryLookupStrategy.Key}.
     *
     * @param em must not be {@literal null}.
     * @param key may be {@literal null}.
     * @param extractor must not be {@literal null}.
     * @param evaluationContextProvider must not be {@literal null}.
     * @return the {@link QueryLookupStrategy}
     */
    public static QueryLookupStrategy create(EntityManager em, QueryLookupStrategy.Key key, QueryExtractor extractor,
                                             EvaluationContextProvider evaluationContextProvider,
                                             CriteriaBuilderFactory cbf, EntityViewManager evm) {

        Assert.notNull(em, "EntityManager must not be null!");
        Assert.notNull(extractor, "QueryExtractor must not be null!");
        Assert.notNull(evaluationContextProvider, "EvaluationContextProvider must not be null!");

        switch (key != null ? key : QueryLookupStrategy.Key.CREATE_IF_NOT_FOUND) {
            case CREATE:
            case CREATE_IF_NOT_FOUND:
                return new BlazePersistenceQueryLookupStrategy.CreateQueryLookupStrategy(em, extractor, cbf, evm);
            default:
                throw new IllegalArgumentException(String.format("Unsupported query lookup strategy %s!", key));
        }
    }
}
