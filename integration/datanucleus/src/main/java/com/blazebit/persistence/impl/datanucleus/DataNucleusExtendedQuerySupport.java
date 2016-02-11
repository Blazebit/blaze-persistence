package com.blazebit.persistence.impl.datanucleus;

import java.lang.reflect.Field;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.metamodel.EntityType;

import org.datanucleus.store.rdbms.query.JPQLQuery;
import org.datanucleus.store.rdbms.query.RDBMSQueryCompilation;

import com.blazebit.apt.service.ServiceProvider;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.ReturningResult;
import com.blazebit.persistence.spi.DbmsDialect;
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
	
	public List<String> getCascadingDeleteSql(EntityManager em, Query query) {
        // TODO: implement
        throw new UnsupportedOperationException("Not yet implemeneted!");
	}

	@Override
    public String[] getColumnNames(EntityManager em, EntityType<?> entityType, String attributeName) {
        // TODO: implement
        throw new UnsupportedOperationException("Not yet implemeneted!");
    }

    @Override
	public int getSqlSelectAliasPosition(EntityManager em, Query query, String alias) {
        // TODO: implement
        throw new UnsupportedOperationException("Not yet implemeneted!");
	}

    @Override
    public String getSqlAlias(EntityManager em, Query query, String alias) {
        // TODO: implement
        throw new UnsupportedOperationException("Not yet implemeneted!");
    }

    @Override
    public int getSqlSelectAttributePosition(EntityManager em, Query query, String alias) {
        // TODO: implement
        throw new UnsupportedOperationException("Not yet implemeneted!");
    }

    @Override
    @SuppressWarnings("rawtypes")
	public List getResultList(CriteriaBuilderFactory cbf, DbmsDialect dbmsDialect, EntityManager em, List<Query> participatingQueries, Query query, String sqlOverride) {
		applySql(query, sqlOverride);
		return query.getResultList();
	}
	
	@Override
	public Object getSingleResult(CriteriaBuilderFactory cbf, DbmsDialect dbmsDialect, EntityManager em, List<Query> participatingQueries, Query query, String sqlOverride) {
		applySql(query, sqlOverride);
		return query.getSingleResult();
	}

    @Override
    public int executeUpdate(CriteriaBuilderFactory cbf, DbmsDialect dbmsDialect, EntityManager em, List<Query> participatingQueries, Query query, String sqlOverride) {
        applySql(query, sqlOverride);
        return query.executeUpdate();
    }

    @Override
    public ReturningResult<Object[]> executeReturning(CriteriaBuilderFactory cbf, DbmsDialect dbmsDialect, EntityManager em, List<Query> participatingQueries, Query exampleQuery, String sqlOverride) {
        // TODO: implement
        throw new UnsupportedOperationException("Not yet implemeneted!");
    }
	
	private void applySql(Query query, String sqlOverride) {
	    // TODO: parameter handling
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
