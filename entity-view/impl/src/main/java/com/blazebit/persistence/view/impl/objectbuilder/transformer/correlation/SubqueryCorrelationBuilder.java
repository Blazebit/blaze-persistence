package com.blazebit.persistence.view.impl.objectbuilder.transformer.correlation;

import com.blazebit.persistence.BaseQueryBuilder;
import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.JoinOnBuilder;
import com.blazebit.persistence.view.CorrelationBuilder;
import com.blazebit.persistence.view.impl.EntityViewConfiguration;

import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class SubqueryCorrelationBuilder implements CorrelationBuilder {

    private final CriteriaBuilder<?> criteriaBuilder;
    private final Map<String, Object> optionalParameters;
    private final EntityViewConfiguration entityViewConfiguration;
    private final AbstractCorrelatedBatchTupleListTransformerFactory<?> finisher;
    private final int batchSize;
    private final String correlationResult;

    public SubqueryCorrelationBuilder(CriteriaBuilder<?> criteriaBuilder, Map<String, Object> optionalParameters, EntityViewConfiguration entityViewConfiguration, AbstractCorrelatedBatchTupleListTransformerFactory<?> finisher, int batchSize, String correlationResult) {
        this.criteriaBuilder = criteriaBuilder;
        this.optionalParameters = optionalParameters;
        this.entityViewConfiguration = entityViewConfiguration;
        this.finisher = finisher;
        this.batchSize = batchSize;
        this.correlationResult = correlationResult;
    }

    @Override
    public JoinOnBuilder<BaseQueryBuilder<?, ?>> correlate(Class<?> entityClass, String alias) {
        if (batchSize > 1) {
            // TODO: This requires table generating functions
            throw new UnsupportedOperationException("Not yet implemented!");
        } else {
            criteriaBuilder.from(entityClass, alias);
        }

        String entityViewRoot;
        if (correlationResult.isEmpty()) {
            entityViewRoot = alias;
        } else if (correlationResult.startsWith(alias) && (correlationResult.length() == alias.length() || correlationResult.charAt(alias.length()) == '.')) {
            entityViewRoot = correlationResult;
        } else {
            entityViewRoot = alias + '.' + correlationResult;
        }
        finisher.finishCriteriaBuilder(criteriaBuilder, optionalParameters, entityViewConfiguration, batchSize, entityViewRoot);
        return criteriaBuilder.getService(JoinOnBuilder.class);
    }

}
