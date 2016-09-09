package com.blazebit.persistence.view.testsuite.subview.model;

import com.blazebit.persistence.BaseQueryBuilder;
import com.blazebit.persistence.view.CorrelationProvider;
import com.blazebit.persistence.view.testsuite.entity.Document;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class OwnerRelatedIdCorrelationProvider implements CorrelationProvider {

    @Override
    public String applyCorrelation(BaseQueryBuilder<?, ?> queryBuilder, String correlationExpression) {
        queryBuilder.from(Document.class, "correlatedDocument");
        queryBuilder.where("correlatedDocument.owner").inExpressions(correlationExpression);
        queryBuilder.where("correlatedDocument").notEqExpression("VIEW_ROOT()");
        queryBuilder.select("correlatedDocument.id");
        queryBuilder.orderByAsc("correlatedDocument.id");
        return "correlatedDocument";
    }
}