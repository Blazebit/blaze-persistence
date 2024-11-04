/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spring.data.testsuite.webmvc.accessor;

import java.util.UUID;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
public interface DocumentAccessor {

    Long getId();

    String getName();

    String getDescription();

    long getAge();

}
