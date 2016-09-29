package com.blazebit.persistence.impl.hibernate;

import org.hibernate.annotations.common.reflection.XClass;
import org.hibernate.boot.internal.ClassLoaderAccessImpl;
import org.hibernate.boot.internal.MetadataBuildingContextRootImpl;
import org.hibernate.boot.registry.classloading.spi.ClassLoaderService;
import org.hibernate.boot.spi.*;
import org.hibernate.cfg.AnnotationBinder;
import org.hibernate.cfg.InheritanceState;
import org.hibernate.cfg.annotations.EntityBinder;
import org.hibernate.mapping.RootClass;
import org.jboss.jandex.IndexView;

import javax.persistence.Entity;
import java.util.HashMap;
import java.util.Map;

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
        XClass clazz = metadataBuildingContext.getBuildingOptions().getReflectionManager().classForName(className);
        Map<XClass, InheritanceState> inheritanceStatePerClass = new HashMap<XClass, InheritanceState>(1);
        InheritanceState state = new InheritanceState( clazz, inheritanceStatePerClass, metadataBuildingContext );
        inheritanceStatePerClass.put( clazz, state );
        AnnotationBinder.bindClass( clazz, inheritanceStatePerClass, metadataBuildingContext );
    }
}
