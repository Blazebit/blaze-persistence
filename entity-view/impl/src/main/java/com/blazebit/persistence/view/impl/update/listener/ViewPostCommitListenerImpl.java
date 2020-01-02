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

package com.blazebit.persistence.view.impl.update.listener;

import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.PostCommitListener;
import com.blazebit.persistence.view.ViewListener;
import com.blazebit.persistence.view.ViewTransition;

import javax.persistence.EntityManager;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
public class ViewPostCommitListenerImpl<T> implements PostCommitListener<T> {

    private final ViewListener<T> listener;

    public ViewPostCommitListenerImpl(ViewListener<T> listener) {
        this.listener = listener;
    }

    @Override
    public void postCommit(EntityViewManager entityViewManager, EntityManager entityManager, T view, ViewTransition transition) {
        listener.call(view);
    }
}
