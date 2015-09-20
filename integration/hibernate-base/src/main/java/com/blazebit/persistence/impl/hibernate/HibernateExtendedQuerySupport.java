package com.blazebit.persistence.impl.hibernate;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.hibernate.HibernateException;
import org.hibernate.TypeMismatchException;
import org.hibernate.ejb.HibernateEntityManagerImplementor;
import org.hibernate.engine.query.spi.HQLQueryPlan;
import org.hibernate.engine.spi.QueryParameters;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.hql.internal.QueryExecutionRequestException;
import org.hibernate.hql.spi.QueryTranslator;

import com.blazebit.apt.service.ServiceProvider;
import com.blazebit.persistence.spi.ExtendedQuerySupport;
import com.blazebit.reflection.ReflectionUtils;

@ServiceProvider(ExtendedQuerySupport.class)
public class HibernateExtendedQuerySupport implements ExtendedQuerySupport {

	@Override
	public String getSql(EntityManager em, Query query) {
    	SessionImplementor session = em.unwrap(SessionImplementor.class);
		SessionFactoryImplementor sfi = session.getFactory();
		org.hibernate.Query hibernateQuery = query.unwrap(org.hibernate.Query.class);
		hibernateQuery.setResultTransformer(null);
		String queryString = hibernateQuery.getQueryString();
		HQLQueryPlan queryPlan = sfi.getQueryPlanCache().getHQLQueryPlan(queryString, false, Collections.emptyMap());
		String sql = queryPlan.getSqlStrings()[0];
		return sql;
	}

	@Override
    public Connection getConnection(EntityManager em) {
		SessionImplementor session = em.unwrap(SessionImplementor.class);
		return session.connection();
    }

	@Override
	public <T> List<T> getResultList(EntityManager em, TypedQuery<T> query, String sqlOverride) {
		try {
			return list(em, query, sqlOverride);
		} catch (QueryExecutionRequestException he) {
			throw new IllegalStateException(he);
		} catch( TypeMismatchException e ) {
			throw new IllegalArgumentException(e);
		} catch (HibernateException he) {
			throw getEntityManager(em).convert( he );
		}
	}
	
	@Override
	public <T> T getSingleResult(EntityManager em, TypedQuery<T> query, String sqlOverride) {
		try {
			final List<T> result = list(em, query, sqlOverride);

			if ( result.size() == 0 ) {
				NoResultException nre = new NoResultException( "No entity found for query" );
				getEntityManager(em).handlePersistenceException( nre );
				throw nre;
			} else if ( result.size() > 1 ) {
				final Set<T> uniqueResult = new HashSet<T>(result);
				if ( uniqueResult.size() > 1 ) {
					NonUniqueResultException nure = new NonUniqueResultException( "result returns more than one elements" );
					getEntityManager(em).handlePersistenceException( nure );
					throw nure;
				} else {
					return uniqueResult.iterator().next();
				}
			} else {
				return result.get(0);
			}
		} catch (QueryExecutionRequestException he) {
			throw new IllegalStateException(he);
		} catch( TypeMismatchException e ) {
			throw new IllegalArgumentException(e);
		} catch (HibernateException he) {
			throw getEntityManager(em).convert( he );
		}
	}
	
	private HibernateEntityManagerImplementor getEntityManager(EntityManager em) {
		return (HibernateEntityManagerImplementor) em.unwrap(EntityManager.class);
	}

	@SuppressWarnings("unchecked")
	private <T> List<T> list(EntityManager em, TypedQuery<T> query, String sqlOverride) {
    	SessionImplementor session = em.unwrap(SessionImplementor.class);
		SessionFactoryImplementor sfi = session.getFactory();
		org.hibernate.Query hibernateQuery = query.unwrap(org.hibernate.Query.class);
		hibernateQuery.setResultTransformer(null);
		String queryString = hibernateQuery.getQueryString();
		
		// TODO: use custom cache because this won't work
		HQLQueryPlan queryPlan = sfi.getQueryPlanCache().getHQLQueryPlan(queryString, false, Collections.emptyMap());
		QueryTranslator translator = queryPlan.getTranslators()[0];

		QueryParameters queryParameters;

		try {
			// Override the sql in the query plan
			Field sqlField = translator.getClass().getDeclaredField("sql");
			sqlField.setAccessible(true);
			sqlField.set(translator, sqlOverride);
			
			// Extract query parameters
			Method getNamedParams = ReflectionUtils.getGetter(hibernateQuery.getClass(), "namedParams");
			getNamedParams.setAccessible(true);
			Map<?, ?> namedParams = (Map<?, ?>) getNamedParams.invoke(hibernateQuery);
			queryParameters = ((org.hibernate.internal.AbstractQueryImpl) hibernateQuery).getQueryParameters(namedParams);
		} catch (Exception e1) {
			throw new RuntimeException(e1);
		}

		return queryPlan.performList(queryParameters, session);
	}

}
