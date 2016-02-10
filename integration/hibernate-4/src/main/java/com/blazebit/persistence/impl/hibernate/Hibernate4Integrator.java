package com.blazebit.persistence.impl.hibernate;

import java.util.Iterator;

import org.hibernate.cfg.Configuration;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.metamodel.source.MetadataImplementor;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;

import com.blazebit.apt.service.ServiceProvider;
import com.blazebit.persistence.CTE;

@ServiceProvider(Integrator.class)
public class Hibernate4Integrator implements Integrator {

	@Override
	public void integrate(Configuration configuration, SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {
		Iterator<PersistentClass> iter = configuration.getClassMappings();
		while (iter.hasNext()) {
			PersistentClass clazz = iter.next();
			Class<?> entityClass = clazz.getMappedClass();
			
			if (entityClass.isAnnotationPresent(CTE.class)) {
				clazz.getTable().setSubselect("select * from " + clazz.getJpaEntityName());
				// TODO: check that no collections are mapped
			}
		}
	}

	@Override
	public void integrate(MetadataImplementor metadata, SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {
	}

	@Override
	public void disintegrate(SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {
	}

}
