/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.deltaspike.data.impl.tx;

import com.blazebit.persistence.deltaspike.data.impl.handler.EntityViewCdiQueryInvocationContext;
import org.apache.deltaspike.core.util.interceptor.AbstractInvocationContext;

/**
 * Implementation is similar to {@link org.apache.deltaspike.data.impl.tx.InvocationContextWrapper} but was modified to
 * work with entity views.
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
public abstract class EntityViewInvocationContextWrapper extends AbstractInvocationContext<Object> {
    public EntityViewInvocationContextWrapper(EntityViewCdiQueryInvocationContext context) {
        super(context.getProxy(), context.getMethod(), context.getMethodParameters(), null);
    }
}