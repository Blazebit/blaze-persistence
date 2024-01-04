/*
 * Copyright 2014 - 2024 Blazebit.
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

package com.blazebit.persistence.view.impl.update.listener;

import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.PostPersistEntityListener;

import javax.persistence.EntityManager;
import java.lang.reflect.Method;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
public class ViewInstancePostPersistEntityListener extends ViewInstanceEntityListener implements PostPersistEntityListener<Object, Object> {

    public ViewInstancePostPersistEntityListener(Method listener) {
        super(listener);
    }

    @Override
    public void postPersist(EntityViewManager entityViewManager, EntityManager entityManager, Object view, Object entity) {
        invoke(entityViewManager, entityManager, view, entity);
    }
}
