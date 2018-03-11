/*
 * Copyright 2014 - 2018 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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