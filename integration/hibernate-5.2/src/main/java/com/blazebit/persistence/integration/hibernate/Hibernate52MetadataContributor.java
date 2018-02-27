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

package com.blazebit.persistence.integration.hibernate;

import org.hibernate.boot.internal.ClassLoaderAccessImpl;
import org.hibernate.boot.internal.MetadataBuildingContextRootImpl;
import org.hibernate.boot.registry.classloading.spi.ClassLoaderService;
import org.hibernate.boot.spi.ClassLoaderAccess;
import org.hibernate.boot.spi.InFlightMetadataCollector;
import org.hibernate.boot.spi.MetadataBuildingContext;
import org.hibernate.boot.spi.MetadataBuildingOptions;
import org.hibernate.boot.spi.MetadataContributor;
import org.hibernate.cfg.AnnotationBinder;
import org.hibernate.cfg.InheritanceState;
import org.jboss.jandex.IndexView;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class Hibernate52MetadataContributor implements MetadataContributor {

    @Override
    public void contribute(InFlightMetadataCollector metadataCollector, IndexView jandexIndex) {
        // Skip if already registered
        if (metadataCollector.getEntityBinding("com.blazebit.persistence.impl.function.entity.ValuesEntity") != null) {
            return;
        }

        MetadataBuildingOptions options = metadataCollector.getMetadataBuildingOptions();
        final ClassLoaderService classLoaderService = options.getServiceRegistry().getService( ClassLoaderService.class );

        final ClassLoaderAccess classLoaderAccess = new ClassLoaderAccessImpl(
                options.getTempClassLoader(),
                classLoaderService
        );

        MetadataBuildingContext metadataBuildingContext = new MetadataBuildingContextRootImpl(
                options,
                classLoaderAccess,
                metadataCollector);

        addEntity("com.blazebit.persistence.impl.function.entity.ValuesEntity", metadataBuildingContext);
    }

    private void addEntity(String className, MetadataBuildingContext metadataBuildingContext) {
        try {
            MetadataBuildingOptions options = metadataBuildingContext.getBuildingOptions();
            Object /*ReflectionManager*/ reflectionManager = MetadataBuildingOptions.class.getMethod("getReflectionManager").invoke(options);
            //            Object /*XClass*/ clazz = reflectionManager.classForName(className);
            Method classForName = reflectionManager.getClass().getMethod("classForName", String.class);
            Object /*XClass*/ clazz = classForName.invoke(reflectionManager, className);
            Map<Object /*XClass*/, InheritanceState> inheritanceStatePerClass = new HashMap<Object /*XClass*/, InheritanceState>(1);

            //        InheritanceState state = new InheritanceState(clazz, inheritanceStatePerClass, metadataBuildingContext);
            InheritanceState state = InheritanceState.class.getConstructor(classForName.getReturnType(), Map.class, MetadataBuildingContext.class)
                    .newInstance(clazz, inheritanceStatePerClass, metadataBuildingContext);

            inheritanceStatePerClass.put(clazz, state);

            //        AnnotationBinder.bindClass(clazz, inheritanceStatePerClass, metadataBuildingContext);
            AnnotationBinder.class.getMethod("bindClass", classForName.getReturnType(), Map.class, MetadataBuildingContext.class)
                    .invoke(null, clazz, inheritanceStatePerClass, metadataBuildingContext);
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException("Could not add entity", ex);
        }
    }
}
