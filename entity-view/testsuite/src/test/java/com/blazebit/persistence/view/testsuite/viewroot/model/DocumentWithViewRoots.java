/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.viewroot.model;

import com.blazebit.persistence.JoinType;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.view.CorrelationBuilder;
import com.blazebit.persistence.view.CorrelationProvider;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.EntityViewRoot;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;

@EntityView(Document.class)
@EntityViewRoot(name = "v1", entity = Document.class, condition = "id = VIEW(id)", joinType = JoinType.INNER)
@EntityViewRoot(name = "v2", expression = "Document[id = VIEW(id)]", limit = "1", order = "id DESC")
@EntityViewRoot(name = "v3", correlator = DocumentWithViewRoots.TestCorrelator.class)
public interface DocumentWithViewRoots {

    @IdMapping
    Long getId();

    String getName();

    @Mapping("v1.name")
    String getV1Name();

    @Mapping("v2.name")
    String getV2Name();

    @Mapping("v3.name")
    String getV3Name();

    @Mapping("this")
    SubView getSubView();

    @EntityView(Document.class)
    @EntityViewRoot(name = "sub1", entity = Document.class, condition = "id = VIEW(id)")
    @EntityViewRoot(name = "sub2", expression = "Document[id = VIEW(id)]")
    @EntityViewRoot(name = "sub3", correlator = DocumentWithViewRoots.TestCorrelator.class)
    interface SubView {

        @Mapping("sub1.name")
        String getV1Name();

        @Mapping("sub2.name")
        String getV2Name();

        @Mapping("sub3.name")
        String getV3Name();
    }

    public static class TestCorrelator implements CorrelationProvider {
        @Override
        public void applyCorrelation(CorrelationBuilder correlationBuilder, String correlationExpression) {
            correlationBuilder.correlate(Document.class)
                    .on(correlationBuilder.getCorrelationAlias()).eqExpression(correlationExpression)
                    .end();
        }
    }
}
