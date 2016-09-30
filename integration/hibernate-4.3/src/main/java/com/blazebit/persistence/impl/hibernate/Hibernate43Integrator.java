package com.blazebit.persistence.impl.hibernate;

import com.blazebit.apt.service.ServiceProvider;
import com.blazebit.persistence.CTE;
import org.hibernate.boot.registry.StandardServiceInitiator;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.integrator.spi.ServiceContributingIntegrator;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.metamodel.source.MetadataImplementor;
import org.hibernate.persister.spi.PersisterClassResolver;
import org.hibernate.service.Service;
import org.hibernate.service.spi.ServiceRegistryImplementor;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;

import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

@ServiceProvider(Integrator.class)
public class Hibernate43Integrator implements ServiceContributingIntegrator {

	private static final Logger LOG = Logger.getLogger(Hibernate43Integrator.class.getName());

	@Override
	public void prepareServices(StandardServiceRegistryBuilder serviceRegistryBuilder) {
		serviceRegistryBuilder.addInitiator(new StandardServiceInitiator() {
			@Override
			public Service initiateService(Map configurationValues, ServiceRegistryImplementor registry) {
				return null;
			}

			@Override
			public Class getServiceInitiated() {
				return Database.class;
			}
		});
	}

	@Override
	public void integrate(Configuration configuration, SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {
		Class<?> valuesEntity;
		boolean registerValuesEntity = true;
		try {
			valuesEntity = Class.forName("com.blazebit.persistence.impl.function.entity.ValuesEntity");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Are you missing blaze-persistence-core-impl on the classpath?", e);
		}

		Iterator<PersistentClass> iter = configuration.getClassMappings();
		while (iter.hasNext()) {
			PersistentClass clazz = iter.next();
			Class<?> entityClass = clazz.getMappedClass();
			
			if (entityClass.isAnnotationPresent(CTE.class)) {
				clazz.getTable().setSubselect("select * from " + clazz.getJpaEntityName());
				// TODO: check that no collections are mapped
			}
		}

		if (registerValuesEntity) {
			// Register values entity if wasn't found
			configuration.addAnnotatedClass(valuesEntity);
			configuration.buildMappings();
			PersistentClass clazz = configuration.getClassMapping(valuesEntity.getName());
			clazz.getTable().setSubselect("select * from " + clazz.getJpaEntityName());
		}

		serviceRegistry.locateServiceBinding(PersisterClassResolver.class).setService(new CustomPersisterClassResolver());
		serviceRegistry.locateServiceBinding(Database.class).setService(new SimpleDatabase(configuration.getTableMappings()));
	}

	@Override
	public void integrate(MetadataImplementor metadata, SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {
	}

	@Override
	public void disintegrate(SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {
	}

}
