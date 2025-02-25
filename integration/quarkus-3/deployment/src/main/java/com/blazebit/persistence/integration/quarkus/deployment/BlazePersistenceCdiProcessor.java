/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
package com.blazebit.persistence.integration.quarkus.deployment;

import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.integration.quarkus.runtime.BlazePersistenceConfiguration;
import com.blazebit.persistence.integration.quarkus.runtime.BlazePersistenceInstance;
import com.blazebit.persistence.integration.quarkus.runtime.BlazePersistenceInstanceUtil;
import com.blazebit.persistence.integration.quarkus.runtime.EntityViewRecorder;
import com.blazebit.persistence.view.EntityViewManager;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.AnnotationsTransformerBuildItem;
import io.quarkus.arc.deployment.SyntheticBeanBuildItem;
import io.quarkus.arc.processor.AnnotationsTransformer;
import io.quarkus.arc.processor.DotNames;
import io.quarkus.arc.processor.Transformation;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.hibernate.orm.runtime.PersistenceUnitUtil;
import jakarta.enterprise.inject.Default;
import jakarta.inject.Singleton;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;

import java.util.List;
import java.util.function.Supplier;

/**
 * @author Moritz Becker
 * @since 1.6.0
 */
class BlazePersistenceCdiProcessor {
    private static final DotName BLAZE_PERSISTENCE_INSTANCE_QUALIFIER = DotName.createSimple(BlazePersistenceInstance.class.getName());

    private static final DotName CRITERIA_BUILDER_FACTORY = DotName.createSimple(CriteriaBuilderFactory.class.getName());
    private static final DotName ENTITY_VIEW_MANAGER = DotName.createSimple(EntityViewManager.class.getName());

    @BuildStep
    AnnotationsTransformerBuildItem convertJpaResourceAnnotationsToQualifier(
            List<BlazePersistenceInstanceDescriptorBuildItem> blazePersistenceDescriptors) {
        AnnotationsTransformer transformer = new AnnotationsTransformer() {

            @Override
            public boolean appliesTo(AnnotationTarget.Kind kind) {
                // at some point we might want to support METHOD_PARAMETER too but for now getting annotations for them
                // is cumbersome so let's wait for Jandex improvements
                return kind == AnnotationTarget.Kind.FIELD;
            }

            @Override
            public void transform(TransformationContext transformationContext) {
                FieldInfo field = transformationContext.getTarget().asField();

                if (!ENTITY_VIEW_MANAGER.equals(field.type().name()) && !CRITERIA_BUILDER_FACTORY.equals(field.type().name())) {
                    return;
                }

                DotName blazePersistenceInstanceAnnotation;
                if (field.hasAnnotation(BLAZE_PERSISTENCE_INSTANCE_QUALIFIER)) {
                    blazePersistenceInstanceAnnotation = BLAZE_PERSISTENCE_INSTANCE_QUALIFIER;
                } else {
                    return;
                }

                AnnotationValue blazePersistenceInstanceAnnotationValue = field.annotation(blazePersistenceInstanceAnnotation).value();

                Transformation transformation = transformationContext.transform()
                        .add(DotNames.INJECT);
                if (blazePersistenceInstanceAnnotationValue == null || blazePersistenceInstanceAnnotationValue.asString().isEmpty()) {
                    transformation.add(DotNames.DEFAULT);
                } else if (blazePersistenceDescriptors.size() == 1
                        && blazePersistenceDescriptors.get(0).getBlazePersistenceInstanceName()
                                .equals(blazePersistenceInstanceAnnotationValue.asString())) {
                    // we are in the case where we have only one Blaze-Persistence instance defined
                    // in this case, we consider it the default too if the name matches
                    transformation.add(DotNames.DEFAULT);
                }
                transformation.done();
            }
        };

        return new AnnotationsTransformerBuildItem(transformer);
    }

