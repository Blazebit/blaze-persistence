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
import com.blazebit.persistence.deltaspike.data.impl.meta.EntityViewRepositoryComponent;
import com.blazebit.persistence.deltaspike.data.impl.meta.EntityViewRepositoryComponents;
import com.blazebit.persistence.deltaspike.data.impl.meta.EntityViewRepositoryMethod;
import org.apache.deltaspike.core.api.lifecycle.Initialized;
import org.apache.deltaspike.core.util.ProxyUtils;
import org.apache.deltaspike.data.api.QueryInvocationException;
import org.apache.deltaspike.data.api.Repository;
import org.apache.deltaspike.data.impl.builder.QueryBuilder;
import org.apache.deltaspike.data.impl.builder.QueryBuilderFactory;
import org.apache.deltaspike.data.impl.handler.CdiQueryContextHolder;
import org.apache.deltaspike.data.impl.handler.CdiQueryInvocationContext;
import org.apache.deltaspike.data.impl.handler.EntityManagerRef;
import org.apache.deltaspike.data.impl.handler.QueryHandler;
import org.apache.deltaspike.data.impl.handler.QueryRunner;
import org.apache.deltaspike.data.impl.meta.RepositoryMethod;
import org.apache.deltaspike.data.spi.QueryInvocationContext;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Specializes;
import javax.inject.Inject;
import javax.persistence.PersistenceException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
@Repository
@Specializes
@ApplicationScoped
public class EntityViewAwareQueryHandler extends QueryHandler {
    private static final Logger LOG = Logger.getLogger(QueryHandler.class.getName());

    @Inject
    @Initialized
    private EntityViewRepositoryComponents components;

    @Inject
    private EntityManagerRefLookup entityManagerRefLookup;

    @Inject
    private EntityViewAwareQueryBuilderFactory entityViewAwareQueryBuilderFactory;
    @Inject
    private QueryBuilderFactory queryBuilderFactory;

    @Inject
    private EntityViewQueryRunner entityViewQueryRunner;
    @Inject
    private QueryRunner runner;

    @Inject
    private EntityViewCdiQueryInvocationContextHolder entityViewCdiQueryInvocationContextHolder;
    @Inject
    private CdiQueryContextHolder cdiQueryContextHolder;

    @Inject
    private CriteriaBuilderFactory criteriaBuilderFactory;

    @Inject
    private EntityViewManagerRefLookup entityViewManagerRefLookup;

    @Override
    public Object process(Object proxy, Method method, Object[] args) throws Throwable {
        QueryInvocationContext queryContext = null;
        EntityManagerRef entityManagerRef = null;
        EntityViewManagerRef entityViewManagerRef = null;
        try {
            List<Class<?>> candidates = ProxyUtils.getProxyAndBaseTypes(proxy.getClass());
            EntityViewRepositoryComponent repo;
            try {
                repo = components.lookupComponent(candidates);
            } catch (RuntimeException e) {
                return super.process(proxy, method, args);
            }
            RepositoryMethod repoMethod = components.lookupMethod(repo, method);

            entityManagerRef = entityManagerRefLookup.lookupReference(repo);
            entityViewManagerRef = entityViewManagerRefLookup.lookupReference(repo);

            if (repoMethod instanceof EntityViewRepositoryMethod) {
                queryContext = new EntityViewCdiQueryInvocationContext(proxy, method, args, (EntityViewRepositoryMethod) repoMethod,
                        entityManagerRef.getEntityManager(), entityViewManagerRef.getEntityViewManager(), criteriaBuilderFactory);
                entityViewCdiQueryInvocationContextHolder.set((EntityViewCdiQueryInvocationContext) queryContext);
                ((EntityViewCdiQueryInvocationContext) queryContext).initMapper();
                EntityViewQueryBuilder builder = entityViewAwareQueryBuilderFactory.build((EntityViewRepositoryMethod) repoMethod, (EntityViewCdiQueryInvocationContext) queryContext);
                return entityViewQueryRunner.executeQuery(builder, (EntityViewCdiQueryInvocationContext) queryContext);
            } else {
                queryContext = new CdiQueryInvocationContext(proxy, method, args, repoMethod,
                        entityManagerRef.getEntityManager());
                cdiQueryContextHolder.set((CdiQueryInvocationContext) queryContext);
                ((CdiQueryInvocationContext) queryContext).initMapper();
                QueryBuilder builder = queryBuilderFactory.build(repoMethod, (CdiQueryInvocationContext) queryContext);
                return runner.executeQuery(builder, (CdiQueryInvocationContext) queryContext);
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
            cdiQueryContextHolder.dispose();
        }
    }
}