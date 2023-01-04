package com.blazebit.persistence.integration.graphql.views;

import com.blazebit.persistence.integration.graphql.entities.Document;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;

/**
 * @author Moritz Becker
 * @since 1.6.4
 */
@EntityView(Document.class)
public interface DocumentView {

    @IdMapping
    Long getId();

    String getName();

    PersonView getOwner();
}
