/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.deltaspike.data.impl.handler;

import com.blazebit.persistence.deltaspike.data.base.handler.EntityViewContext;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import java.util.Stack;

/**
 * Implementation is similar to {@link org.apache.deltaspike.data.impl.handler.CdiQueryContextHolder} but was modified to
 * work with entity views.
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
@ApplicationScoped
public class EntityViewCdiQueryInvocationContextHolder {

    private final ThreadLocal<Stack<EntityViewCdiQueryInvocationContext>> contextStack = new ThreadLocal<>();

    public void set(EntityViewCdiQueryInvocationContext context) {
        if (contextStack.get() == null) {
            contextStack.set(new Stack<EntityViewCdiQueryInvocationContext>());
        }
        contextStack.get().push(context);
    }

    @Produces
    @EntityViewContext
    public EntityViewCdiQueryInvocationContext get() {
        if (contextStack.get() != null && !contextStack.get().isEmpty()) {
            return contextStack.get().peek();
        }
        return null;
    }

    public void dispose() {
        if (contextStack.get() != null && !contextStack.get().isEmpty()) {
            EntityViewCdiQueryInvocationContext ctx = contextStack.get().pop();
            ctx.cleanup();
        }
        if (contextStack.get() != null && contextStack.get().isEmpty()) {
            contextStack.remove();
        }
    }
}