/*
 * Copyright 2014 - 2023 Blazebit.
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

import java.util.Set;

/**
 * A builder for defining flush related configuration.
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
public interface FlushOperationBuilder {

    /**
     * Invoked the flush operation.
     */
    public void flush();

    /**
     * Registers the given listener to the current flush operation.
     *
     * @param listener The listener to register
     * @return This builder for method chaining
     */
    public FlushOperationBuilder onPrePersist(PrePersistListener<?> listener);

    /**
     * Registers the given listener to the current flush operation.
     *
     * @param listener The listener to register
     * @return This builder for method chaining
     */
    public FlushOperationBuilder onPrePersist(PrePersistEntityListener<?, ?> listener);

    /**
     * Registers the given listener to the current flush operation.
     *
     * @param listener The listener to register
     * @return This builder for method chaining
     */
    public FlushOperationBuilder onPostPersist(PostPersistListener<?> listener);

    /**
     * Registers the given listener to the current flush operation.
     *
     * @param listener The listener to register
     * @return This builder for method chaining
     */
    public FlushOperationBuilder onPostPersist(PostPersistEntityListener<?, ?> listener);

    /**
     * Registers the given listener to the current flush operation.
     *
     * @param listener The listener to register
     * @return This builder for method chaining
     */
    public FlushOperationBuilder onPreUpdate(PreUpdateListener<?> listener);

    /**
     * Registers the given listener to the current flush operation.
     *
     * @param listener The listener to register
     * @return This builder for method chaining
     */
    public FlushOperationBuilder onPostUpdate(PostUpdateListener<?> listener);

    /**
     * Registers the given listener to the current flush operation.
     *
     * @param listener The listener to register
     * @return This builder for method chaining
     */
    public FlushOperationBuilder onPreRemove(PreRemoveListener<?> listener);

    /**
     * Registers the given listener to the current flush operation.
     *
     * @param listener The listener to register
     * @return This builder for method chaining
     */
    public FlushOperationBuilder onPostRemove(PostRemoveListener<?> listener);

    /**
     * Registers the given listener to the current flush operation.
     *
     * @param listener The listener to register
     * @return This builder for method chaining
     */
    public FlushOperationBuilder onPostCommit(PostCommitListener<?> listener);

    /**
     * Registers the given listener to the current flush operation for the {@link ViewTransition#PERSIST}.
     *
     * @param listener The listener to register
     * @return This builder for method chaining
     */
    public FlushOperationBuilder onPostCommitPersist(PostCommitListener<?> listener);

    /**
     * Registers the given listener to the current flush operation for the {@link ViewTransition#UPDATE}.
     *
     * @param listener The listener to register
     * @return This builder for method chaining
     */
    public FlushOperationBuilder onPostCommitUpdate(PostCommitListener<?> listener);

    /**
     * Registers the given listener to the current flush operation for the {@link ViewTransition#REMOVE}.
     *
     * @param listener The listener to register
     * @return This builder for method chaining
     */
    public FlushOperationBuilder onPostCommitRemove(PostCommitListener<?> listener);

    /**
     * Registers the given listener to the current flush operation.
     *
     * @param viewTransitions The view transitions
     * @param listener The listener to register
     * @return This builder for method chaining
     */
    public FlushOperationBuilder onPostCommit(Set<ViewTransition> viewTransitions, PostCommitListener<?> listener);

    /**
     * Registers the given listener to the current flush operation.
     *
     * @param listener The listener to register
     * @return This builder for method chaining
     */
    public FlushOperationBuilder onPostRollback(PostRollbackListener<?> listener);

    /**
     * Registers the given listener to the current flush operation for the {@link ViewTransition#PERSIST}.
     *
     * @param listener The listener to register
     * @return This builder for method chaining
     */
    public FlushOperationBuilder onPostRollbackPersist(PostRollbackListener<?> listener);

    /**
     * Registers the given listener to the current flush operation for the {@link ViewTransition#UPDATE}.
     *
     * @param listener The listener to register
     * @return This builder for method chaining
     */
    public FlushOperationBuilder onPostRollbackUpdate(PostRollbackListener<?> listener);

    /**
     * Registers the given listener to the current flush operation for the {@link ViewTransition#REMOVE}.
     *
     * @param listener The listener to register
     * @return This builder for method chaining
     */
    public FlushOperationBuilder onPostRollbackRemove(PostRollbackListener<?> listener);

    /**
     * Registers the given listener to the current flush operation.
     *
     * @param viewTransitions The view transitions
     * @param listener The listener to register
     * @return This builder for method chaining
     */
    public FlushOperationBuilder onPostRollback(Set<ViewTransition> viewTransitions, PostRollbackListener<?> listener);

    /**
     * Registers the given listener to the current flush operation.
     *
     * @param entityViewClass The entity view type for which to register the listener
     * @param listener The listener to register
     * @param <T> The entity view type
     * @return This builder for method chaining
     */
    public <T> FlushOperationBuilder onPrePersist(Class<T> entityViewClass, PrePersistListener<T> listener);

    /**
     * Registers the given listener to the current flush operation.
     *
     * @param entityViewClass The entity view type for which to register the listener
     * @param listener The listener to register
     * @param <T> The entity view type
     * @return This builder for method chaining
     */
    public <T> FlushOperationBuilder onPrePersist(Class<T> entityViewClass, PrePersistEntityListener<T, ?> listener);

    /**
     * Registers the given listener to the current flush operation.
     *
     * @param entityViewClass The entity view type for which to register the listener
     * @param listener The listener to register
     * @param <T> The entity view type
     * @return This builder for method chaining
     */
    public <T> FlushOperationBuilder onPostPersist(Class<T> entityViewClass, PostPersistListener<T> listener);

    /**
     * Registers the given listener to the current flush operation.
     *
     * @param entityViewClass The entity view type for which to register the listener
     * @param listener The listener to register
     * @param <T> The entity view type
     * @return This builder for method chaining
     */
    public <T> FlushOperationBuilder onPostPersist(Class<T> entityViewClass, PostPersistEntityListener<T, ?> listener);

    /**
     * Registers the given listener to the current flush operation.
     *
     * @param entityViewClass The entity view type for which to register the listener
     * @param listener The listener to register
     * @param <T> The entity view type
     * @return This builder for method chaining
     */
    public <T> FlushOperationBuilder onPreUpdate(Class<T> entityViewClass, PreUpdateListener<T> listener);

    /**
     * Registers the given listener to the current flush operation.
     *
     * @param entityViewClass The entity view type for which to register the listener
     * @param listener The listener to register
     * @param <T> The entity view type
     * @return This builder for method chaining
     */
    public <T> FlushOperationBuilder onPostUpdate(Class<T> entityViewClass, PostUpdateListener<T> listener);

    /**
     * Registers the given listener to the current flush operation.
     *
     * @param entityViewClass The entity view type for which to register the listener
     * @param listener The listener to register
     * @param <T> The entity view type
     * @return This builder for method chaining
     */
    public <T> FlushOperationBuilder onPreRemove(Class<T> entityViewClass, PreRemoveListener<T> listener);

    /**
     * Registers the given listener to the current flush operation.
     *
     * @param entityViewClass The entity view type for which to register the listener
     * @param listener The listener to register
     * @param <T> The entity view type
     * @return This builder for method chaining
     */
    public <T> FlushOperationBuilder onPostRemove(Class<T> entityViewClass, PostRemoveListener<T> listener);

    /**
     * Registers the given listener to the current flush operation.
     *
     * @param entityViewClass The entity view type for which to register the listener
     * @param listener The listener to register
     * @param <T> The entity view type
     * @return This builder for method chaining
     */
    public <T> FlushOperationBuilder onPostCommit(Class<T> entityViewClass, PostCommitListener<T> listener);

    /**
     * Registers the given listener to the current flush operation.
     *
     * @param <T> The entity view type
     * @param entityViewClass The entity view type for which to register the listener
     * @param viewTransitions The view transitions
     * @param listener The listener to register
     * @return This builder for method chaining
     */
    public <T> FlushOperationBuilder onPostCommit(Class<T> entityViewClass, Set<ViewTransition> viewTransitions, PostCommitListener<T> listener);

    /**
     * Registers the given listener to the current flush operation for the {@link ViewTransition#PERSIST}.
     *
     * @param entityViewClass The entity view type for which to register the listener
     * @param listener The listener to register
     * @param <T> The entity view type
     * @return This builder for method chaining
     */
    public <T> FlushOperationBuilder onPostCommitPersist(Class<T> entityViewClass, PostCommitListener<T> listener);

    /**
     * Registers the given listener to the current flush operation for the {@link ViewTransition#UPDATE}.
     *
     * @param entityViewClass The entity view type for which to register the listener
     * @param listener The listener to register
     * @param <T> The entity view type
     * @return This builder for method chaining
     */
    public <T> FlushOperationBuilder onPostCommitUpdate(Class<T> entityViewClass, PostCommitListener<T> listener);

    /**
     * Registers the given listener to the current flush operation for the {@link ViewTransition#REMOVE}.
     *
     * @param entityViewClass The entity view type for which to register the listener
     * @param listener The listener to register
     * @param <T> The entity view type
     * @return This builder for method chaining
     */
    public <T> FlushOperationBuilder onPostCommitRemove(Class<T> entityViewClass, PostCommitListener<T> listener);

    /**
     * Registers the given listener to the current flush operation.
     *
     * @param entityViewClass The entity view type for which to register the listener
     * @param listener The listener to register
     * @param <T> The entity view type
     * @return This builder for method chaining
     */
    public <T> FlushOperationBuilder onPostRollback(Class<T> entityViewClass, PostRollbackListener<T> listener);

    /**
     * Registers the given listener to the current flush operation.
     *
     * @param <T> The entity view type
     * @param entityViewClass The entity view type for which to register the listener
     * @param viewTransitions The view transitions
     * @param listener The listener to register
     * @return This builder for method chaining
     */
    public <T> FlushOperationBuilder onPostRollback(Class<T> entityViewClass, Set<ViewTransition> viewTransitions, PostRollbackListener<T> listener);

    /**
     * Registers the given listener to the current flush operation for the {@link ViewTransition#PERSIST}.
     *
     * @param entityViewClass The entity view type for which to register the listener
     * @param listener The listener to register
     * @param <T> The entity view type
     * @return This builder for method chaining
     */
    public <T> FlushOperationBuilder onPostRollbackPersist(Class<T> entityViewClass, PostRollbackListener<T> listener);

    /**
     * Registers the given listener to the current flush operation for the {@link ViewTransition#UPDATE}.
     *
     * @param entityViewClass The entity view type for which to register the listener
     * @param listener The listener to register
     * @param <T> The entity view type
     * @return This builder for method chaining
     */
    public <T> FlushOperationBuilder onPostRollbackUpdate(Class<T> entityViewClass, PostRollbackListener<T> listener);

    /**
     * Registers the given listener to the current flush operation for the {@link ViewTransition#REMOVE}.
     *
     * @param entityViewClass The entity view type for which to register the listener
     * @param listener The listener to register
     * @param <T> The entity view type
     * @return This builder for method chaining
     */
    public <T> FlushOperationBuilder onPostRollbackRemove(Class<T> entityViewClass, PostRollbackListener<T> listener);

    /**
     * Registers the given listener to the current flush operation.
     *
     * @param entityViewClass The entity view type for which to register the listener
     * @param entityClass The entity type for which to register the listener
     * @param listener The listener to register
     * @param <T> The entity view type
     * @param <E> The entity type
     * @return This builder for method chaining
     */
    public <T, E> FlushOperationBuilder onPrePersist(Class<T> entityViewClass, Class<E> entityClass, PrePersistListener<T> listener);

    /**
     * Registers the given listener to the current flush operation.
     *
     * @param entityViewClass The entity view type for which to register the listener
     * @param entityClass The entity type for which to register the listener
     * @param listener The listener to register
     * @param <T> The entity view type
     * @param <E> The entity type
     * @return This builder for method chaining
     */
    public <T, E> FlushOperationBuilder onPrePersist(Class<T> entityViewClass, Class<E> entityClass, PrePersistEntityListener<T, E> listener);

    /**
     * Registers the given listener to the current flush operation.
     *
     * @param entityViewClass The entity view type for which to register the listener
     * @param entityClass The entity type for which to register the listener
     * @param listener The listener to register
     * @param <T> The entity view type
     * @param <E> The entity type
     * @return This builder for method chaining
     */
    public <T, E> FlushOperationBuilder onPostPersist(Class<T> entityViewClass, Class<E> entityClass, PostPersistListener<T> listener);

    /**
     * Registers the given listener to the current flush operation.
     *
     * @param entityViewClass The entity view type for which to register the listener
     * @param entityClass The entity type for which to register the listener
     * @param listener The listener to register
     * @param <T> The entity view type
     * @param <E> The entity type
     * @return This builder for method chaining
     */
    public <T, E> FlushOperationBuilder onPostPersist(Class<T> entityViewClass, Class<E> entityClass, PostPersistEntityListener<T, E> listener);

    /**
     * Registers the given listener to the current flush operation.
     *
     * @param entityViewClass The entity view type for which to register the listener
     * @param entityClass The entity type for which to register the listener
     * @param listener The listener to register
     * @param <T> The entity view type
     * @param <E> The entity type
     * @return This builder for method chaining
     */
    public <T, E> FlushOperationBuilder onPreUpdate(Class<T> entityViewClass, Class<E> entityClass, PreUpdateListener<T> listener);

    /**
     * Registers the given listener to the current flush operation.
     *
     * @param entityViewClass The entity view type for which to register the listener
     * @param entityClass The entity type for which to register the listener
     * @param listener The listener to register
     * @param <T> The entity view type
     * @param <E> The entity type
     * @return This builder for method chaining
     */
    public <T, E> FlushOperationBuilder onPostUpdate(Class<T> entityViewClass, Class<E> entityClass, PostUpdateListener<T> listener);

    /**
     * Registers the given listener to the current flush operation.
     *
     * @param entityViewClass The entity view type for which to register the listener
     * @param entityClass The entity type for which to register the listener
     * @param listener The listener to register
     * @param <T> The entity view type
     * @param <E> The entity type
     * @return This builder for method chaining
     */
    public <T, E> FlushOperationBuilder onPreRemove(Class<T> entityViewClass, Class<E> entityClass, PreRemoveListener<T> listener);

    /**
     * Registers the given listener to the current flush operation.
     *
     * @param entityViewClass The entity view type for which to register the listener
     * @param entityClass The entity type for which to register the listener
     * @param listener The listener to register
     * @param <T> The entity view type
     * @param <E> The entity type
     * @return This builder for method chaining
     */
    public <T, E> FlushOperationBuilder onPostRemove(Class<T> entityViewClass, Class<E> entityClass, PostRemoveListener<T> listener);

    /**
     * Registers the given listener to the current flush operation.
     *
     * @param entityViewClass The entity view type for which to register the listener
     * @param entityClass The entity type for which to register the listener
     * @param listener The listener to register
     * @param <T> The entity view type
     * @param <E> The entity type
     * @return This builder for method chaining
     */
    public <T, E> FlushOperationBuilder onPostCommit(Class<T> entityViewClass, Class<E> entityClass, PostCommitListener<T> listener);

    /**
     * Registers the given listener to the current flush operation.
     *
     * @param <T> The entity view type
     * @param <E> The entity type
     * @param entityViewClass The entity view type for which to register the listener
     * @param entityClass The entity type for which to register the listener
     * @param viewTransitions The view transitions
     * @param listener The listener to register
     * @return This builder for method chaining
     */
    public <T, E> FlushOperationBuilder onPostCommit(Class<T> entityViewClass, Class<E> entityClass, Set<ViewTransition> viewTransitions, PostCommitListener<T> listener);

    /**
     * Registers the given listener to the current flush operation for the {@link ViewTransition#PERSIST}.
     *
     * @param entityViewClass The entity view type for which to register the listener
     * @param entityClass The entity type for which to register the listener
     * @param listener The listener to register
     * @param <T> The entity view type
     * @param <E> The entity type
     * @return This builder for method chaining
     */
    public <T, E> FlushOperationBuilder onPostCommitPersist(Class<T> entityViewClass, Class<E> entityClass, PostCommitListener<T> listener);

    /**
     * Registers the given listener to the current flush operation for the {@link ViewTransition#UPDATE}.
     *
     * @param entityViewClass The entity view type for which to register the listener
     * @param entityClass The entity type for which to register the listener
     * @param listener The listener to register
     * @param <T> The entity view type
     * @param <E> The entity type
     * @return This builder for method chaining
     */
    public <T, E> FlushOperationBuilder onPostCommitUpdate(Class<T> entityViewClass, Class<E> entityClass, PostCommitListener<T> listener);

    /**
     * Registers the given listener to the current flush operation for the {@link ViewTransition#REMOVE}.
     *
     * @param entityViewClass The entity view type for which to register the listener
     * @param entityClass The entity type for which to register the listener
     * @param listener The listener to register
     * @param <T> The entity view type
     * @param <E> The entity type
     * @return This builder for method chaining
     */
    public <T, E> FlushOperationBuilder onPostCommitRemove(Class<T> entityViewClass, Class<E> entityClass, PostCommitListener<T> listener);

    /**
     * Registers the given listener to the current flush operation.
     *
     * @param entityViewClass The entity view type for which to register the listener
     * @param entityClass The entity type for which to register the listener
     * @param listener The listener to register
     * @param <T> The entity view type
     * @param <E> The entity type
     * @return This builder for method chaining
     */
    public <T, E> FlushOperationBuilder onPostRollback(Class<T> entityViewClass, Class<E> entityClass, PostRollbackListener<T> listener);

    /**
     * Registers the given listener to the current flush operation.
     *
     * @param <T> The entity view type
     * @param <E> The entity type
     * @param entityViewClass The entity view type for which to register the listener
     * @param entityClass The entity type for which to register the listener
     * @param viewTransitions The view transitions
     * @param listener The listener to register
     * @return This builder for method chaining
     */
    public <T, E> FlushOperationBuilder onPostRollback(Class<T> entityViewClass, Class<E> entityClass, Set<ViewTransition> viewTransitions, PostRollbackListener<T> listener);

    /**
     * Registers the given listener to the current flush operation for the {@link ViewTransition#PERSIST}.
     *
     * @param entityViewClass The entity view type for which to register the listener
     * @param entityClass The entity type for which to register the listener
     * @param listener The listener to register
     * @param <T> The entity view type
     * @param <E> The entity type
     * @return This builder for method chaining
     */
    public <T, E> FlushOperationBuilder onPostRollbackPersist(Class<T> entityViewClass, Class<E> entityClass, PostRollbackListener<T> listener);

    /**
     * Registers the given listener to the current flush operation for the {@link ViewTransition#UPDATE}.
     *
     * @param entityViewClass The entity view type for which to register the listener
     * @param entityClass The entity type for which to register the listener
     * @param listener The listener to register
     * @param <T> The entity view type
     * @param <E> The entity type
     * @return This builder for method chaining
     */
    public <T, E> FlushOperationBuilder onPostRollbackUpdate(Class<T> entityViewClass, Class<E> entityClass, PostRollbackListener<T> listener);

    /**
     * Registers the given listener to the current flush operation for the {@link ViewTransition#REMOVE}.
     *
     * @param entityViewClass The entity view type for which to register the listener
     * @param entityClass The entity type for which to register the listener
     * @param listener The listener to register
     * @param <T> The entity view type
     * @param <E> The entity type
     * @return This builder for method chaining
     */
    public <T, E> FlushOperationBuilder onPostRollbackRemove(Class<T> entityViewClass, Class<E> entityClass, PostRollbackListener<T> listener);

    /**
     * Registers the given listener to the current flush operation.
     *
     * @param entityViewClass The entity view type for which to register the listener
     * @param listener The listener to register
     * @param <T> The entity view type
     * @return This builder for method chaining
     */
    public <T> FlushOperationBuilder onPrePersist(Class<T> entityViewClass, ViewListener<T> listener);

    /**
     * Registers the given listener to the current flush operation.
     *
     * @param entityViewClass The entity view type for which to register the listener
     * @param listener The listener to register
     * @param <T> The entity view type
     * @return This builder for method chaining
     */
    public <T> FlushOperationBuilder onPrePersist(Class<T> entityViewClass, ViewAndEntityListener<T, ?> listener);

    /**
     * Registers the given listener to the current flush operation.
     *
     * @param entityViewClass The entity view type for which to register the listener
     * @param listener The listener to register
     * @param <T> The entity view type
     * @return This builder for method chaining
     */
    public <T> FlushOperationBuilder onPostPersist(Class<T> entityViewClass, ViewListener<T> listener);

    /**
     * Registers the given listener to the current flush operation.
     *
     * @param entityViewClass The entity view type for which to register the listener
     * @param listener The listener to register
     * @param <T> The entity view type
     * @return This builder for method chaining
     */
    public <T> FlushOperationBuilder onPostPersist(Class<T> entityViewClass, ViewAndEntityListener<T, ?> listener);

    /**
     * Registers the given listener to the current flush operation.
     *
     * @param entityViewClass The entity view type for which to register the listener
     * @param listener The listener to register
     * @param <T> The entity view type
     * @return This builder for method chaining
     */
    public <T> FlushOperationBuilder onPreUpdate(Class<T> entityViewClass, ViewListener<T> listener);

    /**
     * Registers the given listener to the current flush operation.
     *
     * @param entityViewClass The entity view type for which to register the listener
     * @param listener The listener to register
     * @param <T> The entity view type
     * @return This builder for method chaining
     */
    public <T> FlushOperationBuilder onPostUpdate(Class<T> entityViewClass, ViewListener<T> listener);

    /**
     * Registers the given listener to the current flush operation.
     *
     * @param entityViewClass The entity view type for which to register the listener
     * @param listener The listener to register
     * @param <T> The entity view type
     * @return This builder for method chaining
     */
    public <T> FlushOperationBuilder onPreRemove(Class<T> entityViewClass, ViewListener<T> listener);

    /**
     * Registers the given listener to the current flush operation.
     *
     * @param entityViewClass The entity view type for which to register the listener
     * @param listener The listener to register
     * @param <T> The entity view type
     * @return This builder for method chaining
     */
    public <T> FlushOperationBuilder onPostRemove(Class<T> entityViewClass, ViewListener<T> listener);

    /**
     * Registers the given listener to the current flush operation.
     *
     * @param entityViewClass The entity view type for which to register the listener
     * @param listener The listener to register
     * @param <T> The entity view type
     * @return This builder for method chaining
     */
    public <T> FlushOperationBuilder onPostCommit(Class<T> entityViewClass, ViewListener<T> listener);

    /**
     * Registers the given listener to the current flush operation.
     *
     * @param entityViewClass The entity view type for which to register the listener
     * @param listener The listener to register
     * @param <T> The entity view type
     * @return This builder for method chaining
     */
    public <T> FlushOperationBuilder onPostCommit(Class<T> entityViewClass, ViewTransitionListener<T> listener);

    /**
     * Registers the given listener to the current flush operation.
     *
     * @param <T> The entity view type
     * @param entityViewClass The entity view type for which to register the listener
     * @param viewTransitions The view transitions
     * @param listener The listener to register
     * @return This builder for method chaining
     */
    public <T> FlushOperationBuilder onPostCommit(Class<T> entityViewClass, Set<ViewTransition> viewTransitions, ViewListener<T> listener);

    /**
     * Registers the given listener to the current flush operation.
     *
     * @param <T> The entity view type
     * @param entityViewClass The entity view type for which to register the listener
     * @param viewTransitions The view transitions
     * @param listener The listener to register
     * @return This builder for method chaining
     */
    public <T> FlushOperationBuilder onPostCommit(Class<T> entityViewClass, Set<ViewTransition> viewTransitions, ViewTransitionListener<T> listener);

    /**
     * Registers the given listener to the current flush operation for the {@link ViewTransition#PERSIST}.
     *
     * @param entityViewClass The entity view type for which to register the listener
     * @param listener The listener to register
     * @param <T> The entity view type
     * @return This builder for method chaining
     */
    public <T> FlushOperationBuilder onPostCommitPersist(Class<T> entityViewClass, ViewListener<T> listener);

    /**
     * Registers the given listener to the current flush operation for the {@link ViewTransition#UPDATE}.
     *
     * @param entityViewClass The entity view type for which to register the listener
     * @param listener The listener to register
     * @param <T> The entity view type
     * @return This builder for method chaining
     */
    public <T> FlushOperationBuilder onPostCommitUpdate(Class<T> entityViewClass, ViewListener<T> listener);

    /**
     * Registers the given listener to the current flush operation for the {@link ViewTransition#REMOVE}.
     *
     * @param entityViewClass The entity view type for which to register the listener
     * @param listener The listener to register
     * @param <T> The entity view type
     * @return This builder for method chaining
     */
    public <T> FlushOperationBuilder onPostCommitRemove(Class<T> entityViewClass, ViewListener<T> listener);

    /**
     * Registers the given listener to the current flush operation.
     *
     * @param entityViewClass The entity view type for which to register the listener
     * @param listener The listener to register
     * @param <T> The entity view type
     * @return This builder for method chaining
     */
    public <T> FlushOperationBuilder onPostRollback(Class<T> entityViewClass, ViewListener<T> listener);

    /**
     * Registers the given listener to the current flush operation.
     *
     * @param entityViewClass The entity view type for which to register the listener
     * @param listener The listener to register
     * @param <T> The entity view type
     * @return This builder for method chaining
     */
    public <T> FlushOperationBuilder onPostRollback(Class<T> entityViewClass, ViewTransitionListener<T> listener);

    /**
     * Registers the given listener to the current flush operation.
     *
     * @param <T> The entity view type
     * @param entityViewClass The entity view type for which to register the listener
     * @param viewTransitions The view transitions
     * @param listener The listener to register
     * @return This builder for method chaining
     */
    public <T> FlushOperationBuilder onPostRollback(Class<T> entityViewClass, Set<ViewTransition> viewTransitions, ViewListener<T> listener);

    /**
     * Registers the given listener to the current flush operation.
     *
     * @param <T> The entity view type
     * @param entityViewClass The entity view type for which to register the listener
     * @param viewTransitions The view transitions
     * @param listener The listener to register
     * @return This builder for method chaining
     */
    public <T> FlushOperationBuilder onPostRollback(Class<T> entityViewClass, Set<ViewTransition> viewTransitions, ViewTransitionListener<T> listener);

    /**
     * Registers the given listener to the current flush operation for the {@link ViewTransition#PERSIST}.
     *
     * @param entityViewClass The entity view type for which to register the listener
     * @param listener The listener to register
     * @param <T> The entity view type
     * @return This builder for method chaining
     */
    public <T> FlushOperationBuilder onPostRollbackPersist(Class<T> entityViewClass, ViewListener<T> listener);

    /**
     * Registers the given listener to the current flush operation for the {@link ViewTransition#UPDATE}.
     *
     * @param entityViewClass The entity view type for which to register the listener
     * @param listener The listener to register
     * @param <T> The entity view type
     * @return This builder for method chaining
     */
    public <T> FlushOperationBuilder onPostRollbackUpdate(Class<T> entityViewClass, ViewListener<T> listener);

    /**
     * Registers the given listener to the current flush operation for the {@link ViewTransition#REMOVE}.
     *
     * @param entityViewClass The entity view type for which to register the listener
     * @param listener The listener to register
     * @param <T> The entity view type
     * @return This builder for method chaining
     */
    public <T> FlushOperationBuilder onPostRollbackRemove(Class<T> entityViewClass, ViewListener<T> listener);

    /**
     * Registers the given listener to the current flush operation.
     *
     * @param entityViewClass The entity view type for which to register the listener
     * @param entityClass The entity type for which to register the listener
     * @param listener The listener to register
     * @param <T> The entity view type
     * @param <E> The entity type
     * @return This builder for method chaining
     */
    public <T, E> FlushOperationBuilder onPrePersist(Class<T> entityViewClass, Class<E> entityClass, ViewListener<T> listener);

    /**
     * Registers the given listener to the current flush operation.
     *
     * @param entityViewClass The entity view type for which to register the listener
     * @param entityClass The entity type for which to register the listener
     * @param listener The listener to register
     * @param <T> The entity view type
     * @param <E> The entity type
     * @return This builder for method chaining
     */
    public <T, E> FlushOperationBuilder onPrePersist(Class<T> entityViewClass, Class<E> entityClass, ViewAndEntityListener<T, E> listener);

    /**
     * Registers the given listener to the current flush operation.
     *
     * @param entityViewClass The entity view type for which to register the listener
     * @param entityClass The entity type for which to register the listener
     * @param listener The listener to register
     * @param <T> The entity view type
     * @param <E> The entity type
     * @return This builder for method chaining
     */
    public <T, E> FlushOperationBuilder onPostPersist(Class<T> entityViewClass, Class<E> entityClass, ViewListener<T> listener);

    /**
     * Registers the given listener to the current flush operation.
     *
     * @param entityViewClass The entity view type for which to register the listener
     * @param entityClass The entity type for which to register the listener
     * @param listener The listener to register
     * @param <T> The entity view type
     * @param <E> The entity type
     * @return This builder for method chaining
     */
    public <T, E> FlushOperationBuilder onPostPersist(Class<T> entityViewClass, Class<E> entityClass, ViewAndEntityListener<T, E> listener);

    /**
     * Registers the given listener to the current flush operation.
     *
     * @param entityViewClass The entity view type for which to register the listener
     * @param entityClass The entity type for which to register the listener
     * @param listener The listener to register
     * @param <T> The entity view type
     * @param <E> The entity type
     * @return This builder for method chaining
     */
    public <T, E> FlushOperationBuilder onPreUpdate(Class<T> entityViewClass, Class<E> entityClass, ViewListener<T> listener);

    /**
     * Registers the given listener to the current flush operation.
     *
     * @param entityViewClass The entity view type for which to register the listener
     * @param entityClass The entity type for which to register the listener
     * @param listener The listener to register
     * @param <T> The entity view type
     * @param <E> The entity type
     * @return This builder for method chaining
     */
    public <T, E> FlushOperationBuilder onPostUpdate(Class<T> entityViewClass, Class<E> entityClass, ViewListener<T> listener);

    /**
     * Registers the given listener to the current flush operation.
     *
     * @param entityViewClass The entity view type for which to register the listener
     * @param entityClass The entity type for which to register the listener
     * @param listener The listener to register
     * @param <T> The entity view type
     * @param <E> The entity type
     * @return This builder for method chaining
     */
    public <T, E> FlushOperationBuilder onPreRemove(Class<T> entityViewClass, Class<E> entityClass, ViewListener<T> listener);

    /**
     * Registers the given listener to the current flush operation.
     *
     * @param entityViewClass The entity view type for which to register the listener
     * @param entityClass The entity type for which to register the listener
     * @param listener The listener to register
     * @param <T> The entity view type
     * @param <E> The entity type
     * @return This builder for method chaining
     */
    public <T, E> FlushOperationBuilder onPostRemove(Class<T> entityViewClass, Class<E> entityClass, ViewListener<T> listener);

    /**
     * Registers the given listener to the current flush operation.
     *
     * @param entityViewClass The entity view type for which to register the listener
     * @param entityClass The entity type for which to register the listener
     * @param listener The listener to register
     * @param <T> The entity view type
     * @param <E> The entity type
     * @return This builder for method chaining
     */
    public <T, E> FlushOperationBuilder onPostCommit(Class<T> entityViewClass, Class<E> entityClass, ViewListener<T> listener);

    /**
     * Registers the given listener to the current flush operation.
     *
     * @param entityViewClass The entity view type for which to register the listener
     * @param entityClass The entity type for which to register the listener
     * @param listener The listener to register
     * @param <T> The entity view type
     * @param <E> The entity type
     * @return This builder for method chaining
     */
    public <T, E> FlushOperationBuilder onPostCommit(Class<T> entityViewClass, Class<E> entityClass, ViewTransitionListener<T> listener);

    /**
     * Registers the given listener to the current flush operation.
     *
     * @param <T> The entity view type
     * @param <E> The entity type
     * @param entityViewClass The entity view type for which to register the listener
     * @param entityClass The entity type for which to register the listener
     * @param viewTransitions The view transitions
     * @param listener The listener to register
     * @return This builder for method chaining
     */
    public <T, E> FlushOperationBuilder onPostCommit(Class<T> entityViewClass, Class<E> entityClass, Set<ViewTransition> viewTransitions, ViewListener<T> listener);

    /**
     * Registers the given listener to the current flush operation.
     *
     * @param <T> The entity view type
     * @param <E> The entity type
     * @param entityViewClass The entity view type for which to register the listener
     * @param entityClass The entity type for which to register the listener
     * @param viewTransitions The view transitions
     * @param listener The listener to register
     * @return This builder for method chaining
     */
    public <T, E> FlushOperationBuilder onPostCommit(Class<T> entityViewClass, Class<E> entityClass, Set<ViewTransition> viewTransitions, ViewTransitionListener<T> listener);

    /**
     * Registers the given listener to the current flush operation for the {@link ViewTransition#PERSIST}.
     *
     * @param entityViewClass The entity view type for which to register the listener
     * @param entityClass The entity type for which to register the listener
     * @param listener The listener to register
     * @param <T> The entity view type
     * @param <E> The entity type
     * @return This builder for method chaining
     */
    public <T, E> FlushOperationBuilder onPostCommitPersist(Class<T> entityViewClass, Class<E> entityClass, ViewListener<T> listener);

    /**
     * Registers the given listener to the current flush operation for the {@link ViewTransition#UPDATE}.
     *
     * @param entityViewClass The entity view type for which to register the listener
     * @param entityClass The entity type for which to register the listener
     * @param listener The listener to register
     * @param <T> The entity view type
     * @param <E> The entity type
     * @return This builder for method chaining
     */
    public <T, E> FlushOperationBuilder onPostCommitUpdate(Class<T> entityViewClass, Class<E> entityClass, ViewListener<T> listener);

    /**
     * Registers the given listener to the current flush operation for the {@link ViewTransition#REMOVE}.
     *
     * @param entityViewClass The entity view type for which to register the listener
     * @param entityClass The entity type for which to register the listener
     * @param listener The listener to register
     * @param <T> The entity view type
     * @param <E> The entity type
     * @return This builder for method chaining
     */
    public <T, E> FlushOperationBuilder onPostCommitRemove(Class<T> entityViewClass, Class<E> entityClass, ViewListener<T> listener);

    /**
     * Registers the given listener to the current flush operation.
     *
     * @param entityViewClass The entity view type for which to register the listener
     * @param entityClass The entity type for which to register the listener
     * @param listener The listener to register
     * @param <T> The entity view type
     * @param <E> The entity type
     * @return This builder for method chaining
     */
    public <T, E> FlushOperationBuilder onPostRollback(Class<T> entityViewClass, Class<E> entityClass, ViewListener<T> listener);

    /**
     * Registers the given listener to the current flush operation.
     *
     * @param entityViewClass The entity view type for which to register the listener
     * @param entityClass The entity type for which to register the listener
     * @param listener The listener to register
     * @param <T> The entity view type
     * @param <E> The entity type
     * @return This builder for method chaining
     */
    public <T, E> FlushOperationBuilder onPostRollback(Class<T> entityViewClass, Class<E> entityClass, ViewTransitionListener<T> listener);

    /**
     * Registers the given listener to the current flush operation.
     *
     * @param <T> The entity view type
     * @param <E> The entity type
     * @param entityViewClass The entity view type for which to register the listener
     * @param entityClass The entity type for which to register the listener
     * @param viewTransitions The view transitions
     * @param listener The listener to register
     * @return This builder for method chaining
     */
    public <T, E> FlushOperationBuilder onPostRollback(Class<T> entityViewClass, Class<E> entityClass, Set<ViewTransition> viewTransitions, ViewListener<T> listener);

    /**
     * Registers the given listener to the current flush operation.
     *
     * @param <T> The entity view type
     * @param <E> The entity type
     * @param entityViewClass The entity view type for which to register the listener
     * @param entityClass The entity type for which to register the listener
     * @param viewTransitions The view transitions
     * @param listener The listener to register
     * @return This builder for method chaining
     */
    public <T, E> FlushOperationBuilder onPostRollback(Class<T> entityViewClass, Class<E> entityClass, Set<ViewTransition> viewTransitions, ViewTransitionListener<T> listener);

    /**
     * Registers the given listener to the current flush operation for the {@link ViewTransition#PERSIST}.
     *
     * @param entityViewClass The entity view type for which to register the listener
     * @param entityClass The entity type for which to register the listener
     * @param listener The listener to register
     * @param <T> The entity view type
     * @param <E> The entity type
     * @return This builder for method chaining
     */
    public <T, E> FlushOperationBuilder onPostRollbackPersist(Class<T> entityViewClass, Class<E> entityClass, ViewListener<T> listener);

    /**
     * Registers the given listener to the current flush operation for the {@link ViewTransition#UPDATE}.
     *
     * @param entityViewClass The entity view type for which to register the listener
     * @param entityClass The entity type for which to register the listener
     * @param listener The listener to register
     * @param <T> The entity view type
     * @param <E> The entity type
     * @return This builder for method chaining
     */
    public <T, E> FlushOperationBuilder onPostRollbackUpdate(Class<T> entityViewClass, Class<E> entityClass, ViewListener<T> listener);

    /**
     * Registers the given listener to the current flush operation for the {@link ViewTransition#REMOVE}.
     *
     * @param entityViewClass The entity view type for which to register the listener
     * @param entityClass The entity type for which to register the listener
     * @param listener The listener to register
     * @param <T> The entity view type
     * @param <E> The entity type
     * @return This builder for method chaining
     */
    public <T, E> FlushOperationBuilder onPostRollbackRemove(Class<T> entityViewClass, Class<E> entityClass, ViewListener<T> listener);
}
