/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spring.data.base.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Variant that is aware of entity views.
 *
 * @author Christian Beikov
 * @author Eugen Mayer
 * @since 1.6.9
 */
public interface EntityViewAwareCrudMethodMetadata {

    LockModeType getLockModeType();

    Map<String, Object> getQueryHints();

    EntityGraph getEntityGraph();

    Method getMethod();

    Class<?> getEntityViewClass();
}
