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
public class OwnerRelatedCorrelationIdProvider implements CorrelationProvider {

    @Override
    public void applyCorrelation(CorrelationBuilder correlationBuilder, String correlationExpression) {
        BaseQueryBuilder<?, ?> queryBuilder = correlationBuilder.correlate(Document.class, "correlatedDocumentForId")
                .onExpressionSubqueries("alias IN " + correlationExpression + " AND correlatedDocumentForId != VIEW_ROOT()")
                    .with("alias")
                        .from(Document.class, "o")
                        .select("o.owner.id")
                        .where("o.id").eqExpression("correlatedDocumentForId.id")
                    .end()
                // Workaround for HHH-2772
//                .on("correlatedDocumentForId.owner").inExpressions(correlationExpression)
                .end();
//        queryBuilder.where("correlatedDocumentForId").notEqExpression("VIEW_ROOT()");
//        queryBuilder.orderByAsc("correlatedDocumentForId.id");
    }
}