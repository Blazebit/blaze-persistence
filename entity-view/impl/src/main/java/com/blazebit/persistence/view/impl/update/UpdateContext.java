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

package com.blazebit.persistence.view.impl.update;

import com.blazebit.persistence.view.impl.EntityViewManagerImpl;
import com.blazebit.persistence.view.impl.tx.TransactionSynchronizationStrategy;
import com.blazebit.persistence.view.impl.update.flush.PostFlushDeleter;

import javax.persistence.EntityManager;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface UpdateContext {

    public EntityViewManagerImpl getEntityViewManager();

    public EntityManager getEntityManager();

    public boolean containsEntity(Class<?> entityClass, Object id);

    public boolean isForceFull();

    public boolean addVersionCheck(Class<?> entityClass, Object id);

    public boolean addRemovedObject(Object value);

    public boolean isRemovedObject(Object value);

    public TransactionSynchronizationStrategy getSynchronizationStrategy();

    public InitialStateResetter getInitialStateResetter();

    public List<PostFlushDeleter> getOrphanRemovalDeleters();

    public void removeOrphans(int orphanRemovalStartIndex);
}
