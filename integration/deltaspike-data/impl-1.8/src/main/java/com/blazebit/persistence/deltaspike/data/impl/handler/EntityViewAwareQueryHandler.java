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

package com.blazebit.persistence.deltaspike.data.impl.handler;

import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.deltaspike.data.base.handler.EntityViewManagerRef;
import com.blazebit.persistence.deltaspike.data.impl.builder.EntityViewAwareQueryBuilderFactory;
import com.blazebit.persistence.deltaspike.data.impl.builder.EntityViewQueryBuilder;
import com.blazebit.persistence.deltaspike.data.impl.meta.EntityViewAwareRepositoryMetadata;
import com.blazebit.persistence.deltaspike.data.impl.meta.EntityViewAwareRepositoryMethodMetadata;
import org.apache.deltaspike.data.api.QueryInvocationException;
import org.apache.deltaspike.data.api.Repository;
import org.apache.deltaspike.data.impl.builder.QueryBuilder;
import org.apache.deltaspike.data.impl.builder.QueryBuilderFactory;
import org.apache.deltaspike.data.impl.handler.CdiQueryContextHolder;
import org.apache.deltaspike.data.impl.handler.CdiQueryInvocationContext;
import org.apache.deltaspike.data.impl.handler.QueryHandler;
import org.apache.deltaspike.data.impl.handler.QueryRunner;
import org.apache.deltaspike.data.impl.meta.RepositoryMetadata;
import org.apache.deltaspike.data.impl.meta.RepositoryMethodMetadata;
import org.apache.deltaspike.jpa.impl.entitymanager.EntityManagerRef;
import org.apache.deltaspike.jpa.impl.entitymanager.EntityManagerRefLookup;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Specializes;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation is similar to {@link QueryHandler} but was modified to
 * work with entity views.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@Repository
@Specializes
@ApplicationScoped
public class EntityViewAwareQueryHandler extends QueryHandler {
    private static final Logger LOG = Logger.getLogger(QueryHandler.class.getName());

    @Inject
    private QueryBuilderFactory queryBuilderFactory;
    @Inject
    private EntityViewAwareQueryBuilderFactory entityViewAwareQueryBuilderFactory;
    @Inject
    private EntityManagerRefLookup entityManagerRefLookup;
    @Inject
    private EntityViewQueryRunner entityViewQueryRunner;
    @Inject
    private QueryRunner runner;
    @Inject
    private EntityViewCdiQueryInvocationContextHolder entityViewCdiQueryInvocationContextHolder;
    @Inject
    private CdiQueryContextHolder context;
    @Inject
    private CriteriaBuilderFactory criteriaBuilderFactory;
    @Inject
    private EntityViewManagerRefLookup entityViewManagerRefLookup;

    @Override
    protected Object process(Object proxy, Method method, Object[] args, RepositoryMetadata repositoryMetadata, RepositoryMethodMetadata repositoryMethodMetadata) throws Throwable {
        CdiQueryInvocationContext queryContext = null;
        EntityManagerRef entityManagerRef = null;
        EntityViewManagerRef entityViewManagerRef = null;
        try {
            entityManagerRef = entityManagerRefLookup.lookupReference(repositoryMetadata);
            EntityManager entityManager = entityManagerRef.getEntityManager();
            if (entityManager == null) {
                throw new IllegalStateException("Unable to look up EntityManager");
            }
            if (repositoryMethodMetadata instanceof EntityViewAwareRepositoryMethodMetadata) {
                EntityViewAwareRepositoryMetadata entityViewAwareRepositoryMetadata = (EntityViewAwareRepositoryMetadata) repositoryMetadata;
                EntityViewAwareRepositoryMethodMetadata entityViewAwareRepositoryMethodMetadata = (EntityViewAwareRepositoryMethodMetadata) repositoryMethodMetadata;
                entityViewManagerRef = entityViewManagerRefLookup.lookupReference(entityViewAwareRepositoryMetadata);
                queryContext = new EntityViewCdiQueryInvocationContext(proxy, method, args,
                        entityViewAwareRepositoryMetadata, entityViewAwareRepositoryMethodMetadata, entityManager,
                        entityViewManagerRef.getEntityViewManager(), criteriaBuilderFactory);

                entityViewCdiQueryInvocationContextHolder.set((EntityViewCdiQueryInvocationContext) queryContext);
                queryContext.init();
                EntityViewQueryBuilder builder = entityViewAwareQueryBuilderFactory.build(entityViewAwareRepositoryMethodMetadata, (EntityViewCdiQueryInvocationContext) queryContext);
                return entityViewQueryRunner.executeQuery(builder, (EntityViewCdiQueryInvocationContext) queryContext);
            } else {
                queryContext = new CdiQueryInvocationContext(proxy, method, args,
                        repositoryMetadata, repositoryMethodMetadata, entityManager);
                context.set(queryContext);
                queryContext.init();

                QueryBuilder builder = queryBuilderFactory.build(repositoryMethodMetadata, queryContext);
                return runner.executeQuery(builder, queryContext);
            }
        } catch (PersistenceException e) {
            throw e;
        } catch (Exception e) {
            LOG.log(Level.FINEST, "Query execution error", e);
            if (queryContext != null) {
                throw new QueryInvocationException(e, queryContext);
            }
            throw new QueryInvocationException(e, proxy.getClass(), method);
        } finally {
            if (entityManagerRef != null) {
                entityManagerRef.release();
            }
            if (entityViewManagerRef != null) {
                entityViewManagerRef.release();
            }
            entityViewCdiQueryInvocationContextHolder.dispose();
            context.dispose();
        }
    }
}