    @Record(ExecutionTime.RUNTIME_INIT)
    @BuildStep
    void generateBeans(EntityViewRecorder recorder,
                                 BlazePersistenceConfiguration blazePersistenceConfig,
                                 List<BlazePersistenceInstanceDescriptorBuildItem> blazePersistenceDescriptors,
                                 BuildProducer<AdditionalBeanBuildItem> additionalBeans,
                                 BuildProducer<SyntheticBeanBuildItem> syntheticBeanBuildItemBuildProducer) {
        if (blazePersistenceDescriptors.isEmpty()) {
            // No persistence units have been configured so bail out
            return;
        }

        // add the @BlazePersistenceInstance class otherwise it won't be registered as a qualifier
        additionalBeans.produce(AdditionalBeanBuildItem.builder().addBeanClass(BlazePersistenceInstance.class).build());

        // we have only one Blaze-Persistence instance defined: we make it the default even if it has a name
        if (blazePersistenceDescriptors.size() == 1) {
            BlazePersistenceInstanceDescriptorBuildItem blazePersistenceDescriptor = blazePersistenceDescriptors.get(0);
            String blazePersistenceInstanceName = blazePersistenceDescriptor.getBlazePersistenceInstanceName();
            String persistenceUnitName = blazePersistenceConfig.blazePersistenceInstances().get(blazePersistenceInstanceName)
                    .persistenceUnit().orElse(PersistenceUnitUtil.DEFAULT_PERSISTENCE_UNIT_NAME);

            syntheticBeanBuildItemBuildProducer
                    .produce(createSyntheticBean(blazePersistenceInstanceName,
                            true,
                            CriteriaBuilderFactory.class,
                            recorder.criteriaBuilderFactorySupplier(blazePersistenceConfig, blazePersistenceInstanceName, persistenceUnitName),
                            true));

            syntheticBeanBuildItemBuildProducer
                    .produce(createSyntheticBean(blazePersistenceInstanceName,
                            true,
                            EntityViewManager.class,
                            recorder.entityViewManagerSupplier(
                                    blazePersistenceConfig,
                                    blazePersistenceInstanceName,
                                    blazePersistenceDescriptor.getEntityViewClasses(),
                                    blazePersistenceDescriptor.getEntityViewListenerClasses()
                            ),
                            false));

            return;
        }

        for (BlazePersistenceInstanceDescriptorBuildItem blazePersistenceDescriptor : blazePersistenceDescriptors) {
            String blazePersistenceInstanceName = blazePersistenceDescriptor.getBlazePersistenceInstanceName();
            String persistenceUnitName = blazePersistenceConfig.blazePersistenceInstances().get(blazePersistenceInstanceName)
                    .persistenceUnit().orElse(PersistenceUnitUtil.DEFAULT_PERSISTENCE_UNIT_NAME);
            boolean defaultBlazePersistenceInstance = BlazePersistenceInstanceUtil.isDefaultBlazePersistenceInstance(blazePersistenceInstanceName);

            syntheticBeanBuildItemBuildProducer
                    .produce(createSyntheticBean(blazePersistenceInstanceName,
                            defaultBlazePersistenceInstance,
                            CriteriaBuilderFactory.class,
                            recorder.criteriaBuilderFactorySupplier(blazePersistenceConfig, blazePersistenceInstanceName, persistenceUnitName),
                            true));

            syntheticBeanBuildItemBuildProducer
                    .produce(createSyntheticBean(blazePersistenceInstanceName,
                            defaultBlazePersistenceInstance,
                            EntityViewManager.class,
                            recorder.entityViewManagerSupplier(
                                    blazePersistenceConfig,
                                    blazePersistenceInstanceName,
                                    blazePersistenceDescriptor.getEntityViewClasses(),
                                    blazePersistenceDescriptor.getEntityViewListenerClasses()
                            ),
                            false));
        }
    }

    private static <T> SyntheticBeanBuildItem createSyntheticBean(String blazePersistenceInstanceName, boolean isDefaultBlazePersistenceInstance,
                                                                  Class<T> type, Supplier<T> supplier, boolean defaultBean) {
        SyntheticBeanBuildItem.ExtendedBeanConfigurator configurator = SyntheticBeanBuildItem
                .configure(type)
                .scope(Singleton.class)
                .setRuntimeInit()
                .unremovable()
                .supplier(supplier);

        if (defaultBean) {
            configurator.defaultBean();
        }

        if (isDefaultBlazePersistenceInstance) {
            configurator.addQualifier(Default.class);
        } else {
            configurator.addQualifier().annotation(DotNames.NAMED).addValue("value", blazePersistenceInstanceName).done();
            configurator.addQualifier().annotation(BlazePersistenceInstance.class).addValue("value", blazePersistenceInstanceName).done();
        }

        return configurator.done();
    }
}
