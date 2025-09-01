/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spring.data.impl.repository;

import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.view.EntityViewManager;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.data.jpa.repository.query.EscapeCharacter;
import org.springframework.data.jpa.repository.query.JpaQueryMethodFactory;
import org.springframework.data.jpa.repository.query.QueryEnhancerSelector;
import org.springframework.data.jpa.repository.support.JpaRepositoryFragmentsContributor;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.querydsl.EntityPathResolver;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.core.support.TransactionalRepositoryFactoryBeanSupport;
import org.springframework.util.Assert;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.io.Serializable;
import java.util.function.Function;

/**
 * @author Moritz Becker
 * @author Eugen Mayer
 * @since 1.6.9
 */
public class BlazePersistenceRepositoryFactoryBean<T extends Repository<S, ID>, S, ID extends Serializable> extends
        TransactionalRepositoryFactoryBeanSupport<T, S, ID> {

    private EntityManager entityManager;
    private BeanFactory beanFactory;
    private EntityPathResolver entityPathResolver = SimpleEntityPathResolver.INSTANCE;
    private JpaRepositoryFragmentsContributor repositoryFragmentsContributor = JpaRepositoryFragmentsContributor.DEFAULT;
    private EscapeCharacter escapeCharacter = EscapeCharacter.DEFAULT;
    private JpaQueryMethodFactory queryMethodFactory;
    private Function<BeanFactory, QueryEnhancerSelector> queryEnhancerSelectorSource;

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

    /**
     * The {@link EntityManager} to be used.
     *
     * @param entityManager the entityManager to set
     */
    @PersistenceContext
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public void setMappingContext(MappingContext<?, ?> mappingContext) {
        super.setMappingContext(mappingContext);
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
        super.setBeanFactory(beanFactory);
    }

    /**
     * Configures the {@link EntityPathResolver} to be used. Will expect a canonical bean to be present but fallback to
     * {@link SimpleEntityPathResolver#INSTANCE} in case none is available.
     *
     * @param resolver must not be {@literal null}.
     */
    @Autowired
    public void setEntityPathResolver(ObjectProvider<EntityPathResolver> resolver) {
        this.entityPathResolver = resolver.getIfAvailable(() -> SimpleEntityPathResolver.INSTANCE);
    }

    @Override
    public JpaRepositoryFragmentsContributor getRepositoryFragmentsContributor() {
        return repositoryFragmentsContributor;
    }

    /**
     * Configures the {@link JpaRepositoryFragmentsContributor} to contribute built-in fragment functionality to the
     * repository.
     *
     * @param repositoryFragmentsContributor must not be {@literal null}.
     * @since 4.0
     */
    public void setRepositoryFragmentsContributor(JpaRepositoryFragmentsContributor repositoryFragmentsContributor) {
        this.repositoryFragmentsContributor = repositoryFragmentsContributor;
    }

    public void setEscapeCharacter(char escapeCharacter) {
        this.escapeCharacter = EscapeCharacter.of(escapeCharacter);
    }

    /**
     * Configures the {@link QueryEnhancerSelector} to be used. Defaults to
     * {@link QueryEnhancerSelector#DEFAULT_SELECTOR}.
     *
     * @param queryEnhancerSelectorSource must not be {@literal null}.
     */
    public void setQueryEnhancerSelectorSource(QueryEnhancerSelector queryEnhancerSelectorSource) {
        this.queryEnhancerSelectorSource = bf -> queryEnhancerSelectorSource;
    }

    /**
     * Configures the {@link QueryEnhancerSelector} to be used.
     *
     * @param queryEnhancerSelectorType must not be {@literal null}.
     */
    public void setQueryEnhancerSelector(Class<? extends QueryEnhancerSelector> queryEnhancerSelectorType) {

        this.queryEnhancerSelectorSource = bf -> {

            if (bf != null) {

                ObjectProvider<? extends QueryEnhancerSelector> beanProvider = bf.getBeanProvider(queryEnhancerSelectorType);
                QueryEnhancerSelector selector = beanProvider.getIfAvailable();

                if (selector != null) {
                    return selector;
                }

                if (bf instanceof AutowireCapableBeanFactory acbf) {
                    return acbf.createBean(queryEnhancerSelectorType);
                }
            }

            return BeanUtils.instantiateClass( queryEnhancerSelectorType);
        };
    }

    /**
     * Configures the {@link JpaQueryMethodFactory} to be used. Will expect a canonical bean to be present but will
     * fallback to {@link org.springframework.data.jpa.repository.query.DefaultJpaQueryMethodFactory} in case none is
     * available.
     *
     * @param resolver may be {@literal null}.
     */
    @Autowired
    public void setQueryMethodFactory(ObjectProvider<JpaQueryMethodFactory> resolver) { // TODO: nullable insteand of
                                                                                                                                                                            // ObjectProvider

        JpaQueryMethodFactory factory = resolver.getIfAvailable();
        if (factory != null) {
            this.queryMethodFactory = factory;
        }
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
        BlazePersistenceRepositoryFactory factory = new BlazePersistenceRepositoryFactory(entityManager, cbf, evm);
        factory.setEntityPathResolver(entityPathResolver);
        factory.setEscapeCharacter(escapeCharacter);
        factory.setFragmentsContributor(getRepositoryFragmentsContributor());

        if (queryMethodFactory != null) {
            factory.setQueryMethodFactory(queryMethodFactory);
        }

        if (queryEnhancerSelectorSource != null) {
            factory.setQueryEnhancerSelector(queryEnhancerSelectorSource.apply(beanFactory));
        }
        return factory;
    }

    @Override
    public void afterPropertiesSet() {
        Assert.notNull(entityManager, "EntityManager must not be null!");
        super.afterPropertiesSet();
    }

}
