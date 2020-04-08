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

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.spi.ServiceProvider;
import com.blazebit.persistence.view.change.SingularChangeModel;
import com.blazebit.persistence.view.metamodel.ViewMetamodel;

import javax.persistence.EntityManager;
import java.util.Map;

/**
 * An interface that gives access to the metamodel and object builders.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface EntityViewManager extends ServiceProvider {

    /**
     * Returns the metamodel for this entity view manager.
     *
     * @return The metamodel for this entity view manager
     */
    public ViewMetamodel getMetamodel();

    /**
     * Returns the globally configured optional parameters.
     *
     * @return The optional parameters
     * @since 1.5.0
     */
    public Map<String, Object> getOptionalParameters();

    /**
     * Loads and returns an entity view of the given type having the given entity id.
     *
     * @param entityManager The entity manager to use for querying
     * @param entityViewClass The entity view class to use
     * @param entityId The id of the entity
     * @param <T> The type of the entity view class
     * @return The loaded instance of the given entity view type with the id
     * @since 1.2.0
     */
    public <T> T find(EntityManager entityManager, Class<T> entityViewClass, Object entityId);

    /**
     * Loads and returns an entity view as determined by the given type {@link EntityViewSetting} having the given entity id.
     *
     * @param entityManager The entity manager to use for querying
     * @param entityViewSetting The entity view setting to use
     * @param entityId The id of the entity
     * @param <T> The type of the entity view class
     * @return The loaded instance of the given entity view type with the id
     * @since 1.2.0
     */
    public <T> T find(EntityManager entityManager, EntityViewSetting<T, CriteriaBuilder<T>> entityViewSetting, Object entityId);

    /**
     * Creates a reference instance of the entity view class for the given id and returns it.
     *
     * @param entityViewClass The entity view class to construct
     * @param id The id of the entity view
     * @param <T> The type of the entity view class
     * @return A reference instance of the given entity view type with the id
     * @since 1.2.0
     */
    public <T> T getReference(Class<T> entityViewClass, Object id);

    /**
     * Creates an entity reference for the given entity view and returns it.
     *
     * @param entityManager The entity manager to use for the entity reference
     * @param entityView The entity view class for which to get the entity reference
     * @param <T> The type of the entity class
     * @return An entity reference for given entity view object
     * @since 1.3.0
     */
    public <T> T getEntityReference(EntityManager entityManager, Object entityView);

    /**
     * Gives access to the change model of the entity view instance.
     *
     * @param entityView The entity view
     * @param <T> The type of the given entity view
     * @return The change model of the entity view instance
     * @since 1.2.0
     */
    public <T> SingularChangeModel<T> getChangeModel(T entityView);

    /**
     * Creates a new instance of the entity view class and returns it.
     *
     * @param entityViewClass The entity view class to construct
     * @param <T> The type of the entity view class
     * @return A new instance of the given entity view class
     * @since 1.2.0
     */
    public <T> T create(Class<T> entityViewClass);

    /**
     * Creates a new instance of the entity view class and returns it.
     *
     * @param entityViewClass The entity view class to construct
     * @param optionalParameters The optional parameters to consider
     * @param <T> The type of the entity view class
     * @return A new instance of the given entity view class
     * @since 1.5.0
     */
    public <T> T create(Class<T> entityViewClass, Map<String, Object> optionalParameters);

    /**
     * Creates a builder for the entity view of the given class type.
     *
     * @param clazz The entity view type to build
     * @param <X> The entity view type
     * @return The builder
     * @since 1.5.0
     */
    public <X> EntityViewBuilder<X> createBuilder(Class<X> clazz);

    /**
     * Creates a builder for the entity view of the given class type.
     *
     * @param clazz The entity view type to build
     * @param constructorName The entity view constructor to use
     * @param <X> The entity view type
     * @return The builder
     * @since 1.5.0
     */
    public <X> EntityViewBuilder<X> createBuilder(Class<X> clazz, String constructorName);

    /**
     * Copies the state from the given view into a builder for the same entity view type.
     *
     * @param view The entity view to copy into a builder
     * @param <X> The entity view type
     * @return The builder
     * @since 1.5.0
     */
    public <X> EntityViewBuilder<X> createBuilder(X view);

    /**
     * Copies the state from the given view into a builder for the same entity view type.
     *
     * @param view The entity view to copy into a builder
     * @param constructorName The entity view constructor to use
     * @param <X> The entity view type
     * @return The builder
     * @since 1.5.0
     */
    public <X> EntityViewBuilder<X> createBuilder(X view, String constructorName);

    /**
     * Creates a builder for the entity view of the given class type.
     *
     * @param clazz The entity view type to build
     * @param optionalParameters The optional parameters to consider
     * @param <X> The entity view type
     * @return The builder
     * @since 1.5.0
     */
    public <X> EntityViewBuilder<X> createBuilder(Class<X> clazz, Map<String, Object> optionalParameters);

    /**
     * Creates a builder for the entity view of the given class type.
     *
     * @param clazz The entity view type to build
     * @param optionalParameters The optional parameters to consider
     * @param constructorName The entity view constructor to use
     * @param <X> The entity view type
     * @return The builder
     * @since 1.5.0
     */
    public <X> EntityViewBuilder<X> createBuilder(Class<X> clazz, Map<String, Object> optionalParameters, String constructorName);

    /**
     * Copies the state from the given view into a builder for the same entity view type.
     *
     * @param view The entity view to copy into a builder
     * @param optionalParameters The optional parameters to consider
     * @param <X> The entity view type
     * @return The builder
     * @since 1.5.0
     */
    public <X> EntityViewBuilder<X> createBuilder(X view, Map<String, Object> optionalParameters);

    /**
     * Copies the state from the given view into a builder for the same entity view type.
     *
     * @param view The entity view to copy into a builder
     * @param optionalParameters The optional parameters to consider
     * @param constructorName The entity view constructor to use
     * @param <X> The entity view type
     * @return The builder
     * @since 1.5.0
     */
    public <X> EntityViewBuilder<X> createBuilder(X view, Map<String, Object> optionalParameters, String constructorName);

    /**
     * Creates a new instance of the entity view class, copies the values
     * of matching attributes of the source object and returns the instance.
     *
     * @param <T> The type of the entity view class
     * @param source The source object from which to copy values
     * @param entityViewClass The entity view class to construct
     * @param convertOptions The options to use for converting
     * @return A new instance of the given entity view class
     * @since 1.2.0
     */
    public <T> T convert(Object source, Class<T> entityViewClass, ConvertOption... convertOptions);

    /**
     * Creates a new instance of the entity view class, copies the values
     * of matching attributes of the source object and returns the instance.
     *
     * @param <T> The type of the entity view class
     * @param source The source object from which to copy values
     * @param entityViewClass The entity view class to construct
     * @param constructorName The name of the entity view constructor to use
     * @param convertOptions The options to use for converting
     * @return A new instance of the given entity view class
     * @since 1.5.0
     */
    public <T> T convert(Object source, Class<T> entityViewClass, String constructorName, ConvertOption... convertOptions);

    /**
     * Creates a new instance of the entity view class, copies the values
     * of matching attributes of the source object and returns the instance.
     *
     * @param <T> The type of the entity view class
     * @param source The source object from which to copy values
     * @param entityViewClass The entity view class to construct
     * @param optionalParameters The optional parameters to consider
     * @param convertOptions The options to use for converting
     * @return A new instance of the given entity view class
     * @since 1.5.0
     */
    public <T> T convert(Object source, Class<T> entityViewClass, Map<String, Object> optionalParameters, ConvertOption... convertOptions);

    /**
     * Creates a new instance of the entity view class, copies the values
     * of matching attributes of the source object and returns the instance.
     *
     * @param <T> The type of the entity view class
     * @param source The source object from which to copy values
     * @param entityViewClass The entity view class to construct
     * @param constructorName The name of the entity view constructor to use
     * @param optionalParameters The optional parameters to consider
     * @param convertOptions The options to use for converting
     * @return A new instance of the given entity view class
     * @since 1.5.0
     */
    public <T> T convert(Object source, Class<T> entityViewClass, String constructorName, Map<String, Object> optionalParameters, ConvertOption... convertOptions);

    /**
     * Creates a new instance of the entity view class, copies the values
     * of matching attributes of the source object and returns the instance.
     *
     * @param <T> The type of the entity view class
     * @param source The source object from which to copy values
     * @param entityViewClass The entity view class to construct
     * @param convertOptions The options to use for converting
     * @return A builder to specify attribute entity view types and conversion options
     * @since 1.4.0
     */
    public <T> ConvertOperationBuilder<T> convertWith(Object source, Class<T> entityViewClass, ConvertOption... convertOptions);

    /**
     * Creates a new instance of the entity view class, copies the values
     * of matching attributes of the source object and returns the instance.
     *
     * @param <T> The type of the entity view class
     * @param source The source object from which to copy values
     * @param entityViewClass The entity view class to construct
     * @param constructorName The name of the entity view constructor to use
     * @param convertOptions The options to use for converting
     * @return A builder to specify attribute entity view types and conversion options
     * @since 1.5.0
     */
    public <T> ConvertOperationBuilder<T> convertWith(Object source, Class<T> entityViewClass, String constructorName, ConvertOption... convertOptions);

    /**
     * Creates a new instance of the entity view class, copies the values
     * of matching attributes of the source object and returns the instance.
     *
     * @param <T> The type of the entity view class
     * @param source The source object from which to copy values
     * @param entityViewClass The entity view class to construct
     * @param optionalParameters The optional parameters to consider
     * @param convertOptions The options to use for converting
     * @return A builder to specify attribute entity view types and conversion options
     * @since 1.5.0
     */
    public <T> ConvertOperationBuilder<T> convertWith(Object source, Class<T> entityViewClass, Map<String, Object> optionalParameters, ConvertOption... convertOptions);

    /**
     * Creates a new instance of the entity view class, copies the values
     * of matching attributes of the source object and returns the instance.
     *
     * @param <T> The type of the entity view class
     * @param source The source object from which to copy values
     * @param entityViewClass The entity view class to construct
     * @param constructorName The name of the entity view constructor to use
     * @param optionalParameters The optional parameters to consider
     * @param convertOptions The options to use for converting
     * @return A builder to specify attribute entity view types and conversion options
     * @since 1.5.0
     */
    public <T> ConvertOperationBuilder<T> convertWith(Object source, Class<T> entityViewClass, String constructorName, Map<String, Object> optionalParameters, ConvertOption... convertOptions);

    /**
     * Saves the entity which the given entity view maps to.
     * Issues a partial update if enabled for the given view.
     * 
     * @param entityManager The entity manager to use for the save
     * @param view The view to use for saving
     * @since 1.4.0
     */
    public void save(EntityManager entityManager, Object view);
    
    /**
     * Fully saves the entity which the given entity view maps to.
     * 
     * @param entityManager The entity manager to use for the save
     * @param view The view to use for saving
     * @since 1.4.0
     */
    public void saveFull(EntityManager entityManager, Object view);

    /**
     * Saves the entity view state onto the given entity.
     * Issues a partial update if enabled for the given view.
     *
     * @param entityManager The entity manager to use for the save
     * @param view The view to use for saving
     * @param entity The entity to apply data to
     * @since 1.4.0
     */
    public void saveTo(EntityManager entityManager, Object view, Object entity);

    /**
     * Fully saves the entity view state onto the given entity.
     *
     * @param entityManager The entity manager to use for the save
     * @param view The view to use for saving
     * @param entity The entity to apply data to
     * @since 1.4.0
     */
    public void saveFullTo(EntityManager entityManager, Object view, Object entity);

    /**
     * Updates the entity which the given entity view maps to.
     * Issues a partial update if enabled for the given view.
     *
     * @param entityManager The entity manager to use for the update
     * @param view The view to use for updating
     * @since 1.1.0
     * @deprecated Use {@link #save(EntityManager, Object)} instead. This will be removed in 2.0
     */
    @Deprecated
    public void update(EntityManager entityManager, Object view);

    /**
     * Fully updates the entity which the given entity view maps to.
     *
     * @param entityManager The entity manager to use for the update
     * @param view The view to use for updating
     * @since 1.1.0
     * @deprecated Use {@link #saveFull(EntityManager, Object)} instead. This will be removed in 2.0
     */
    @Deprecated
    public void updateFull(EntityManager entityManager, Object view);

    /**
     * Saves the entity which the given entity view maps to.
     * Issues a partial update if enabled for the given view.
     *
     * @param entityManager The entity manager to use for the update
     * @param view The view to use for updating
     * @return A flush operation builder for further configuring the flush operation
     * @since 1.4.0
     */
    public FlushOperationBuilder saveWith(EntityManager entityManager, Object view);

    /**
     * Fully saves the entity which the given entity view maps to.
     *
     * @param entityManager The entity manager to use for the update
     * @param view The view to use for updating
     * @return A flush operation builder for further configuring the flush operation
     * @since 1.4.0
     */
    public FlushOperationBuilder saveFullWith(EntityManager entityManager, Object view);

    /**
     * Saves the entity view state onto the given entity.
     * Issues a partial update if enabled for the given view.
     *
     * @param entityManager The entity manager to use for the update
     * @param view The view to use for updating
     * @param entity The entity to apply data to
     * @return A flush operation builder for further configuring the flush operation
     * @since 1.4.0
     */
    public FlushOperationBuilder saveWithTo(EntityManager entityManager, Object view, Object entity);

    /**
     * Fully saves the entity view state onto the given entity.
     *
     * @param entityManager The entity manager to use for the update
     * @param view The view to use for updating
     * @param entity The entity to apply data to
     * @return A flush operation builder for further configuring the flush operation
     * @since 1.4.0
     */
    public FlushOperationBuilder saveFullWithTo(EntityManager entityManager, Object view, Object entity);

    /**
     * Removes the entity represented by the given view.
     * Also cascades deletes to attributes that have {@link CascadeType#DELETE} enabled.
     *
     * @param entityManager The entity manager to use for the removing
     * @param view The view for which the entity should be removed
     * @since 1.2.0
     */
    public void remove(EntityManager entityManager, Object view);

    /**
     * Removes the entity represented by the given view.
     * Also cascades deletes to attributes that have {@link CascadeType#DELETE} enabled.
     *
     * @param entityManager The entity manager to use for the removing
     * @param view The view for which the entity should be removed
     * @return A flush operation builder for further configuring the flush operation
     * @since 1.4.0
     */
    public FlushOperationBuilder removeWith(EntityManager entityManager, Object view);

    /**
     * Removes the entity represented by the entity type defiend for the given view and the given entity id.
     * Also cascades deletes to attributes that have {@link CascadeType#DELETE} enabled.
     *
     * @param entityManager The entity manager to use for the removing
     * @param entityViewClass The entity view class to use
     * @param viewId The id of entity view
     * @since 1.2.0
     */
    public void remove(EntityManager entityManager, Class<?> entityViewClass, Object viewId);

    /**
     * Removes the entity represented by the entity type defiend for the given view and the given entity id.
     * Also cascades deletes to attributes that have {@link CascadeType#DELETE} enabled.
     *
     * @param entityManager The entity manager to use for the removing
     * @param entityViewClass The entity view class to use
     * @param viewId The id of entity view
     * @return A flush operation builder for further configuring the flush operation
     * @since 1.4.0
     */
    public FlushOperationBuilder removeWith(EntityManager entityManager, Class<?> entityViewClass, Object viewId);

    /**
     * Applies the entity view setting to the given criteria builder.
     *
     * @param setting         The setting that should be applied
     * @param criteriaBuilder The criteria builder on which the setting should be applied
     * @param <T>             The type of the entity view
     * @param <Q>             {@linkplain PaginatedCriteriaBuilder} if paginated, {@linkplain CriteriaBuilder} otherwise
     * @return {@linkplain PaginatedCriteriaBuilder} if paginated,
     *         {@linkplain CriteriaBuilder} otherwise
     */
    public <T, Q extends FullQueryBuilder<T, Q>> Q applySetting(EntityViewSetting<T, Q> setting, CriteriaBuilder<?> criteriaBuilder);

    /**
     * Applies the entity view setting to the given entity view root of the criteria builder.
     *
     * @param setting         The setting that should be applied
     * @param criteriaBuilder The criteria builder on which the setting should be applied
     * @param entityViewRoot  The relation from which the entity view should be materialized
     * @param <T>             The type of the entity view
     * @param <Q>             {@linkplain PaginatedCriteriaBuilder} if paginated, {@linkplain CriteriaBuilder} otherwise
     * @return {@linkplain PaginatedCriteriaBuilder} if paginated,
     *         {@linkplain CriteriaBuilder} otherwise
     * @since 1.2.0
     */
    public <T, Q extends FullQueryBuilder<T, Q>> Q applySetting(EntityViewSetting<T, Q> setting, CriteriaBuilder<?> criteriaBuilder, String entityViewRoot);
}
