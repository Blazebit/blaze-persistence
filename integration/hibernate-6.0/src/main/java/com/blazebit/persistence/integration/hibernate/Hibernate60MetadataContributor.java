/*
 * Copyright 2014 - 2023 Blazebit.
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

package com.blazebit.persistence.integration.hibernate;

import com.blazebit.apt.service.ServiceProvider;
import org.hibernate.annotations.common.reflection.ReflectionManager;
import org.hibernate.annotations.common.reflection.XClass;
import org.hibernate.boot.internal.MetadataBuildingContextRootImpl;
import org.hibernate.boot.spi.InFlightMetadataCollector;
import org.hibernate.boot.spi.MetadataBuildingContext;
import org.hibernate.boot.spi.MetadataContributor;
import org.hibernate.cfg.AnnotationBinder;
import org.hibernate.cfg.InheritanceState;
import org.jboss.jandex.IndexView;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.6.7
 */
@ServiceProvider(MetadataContributor.class)
public class Hibernate60MetadataContributor implements MetadataContributor {

    @Override
    public void contribute(InFlightMetadataCollector metadataCollector, IndexView jandexIndex) {
        // Skip if already registered
        if (metadataCollector.getEntityBinding("com.blazebit.persistence.impl.function.entity.ValuesEntity") != null) {
            return;
        }

        MetadataBuildingContext metadataBuildingContext = new MetadataBuildingContextRootImpl(
                "blaze-persistence",
                metadataCollector.getBootstrapContext(),
                metadataCollector.getMetadataBuildingOptions(),
                metadataCollector);

        addEntity("com.blazebit.persistence.impl.function.entity.ValuesEntity", metadataBuildingContext);
    }

    private void addEntity(String className, MetadataBuildingContext metadataBuildingContext) {
        try {
            ReflectionManager reflectionManager = metadataBuildingContext.getBootstrapContext().getReflectionManager();
            XClass clazz = reflectionManager.toXClass(Class.forName(className));
            Map<XClass, InheritanceState> inheritanceStatePerClass = new HashMap<>(1);

            InheritanceState state = new InheritanceState(clazz, inheritanceStatePerClass, metadataBuildingContext);
            inheritanceStatePerClass.put(clazz, state);

            AnnotationBinder.bindClass(clazz, inheritanceStatePerClass, metadataBuildingContext);
        } catch (Exception ex) {
            throw new RuntimeException("Could not add entity", ex);
        }
    }
}
