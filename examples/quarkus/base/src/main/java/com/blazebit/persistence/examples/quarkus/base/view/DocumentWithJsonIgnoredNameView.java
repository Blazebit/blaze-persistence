/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.quarkus.base.view;

import com.blazebit.persistence.examples.quarkus.base.entity.Document;
import com.blazebit.persistence.view.EntityView;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
@EntityView(Document.class)
public interface DocumentWithJsonIgnoredNameView extends DocumentView {

    @JsonIgnore
    String getName();
}
