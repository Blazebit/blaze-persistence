/*
 * Copyright 2014 - 2020 Blazebit.
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

package com.blazebit.persistence.integration.quarkus.deployment;

import com.blazebit.persistence.impl.function.entity.ValuesEntity;
import com.blazebit.persistence.integration.quarkus.runtime.BlazePersistenceConfiguration;
import com.blazebit.persistence.integration.quarkus.runtime.CriteriaBuilderConfigurationHolder;
import com.blazebit.persistence.integration.quarkus.runtime.DefaultCriteriaBuilderFactoryProducer;
import com.blazebit.persistence.integration.quarkus.runtime.DefaultEntityViewManagerProducer;
import com.blazebit.persistence.integration.quarkus.runtime.EntityViewConfigurationHolder;
import com.blazebit.persistence.integration.quarkus.runtime.EntityViewRecorder;
import com.blazebit.persistence.parser.expression.ConcurrentHashMapExpressionCache;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.spi.EntityViewMapping;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.BeanContainerListenerBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.CapabilityBuildItem;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ServiceProviderBuildItem;
import io.quarkus.deployment.recording.RecorderContext;
import io.quarkus.hibernate.orm.deployment.PersistenceUnitDescriptorBuildItem;

import java.util.Arrays;
import java.util.List;

import static io.quarkus.deployment.annotations.ExecutionTime.STATIC_INIT;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
class BlazePersistenceProcessor {

    static final String CAPABILITY = "com.blazebit.persistence.integration.quarkus";
    static final String FEATURE = "blaze-persistence";

    @BuildStep
    CapabilityBuildItem capability() {
        return new CapabilityBuildItem(CAPABILITY);
    }

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    public EntityViewsBuildItem produceEntityViewsBuildItem(CombinedIndexBuildItem index) {
        EntityViewJandexScavenger entityViewJandexScavenger = new EntityViewJandexScavenger(index.getIndex());
        return entityViewJandexScavenger.discoverAndRegisterEntityViews();
    }

    @BuildStep
    public EntityViewListenersBuildItem produceEntityViewListenersBuildItem(CombinedIndexBuildItem index) {
        EntityViewJandexScavenger entityViewJandexScavenger = new EntityViewJandexScavenger(index.getIndex());
        return entityViewJandexScavenger.discoverAndRegisterEntityViewListeners();
    }

    @BuildStep
    public EntityViewConfigurationBuildItem produceEntityViewConfigurationBuildItem(
            EntityViewsBuildItem entityViewsBuildItem) {
        EntityViewConfiguration evc = EntityViews.createDefaultConfiguration();
        for (String entityViewClassName : entityViewsBuildItem.getEntityViewClassNames()) {
            try {
                evc.addEntityView(Thread.currentThread().getContextClassLoader().loadClass(entityViewClassName));
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return new EntityViewConfigurationBuildItem(evc);
    }

    @BuildStep
    @Record(STATIC_INIT)
    void registerBeans(EntityViewsBuildItem entityViewsBuildItem,
                       EntityViewListenersBuildItem entityViewListenersBuildItem,
                       BuildProducer<AdditionalBeanBuildItem> additionalBeans,
                       EntityViewRecorder entityViewRecorder,
                       BuildProducer<BeanContainerListenerBuildItem> containerListenerProducer,
                       RecorderContext recorderContext,
                       List<PersistenceUnitDescriptorBuildItem> descriptors,
                       BlazePersistenceConfiguration blazePersistenceConfig) {
        if (descriptors.size() == 1) {
            // There is only one persistence unit - register default CDI beans for CBF and EVM
            additionalBeans.produce(AdditionalBeanBuildItem.builder().setUnremovable()
                    .addBeanClasses(
                            DefaultCriteriaBuilderFactoryProducer.class, DefaultEntityViewManagerProducer.class,
                            CriteriaBuilderConfigurationHolder.class, EntityViewConfigurationHolder.class
                    )
                    .build());
        }

        for (String entityViewClassName : entityViewsBuildItem.getEntityViewClassNames()) {
            entityViewRecorder.addEntityView(recorderContext.classProxy(entityViewClassName));
        }

        for (String entityViewListenerClassName : entityViewListenersBuildItem.getEntityViewListenerClassNames()) {
            entityViewRecorder.addEntityViewListener(recorderContext.classProxy(entityViewListenerClassName));
        }

        containerListenerProducer.produce(new BeanContainerListenerBuildItem(entityViewRecorder.setEntityViewConfiguration(blazePersistenceConfig)));
        containerListenerProducer.produce(new BeanContainerListenerBuildItem(entityViewRecorder.setCriteriaBuilderConfiguration(blazePersistenceConfig)));
    }

    @BuildStep
    void reflection(EntityViewsBuildItem entityViewsBuildItem,
                    EntityViewConfigurationBuildItem entityViewConfigurationBuildItem,
                    BuildProducer<ReflectiveClassBuildItem> reflectionProducer) {
        reflectionProducer.produce(new ReflectiveClassBuildItem(true, true, ValuesEntity.class));
        reflectionProducer.produce(new ReflectiveClassBuildItem(true, false, false, ConcurrentHashMapExpressionCache.class));
        reflectionProducer.produce(new ReflectiveClassBuildItem(true, false, false, "com.blazebit.persistence.integration.hibernate.CustomOneToManyPersister"));
        reflectionProducer.produce(new ReflectiveClassBuildItem(true, false, false, "com.blazebit.persistence.integration.hibernate.CustomBasicCollectionPersister"));
        reflectionProducer.produce(ReflectiveClassBuildItem.builder("org.hibernate.dialect.function.SQLFunctionRegistry")
                .fields(true)
                .finalFieldsWritable(true)
                .build()
        );
        reflectionProducer.produce(ReflectiveClassBuildItem.builder("org.hibernate.dialect.Dialect")
                .fields(true)
                .finalFieldsWritable(true)
                .build()
        );
        reflectionProducer.produce(ReflectiveClassBuildItem.builder(
                "org.hibernate.hql.internal.ast.exec.MultiTableUpdateExecutor",
                "org.hibernate.hql.internal.ast.exec.BasicExecutor",
                "org.hibernate.hql.internal.ast.exec.DeleteExecutor",
                "org.hibernate.hql.internal.ast.exec.MultiTableDeleteExecutor")
                .fields(true)
                .build()
        );
        reflectionProducer.produce(ReflectiveClassBuildItem.builder(
                "org.hibernate.hql.internal.classic.QueryTranslatorImpl",
                "org.hibernate.hql.internal.ast.QueryTranslatorImpl")
                .fields(true)
                .build()
        );
        // add entity view model classes generated by annotation processor
        for (String entityViewClassName : entityViewsBuildItem.getEntityViewClassNames()) {
            reflectionProducer.produce(new ReflectiveClassBuildItem(true, false, false, entityViewClassName));
            for (String generatedStaticModelClass : getGeneratedEntityViewModelImplClassName(entityViewClassName)) {
                reflectionProducer.produce(ReflectiveClassBuildItem.builder(generatedStaticModelClass)
                        .constructors(true)
                        .fields(true)
                        .finalFieldsWritable(true)
                        .build()
                );
            }
        }

        // add fromString methods from entity view id types
        for (EntityViewMapping entityViewMapping : entityViewConfigurationBuildItem.getEntityViewConfiguration().getEntityViewMappings()) {
            if (entityViewMapping.getIdAttribute() != null) {
                reflectionProducer.produce(ReflectiveClassBuildItem.builder(entityViewMapping.getIdAttribute().getDeclaredType())
                        .constructors(true)
                        .methods(true)
                        .build()
                );
            }
        }
    }

    private List<String> getGeneratedEntityViewModelImplClassName(String entityViewClassName) {
        return Arrays.asList(
                entityViewClassName.replace("$", "") + "_",
                entityViewClassName.replace("$", "") + "Relation",
                entityViewClassName.replace("$", "") + "MultiRelation",
                entityViewClassName.replace("$", "") + "Impl",
                entityViewClassName.replace("$", "") + "Builder",
                entityViewClassName.replace("$", "") + "Builder$Init"
        );
    }

    @BuildStep
    ServiceProviderBuildItem criteriaBuilderConfigurationProvider() {
        return new ServiceProviderBuildItem("com.blazebit.persistence.spi.CriteriaBuilderConfigurationProvider",
                "com.blazebit.persistence.impl.CriteriaBuilderConfigurationProviderImpl");
    }

    @BuildStep
    ServiceProviderBuildItem hibernateMetadataContributor() {
        return new ServiceProviderBuildItem("org.hibernate.boot.spi.MetadataContributor",
                "com.blazebit.persistence.integration.hibernate.Hibernate5MetadataContributor",
                "com.blazebit.persistence.integration.hibernate.Hibernate52MetadataContributor",
                "com.blazebit.persistence.integration.hibernate.Hibernate53MetadataContributor",
                "com.blazebit.persistence.integration.hibernate.Hibernate60MetadataContributor");
    }

    @BuildStep
    ServiceProviderBuildItem hibernateTypeContributor() {
        return new ServiceProviderBuildItem("org.hibernate.metamodel.spi.TypeContributor",
                "com.blazebit.persistence.integration.hibernate.Hibernate4Integrator",
                "com.blazebit.persistence.integration.hibernate.Hibernate43Integrator");
    }

    @BuildStep
    ServiceProviderBuildItem hibernateAccess() {
        return new ServiceProviderBuildItem("com.blazebit.persistence.integration.hibernate.base.HibernateAccess",
                "com.blazebit.persistence.integration.hibernate.Hibernate53Access");
    }

    @BuildStep
    ServiceProviderBuildItem entityManagerFactoryIntegrator() {
        return new ServiceProviderBuildItem("com.blazebit.persistence.spi.EntityManagerFactoryIntegrator",
                "com.blazebit.persistence.integration.hibernate.Hibernate53EntityManagerFactoryIntegrator");
    }

    @BuildStep
    ServiceProviderBuildItem transactionAccessFactory() {
        return new ServiceProviderBuildItem("com.blazebit.persistence.view.spi.TransactionAccessFactory",
                "com.blazebit.persistence.integration.hibernate.Hibernate4TransactionAccessFactory",
                "com.blazebit.persistence.integration.hibernate.Hibernate5TransactionAccessFactory");
    }

    @BuildStep
    ServiceProviderBuildItem extendedQuerySupport() {
        return new ServiceProviderBuildItem("com.blazebit.persistence.spi.ExtendedQuerySupport",
                "com.blazebit.persistence.integration.hibernate.base.HibernateExtendedQuerySupport");
    }

    @BuildStep
    ServiceProviderBuildItem blazeCriteriaBuilderFactory() {
        return new ServiceProviderBuildItem("com.blazebit.persistence.criteria.spi.BlazeCriteriaBuilderFactory",
                "com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderFactoryImpl");
    }
}
