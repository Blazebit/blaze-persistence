/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.cte.model;

import com.blazebit.persistence.CTEBuilder;
import com.blazebit.persistence.FullSelectCTECriteriaBuilder;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.CTEProvider;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.FetchStrategy;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.MappingCorrelatedSimple;
import com.blazebit.persistence.view.With;
import com.blazebit.persistence.view.testsuite.cte.model.DocumentWithCTE.OwnersCTEProvider;

import java.util.Map;

@With(OwnersCTEProvider.class)
@EntityView(Document.class)
public interface DocumentWithCTE {

    @IdMapping
    Long getId();

    String getName();

    @MappingCorrelatedSimple(correlated = DocumentOwnersCTE.class,
            correlationBasis = "owner.id",
            correlationExpression = "id = correlationKey",
            correlationResult = "documentCount",
            fetch = FetchStrategy.JOIN)
    Long getOwnedDocumentCount();

    class OwnersCTEProvider implements CTEProvider {

        @Override
        public void applyCtes(CTEBuilder<?> builder, Map<String, Object> optionalParameters) {
            Long ownerMaxAge = (Long) optionalParameters.get("ownerMaxAge");
            FullSelectCTECriteriaBuilder<?> startedBuilder = builder.with(DocumentOwnersCTE.class)
                .from(Person.class, "p")
                .bind("id").select("p.id")
                .bind("documentCount").select("size(p.ownedDocuments)");

            if (ownerMaxAge != null) {
                startedBuilder.where("p.age").le(ownerMaxAge);
            }

            startedBuilder.end();
        }
    }
}
