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

import com.blazebit.persistence.integration.quarkus.runtime.DefaultCriteriaBuilderFactoryProducer;
import com.blazebit.persistence.integration.quarkus.runtime.DefaultEntityViewManagerProducer;
import com.blazebit.persistence.integration.quarkus.runtime.EntityViewConfigurationHolder;
import com.blazebit.persistence.integration.quarkus.runtime.EntityViewRecorder;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.BeanContainerListenerBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.CapabilityBuildItem;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.recording.RecorderContext;

import static io.quarkus.deployment.annotations.ExecutionTime.STATIC_INIT;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
class EntityViewProcessor {

    static final String CAPABILITY = "com.blazebit.persistence.integration.quarkus";
    static final String FEATURE = "entity-views";

    @BuildStep
    CapabilityBuildItem capability() {
        return new CapabilityBuildItem(CAPABILITY);
    }

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }


    @BuildStep
    public EntityViewsBuildItem produceModel(CombinedIndexBuildItem index) {
        EntityViewJandexScavenger entityViewJandexScavenger = new EntityViewJandexScavenger(index.getIndex());
        return entityViewJandexScavenger.discoverAndRegisterEntityViewsModel();
    }

    @BuildStep
    @Record(STATIC_INIT)
    void registerBeans(EntityViewsBuildItem entityViewsBuildItem,
                       BuildProducer<AdditionalBeanBuildItem> additionalBeans,
                       EntityViewRecorder entityViewRecorder,
                       BuildProducer<BeanContainerListenerBuildItem> containerListenerProducer,
                       RecorderContext recorderContext) {
        additionalBeans.produce(AdditionalBeanBuildItem.builder().setUnremovable()
                .addBeanClasses(DefaultCriteriaBuilderFactoryProducer.class, DefaultEntityViewManagerProducer.class, EntityViewConfigurationHolder.class)
                .build());

        for (String entityViewClassName : entityViewsBuildItem.getEntityViewClassNames()) {
            entityViewRecorder.addEntityView(recorderContext.classProxy(entityViewClassName));
        }

        containerListenerProducer.produce(new BeanContainerListenerBuildItem(entityViewRecorder.setEntityViewConfiguration()));
    }
}
