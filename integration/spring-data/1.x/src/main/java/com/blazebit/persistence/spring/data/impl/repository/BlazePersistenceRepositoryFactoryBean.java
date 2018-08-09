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

package com.blazebit.persistence.spring.data.impl.repository;

import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.spring.data.base.SharedEntityManagerCreator;
import com.blazebit.persistence.view.EntityViewManager;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.data.jpa.util.BeanDefinitionUtils;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.core.support.TransactionalRepositoryFactoryBeanSupport;
import org.springframework.util.Assert;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import java.io.Serializable;
import java.util.Iterator;

import static org.springframework.data.jpa.util.BeanDefinitionUtils.getEntityManagerFactoryBeanDefinitions;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
public class BlazePersistenceRepositoryFactoryBean<T extends Repository<S, ID>, S, ID extends Serializable> extends
        TransactionalRepositoryFactoryBeanSupport<T, S, ID> {

    private EntityManager entityManager;

    @Autowired
    private CriteriaBuilderFactory cbf;

    @Autowired
    private EntityViewManager evm;

    /**
     * Creates a new {@link BlazePersistenceRepositoryFactoryBean}.
     */
    protected BlazePersistenceRepositoryFactoryBean() {
        super(null);
    }

    /**
     * Creates a new {@link BlazePersistenceRepositoryFactoryBean} for the given repository interface.
     *
     * @param repositoryInterface must not be {@literal null}.
     */
    protected BlazePersistenceRepositoryFactoryBean(Class<? extends T> repositoryInterface) {
        super(repositoryInterface);
    }

    public boolean isSingleton() {
        return true;
    }

    /**
     * The {@link EntityManager} to be used.
     *
     * @param entityManager the entityManager to set
     */
    @PersistenceContext
    public void setEntityManager(EntityManager entityManager) {
        if (this.entityManager == null) {
            this.entityManager = entityManager;
        }
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        // Workaround that Spring's version of the SharedEntityManagerCreator requires a transaction for invoking unwrap
        this.entityManager = null;
        ConfigurableListableBeanFactory configurableListableBeanFactory = (ConfigurableListableBeanFactory) beanFactory;
        Iterator<BeanDefinitionUtils.EntityManagerFactoryBeanDefinition> iterator = getEntityManagerFactoryBeanDefinitions(configurableListableBeanFactory).iterator();
        if (iterator.hasNext()) {
            BeanDefinitionUtils.EntityManagerFactoryBeanDefinition definition = iterator.next();
            setEntityManager(SharedEntityManagerCreator.createSharedEntityManager(configurableListableBeanFactory.getBean(definition.getBeanName(), EntityManagerFactory.class)));
        }

        super.setBeanFactory(beanFactory);
    }

    /*
     * (non-Javadoc)
     * @see com.blazebit.persistence.spring.data.impl.repository.BlazeRepositoryFactoryBeanSupport#setMappingContext(org.springframework.data.mapping.context.MappingContext)
     */
    @Override
    public void setMappingContext(MappingContext<?, ?> mappingContext) {
        super.setMappingContext(mappingContext);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.data.repository.support.
     * BlazeTransactionalRepositoryFactoryBeanSupport#doCreateRepositoryFactory()
     */
    @Override
    protected BlazePersistenceRepositoryFactory doCreateRepositoryFactory() {
        return createRepositoryFactory(entityManager);
    }

    /**
     * Returns a {@link RepositoryFactorySupport}.
     *
     * @param entityManager
     * @return
     */
    protected BlazePersistenceRepositoryFactory createRepositoryFactory(EntityManager entityManager) {
        return new BlazePersistenceRepositoryFactory(entityManager, cbf, evm);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() {
        Assert.notNull(entityManager, "EntityManager must not be null!");
        super.afterPropertiesSet();
    }

}
