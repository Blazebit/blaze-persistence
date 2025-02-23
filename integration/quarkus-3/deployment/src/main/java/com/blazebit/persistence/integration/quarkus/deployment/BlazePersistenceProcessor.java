/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.quarkus.deployment;

import com.blazebit.persistence.impl.function.entity.ValuesEntity;
import com.blazebit.persistence.integration.quarkus.runtime.BlazePersistenceConfiguration;
import com.blazebit.persistence.integration.quarkus.runtime.BlazePersistenceInstance;
import com.blazebit.persistence.integration.quarkus.runtime.BlazePersistenceInstanceConfiguration;
import com.blazebit.persistence.integration.quarkus.runtime.BlazePersistenceInstanceUtil;
import com.blazebit.persistence.parser.expression.ConcurrentHashMapExpressionCache;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.spi.EntityViewMapping;
import io.quarkus.builder.Version;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.AdditionalApplicationArchiveMarkerBuildItem;
import io.quarkus.deployment.builditem.AdditionalIndexedClassesBuildItem;
import io.quarkus.deployment.builditem.CapabilityBuildItem;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ServiceProviderBuildItem;
import io.quarkus.hibernate.orm.deployment.AdditionalJpaModelBuildItem;
import io.quarkus.hibernate.orm.deployment.PersistenceUnitDescriptorBuildItem;
import io.quarkus.hibernate.orm.runtime.PersistenceUnitUtil;
import io.quarkus.runtime.configuration.ConfigurationException;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.jboss.logging.Logger;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
class BlazePersistenceProcessor {

    static final String CAPABILITY = "com.blazebit.persistence.integration.quarkus";
    static final String FEATURE = "blaze-persistence";

    private static final Logger LOG = Logger.getLogger(BlazePersistenceProcessor.class);
    private static final DotName ENTITY_VIEW = DotName.createSimple(EntityView.class.getName());
    private static final DotName BLAZE_PERSISTENCE_INSTANCE = DotName.createSimple(BlazePersistenceInstance.class.getName());
    private static final DotName BLAZE_PERSISTENCE_INSTANCE_REPEATABLE_CONTAINER = DotName
            .createSimple(BlazePersistenceInstance.List.class.getName());

    @BuildStep
    CapabilityBuildItem capability() {
        String version = Version.getVersion();
        int dotIndex = version.indexOf('.');
        // As of version Quarkus 2, the capabilities are read from the extension descriptor
        if (dotIndex == -1 || Integer.parseInt(version.substring(0, dotIndex)) >= 2) {
            return null;
        }
        return new CapabilityBuildItem(CAPABILITY);
    }

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    AdditionalIndexedClassesBuildItem addBlazePersistenceInstanceAnnotationToIndex() {
        return new AdditionalIndexedClassesBuildItem(BlazePersistenceInstance.class.getName());
    }

    @BuildStep
    void addBlazePersistenceEntities(BuildProducer<AdditionalJpaModelBuildItem> jpaModelBuildItemBuildProducer) {
        Constructor<AdditionalJpaModelBuildItem> constructor = (Constructor<AdditionalJpaModelBuildItem>) AdditionalJpaModelBuildItem.class.getDeclaredConstructors()[0];
        Object argument;
        if (constructor.getParameterTypes()[0] == Class.class) {
            argument = ValuesEntity.class;
        } else {
            argument = ValuesEntity.class.getName();
        }
        try {
            jpaModelBuildItemBuildProducer.produce(constructor.newInstance(argument));
        } catch (Exception e) {
            throw new RuntimeException("Could not add ValuesEntity class to persistence unit!", e);
        }
    }

