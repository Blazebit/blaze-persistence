package com.blazebit.persistence.impl.hibernate;

import com.blazebit.apt.service.ServiceProvider;
import com.blazebit.persistence.CTE;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.integrator.spi.ServiceContributingIntegrator;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.metamodel.source.MetadataImplementor;
import org.hibernate.persister.spi.PersisterClassResolver;
import org.hibernate.service.Service;
import org.hibernate.service.ServiceRegistryBuilder;
import org.hibernate.service.spi.BasicServiceInitiator;
import org.hibernate.service.spi.ServiceRegistryImplementor;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;

import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

@ServiceProvider(Integrator.class)
public class Hibernate4Integrator implements ServiceContributingIntegrator {

	private static final Logger LOG = Logger.getLogger(Hibernate4Integrator.class.getName());

	@Override
	public void prepareServices(ServiceRegistryBuilder serviceRegistryBuilder) {
		serviceRegistryBuilder.addInitiator(new BasicServiceInitiator() {
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
		try {
			configuration.addAnnotatedClass(Class.forName("com.blazebit.persistence.impl.function.entity.ValuesEntity"));
			configuration.buildMappings();
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
