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

package com.blazebit.persistence.view;

/**
 * Thrown when an optimistic lock conflict has been detected.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class OptimisticLockException extends javax.persistence.OptimisticLockException {

    /**
     * The entity view object that caused the exception.
     */
    private final Object entityView;

    /**
     * Constructs a new <code>OptimisticLockException</code> with given entity and entity view objects.
     *
     * @param entity The entity that caused the exception
     * @param entityView The entity view that caused the exception
     */
    public OptimisticLockException(Object entity, Object entityView) {
        super(null, null, entity);
        this.entityView = entityView;
    }

    /**
     * Returns the entity view object that caused this exception.
     *
     * @return The entity view
     */
    public Object getEntityView() {
        return entityView;
    }
}
