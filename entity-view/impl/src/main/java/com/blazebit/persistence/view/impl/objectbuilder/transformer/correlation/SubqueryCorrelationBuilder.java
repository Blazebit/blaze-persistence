package com.blazebit.persistence.view.impl.objectbuilder.transformer.correlation;

import com.blazebit.persistence.BaseQueryBuilder;
import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.JoinOnBuilder;
import com.blazebit.persistence.view.CorrelationBuilder;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class SubqueryCorrelationBuilder implements CorrelationBuilder {

    private final CriteriaBuilder<?> criteriaBuilder;
    private final String correlationResult;
    private String correlationRoot;

    public SubqueryCorrelationBuilder(CriteriaBuilder<?> criteriaBuilder, String correlationResult) {
        this.criteriaBuilder = criteriaBuilder;
        this.correlationResult = correlationResult;
    }

    public String getCorrelationRoot() {
        return correlationRoot;
    }

    @Override
    public JoinOnBuilder<BaseQueryBuilder<?, ?>> correlate(Class<?> entityClass, String alias) {
        criteriaBuilder.from(entityClass, alias);

        String correlationRoot;
        if (correlationResult.isEmpty()) {
            correlationRoot = alias;
        } else if (correlationResult.startsWith(alias) && (correlationResult.length() == alias.length() || correlationResult.charAt(alias.length()) == '.')) {
            correlationRoot = correlationResult;
        } else {
            correlationRoot = alias + '.' + correlationResult;
        }
        this.correlationRoot = correlationRoot;
        return criteriaBuilder.getService(JoinOnBuilder.class);
    }

}
