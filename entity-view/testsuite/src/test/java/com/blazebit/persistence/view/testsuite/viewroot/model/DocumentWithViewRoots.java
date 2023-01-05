/*
 * Copyright 2014 - 2023 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
