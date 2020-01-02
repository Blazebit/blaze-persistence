/*
 * Copyright 2014 - 2020 Blazebit.
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

import javax.persistence.EntityManager;

/**
 * A listener for getting a callback after rolling back a flush for an entity view.
 *
 * @param <T> The view type
 * @author Christian Beikov
 * @since 1.4.0
 */
public interface PostRollbackListener<T> {

    /**
     * A callback that is invoked after the flush for given view was rolled back.
     *
     * @param entityViewManager The entity view manager
     * @param entityManager The entity manager
     * @param view The view for which the flush rolled back
     * @param transition The view transition
     */
    public void postRollback(EntityViewManager entityViewManager, EntityManager entityManager, T view, ViewTransition transition);
}
