package com.blazebit.persistence.impl.datanucleus;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.datanucleus.store.rdbms.query.JPQLQuery;
import org.datanucleus.store.rdbms.query.RDBMSQueryCompilation;

import com.blazebit.apt.service.ServiceProvider;
import com.blazebit.persistence.spi.ExtendedQuerySupport;

@ServiceProvider(ExtendedQuerySupport.class)
public class DataNucleusExtendedQuerySupport implements ExtendedQuerySupport {
	
	private static final Field datastoreCompilationField;
	
	static {
		try {
			datastoreCompilationField = JPQLQuery.class.getDeclaredField("datastoreCompilation");
			datastoreCompilationField.setAccessible(true);
		} catch (Exception e) {
			throw new RuntimeException("Unsupported datanucleus version!", e);
		}
	}

	@Override
	public String getSql(EntityManager em, Query query) {
		org.datanucleus.store.query.Query<?> dnQuery = query.unwrap(org.datanucleus.store.query.Query.class);
		dnQuery.compile();
		return (String) dnQuery.getNativeQuery();
	}

	@Override
    public Connection getConnection(EntityManager em) {
		return em.unwrap(Connection.class);
    }

	@Override
	public <T> List<T> getResultList(EntityManager em, TypedQuery<T> query, String sqlOverride) {
		applySql(query, sqlOverride);
		return query.getResultList();
	}
	
	@Override
	public <T> T getSingleResult(EntityManager em, TypedQuery<T> query, String sqlOverride) {
		applySql(query, sqlOverride);
		return query.getSingleResult();
	}
	
	private void applySql(TypedQuery<?> query, String sqlOverride) {
		org.datanucleus.store.query.Query<?> dnQuery = query.unwrap(org.datanucleus.store.query.Query.class);
		// Disable caching for these queries
		dnQuery.addExtension("datanucleus.query.compilation.cached", Boolean.FALSE);
		try {
			RDBMSQueryCompilation datastoreCompilation = (RDBMSQueryCompilation) datastoreCompilationField.get(dnQuery);
			datastoreCompilation.setSQL(sqlOverride);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
