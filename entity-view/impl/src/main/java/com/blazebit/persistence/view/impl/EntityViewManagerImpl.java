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

package com.blazebit.persistence.view.impl;

import com.blazebit.exception.ExceptionUtils;
import com.blazebit.lang.StringUtils;
import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.Path;
import com.blazebit.persistence.parser.EntityMetamodel;
import com.blazebit.persistence.parser.expression.AbstractCachingExpressionFactory;
import com.blazebit.persistence.parser.expression.ExpressionFactory;
import com.blazebit.persistence.parser.expression.MacroConfiguration;
import com.blazebit.persistence.parser.expression.MacroFunction;
import com.blazebit.persistence.parser.util.JpaMetamodelUtils;
import com.blazebit.persistence.spi.DbmsDialect;
import com.blazebit.persistence.spi.JpaProvider;
import com.blazebit.persistence.spi.JpqlMacro;
import com.blazebit.persistence.spi.PackageOpener;
import com.blazebit.persistence.view.AttributeFilterProvider;
import com.blazebit.persistence.view.ConvertOption;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.ViewFilterProvider;
import com.blazebit.persistence.view.change.SingularChangeModel;
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
import com.blazebit.persistence.view.impl.accessor.AttributeAccessor;
import com.blazebit.persistence.view.impl.accessor.EntityIdAttributeAccessor;
import com.blazebit.persistence.view.impl.change.ViewChangeModel;
import com.blazebit.persistence.view.impl.macro.EmbeddingViewJpqlMacro;
import com.blazebit.persistence.view.impl.mapper.ViewMapper;
import com.blazebit.persistence.view.impl.update.DefaultUpdateContext;
import com.blazebit.persistence.view.impl.update.UpdateContext;
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
import com.blazebit.persistence.view.impl.metamodel.ManagedViewTypeImplementor;
import com.blazebit.persistence.view.impl.metamodel.MappingConstructorImpl;
import com.blazebit.persistence.view.impl.metamodel.MetamodelBuildingContext;
import com.blazebit.persistence.view.impl.metamodel.MetamodelBuildingContextImpl;
import com.blazebit.persistence.view.impl.metamodel.ViewMetamodelImpl;
import com.blazebit.persistence.view.impl.metamodel.ViewTypeImpl;
import com.blazebit.persistence.view.impl.objectbuilder.ViewTypeObjectBuilderTemplate;
import com.blazebit.persistence.view.impl.proxy.DirtyStateTrackable;
import com.blazebit.persistence.view.impl.proxy.MutableStateTrackable;
import com.blazebit.persistence.view.impl.proxy.ProxyFactory;
import com.blazebit.persistence.view.impl.type.DefaultBasicUserTypeRegistry;
import com.blazebit.persistence.view.impl.update.EntityViewUpdater;
import com.blazebit.persistence.view.impl.update.EntityViewUpdaterImpl;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.MappingConstructor;
import com.blazebit.persistence.view.metamodel.ViewType;
import com.blazebit.persistence.view.spi.type.EntityViewProxy;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class EntityViewManagerImpl implements EntityViewManager {

    private final CriteriaBuilderFactory cbf;
    private final JpaProvider jpaProvider;
    private final DbmsDialect dbmsDialect;
    private final ExpressionFactory expressionFactory;
    private final PackageOpener packageOpener;
    private final AttributeAccessor entityIdAccessor;
    private final ViewMetamodelImpl metamodel;
    private final ProxyFactory proxyFactory;
    private final boolean supportsTransientReference;
    private final ConcurrentMap<ViewTypeObjectBuilderTemplate.Key, ViewTypeObjectBuilderTemplate<?>> objectBuilderCache;
    private final ConcurrentMap<ManagedViewType<?>, EntityViewUpdaterImpl> entityViewUpdaterCache;
    private final ConcurrentMap<ContextAwareUpdaterKey, EntityViewUpdaterImpl> contextAwareEntityViewUpdaterCache;
    private final ConcurrentMap<ViewMapper.Key<?, ?>, ViewMapper<?, ?>> entityViewMappers;
    private final Map<String, Class<? extends AttributeFilterProvider>> filterMappings;
    
    private final boolean unsafeDisabled;

    public EntityViewManagerImpl(EntityViewConfigurationImpl config, CriteriaBuilderFactory cbf) {
        this.cbf = cbf;
        this.jpaProvider = cbf.getService(JpaProvider.class);
        this.dbmsDialect = cbf.getService(DbmsDialect.class);
        EntityMetamodel entityMetamodel = cbf.getService(EntityMetamodel.class);
        this.expressionFactory = cbf.getService(ExpressionFactory.class);
        this.packageOpener = cbf.getService(PackageOpener.class);
        this.entityIdAccessor = new EntityIdAttributeAccessor(jpaProvider);
        this.unsafeDisabled = !Boolean.valueOf(String.valueOf(config.getProperty(ConfigurationProperties.PROXY_UNSAFE_ALLOWED)));
        this.proxyFactory = new ProxyFactory(unsafeDisabled, packageOpener);

        boolean validateExpressions = !Boolean.valueOf(String.valueOf(config.getProperty(ConfigurationProperties.EXPRESSION_VALIDATION_DISABLED)));

        Set<String> errors = config.getBootContext().getErrors();

        MetamodelBuildingContext context = new MetamodelBuildingContextImpl(
                config.getProperties(),
                new DefaultBasicUserTypeRegistry(config.getUserTypeRegistry(), cbf),
                entityMetamodel,
                jpaProvider,
                cbf.getRegisteredFunctions(),
                expressionFactory,
                proxyFactory,
                config.getBootContext().getViewMappingMap(),
                errors
        );

        ViewMetamodelImpl viewMetamodel = null;
        RuntimeException exception = null;

        try {
            viewMetamodel = new ViewMetamodelImpl(entityMetamodel, context, validateExpressions);
        } catch (RuntimeException ex) {
            exception = ex;
        }

        if (!errors.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("There are error(s) in entity views!");

            for (String error : errors) {
                sb.append('\n');
                sb.append(error);
            }

            throw new IllegalArgumentException(sb.toString(), exception);
        } else if (exception != null) {
            throw new IllegalArgumentException("An error happened during entity view metamodel building!", exception);
        }

        this.metamodel = viewMetamodel;
        this.supportsTransientReference = jpaProvider.supportsTransientEntityAsParameter();
        this.objectBuilderCache = new ConcurrentHashMap<>();
        this.entityViewUpdaterCache = new ConcurrentHashMap<>();
        this.contextAwareEntityViewUpdaterCache = new ConcurrentHashMap<>();
        this.entityViewMappers = new ConcurrentHashMap<>();
        this.filterMappings = new HashMap<>();
        registerFilterMappings();
        
        if (Boolean.valueOf(String.valueOf(config.getProperty(ConfigurationProperties.TEMPLATE_EAGER_LOADING)))) {
            for (ViewTypeImpl<?> view : metamodel.views()) {
                // TODO: Might be a good idea to let the view root be overridden or specified via the annotation
                String probableViewRoot = StringUtils.firstToLower(view.getEntityClass().getSimpleName());
                ExpressionFactory macroAwareExpressionFactory = context.createMacroAwareExpressionFactory(probableViewRoot);
                EmbeddingViewJpqlMacro embeddingViewJpqlMacro = (EmbeddingViewJpqlMacro) macroAwareExpressionFactory.getDefaultMacroConfiguration().get("EMBEDDING_VIEW").getState()[0];
                getTemplate(macroAwareExpressionFactory, view, null, null, null, embeddingViewJpqlMacro);

                for (MappingConstructor<?> constructor : view.getConstructors()) {
                    getTemplate(macroAwareExpressionFactory, view, (MappingConstructorImpl) constructor, null, null, embeddingViewJpqlMacro);
                }
            }
        } else if (Boolean.valueOf(String.valueOf(config.getProperty(ConfigurationProperties.PROXY_EAGER_LOADING)))) {
            // Loading template will always involve also loading the proxies, so we use else if
            for (ViewType<?> view : metamodel.getViews()) {
                proxyFactory.getProxy(this, (ManagedViewTypeImplementor<Object>) view, null);
            }
        }

        if (Boolean.valueOf(String.valueOf(config.getProperty(ConfigurationProperties.UPDATER_EAGER_LOADING)))) {
            for (ManagedViewType<?> view : metamodel.getManagedViews()) {
                getUpdater((ManagedViewTypeImplementor<?>) view, null);
            }
        }
    }

    public CriteriaBuilderFactory getCriteriaBuilderFactory() {
        return cbf;
    }

    @Override
    public ViewMetamodelImpl getMetamodel() {
        return metamodel;
    }

    public JpaProvider getJpaProvider() {
        return jpaProvider;
    }

    public DbmsDialect getDbmsDialect() {
        return dbmsDialect;
    }

    public AttributeAccessor getEntityIdAccessor() {
        return entityIdAccessor;
    }

    public ProxyFactory getProxyFactory() {
        return proxyFactory;
    }

    @Override
    public <T> T find(EntityManager entityManager, Class<T> entityViewClass, Object entityId) {
        return find(entityManager, EntityViewSetting.create(entityViewClass), entityId);
    }

    @Override
    public <T> T find(EntityManager entityManager, EntityViewSetting<T, CriteriaBuilder<T>> entityViewSetting, Object entityId) {
        ViewTypeImpl<T> managedViewType = metamodel.view(entityViewSetting.getEntityViewClass());
        EntityType<?> entityType = (EntityType<?>) managedViewType.getJpaManagedType();
        SingularAttribute<?, ?> idAttribute = JpaMetamodelUtils.getSingleIdAttribute(entityType);
        CriteriaBuilder<?> cb = cbf.create(entityManager, managedViewType.getEntityClass())
                .where(idAttribute.getName()).eq(entityId);
        List<T> resultList = applySetting(entityViewSetting, cb).getResultList();
        return resultList.isEmpty() ? null : resultList.get(0);
    }

    @Override
    public <T> T getReference(Class<T> entityViewClass, Object id) {
        // TODO: cache constructor
        ViewTypeImpl<T> managedViewType = metamodel.view(entityViewClass);
        Class<? extends T> proxyClass = proxyFactory.getProxy(this, managedViewType, null);
        try {
            return proxyClass.getConstructor(managedViewType.getIdAttribute().getConvertedJavaType()).newInstance(id);
        } catch (Exception e) {
            throw new IllegalArgumentException("Couldn't instantiate entity view object for type: " + entityViewClass.getName() + "\nDid you forget to add a no-args constructor to the view? Consider adding a no-args constructor annotated with @ViewConstructor(\"reference\").", e);
        }
    }

    @Override
    public <T> T create(Class<T> entityViewClass) {
        // TODO: cache constructor
        ManagedViewTypeImplementor<T> managedViewType = metamodel.managedView(entityViewClass);
        Class<? extends T> proxyClass = proxyFactory.getProxy(this, managedViewType, null);
        try {
            return proxyClass.getConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException("Couldn't instantiate entity view object for type: " + entityViewClass.getName() + "\nDid you forget to add a no-args constructor to the view? Consider adding a no-args constructor annotated with @ViewConstructor(\"create\").", e);
        }
    }

    @Override
    public <T> T convert(Object source, Class<T> entityViewClass, ConvertOption... convertOptions) {
        boolean ignoreMissingAttributes = false;
        boolean markNew = false;
        for (ConvertOption copyOption : convertOptions) {
            switch (copyOption) {
                case CREATE_NEW:
                    markNew = true;
                    break;
                case IGNORE_MISSING_ATTRIBUTES:
                    ignoreMissingAttributes = true;
                    break;
                default:
                    break;
            }
        }

        return convert(source, entityViewClass, ignoreMissingAttributes, markNew);
    }

    private <T> T convert(Object source, Class<T> entityViewClass, boolean ignoreMissingAttributes, boolean markNew) {
        if (!(source instanceof EntityViewProxy)) {
            throw new IllegalArgumentException("Can only convert one entity view to another. Invalid source type: " + source.getClass());
        }

        EntityViewProxy sourceProxy = (EntityViewProxy) source;
        ManagedViewTypeImplementor<Object> sourceViewType = (ManagedViewTypeImplementor<Object>) metamodel.managedView(sourceProxy.$$_getEntityViewClass());
        if (sourceViewType == null) {
            throw new IllegalArgumentException("Unknown source view type: " + sourceProxy.$$_getEntityViewClass().getName());
        }
        ManagedViewTypeImplementor<T> targetViewType = metamodel.managedView(entityViewClass);
        if (targetViewType == null) {
            throw new IllegalArgumentException("Unknown target view type: " + entityViewClass.getName());
        }
        ViewMapper<Object, T> viewMapper = getViewMapper(sourceViewType, targetViewType, ignoreMissingAttributes);
        T object = viewMapper.map(source);
        if (markNew) {
            if (!targetViewType.isCreatable()) {
                throw new IllegalArgumentException("Defined to convert to new object but target view type isn't annotated with @CreatableEntityView: " + entityViewClass.getName());
            }
            ((MutableStateTrackable) object).$$_setIsNew(true);
        }
        return object;
    }

    @SuppressWarnings("unchecked")
    public final <S, T> ViewMapper<S, T> getViewMapper(ManagedViewTypeImplementor<S> sourceViewType, ManagedViewTypeImplementor<T> targetViewType, boolean ignoreMissing) {
        ViewMapper.Key key = new ViewMapper.Key<>(sourceViewType, targetViewType, ignoreMissing);
        ViewMapper<?, ?> viewMapper = entityViewMappers.get(key);
        if (viewMapper == null) {
            viewMapper = new ViewMapper(sourceViewType, targetViewType, ignoreMissing, this, proxyFactory);
            ViewMapper<?, ?> old = entityViewMappers.putIfAbsent(key, viewMapper);
            if (old != null) {
                viewMapper = old;
            }
        }
        return (ViewMapper<S, T>) viewMapper;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> SingularChangeModel<T> getChangeModel(T entityView) {
        if (!(entityView instanceof DirtyStateTrackable)) {
            throw new IllegalArgumentException("Change model can only be accessed for updatable entity views that use dirty tracking! Switch to the LAZY or PARTIAL FlushMode instead!");
        }
        DirtyStateTrackable updatableProxy = (DirtyStateTrackable) entityView;
        Class<?> entityViewClass = updatableProxy.$$_getEntityViewClass();
        ManagedViewTypeImplementor<DirtyStateTrackable> viewType = (ManagedViewTypeImplementor<DirtyStateTrackable>) metamodel.managedView(entityViewClass);
        EntityViewUpdater updater = getUpdater(viewType, null);
        return (SingularChangeModel<T>) new ViewChangeModel<>(viewType, updatableProxy, updater.getDirtyChecker());
    }

    @Override
    public void update(EntityManager em, Object view) {
        update(em, view, false);
    }

    @Override
    public void updateFull(EntityManager em, Object view) {
        update(em, view, true);
    }

    @Override
    public void remove(EntityManager entityManager, Object view) {
        if (!(view instanceof EntityViewProxy)) {
            throw new IllegalArgumentException("Can't remove non entity view object: " + view);
        }
        DefaultUpdateContext context = new DefaultUpdateContext(this, entityManager, false);
        EntityViewProxy proxy = (EntityViewProxy) view;
        Class<?> entityViewClass = proxy.$$_getEntityViewClass();
        ManagedViewTypeImplementor<?> viewType = metamodel.managedView(entityViewClass);
        EntityViewUpdater updater = getUpdater(viewType, null);
        try {
            if (proxy.$$_isNew()) {
                MutableStateTrackable updatableProxy = (MutableStateTrackable) proxy;
                // If it has a parent, we can't just ignore this call
                if (updatableProxy.$$_hasParent()) {
                    throw new IllegalStateException("Can't remove not-yet-persisted object [" + view + "] that is referenced by: " + updatableProxy.$$_getParent());
                }
            } else {
                updater.remove(context, proxy);
            }
        } catch (Throwable t) {
            context.getSynchronizationStrategy().markRollbackOnly();
            ExceptionUtils.doThrow(t);
        }
    }

    @Override
    public void remove(EntityManager entityManager, Class<?> entityViewClass, Object viewId) {
        DefaultUpdateContext context = new DefaultUpdateContext(this, entityManager, false);
        ManagedViewTypeImplementor<?> viewType = metamodel.managedView(entityViewClass);
        if (viewType == null) {
            throw new IllegalArgumentException("Can't remove non entity view object: " + entityViewClass.getName());
        }
        EntityViewUpdater updater = getUpdater(viewType, null);
        try {
            updater.remove(context, viewId);
        } catch (Throwable t) {
            context.getSynchronizationStrategy().markRollbackOnly();
            ExceptionUtils.doThrow(t);
        }
    }

    public void update(EntityManager em, Object view, boolean forceFull) {
        update(new DefaultUpdateContext(this, em, forceFull), view);
    }
    
    public void update(UpdateContext context, Object view) {
        if (!(view instanceof MutableStateTrackable)) {
            throw new IllegalArgumentException("Can't update non-updatable entity views: " + view);
        }

        MutableStateTrackable updatableProxy = (MutableStateTrackable) view;
        Class<?> entityViewClass = updatableProxy.$$_getEntityViewClass();
        ManagedViewTypeImplementor<?> viewType = metamodel.managedView(entityViewClass);
        EntityViewUpdater updater = getUpdater(viewType, null);
        try {
            if (updatableProxy.$$_isNew()) {
                updater.executePersist(context, updatableProxy);
            } else {
                updater.executeUpdate(context, updatableProxy);
            }
        } catch (Throwable t) {
            context.getSynchronizationStrategy().markRollbackOnly();
            ExceptionUtils.doThrow(t);
        }
    }

    public Object persist(UpdateContext context, Object view) {
        if (!(view instanceof MutableStateTrackable)) {
            throw new IllegalArgumentException("Can't persist non-updatable entity views: " + view);
        }

        MutableStateTrackable updatableProxy = (MutableStateTrackable) view;
        Class<?> entityViewClass = updatableProxy.$$_getEntityViewClass();
        ManagedViewTypeImplementor<?> viewType = metamodel.managedView(entityViewClass);
        EntityViewUpdater updater = getUpdater(viewType, null);
        return updater.executePersist(context, updatableProxy);
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

    public boolean supportsTransientReference() {
        return supportsTransientReference;
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
        ManagedViewTypeImplementor<?> viewType = getMetamodel().managedView(clazz);
        if (viewType == null) {
            throw new IllegalArgumentException("There is no entity view for the class '" + clazz.getName() + "' registered!");
        }
        MappingConstructorImpl<?> mappingConstructor = (MappingConstructorImpl<?>) viewType.getConstructor(mappingConstructorName);
        String viewName;
        if (viewType instanceof ViewType<?>) {
            viewName = ((ViewType) viewType).getName();
        } else {
            viewName = viewType.getJavaType().getSimpleName();
        }
        return applyObjectBuilder(viewType, mappingConstructor, viewName, entityViewRoot, configuration.getCriteriaBuilder(), configuration, 0);
    }

    public String applyObjectBuilder(ManagedViewTypeImplementor<?> viewType, MappingConstructorImpl<?> mappingConstructor, String viewName, String entityViewRoot, FullQueryBuilder<?, ?> criteriaBuilder, EntityViewConfiguration configuration, int offset) {
        Path root = getPath(criteriaBuilder, entityViewRoot);
        String path = root.getPath();
        criteriaBuilder.selectNew(createObjectBuilder(viewType, mappingConstructor, viewName, root.getJavaType(), path, null, criteriaBuilder, configuration, offset, 0));
        return path;
    }

    public ObjectBuilder<?> createObjectBuilder(ManagedViewTypeImplementor<?> viewType, MappingConstructorImpl<?> mappingConstructor, String viewName, String entityViewRoot, String embeddingViewPath, FullQueryBuilder<?, ?> criteriaBuilder, EntityViewConfiguration configuration, int offset, int suffix) {
        Path root = getPath(criteriaBuilder, entityViewRoot);
        return createObjectBuilder(viewType, mappingConstructor, viewName, root.getJavaType(), root.getPath(), embeddingViewPath, criteriaBuilder, configuration, offset, suffix);
    }

    public ObjectBuilder<?> createObjectBuilder(ManagedViewTypeImplementor<?> viewType, MappingConstructorImpl<?> mappingConstructor, String viewName, Class<?> rootType, String entityViewRoot, String embeddingViewPath, FullQueryBuilder<?, ?> criteriaBuilder, EntityViewConfiguration configuration, int offset, int suffix) {
        ExpressionFactory ef = criteriaBuilder.getService(ExpressionFactory.class);
        if (!viewType.getEntityClass().isAssignableFrom(rootType)) {
            if (rootType.isAssignableFrom(viewType.getEntityClass())) {
                entityViewRoot = "TREAT(" + entityViewRoot + " AS " + metamodel.getEntityMetamodel().getEntity(viewType.getJavaType()).getName() + ")";
            } else {
                throw new IllegalArgumentException("The given view type with the entity type '" + viewType.getEntityClass().getName()
                        + "' can not be applied to the query builder with result type '" + rootType.getName() + "'");
            }
        }

        MacroConfiguration originalMacroConfiguration = ef.getDefaultMacroConfiguration();
        ExpressionFactory cachingExpressionFactory = ef.unwrap(AbstractCachingExpressionFactory.class);
        JpqlMacro viewRootJpqlMacro = new DefaultViewRootJpqlMacro(entityViewRoot);
        EmbeddingViewJpqlMacro embeddingViewJpqlMacro = configuration.getEmbeddingViewJpqlMacro();
        Map<String, MacroFunction> macros = new HashMap<>();
        macros.put("view_root", new JpqlMacroAdapter(viewRootJpqlMacro, cachingExpressionFactory));
        macros.put("embedding_view", new JpqlMacroAdapter(embeddingViewJpqlMacro, cachingExpressionFactory));
        MacroConfiguration macroConfiguration = originalMacroConfiguration.with(macros);
        ef = new MacroConfigurationExpressionFactory(cachingExpressionFactory, macroConfiguration);
        criteriaBuilder.registerMacro("view_root", viewRootJpqlMacro);

        return getTemplate(ef, viewType, mappingConstructor, viewName, entityViewRoot, embeddingViewPath, embeddingViewJpqlMacro, offset)
            .createObjectBuilder(criteriaBuilder, configuration.getOptionalParameters(), configuration, suffix);
    }

    private static Path getPath(FullQueryBuilder<?, ?> queryBuilder, String entityViewRoot) {
        return queryBuilder.getPath(entityViewRoot);
    }

    @SuppressWarnings("unchecked")
    public ViewTypeObjectBuilderTemplate<?> getTemplate(ExpressionFactory ef, ViewTypeImpl<?> viewType, MappingConstructorImpl<?> mappingConstructor, String entityViewRoot, String embeddingViewPath, EmbeddingViewJpqlMacro embeddingViewJpqlMacro) {
        return getTemplate(ef, viewType, mappingConstructor, viewType.getName(), entityViewRoot, embeddingViewPath, embeddingViewJpqlMacro, 0);
    }

    public ViewTypeObjectBuilderTemplate<?> getTemplate(ExpressionFactory ef, ManagedViewTypeImplementor<?> viewType, MappingConstructorImpl<?> mappingConstructor, String name, String entityViewRoot, String embeddingViewPath, EmbeddingViewJpqlMacro embeddingViewJpqlMacro, int offset) {
        ViewTypeObjectBuilderTemplate.Key key = new ViewTypeObjectBuilderTemplate.Key(ef, viewType, mappingConstructor, name, entityViewRoot, embeddingViewPath, offset);
        ViewTypeObjectBuilderTemplate<?> value = objectBuilderCache.get(key);

        if (value == null) {
            value = key.createValue(this, proxyFactory, embeddingViewJpqlMacro);
            ViewTypeObjectBuilderTemplate<?> oldValue = objectBuilderCache.putIfAbsent(key, value);

            if (oldValue != null) {
                value = oldValue;
            }
        }

        return value;
    }
    
    public EntityViewUpdater getUpdater(ManagedViewTypeImplementor<?> viewType, ManagedViewTypeImplementor<?> declaredViewType) {
        if (declaredViewType != null && declaredViewType != viewType) {
            ContextAwareUpdaterKey key = new ContextAwareUpdaterKey(viewType, declaredViewType);
            EntityViewUpdaterImpl value = contextAwareEntityViewUpdaterCache.get(key);

            if (value == null) {
                value = new EntityViewUpdaterImpl(this, viewType, declaredViewType);
                EntityViewUpdaterImpl oldValue = contextAwareEntityViewUpdaterCache.putIfAbsent(key, value);

                if (oldValue != null) {
                    value = oldValue;
                }
            }

            return value;
        } else {
            EntityViewUpdaterImpl value = entityViewUpdaterCache.get(viewType);

            if (value == null) {
                value = new EntityViewUpdaterImpl(this, viewType, null);
                EntityViewUpdaterImpl oldValue = entityViewUpdaterCache.putIfAbsent(viewType, value);

                if (oldValue != null) {
                    value = oldValue;
                }
            }

            return value;
        }
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

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    private static class ContextAwareUpdaterKey {
        private final ManagedViewTypeImplementor<?> viewType;
        private final ManagedViewTypeImplementor<?> declaredViewType;

        public ContextAwareUpdaterKey(ManagedViewTypeImplementor<?> viewType, ManagedViewTypeImplementor<?> declaredViewType) {
            this.viewType = viewType;
            this.declaredViewType = declaredViewType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            ContextAwareUpdaterKey that = (ContextAwareUpdaterKey) o;

            if (!viewType.equals(that.viewType)) {
                return false;
            }
            return declaredViewType.equals(that.declaredViewType);
        }

        @Override
        public int hashCode() {
            int result = viewType.hashCode();
            result = 31 * result + declaredViewType.hashCode();
            return result;
        }
    }
}
