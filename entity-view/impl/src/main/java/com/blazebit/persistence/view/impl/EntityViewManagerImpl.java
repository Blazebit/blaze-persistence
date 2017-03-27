/*
 * Copyright 2014 - 2017 Blazebit.
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

package com.blazebit.persistence.view.impl;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.persistence.EntityManager;

import com.blazebit.lang.StringUtils;
import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.From;
import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.impl.EntityMetamodel;
import com.blazebit.persistence.impl.expression.ExpressionFactory;
import com.blazebit.persistence.view.AttributeFilterProvider;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.ViewFilterProvider;
import com.blazebit.persistence.view.filter.ContainsFilter;
import com.blazebit.persistence.view.filter.ContainsIgnoreCaseFilter;
import com.blazebit.persistence.view.filter.EndsWithFilter;
import com.blazebit.persistence.view.filter.EndsWithIgnoreCaseFilter;
import com.blazebit.persistence.view.filter.EqualFilter;
import com.blazebit.persistence.view.filter.GreaterOrEqualFilter;
import com.blazebit.persistence.view.filter.GreaterThanFilter;
import com.blazebit.persistence.view.filter.LessOrEqualFilter;
import com.blazebit.persistence.view.filter.LessThanFilter;
import com.blazebit.persistence.view.filter.NullFilter;
import com.blazebit.persistence.view.filter.StartsWithFilter;
import com.blazebit.persistence.view.filter.StartsWithIgnoreCaseFilter;
import com.blazebit.persistence.view.impl.filter.ContainsFilterImpl;
import com.blazebit.persistence.view.impl.filter.ContainsIgnoreCaseFilterImpl;
import com.blazebit.persistence.view.impl.filter.EndsWithFilterImpl;
import com.blazebit.persistence.view.impl.filter.EndsWithIgnoreCaseFilterImpl;
import com.blazebit.persistence.view.impl.filter.EqualFilterImpl;
import com.blazebit.persistence.view.impl.filter.GreaterOrEqualFilterImpl;
import com.blazebit.persistence.view.impl.filter.GreaterThanFilterImpl;
import com.blazebit.persistence.view.impl.filter.LessOrEqualFilterImpl;
import com.blazebit.persistence.view.impl.filter.LessThanFilterImpl;
import com.blazebit.persistence.view.impl.filter.NullFilterImpl;
import com.blazebit.persistence.view.impl.filter.StartsWithFilterImpl;
import com.blazebit.persistence.view.impl.filter.StartsWithIgnoreCaseFilterImpl;
import com.blazebit.persistence.view.impl.macro.DefaultViewRootJpqlMacro;
import com.blazebit.persistence.view.impl.metamodel.ManagedViewTypeImpl;
import com.blazebit.persistence.view.impl.metamodel.MetamodelBuildingContext;
import com.blazebit.persistence.view.impl.metamodel.MetamodelBuildingContextImpl;
import com.blazebit.persistence.view.impl.metamodel.ViewMetamodelImpl;
import com.blazebit.persistence.view.impl.objectbuilder.ViewTypeObjectBuilderTemplate;
import com.blazebit.persistence.view.impl.proxy.ProxyFactory;
import com.blazebit.persistence.view.impl.proxy.UpdatableProxy;
import com.blazebit.persistence.view.impl.update.EntityViewUpdater;
import com.blazebit.persistence.view.impl.update.FullEntityViewUpdater;
import com.blazebit.persistence.view.impl.update.PartialEntityViewUpdater;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.MappingConstructor;
import com.blazebit.persistence.view.metamodel.ViewType;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class EntityViewManagerImpl implements EntityViewManager {

    private final ViewMetamodelImpl metamodel;
    private final ProxyFactory proxyFactory;
    private final Map<String, Object> properties;
    private final ConcurrentMap<ViewTypeObjectBuilderTemplate.Key, ViewTypeObjectBuilderTemplate<?>> objectBuilderCache;
    private final ConcurrentMap<ViewType<?>, PartialEntityViewUpdater> partialEntityViewUpdaterCache;
    private final ConcurrentMap<ViewType<?>, FullEntityViewUpdater> fullEntityViewUpdaterCache;
    private final Map<String, Class<? extends AttributeFilterProvider>> filterMappings;
    
    private final boolean unsafeDisabled;

    public EntityViewManagerImpl(EntityViewConfigurationImpl config, CriteriaBuilderFactory cbf) {
        this.proxyFactory = new ProxyFactory();

        boolean validateExpressions = !Boolean.valueOf(String.valueOf(config.getProperty(ConfigurationProperties.EXPRESSION_VALIDATION_DISABLED)));
        Set<Class<?>> entityViews = config.getEntityViews();

        Set<String> errors = new HashSet<String>();
        ExpressionFactory expressionFactory = cbf.getService(ExpressionFactory.class);
        MetamodelBuildingContext context = new MetamodelBuildingContextImpl(cbf.getService(EntityMetamodel.class), cbf.getRegisteredFunctions(), expressionFactory, proxyFactory, entityViews, errors);
        this.metamodel = new ViewMetamodelImpl(entityViews, cbf, context, validateExpressions);

        if (!errors.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("There are error(s) in entity views!");

            for (String error : errors) {
                sb.append('\n');
                sb.append(error);
            }

            throw new IllegalArgumentException(sb.toString());
        }

        this.properties = copyProperties(config.getProperties());
        this.objectBuilderCache = new ConcurrentHashMap<ViewTypeObjectBuilderTemplate.Key, ViewTypeObjectBuilderTemplate<?>>();
        this.partialEntityViewUpdaterCache = new ConcurrentHashMap<ViewType<?>, PartialEntityViewUpdater>();
        this.fullEntityViewUpdaterCache = new ConcurrentHashMap<ViewType<?>, FullEntityViewUpdater>();
        this.filterMappings = new HashMap<String, Class<? extends AttributeFilterProvider>>();
        registerFilterMappings();
        
        this.unsafeDisabled = !Boolean.valueOf(String.valueOf(properties.get(ConfigurationProperties.PROXY_UNSAFE_ALLOWED)));
        
        if (Boolean.valueOf(String.valueOf(properties.get(ConfigurationProperties.TEMPLATE_EAGER_LOADING)))) {
            for (ViewType<?> view : metamodel.getViews()) {
                // TODO: Might be a good idea to let the view root be overridden or specified via the annotation
                String probableViewRoot = StringUtils.firstToLower(view.getEntityClass().getSimpleName());
                ExpressionFactory macroAwareExpressionFactory = context.createMacroAwareExpressionFactory(probableViewRoot);
                getTemplate(macroAwareExpressionFactory, view, null, null);

                for (MappingConstructor<?> constructor : view.getConstructors()) {
                    getTemplate(macroAwareExpressionFactory, view, (MappingConstructor) constructor, null);
                }
            }
        } else if (Boolean.valueOf(String.valueOf(properties.get(ConfigurationProperties.PROXY_EAGER_LOADING)))) {
            // Loading template will always involve also loading the proxies, so we use else if
            for (ViewType<?> view : metamodel.getViews()) {
                if (view.getConstructors().isEmpty() || unsafeDisabled) {
                    proxyFactory.getProxy(view);
                } else {
                    proxyFactory.getUnsafeProxy(view);
                }
            }
        }
    }

    @Override
    public ViewMetamodelImpl getMetamodel() {
        return metamodel;
    }

    @Override
    public void update(EntityManager em, Object view) {
        update(em, view, true);
    }

    @Override
    public void updateFull(EntityManager em, Object view) {
        update(em, view, false);
    }
    
    private void update(EntityManager em, Object view, boolean partial) {
        if (!(view instanceof UpdatableProxy)) {
            throw new IllegalArgumentException("Only updatable entity views can be updated!");
        }
        
        UpdatableProxy updatableProxy = (UpdatableProxy) view;
        Class<?> entityViewClass = updatableProxy.$$_getEntityViewClass();
        ViewType<?> viewType = metamodel.view(entityViewClass);
        partial = viewType.isPartiallyUpdatable() && partial;
        EntityViewUpdater updater = getUpdater(viewType, partial);
        updater.executeUpdate(em, updatableProxy);
    }

    @Override
    public <T, Q extends FullQueryBuilder<T, Q>> Q applySetting(EntityViewSetting<T, Q> setting, CriteriaBuilder<?> criteriaBuilder) {
        return EntityViewSettingHelper.apply(setting, this, criteriaBuilder, null);
    }

    @Override
    public <T, Q extends FullQueryBuilder<T, Q>> Q applySetting(EntityViewSetting<T, Q> setting, CriteriaBuilder<?> criteriaBuilder, String entityViewRoot) {
        return EntityViewSettingHelper.apply(setting, this, criteriaBuilder, entityViewRoot);
    }

    public boolean isUnsafeDisabled() {
        return unsafeDisabled;
    }

    /**
     * Creates a new filter instance of the given filter class.
     *
     * @param <T>         The filter type
     * @param filterClass The filter class
     * @return An instance of the given filter class
     */
    public <T extends ViewFilterProvider> T createViewFilter(Class<T> filterClass) {
        try {
            return filterClass.newInstance();
        } catch (Exception ex) {
            throw new IllegalArgumentException("Could not instantiate the view filter class: " + filterClass.getName(), ex);
        }
    }

    /**
     * Creates a new filter instance of the given filter class. If the filter class is a registered placeholder, the real
     * implementation will be resolved and instantiated.
     *
     * This method tries to instantiate an object by invoking one of the allowed constructors as defined in {@link AttributeFilterProvider}
     *
     * @param <T>          The filter type
     * @param filterClass  The filter class or a filter placeholder
     * @param expectedType The expected type of the argument into which it should be converted to
     * @param argument     The filter argument which is passed to the filter constructor
     * @return An instance of the given filter class
     */
    public <T extends AttributeFilterProvider> T createAttributeFilter(Class<T> filterClass, Class<?> expectedType, Object argument) {
        @SuppressWarnings("unchecked")
        Class<T> filterClassImpl = (Class<T>) filterMappings.get(filterClass.getName());

        if (filterClassImpl == null) {
            return createFilterInstance(filterClass, expectedType, argument);
        } else {
            return createFilterInstance(filterClassImpl, expectedType, argument);
        }
    }

    private <T extends AttributeFilterProvider> T createFilterInstance(Class<T> filterClass, Class<?> expectedType, Object argument) {
        try {
            @SuppressWarnings("unchecked")
            Constructor<T>[] constructors = (Constructor<T>[]) filterClass.getDeclaredConstructors();
            Constructor<T> filterConstructor = findConstructor(constructors, Class.class, Object.class);

            if (filterConstructor != null) {
                return filterConstructor.newInstance(expectedType, argument);
            } else {
                filterConstructor = findConstructor(constructors, Class.class);

                if (filterConstructor != null) {
                    return filterConstructor.newInstance(expectedType);
                } else {
                    filterConstructor = findConstructor(constructors, Object.class);

                    if (filterConstructor != null) {
                        return filterConstructor.newInstance(argument);
                    } else {
                        filterConstructor = findConstructor(constructors);

                        if (filterConstructor == null) {
                            throw new IllegalArgumentException("No suitable constructor found for filter class '" + filterClass.getName() + "'");
                        }

                        return filterConstructor.newInstance();
                    }
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException("Could not create an instance of the filter class '" + filterClass.getName() + "'", ex);
        }
    }

    private <T> Constructor<T> findConstructor(Constructor<T>[] constructors, Class<?>... classes) {
        for (int i = 0; i < constructors.length; i++) {
            if (Arrays.equals(constructors[i].getParameterTypes(), classes)) {
                return (Constructor<T>) constructors[i];
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public String applyObjectBuilder(Class<?> clazz, String mappingConstructorName, String entityViewRoot, EntityViewConfiguration configuration) {
        ManagedViewTypeImpl<?> viewType = getMetamodel().managedView(clazz);
        if (viewType == null) {
            throw new IllegalArgumentException("There is no entity view for the class '" + clazz.getName() + "' registered!");
        }
        MappingConstructor<?> mappingConstructor = viewType.getConstructor(mappingConstructorName);
        String viewName;
        if (viewType instanceof ViewType<?>) {
            viewName = ((ViewType) viewType).getName();
        } else {
            viewName = viewType.getJavaType().getSimpleName();
        }
        return applyObjectBuilder(viewType, mappingConstructor, viewName, entityViewRoot, configuration.getCriteriaBuilder(), configuration, 0, true);
    }

    public String applyObjectBuilder(ManagedViewType<?> viewType, MappingConstructor<?> mappingConstructor, String viewName, String entityViewRoot, FullQueryBuilder<?, ?> criteriaBuilder, EntityViewConfiguration configuration, int offset, boolean registerMacro) {
        From root = getFromByViewRoot(criteriaBuilder, entityViewRoot);
        criteriaBuilder.selectNew(createObjectBuilder(viewType, mappingConstructor, viewName, root, criteriaBuilder, configuration, offset, registerMacro));
        return root.getAlias();
    }

    public ObjectBuilder<?> createObjectBuilder(ManagedViewType<?> viewType, MappingConstructor<?> mappingConstructor, String viewName, String entityViewRoot, FullQueryBuilder<?, ?> criteriaBuilder, EntityViewConfiguration configuration, int offset, boolean registerMacro) {
        From root = getFromByViewRoot(criteriaBuilder, entityViewRoot);
        return createObjectBuilder(viewType, mappingConstructor, viewName, root, criteriaBuilder, configuration, offset, registerMacro);
    }

    public ObjectBuilder<?> createObjectBuilder(ManagedViewType<?> viewType, MappingConstructor<?> mappingConstructor, String viewName, From root, FullQueryBuilder<?, ?> criteriaBuilder, EntityViewConfiguration configuration, int offset, boolean registerMacro) {
        Class<?> entityClazz = root.getType();
        String entityViewRoot = root.getAlias();
        ExpressionFactory ef = criteriaBuilder.getService(ExpressionFactory.class);
        if (!viewType.getEntityClass().isAssignableFrom(entityClazz)) {
            throw new IllegalArgumentException("The given view type with the entity type '" + viewType.getEntityClass().getName()
                + "' can not be applied to the query builder with result type '" + criteriaBuilder.getResultType().getName() + "'");
        }

        if (registerMacro) {
            criteriaBuilder.registerMacro("view_root", new DefaultViewRootJpqlMacro(entityViewRoot));
        }
        return getTemplate(ef, viewType, mappingConstructor, viewName, entityViewRoot, offset)
            .createObjectBuilder(criteriaBuilder, configuration.getOptionalParameters(), configuration);
    }

    private static From getFromByViewRoot(FullQueryBuilder<?, ?> queryBuilder, String entityViewRoot) {
        if (entityViewRoot == null || entityViewRoot.isEmpty()) {
            Set<From> roots = queryBuilder.getRoots();
            if (roots.size() > 1) {
                throw new IllegalArgumentException("Can not apply entity view to given criteria builder because it has multiple query roots! Please specify the entity view root when applying the entity-view setting!");
            }

            return roots.iterator().next();
        }

        return queryBuilder.getFromByPath(entityViewRoot);
    }

    @SuppressWarnings("unchecked")
    public ViewTypeObjectBuilderTemplate<?> getTemplate(ExpressionFactory ef, ViewType<?> viewType, MappingConstructor<?> mappingConstructor, String entityViewRoot) {
        return getTemplate(ef, viewType, mappingConstructor, viewType.getName(), entityViewRoot, 0);
    }

    public ViewTypeObjectBuilderTemplate<?> getTemplate(ExpressionFactory ef, ManagedViewType<?> viewType, MappingConstructor<?> mappingConstructor, String name, String entityViewRoot, int offset) {
        ViewTypeObjectBuilderTemplate.Key key = new ViewTypeObjectBuilderTemplate.Key(ef, viewType, mappingConstructor, name, entityViewRoot, offset);
        ViewTypeObjectBuilderTemplate<?> value = objectBuilderCache.get(key);

        if (value == null) {
            value = key.createValue(this, proxyFactory);
            ViewTypeObjectBuilderTemplate<?> oldValue = objectBuilderCache.putIfAbsent(key, value);

            if (oldValue != null) {
                value = oldValue;
            }
        }

        return value;
    }
    
    private EntityViewUpdater getUpdater(ViewType<?> viewType, boolean partial) {
        if (partial) {
            return getPartialUpdater(viewType);
        } else {
            return getFullUpdater(viewType);
        }
    }
    
    private FullEntityViewUpdater getFullUpdater(ViewType<?> viewType) {
        FullEntityViewUpdater value = fullEntityViewUpdaterCache.get(viewType);

        if (value == null) {
            value = new FullEntityViewUpdater(viewType);
            FullEntityViewUpdater oldValue = fullEntityViewUpdaterCache.putIfAbsent(viewType, value);

            if (oldValue != null) {
                value = oldValue;
            }
        }

        return value;
    }
    
    private PartialEntityViewUpdater getPartialUpdater(ViewType<?> viewType) {
        PartialEntityViewUpdater value = partialEntityViewUpdaterCache.get(viewType);

        if (value == null) {
            value = new PartialEntityViewUpdater(viewType);
            PartialEntityViewUpdater oldValue = partialEntityViewUpdaterCache.putIfAbsent(viewType, value);

            if (oldValue != null) {
                value = oldValue;
            }
        }

        return value;
    }

    private void registerFilterMappings() {
        filterMappings.put(ContainsFilter.class.getName(), ContainsFilterImpl.class);
        filterMappings.put(ContainsIgnoreCaseFilter.class.getName(), ContainsIgnoreCaseFilterImpl.class);
        filterMappings.put(StartsWithFilter.class.getName(), StartsWithFilterImpl.class);
        filterMappings.put(StartsWithIgnoreCaseFilter.class.getName(), StartsWithIgnoreCaseFilterImpl.class);
        filterMappings.put(EndsWithFilter.class.getName(), EndsWithFilterImpl.class);
        filterMappings.put(EndsWithIgnoreCaseFilter.class.getName(), EndsWithIgnoreCaseFilterImpl.class);
        filterMappings.put(EqualFilter.class.getName(), EqualFilterImpl.class);
        filterMappings.put(NullFilter.class.getName(), NullFilterImpl.class);
        filterMappings.put(GreaterThanFilter.class.getName(), GreaterThanFilterImpl.class);
        filterMappings.put(GreaterOrEqualFilter.class.getName(), GreaterOrEqualFilterImpl.class);
        filterMappings.put(LessThanFilter.class.getName(), LessThanFilterImpl.class);
        filterMappings.put(LessOrEqualFilter.class.getName(), LessOrEqualFilterImpl.class);
    }

    private Map<String, Object> copyProperties(Properties properties) {
        Map<String, Object> newProperties = new HashMap<String, Object>();

        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            String key = (String) entry.getKey();
            Object value = entry.getValue();
            newProperties.put(key, value);
        }

        return newProperties;
    }

}
