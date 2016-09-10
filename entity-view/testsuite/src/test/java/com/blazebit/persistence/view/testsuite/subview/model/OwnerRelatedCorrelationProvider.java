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
public class OwnerRelatedCorrelationProvider implements CorrelationProvider {

    @Override
    public void applyCorrelation(CorrelationBuilder correlationBuilder, String correlationExpression) {
        BaseQueryBuilder<?, ?> queryBuilder = correlationBuilder.correlate(Document.class, "correlatedDocumentForSubview")
                .onExpressionSubqueries("alias IN " + correlationExpression + " AND correlatedDocumentForSubview != VIEW_ROOT()")
                    .with("alias")
                        .from(Document.class, "o")
                        .select("o.owner.id")
                        .where("o.id").eqExpression("correlatedDocumentForSubview.id")
                    .end()
                // Workaround for HHH-2772
//                .on("correlatedDocumentForSubview.owner").inExpressions(correlationExpression)
                .end();
        // TODO: I think it would be better if we do not allow WHERE at all
        // Only join, from, and parameter handling make sense I think
//        queryBuilder.where("correlatedDocumentForSubview").notEqExpression("VIEW_ROOT()");
//        queryBuilder.orderByAsc("correlatedDocumentForSubview.id");
    }
}