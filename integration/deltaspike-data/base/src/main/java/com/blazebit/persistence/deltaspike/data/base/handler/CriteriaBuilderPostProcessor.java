/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.deltaspike.data.base.handler;

import com.blazebit.persistence.FullQueryBuilder;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
public interface CriteriaBuilderPostProcessor {

    FullQueryBuilder<?, ?> postProcess(FullQueryBuilder<?, ?> queryString);
}
