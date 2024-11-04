/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spring.data.base.repository;

import org.springframework.data.jpa.repository.EntityGraph;

import javax.persistence.LockModeType;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Variant that is aware of entity views.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface EntityViewAwareCrudMethodMetadata {

    LockModeType getLockModeType();

    Map<String, Object> getQueryHints();

    EntityGraph getEntityGraph();

    Method getMethod();

    Class<?> getEntityViewClass();
}
