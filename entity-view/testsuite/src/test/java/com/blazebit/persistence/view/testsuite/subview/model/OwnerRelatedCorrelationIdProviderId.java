package com.blazebit.persistence.view.testsuite.subview.model;

import com.blazebit.persistence.BaseQueryBuilder;
import com.blazebit.persistence.view.CorrelationBuilder;
import com.blazebit.persistence.view.CorrelationProvider;
import com.blazebit.persistence.view.testsuite.entity.Document;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class OwnerRelatedCorrelationIdProviderId implements CorrelationProvider {

    @Override
    public void applyCorrelation(CorrelationBuilder correlationBuilder, String correlationExpression) {
        BaseQueryBuilder<?, ?> queryBuilder = correlationBuilder.correlate(Document.class, "correlatedDocumentForId")
                .on("correlatedDocumentForId.owner.id").inExpressions(correlationExpression)
                .on("correlatedDocumentForId.id").notInExpressions("VIEW_ROOT(id)")
                .end();

        // TODO: I think it would be better if we do not allow WHERE at all
        // Only join, from, and parameter handling make sense I think
//        queryBuilder.where("correlatedDocumentForId").notEqExpression("VIEW_ROOT()");
//        queryBuilder.orderByAsc("correlatedDocumentForId.id");
    }
}