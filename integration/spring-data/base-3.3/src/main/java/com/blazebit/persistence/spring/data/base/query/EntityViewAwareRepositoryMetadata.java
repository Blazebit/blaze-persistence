/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spring.data.base.query;

import com.blazebit.persistence.view.EntityViewManager;
import org.springframework.data.repository.core.RepositoryMetadata;

import java.lang.reflect.Method;

/**
 * @author Christian Beikov
 * @author Eugen Mayer
 * @since 1.6.9
 */
public interface EntityViewAwareRepositoryMetadata extends RepositoryMetadata {

    public EntityViewManager getEntityViewManager();

    public Class<?> getEntityViewType();

    public Class<?> getReturnedEntityViewClass(Method method);

}
