package com.blazebit.persistence.impl.hibernate;

import org.hibernate.annotations.common.reflection.XClass;
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

import java.util.HashMap;
import java.util.Map;

public class Hibernate60MetadataContributor implements MetadataContributor {

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