    @BuildStep
    void includeArchivesHostingEntityViewPackagesInIndex(BlazePersistenceConfiguration blazePersistenceConfig,
                                                     BuildProducer<AdditionalApplicationArchiveMarkerBuildItem> additionalApplicationArchiveMarkers) {
        for (BlazePersistenceInstanceConfiguration blazePersistenceInstanceConfig : blazePersistenceConfig.blazePersistenceInstances().values()) {
            if (blazePersistenceInstanceConfig.packages().isPresent()) {
                for (String pakkage : blazePersistenceInstanceConfig.packages().get()) {
                    additionalApplicationArchiveMarkers
                            .produce(new AdditionalApplicationArchiveMarkerBuildItem(pakkage.replace('.', '/')));
                }
            }
        }
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
    void buildBlazePersistenceInstanceDescriptors(EntityViewsBuildItem entityViewsBuildItem,
                                                  CombinedIndexBuildItem indexBuildItem,
                                                  EntityViewListenersBuildItem entityViewListenersBuildItem,
                                                  List<PersistenceUnitDescriptorBuildItem> persistenceUnitDescriptors,
                                                  BlazePersistenceConfiguration blazePersistenceConfig,
                                                  BuildProducer<BlazePersistenceInstanceDescriptorBuildItem> blazePersistenceDescriptorBuildItemProducer) {
        if (persistenceUnitDescriptors.isEmpty()) {
            LOG.warn("The Blaze-Persistence dependency is present but no persistence units have been defined.");
            return;
        }

        Optional<PersistenceUnitDescriptorBuildItem> defaultPersistenceUnit = persistenceUnitDescriptors.stream()
                .filter(pu -> PersistenceUnitUtil.isDefaultPersistenceUnit(pu.getPersistenceUnitName()))
                .findFirst();
        boolean enableDefaultBlazePersistence = (defaultPersistenceUnit.isPresent()
                && blazePersistenceConfig.namedBlazePersistenceInstances().isEmpty())
                || blazePersistenceConfig.defaultBlazePersistenceInstance().isAnyPropertySet();
        boolean hasPackagesInQuarkusConfig = hasPackagesInQuarkusConfig(blazePersistenceConfig);
        Collection<AnnotationInstance> packageLevelBlazePersistenceInstanceAnnotations = getPackageLevelBlazePersistenceInstanceAnnotations(
                indexBuildItem.getIndex()
        );
        Map<String, Set<String>> entityViewClassesPerBlazePersistenceInstance;
        Map<String, Set<String>> entityViewListenerClassesPerBlazePersistenceInstance;
        Set<String> entityViewClassesForDefaultBlazePersistenceInstance;
        Set<String> entityViewListenerClassesForDefaultBlazePersistenceInstance;
        if (enableDefaultBlazePersistence && blazePersistenceConfig.namedBlazePersistenceInstances().isEmpty() &&
                !hasPackagesInQuarkusConfig && packageLevelBlazePersistenceInstanceAnnotations.isEmpty()) {
            // Only the default Blaze-Persistence instance exists and no package or annotation configuration is specified.
            // In this case we just assign all entity views to the default instance.
            entityViewClassesPerBlazePersistenceInstance = Collections.emptyMap();
            entityViewListenerClassesPerBlazePersistenceInstance = Collections.emptyMap();
            entityViewClassesForDefaultBlazePersistenceInstance = entityViewsBuildItem.getEntityViewClassNames();
            entityViewListenerClassesForDefaultBlazePersistenceInstance = entityViewListenersBuildItem.getEntityViewListenerClassNames();
        } else {
            Map<String, Set<String>> blazePersistenceInstanceToPackagesMapping = mapBlazePersistenceInstancesToPackages(
                blazePersistenceConfig,
                enableDefaultBlazePersistence,
                hasPackagesInQuarkusConfig,
                packageLevelBlazePersistenceInstanceAnnotations
            );
            entityViewClassesPerBlazePersistenceInstance = buildClassesToBlazePersistenceInstanceMapping(
                    blazePersistenceInstanceToPackagesMapping,
                    entityViewsBuildItem.getEntityViewClassNames(),
                    indexBuildItem.getIndex()
            );
            entityViewListenerClassesPerBlazePersistenceInstance = buildClassesToBlazePersistenceInstanceMapping(
                    blazePersistenceInstanceToPackagesMapping,
                    entityViewListenersBuildItem.getEntityViewListenerClassNames(),
                    indexBuildItem.getIndex()
            );
            entityViewClassesForDefaultBlazePersistenceInstance = entityViewClassesPerBlazePersistenceInstance
                .getOrDefault(PersistenceUnitUtil.DEFAULT_PERSISTENCE_UNIT_NAME, Collections.emptySet());
            entityViewListenerClassesForDefaultBlazePersistenceInstance = entityViewListenerClassesPerBlazePersistenceInstance
                .getOrDefault(PersistenceUnitUtil.DEFAULT_PERSISTENCE_UNIT_NAME, Collections.emptySet());
        }

        if (enableDefaultBlazePersistence) {
            blazePersistenceDescriptorBuildItemProducer.produce(new BlazePersistenceInstanceDescriptorBuildItem(
                    BlazePersistenceInstanceUtil.DEFAULT_BLAZE_PERSISTENCE_NAME,
                    entityViewClassesForDefaultBlazePersistenceInstance,
                    entityViewListenerClassesForDefaultBlazePersistenceInstance));
        } else if ((!blazePersistenceConfig.defaultBlazePersistenceInstance().persistenceUnit().isPresent()
                        || PersistenceUnitUtil.isDefaultPersistenceUnit(blazePersistenceConfig.defaultBlazePersistenceInstance().persistenceUnit().get()))
                && !defaultPersistenceUnit.isPresent()) {
            if (!entityViewClassesForDefaultBlazePersistenceInstance.isEmpty()) {
                LOG.warn(
                        "Entity view classes are defined for the default Blaze-Persistence instance but no default persistence unit found: the default Blaze-Persistence instance will not be created.");
            }
            if (!entityViewListenerClassesForDefaultBlazePersistenceInstance.isEmpty()) {
                LOG.warn(
                        "Entity view listener classes are defined for the default Blaze-Persistence instance but no default persistence unit found: the default Blaze-Persistence instance will not be created.");
            }
        }

        for (Map.Entry<String, BlazePersistenceInstanceConfiguration> namedInstance : blazePersistenceConfig.namedBlazePersistenceInstances()
                .entrySet()) {
            BlazePersistenceInstanceConfiguration blazePersistenceInstanceConfig = namedInstance.getValue();
            if (blazePersistenceInstanceConfig.persistenceUnit().isPresent()) {
                persistenceUnitDescriptors.stream()
                        .filter(descriptor -> blazePersistenceInstanceConfig.persistenceUnit().get().equals(descriptor.getPersistenceUnitName()))
                        .findFirst()
                        .orElseThrow(() -> new ConfigurationException(
                                String.format("The persistence unit '%1$s' is not configured but the Blaze-Persistence instance '%2$s' uses it.",
                                        blazePersistenceInstanceConfig.persistenceUnit().get(), namedInstance.getKey())));
            } else {
                if (!BlazePersistenceInstanceUtil.isDefaultBlazePersistenceInstance(namedInstance.getKey())) {
                    // if it's not the default Blaze-Persistence instance, we mandate a persistence unit to prevent common errors
                    throw new ConfigurationException(
                            String.format("Persistence unit must be defined for Blaze-Persistence instance '%s'.", namedInstance.getKey()));
                }

                if (!defaultPersistenceUnit.isPresent()) {
                    throw new ConfigurationException(
                            String.format("The default persistence unit is not configured but the Blaze-Persistence instance '%s' uses it.",
                                    namedInstance.getKey()));
                }
            }

            blazePersistenceDescriptorBuildItemProducer.produce(new BlazePersistenceInstanceDescriptorBuildItem(
                    namedInstance.getKey(),
                    entityViewClassesPerBlazePersistenceInstance.getOrDefault(namedInstance.getKey(), Collections.emptySet()),
                    entityViewListenerClassesPerBlazePersistenceInstance.getOrDefault(namedInstance.getKey(), Collections.emptySet())));
        }
    }

    @BuildStep
    void reflection(EntityViewsBuildItem entityViewsBuildItem,
                    BuildProducer<ReflectiveClassBuildItem> reflectionProducer) {
        reflectionProducer.produce(new ReflectiveClassBuildItem(true, true, ValuesEntity.class));
        reflectionProducer.produce(new ReflectiveClassBuildItem(true, false, false, ConcurrentHashMapExpressionCache.class));
        reflectionProducer.produce(new ReflectiveClassBuildItem(true, false, false, "com.blazebit.persistence.integration.hibernate.CustomOneToManyPersister"));
        reflectionProducer.produce(new ReflectiveClassBuildItem(true, false, false, "com.blazebit.persistence.integration.hibernate.CustomBasicCollectionPersister"));
        // Needed by AbstractHibernateEntityManagerFactoryIntegrator
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
        // Needed by HibernateExtendedQuerySupport
        reflectionProducer.produce(ReflectiveClassBuildItem.builder(
                "org.hibernate.hql.internal.ast.exec.MultiTableUpdateExecutor",
                "org.hibernate.hql.internal.ast.exec.BasicExecutor",
                "org.hibernate.hql.internal.ast.exec.DeleteExecutor",
                "org.hibernate.hql.internal.ast.exec.MultiTableDeleteExecutor",
                "org.hibernate.hql.internal.ast.exec.IdSubselectUpdateExecutor",
                "org.hibernate.hql.internal.ast.exec.SimpleUpdateExecutor",
                "org.hibernate.hql.internal.ast.exec.InsertExecutor")
                .fields(true)
                .finalFieldsWritable(true)
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
            // We need entity view methods because we might call them as listeners but we also need them for metadata discovery
            reflectionProducer.produce(new ReflectiveClassBuildItem(true, true, false, entityViewClassName));
            String baseName = entityViewClassName.replace("$", "");
            // For entity view implementations we need full reflection because instances of these classes are usually serialized
            reflectionProducer.produce(ReflectiveClassBuildItem.builder(baseName + "Impl")
                    .constructors(true)
                    .fields(true)
                    .methods(true)
                    .finalFieldsWritable(true)
                    .build()
            );
            // For static metamodel classes, we only need the fields
            reflectionProducer.produce(new ReflectiveClassBuildItem(false, false, true, baseName + "_"));
            // For Relation classes, we only need the constructor
            for (String generatedStaticModelClass : Arrays.asList(baseName + "Relation", baseName + "MultiRelation")) {
                reflectionProducer.produce(new ReflectiveClassBuildItem(true, false, false, generatedStaticModelClass));
            }
            // For builder classes, we only need constructors
            try {
                Class<?> builderClass = Thread.currentThread().getContextClassLoader().loadClass(baseName + "Builder");
                reflectionProducer.produce(new ReflectiveClassBuildItem(true, false, false, builderClass.getName()));
                for (Class<?> builderSubClass : builderClass.getDeclaredClasses()) {
                    reflectionProducer.produce(new ReflectiveClassBuildItem(true, false, false, builderSubClass.getName()));
                }

            } catch (ClassNotFoundException ex) {
                // Ignore
            }
        }

        // add fromString methods from entity view id types
        EntityViewConfiguration evc = EntityViews.createDefaultConfiguration();
        for (String entityViewClassName : entityViewsBuildItem.getEntityViewClassNames()) {
            try {
                evc.addEntityView(Thread.currentThread().getContextClassLoader().loadClass(entityViewClassName));
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        for (EntityViewMapping entityViewMapping : evc.getEntityViewMappings()) {
            if (entityViewMapping.getIdAttribute() != null) {
                reflectionProducer.produce(ReflectiveClassBuildItem.builder(entityViewMapping.getIdAttribute().getDeclaredType())
                        .constructors(true)
                        .methods(true)
                        .build()
                );
            }
        }
    }

    private List<String> getGeneratedEntityViewModelClassNames(String entityViewClassName) {
        return Arrays.asList(
                entityViewClassName.replace("$", "") + "_",
                entityViewClassName.replace("$", "") + "Relation",
                entityViewClassName.replace("$", "") + "MultiRelation",
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
    ServiceProviderBuildItem entityViewConfigurationProvider() {
        return new ServiceProviderBuildItem("com.blazebit.persistence.view.spi.EntityViewConfigurationProvider",
            "com.blazebit.persistence.view.impl.EntityViewConfigurationProviderImpl");
    }

    @BuildStep
    ServiceProviderBuildItem blazeCriteriaBuilderFactory() {
        return new ServiceProviderBuildItem("com.blazebit.persistence.criteria.spi.BlazeCriteriaBuilderFactory",
            "com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderFactoryImpl");
    }

    @BuildStep
    ServiceProviderBuildItem extendedQuerySupport() {
        return new ServiceProviderBuildItem("com.blazebit.persistence.spi.ExtendedQuerySupport",
            "com.blazebit.persistence.integration.hibernate.base.HibernateExtendedQuerySupport");
    }

    @BuildStep
    ServiceProviderBuildItem hibernateAccess() {
        return new ServiceProviderBuildItem("com.blazebit.persistence.integration.hibernate.base.HibernateAccess",
            "com.blazebit.persistence.integration.hibernate.Hibernate62Access");
    }

    @BuildStep
    ServiceProviderBuildItem entityManagerFactoryIntegrator() {
        return new ServiceProviderBuildItem("com.blazebit.persistence.spi.EntityManagerFactoryIntegrator",
            "com.blazebit.persistence.integration.hibernate.Hibernate62EntityManagerFactoryIntegrator");
    }

    @BuildStep
    ServiceProviderBuildItem transactionAccessFactory() {
        return new ServiceProviderBuildItem("com.blazebit.persistence.view.spi.TransactionAccessFactory",
            "com.blazebit.persistence.integration.hibernate.Hibernate6TransactionAccessFactory");
    }

    @BuildStep
    ServiceProviderBuildItem hibernateAdditionalMappingContributor() {
        return new ServiceProviderBuildItem("org.hibernate.boot.spi.AdditionalMappingContributor",
                "com.blazebit.persistence.integration.hibernate.Hibernate62AdditionalMappingContributor");
    }

    @BuildStep
    ServiceProviderBuildItem hibernateTypeContributor() {
        return new ServiceProviderBuildItem("org.hibernate.metamodel.spi.TypeContributor",
                "com.blazebit.persistence.integration.hibernate.Hibernate62Integrator");
    }

    private static Map<String, Set<String>> buildClassesToBlazePersistenceInstanceMapping(Map<String, Set<String>> blazePersistenceInstanceToPackageMapping,
                                                                                          Set<String> classes, IndexView index) {
        Map<String, Set<String>> entityViewClassesPerBlazePersistenceInstance = new HashMap<>();
        Set<String> entityViewClassesWithBlazePersistenceInstanceAnnotations = new TreeSet<>();

        for (String entityViewClassName : classes) {
            ClassInfo entityViewClassInfo = index.getClassByName(DotName.createSimple(entityViewClassName));
            Set<String> relatedEntityViewClassNames = Collections.emptySet();//getRelatedEntityViewClassNames(index, entityViews.getEntityViewClassNames(), entityViewClassInfo);

            if (entityViewClassInfo != null && (entityViewClassInfo.classAnnotation(BLAZE_PERSISTENCE_INSTANCE) != null
                    || entityViewClassInfo.classAnnotation(BLAZE_PERSISTENCE_INSTANCE_REPEATABLE_CONTAINER) != null)) {
                entityViewClassesWithBlazePersistenceInstanceAnnotations.add(entityViewClassInfo.name().toString());
            }

            for (Map.Entry<String, Set<String>> packageRuleEntry : blazePersistenceInstanceToPackageMapping.entrySet()) {
                if (entityViewClassName.startsWith(packageRuleEntry.getKey())) {
                    for (String blazePersistenceInstanceName : packageRuleEntry.getValue()) {
                        entityViewClassesPerBlazePersistenceInstance.computeIfAbsent(blazePersistenceInstanceName, (key) -> new HashSet<>())
                                .add(entityViewClassName);

                        // also add the hierarchy to the persistence unit
                        // we would need to add all the underlying model to it but adding the hierarchy
                        // is necessary for Panache as we need to add PanacheEntity to the PU
                        for (String relatedEntityViewClassName : relatedEntityViewClassNames) {
                            entityViewClassesPerBlazePersistenceInstance.get(blazePersistenceInstanceName).add(relatedEntityViewClassName);
                        }
                    }
                }
            }
        }

        if (!entityViewClassesWithBlazePersistenceInstanceAnnotations.isEmpty()) {
            throw new IllegalStateException(String.format(
                    "@BlazePersistenceInstance annotations are not supported at the class level on entity view classes:\n\t- %s\nUse the `.packages` configuration property or package-level annotations instead.",
                    String.join("\n\t- ", entityViewClassesWithBlazePersistenceInstanceAnnotations)));
        }

        Set<String> affectedEntityViewClasses = entityViewClassesPerBlazePersistenceInstance.values().stream().flatMap(Set::stream)
                .collect(Collectors.toSet());
        Set<String> unaffectedEntityViewClasses = classes.stream()
                .filter(c -> !affectedEntityViewClasses.contains(c))
                .collect(Collectors.toCollection(TreeSet::new));
        if (!unaffectedEntityViewClasses.isEmpty()) {
            LOG.warnf("Could not find a suitable Blaze-Persistence instance for entity view classes:\n\t- %s",
                    String.join("\n\t- ", unaffectedEntityViewClasses));
        }

        return entityViewClassesPerBlazePersistenceInstance;
    }

    private static Map<String, Set<String>> mapBlazePersistenceInstancesToPackages(BlazePersistenceConfiguration blazePersistenceConfig,
                                                                                   boolean enableDefaultBlazePersistenceInstance,
                                                                                   boolean hasPackagesInQuarkusConfig,
                                                                                   Collection<AnnotationInstance> packageLevelBlazePersistenceInstanceAnnotations) {
        Map<String, Set<String>> packageRules = new HashMap<>();
        if (hasPackagesInQuarkusConfig) {
            // Config based packages have priorities over annotations.
            // As long as there is one defined, annotations are ignored.
            if (!packageLevelBlazePersistenceInstanceAnnotations.isEmpty()) {
                LOG.warn(
                        "Mixing Quarkus configuration and @BlazePersistenceInstance annotations to define the Blaze-Persistence instances is not supported. Ignoring the annotations.");
            }

            for (Map.Entry<String, BlazePersistenceInstanceConfiguration> candidateBlazePersistenceInstanceEntry : blazePersistenceConfig.blazePersistenceInstances()
                    .entrySet()) {
                String candidateBlazePersistenceInstanceName = candidateBlazePersistenceInstanceEntry.getKey();

                Set<String> candidateBlazePersistenceInstancePackages = candidateBlazePersistenceInstanceEntry.getValue().packages()
                        .orElseThrow(() -> new ConfigurationException(String.format(
                                "Packages must be configured for Blaze-Persistence instance '%s'.", candidateBlazePersistenceInstanceName)));

                for (String packageName : candidateBlazePersistenceInstancePackages) {
                    packageRules.computeIfAbsent(normalizePackage(packageName), p -> new HashSet<>())
                            .add(candidateBlazePersistenceInstanceName);
                }
            }
        } else if (!packageLevelBlazePersistenceInstanceAnnotations.isEmpty()) {
            for (AnnotationInstance packageLevelBlazePersistenceInstanceAnnotation : packageLevelBlazePersistenceInstanceAnnotations) {
                String className = packageLevelBlazePersistenceInstanceAnnotation.target().asClass().name().toString();
                String packageName;
                if (className == null || className.isEmpty() || className.indexOf('.') == -1) {
                    packageName = "";
                } else {
                    packageName = normalizePackage(className.substring(0, className.lastIndexOf('.')));
                }

                String blazePersistenceInstanceName = packageLevelBlazePersistenceInstanceAnnotation.value().asString();
                if (blazePersistenceInstanceName != null && !blazePersistenceInstanceName.isEmpty()) {
                    packageRules.computeIfAbsent(packageName, p -> new HashSet<>())
                            .add(blazePersistenceInstanceName);
                }
            }
        } else {
            throw new ConfigurationException(
                    "Multiple Blaze-Persistence instances are defined but the entity views are not mapped to them. You should either use the .packages Quarkus configuration property or package-level @BlazePersistenceInstance annotations.");
        }
        return packageRules;
    }

    private static boolean hasPackagesInQuarkusConfig(BlazePersistenceConfiguration blazePersistenceConfig) {
        for (BlazePersistenceInstanceConfiguration blazePersistenceInstanceConfig : blazePersistenceConfig.blazePersistenceInstances().values()) {
            if (blazePersistenceInstanceConfig.packages().isPresent()) {
                return true;
            }
        }

        return false;
    }

    private static Collection<AnnotationInstance> getPackageLevelBlazePersistenceInstanceAnnotations(IndexView index) {
        Collection<AnnotationInstance> blazePersistenceInstanceAnnotations = index.getAnnotationsWithRepeatable(BLAZE_PERSISTENCE_INSTANCE, index);
        Collection<AnnotationInstance> packageLevelBlazePersistenceInstanceAnnotations = new ArrayList<>();

        for (AnnotationInstance blazePersistenceInstanceAnnotation : blazePersistenceInstanceAnnotations) {
            if (blazePersistenceInstanceAnnotation.target().kind() != AnnotationTarget.Kind.CLASS) {
                continue;
            }

            if (!"package-info".equals(blazePersistenceInstanceAnnotation.target().asClass().simpleName())) {
                continue;
            }
            packageLevelBlazePersistenceInstanceAnnotations.add(blazePersistenceInstanceAnnotation);
        }

        return packageLevelBlazePersistenceInstanceAnnotations;
    }

    private static String normalizePackage(String pakkage) {
        if (pakkage.endsWith(".")) {
            return pakkage;
        }
        return pakkage + ".";
    }
}
