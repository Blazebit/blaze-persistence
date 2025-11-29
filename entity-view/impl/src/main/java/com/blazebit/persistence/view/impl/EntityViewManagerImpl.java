/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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
import com.blazebit.persistence.spi.JpqlFunction;
import com.blazebit.persistence.spi.JpqlMacro;
import com.blazebit.persistence.spi.PackageOpener;
import com.blazebit.persistence.view.AttributeFilterProvider;
import com.blazebit.persistence.view.ConfigurationProperties;
import com.blazebit.persistence.view.ConvertOperationBuilder;
import com.blazebit.persistence.view.ConvertOption;
import com.blazebit.persistence.view.EntityViewBuilder;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.FlushOperationBuilder;
import com.blazebit.persistence.view.PostCommitListener;
import com.blazebit.persistence.view.PostPersistEntityListener;
import com.blazebit.persistence.view.PostPersistListener;
import com.blazebit.persistence.view.PostRemoveListener;
import com.blazebit.persistence.view.PostRollbackListener;
import com.blazebit.persistence.view.PostUpdateListener;
import com.blazebit.persistence.view.PrePersistEntityListener;
import com.blazebit.persistence.view.PrePersistListener;
import com.blazebit.persistence.view.PreRemoveListener;
import com.blazebit.persistence.view.PreUpdateListener;
import com.blazebit.persistence.view.SerializableEntityViewManager;
import com.blazebit.persistence.view.StaticBuilder;
import com.blazebit.persistence.view.StaticMetamodel;
import com.blazebit.persistence.view.StaticRelation;
import com.blazebit.persistence.view.ViewFilterProvider;
import com.blazebit.persistence.view.ViewTransition;
import com.blazebit.persistence.view.change.SingularChangeModel;
import com.blazebit.persistence.view.filter.BetweenFilter;
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
import com.blazebit.persistence.view.impl.filter.BetweenFilterImpl;
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
import com.blazebit.persistence.view.impl.mapper.ConvertOperationBuilderImpl;
import com.blazebit.persistence.view.impl.mapper.ViewMapper;
import com.blazebit.persistence.view.impl.metamodel.AbstractMethodAttribute;
import com.blazebit.persistence.view.impl.metamodel.ConstrainedAttribute;
import com.blazebit.persistence.view.impl.metamodel.ManagedViewTypeImpl;
import com.blazebit.persistence.view.impl.metamodel.ManagedViewTypeImplementor;
import com.blazebit.persistence.view.impl.metamodel.MappingConstructorImpl;
import com.blazebit.persistence.view.impl.metamodel.MetamodelBuildingContext;
import com.blazebit.persistence.view.impl.metamodel.MetamodelBuildingContextImpl;
import com.blazebit.persistence.view.impl.metamodel.ViewMetamodelImpl;
import com.blazebit.persistence.view.impl.metamodel.ViewTypeImpl;
import com.blazebit.persistence.view.impl.objectbuilder.ContainerAccumulator;
import com.blazebit.persistence.view.impl.objectbuilder.ViewTypeObjectBuilderTemplate;
import com.blazebit.persistence.view.impl.proxy.ProxyFactory;
import com.blazebit.persistence.view.impl.type.DefaultBasicUserTypeRegistry;
import com.blazebit.persistence.view.impl.update.DefaultUpdateContext;
import com.blazebit.persistence.view.impl.update.EntityViewUpdater;
import com.blazebit.persistence.view.impl.update.EntityViewUpdaterImpl;
import com.blazebit.persistence.view.impl.update.Listeners;
import com.blazebit.persistence.view.impl.update.SimpleUpdateContext;
import com.blazebit.persistence.view.impl.update.UpdateContext;
import com.blazebit.persistence.view.impl.update.flush.CompositeAttributeFlusher;
import com.blazebit.persistence.view.impl.update.listener.ViewInstancePostCommitListener;
import com.blazebit.persistence.view.impl.update.listener.ViewInstancePostPersistEntityListener;
import com.blazebit.persistence.view.impl.update.listener.ViewInstancePostRemoveListener;
import com.blazebit.persistence.view.impl.update.listener.ViewInstancePostRollbackListener;
import com.blazebit.persistence.view.impl.update.listener.ViewInstancePostUpdateListener;
import com.blazebit.persistence.view.impl.update.listener.ViewInstancePrePersistEntityListener;
import com.blazebit.persistence.view.impl.update.listener.ViewInstancePreRemoveListener;
import com.blazebit.persistence.view.impl.update.listener.ViewInstancePreUpdateListener;
import com.blazebit.persistence.view.metamodel.Attribute;
import com.blazebit.persistence.view.metamodel.AttributeFilterMapping;
import com.blazebit.persistence.view.metamodel.AttributePath;
import com.blazebit.persistence.view.metamodel.AttributePaths;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.MapAttribute;
import com.blazebit.persistence.view.metamodel.MappingConstructor;
import com.blazebit.persistence.view.metamodel.MethodAttribute;
import com.blazebit.persistence.view.metamodel.MethodMultiListAttribute;
import com.blazebit.persistence.view.metamodel.MethodMultiMapAttribute;
import com.blazebit.persistence.view.metamodel.MethodPluralAttribute;
import com.blazebit.persistence.view.metamodel.MethodSingularAttribute;
import com.blazebit.persistence.view.metamodel.PluralAttribute;
import com.blazebit.persistence.view.metamodel.SingularAttribute;
import com.blazebit.persistence.view.metamodel.ViewType;
import com.blazebit.persistence.view.spi.EmbeddingViewJpqlMacro;
import com.blazebit.persistence.view.spi.TransactionSupport;
import com.blazebit.persistence.view.spi.ViewJpqlMacro;
import com.blazebit.persistence.view.spi.type.DirtyStateTrackable;
import com.blazebit.persistence.view.spi.type.EntityViewProxy;
import com.blazebit.persistence.view.spi.type.MutableStateTrackable;
import com.blazebit.reflection.ReflectionUtils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.ManagedType;
import jakarta.persistence.metamodel.Metamodel;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
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

    private static final String META_MODEL_CLASS_NAME_SUFFIX = "_";
    private static final String RELATION_CLASS_NAME_SUFFIX = "Relation";
    private static final String MULTI_RELATION_CLASS_NAME_SUFFIX = "MultiRelation";
    private static final String BUILDER_CLASS_NAME_SUFFIX = "Builder";
    private static final Set<ViewTransition> VIEW_TRANSITIONS = EnumSet.allOf(ViewTransition.class);
    private static final Method SYNTHETIC_VERSION_GETTER;

    static {
        try {
            SYNTHETIC_VERSION_GETTER = EntityViewProxy.class.getMethod("$$_getVersion");
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private final CriteriaBuilderFactory cbf;
    private final JpaProvider jpaProvider;
    private final DbmsDialect dbmsDialect;
    private final ExpressionFactory expressionFactory;
    private final PackageOpener packageOpener;
    private final AttributeAccessor entityIdAccessor;
    private final ViewMetamodelImpl metamodel;
    private final ProxyFactory proxyFactory;
    private final TransactionSupport transactionSupport;
    private final Map<String, Object> optionalParameters;
    private final boolean supportsTransientReference;
    private final ConcurrentMap<ViewTypeObjectBuilderTemplate.Key, ViewTypeObjectBuilderTemplate<?>> objectBuilderCache;
    private final ConcurrentMap<ManagedViewType<?>, EntityViewUpdaterImpl> entityViewUpdaterCache;
    private final ConcurrentMap<ContextAwareUpdaterKey, EntityViewUpdaterImpl> contextAwareEntityViewUpdaterCache;
    private final ConcurrentMap<ViewMapper.Key<?, ?>, ViewMapper<?, ?>> entityViewMappers;
    private final ConcurrentMap<ViewMapperConfigKey, ViewMapper<?, ?>> configuredEntityViewMappers;
    private final ConcurrentMap<Class<?>, Constructor<?>> createConstructorCache;
    private final ConcurrentMap<Class<?>, Constructor<?>> referenceConstructorCache;
    private final ConcurrentMap<Class<?>, ListenerTypeInfo> listenerClassTypeInfo;
    private final ClassValue<EntityViewManager> serializableDelegates;
    private final Map<String, Class<? extends AttributeFilterProvider>> filterMappings;
    private final Map<Class<?>, Set<Class<?>>> javaTypeToManagedTypeJavaTypes;
    private final Map<Class<?>, Listeners> listeners; // A mapping from JPA managed type java type and entity view java type to listeners
    private final Map<Class<?>, Set<Class<?>>> convertibleManagedViewTypes;
    private final Map<ViewBuilderKey, Constructor<? extends EntityViewBuilder<?>>> viewBuilderClasses;
    private final boolean unsafeDisabled;
    private final boolean strictCascadingCheck;

    public EntityViewManagerImpl(EntityViewConfigurationImpl config, CriteriaBuilderFactory cbf) {
        this.cbf = cbf;
        this.jpaProvider = cbf.getService(JpaProvider.class);
        this.dbmsDialect = cbf.getService(DbmsDialect.class);
        EntityMetamodel entityMetamodel = cbf.getService(EntityMetamodel.class);
        this.expressionFactory = cbf.getService(ExpressionFactory.class);
        this.packageOpener = cbf.getService(PackageOpener.class);
        this.entityIdAccessor = new EntityIdAttributeAccessor(jpaProvider);
        this.unsafeDisabled = !Boolean.valueOf(String.valueOf(config.getProperty(ConfigurationProperties.PROXY_UNSAFE_ALLOWED)));
        this.strictCascadingCheck = Boolean.valueOf(String.valueOf(config.getProperty(ConfigurationProperties.UPDATER_STRICT_CASCADING_CHECK)));
        this.proxyFactory = new ProxyFactory(unsafeDisabled, strictCascadingCheck, packageOpener);
        this.transactionSupport = config.getTransactionSupport();
        this.optionalParameters = Collections.unmodifiableMap(new HashMap<>(config.getOptionalParameters()));
        this.serializableDelegates = new ClassValue<EntityViewManager>() {
            @Override
            protected EntityViewManager computeValue(Class<?> type) {
                try {
                    return (EntityViewManager) type.getField(SerializableEntityViewManager.SERIALIZABLE_EVM_FIELD_NAME).get(null);
                } catch (Exception e) {
                    throw new IllegalArgumentException(e);
                }
            }
        };

        boolean validateManagedTypes = !Boolean.valueOf(String.valueOf(config.getProperty(ConfigurationProperties.MANAGED_TYPE_VALIDATION_DISABLED)));
        boolean validateExpressions = !Boolean.valueOf(String.valueOf(config.getProperty(ConfigurationProperties.EXPRESSION_VALIDATION_DISABLED)));
        boolean scanStaticBuilder = !Boolean.valueOf(String.valueOf(config.getProperty(ConfigurationProperties.STATIC_BUILDER_SCANNING_DISABLED)));
        boolean scanStaticImplementations = !Boolean.valueOf(String.valueOf(config.getProperty(ConfigurationProperties.STATIC_IMPLEMENTATION_SCANNING_DISABLED)));
        boolean scanStaticMetamodels = !Boolean.valueOf(String.valueOf(config.getProperty(ConfigurationProperties.STATIC_METAMODEL_SCANNING_DISABLED)));

        Set<String> errors = config.getBootContext().getErrors();
        Map<String, JpqlFunction> functions = cbf.getRegisteredFunctions();
        Map<String, JpqlFunction> registeredFunctions = new HashMap<>(functions.size());
        for (Map.Entry<String, JpqlFunction> entry : functions.entrySet()) {
            registeredFunctions.put(entry.getKey().toLowerCase(), entry.getValue());
        }

        MetamodelBuildingContext context = new MetamodelBuildingContextImpl(
                config.getProperties(),
                new DefaultBasicUserTypeRegistry(config.getUserTypeRegistry(), cbf),
                entityMetamodel,
                jpaProvider,
                dbmsDialect,
                registeredFunctions,
                expressionFactory,
                proxyFactory,
                config.getBootContext().getViewMappingMap(),
                errors
        );

        ViewMetamodelImpl viewMetamodel = null;
        RuntimeException exception = null;
        Map<Class<?>, Object> typeTestValues = config.getTypeTestValues();

        try {
            viewMetamodel = new ViewMetamodelImpl(entityMetamodel, context, typeTestValues, validateManagedTypes, validateExpressions);
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
        this.configuredEntityViewMappers = new ConcurrentHashMap<>();
        this.createConstructorCache = new ConcurrentHashMap<>();
        this.referenceConstructorCache = new ConcurrentHashMap<>();
        this.listenerClassTypeInfo = new ConcurrentHashMap<>();
        this.filterMappings = new HashMap<>();
        registerFilterMappings();

        // Map all java types part of the JPA Managed types to JPA Managed types
        // We lookup the registration keys for listeners by java type with this map
        Map<Class<?>, Set<Class<?>>> javaTypeToManagedTypeJavaTypes = new HashMap<>();
        for (ManagedType<?> managedType : entityMetamodel.getManagedTypes()) {
            Class<?> javaType = managedType.getJavaType();
            if (javaType != null) {
                for (Class<?> superType : ReflectionUtils.getSuperTypes(javaType)) {
                    Set<Class<?>> classes = javaTypeToManagedTypeJavaTypes.get(superType);
                    if (classes == null) {
                        classes = new HashSet<>();
                        javaTypeToManagedTypeJavaTypes.put(superType, classes);
                    }
                    classes.add(javaType);
                }
            }
        }

        this.javaTypeToManagedTypeJavaTypes = javaTypeToManagedTypeJavaTypes;

        Map<Class<?>, Set<Class<?>>> convertibleManagedViewTypes = new HashMap<>();
        Map<Class<?>, Listeners> listeners = new HashMap<>();
        Map<ViewBuilderKey, Constructor<? extends EntityViewBuilder<?>>> viewBuilderConstructors = new HashMap<>();
        Map<Class<?>, Constructor<?>> relationConstructors = new HashMap<>(viewMetamodel.getManagedViews().size());
        Map<Class<?>, Constructor<?>> multiRelationConstructors = new HashMap<>(viewMetamodel.getManagedViews().size());
        for (ManagedViewType<?> managedView : viewMetamodel.getManagedViews()) {
            Class<?> javaType = managedView.getJavaType();
            Listeners l = new Listeners(managedView.getEntityClass());
            listeners.put(javaType, l);
            if (managedView.getPrePersistMethod() != null) {
                l.addPrePersistEntityListener(javaType, new ViewInstancePrePersistEntityListener(managedView.getPrePersistMethod()));
            }
            if (managedView.getPostPersistMethod() != null) {
                l.addPostPersistEntityListener(javaType, new ViewInstancePostPersistEntityListener(managedView.getPostPersistMethod()));
            }
            if (managedView.getPreUpdateMethod() != null) {
                l.addPreUpdateListener(javaType, new ViewInstancePreUpdateListener(managedView.getPreUpdateMethod()));
            }
            if (managedView.getPostUpdateMethod() != null) {
                l.addPostUpdateListener(javaType, new ViewInstancePostUpdateListener(managedView.getPostUpdateMethod()));
            }
            if (managedView.getPreRemoveMethod() != null) {
                l.addPreRemoveListener(javaType, new ViewInstancePreRemoveListener(managedView.getPreRemoveMethod()));
            }
            if (managedView.getPostRemoveMethod() != null) {
                l.addPostRemoveListener(javaType, new ViewInstancePostRemoveListener(managedView.getPostRemoveMethod()));
            }
            if (managedView.getPostCommitMethod() != null) {
                l.addPostCommitListener(javaType, new ViewInstancePostCommitListener(managedView.getPostCommitMethod()), managedView.getPostCommitTransitions());
            }
            if (managedView.getPostRollbackMethod() != null) {
                l.addPostRollbackListener(javaType, new ViewInstancePostRollbackListener(managedView.getPostRollbackMethod()), managedView.getPostCommitTransitions());
            }

            if (!javaType.isInterface() && !Modifier.isAbstract(javaType.getModifiers())) {
                proxyFactory.setImplementation(javaType);
            } else if (scanStaticImplementations) {
                proxyFactory.loadImplementation(errors, managedView, this);
            }
            if (scanStaticMetamodels) {
                initializeStaticMetamodel(errors, managedView, relationConstructors, multiRelationConstructors);
            }
            if (scanStaticBuilder) {
                initializeStaticBuilder(errors, managedView, viewBuilderConstructors);
            }

            HashSet<Class<?>> classes = new HashSet<>();
            convertibleManagedViewTypes.put(javaType, classes);

            for (ManagedViewType<?> targetType : viewMetamodel.getManagedViews()) {
                if (isConvertible(managedView, targetType)) {
                    classes.add(targetType.getJavaType());
                }
            }
        }

        if (!errors.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("There are error(s) in entity views!");

            for (String error : errors) {
                sb.append('\n');
                sb.append(error);
            }

            throw new IllegalArgumentException(sb.toString(), exception);
        }

        this.convertibleManagedViewTypes = convertibleManagedViewTypes;
        this.viewBuilderClasses = viewBuilderConstructors;

        for (Map.Entry<EntityViewListenerClassKey, EntityViewListenerFactory<?>> entry : config.getBootContext().getViewListeners().entrySet()) {
            for (Class<?> clazz : javaTypeToManagedTypeJavaTypes.get(entry.getKey().getEntityClass())) {
                Class<?> entityViewClass = entry.getKey().getEntityViewClass();
                ManagedViewTypeImplementor<?> managedView = metamodel.managedView(entityViewClass);
                // The entity class for which the view type is mapped, must be a super type of the entity type for which to register the listener
                // If the managed view is null i.e. maybe the type is Object, we want to register the listener for all entity and view types
                if (managedView != null && !managedView.getEntityClass().isAssignableFrom(clazz)) {
                    continue;
                }
                Listeners l = listeners.get(clazz);
                if (l == null) {
                    l = new Listeners(clazz);
                    listeners.put(clazz, l);
                }

                Object listener = entry.getValue().createListener();
                Class<?> kind = entry.getValue().getListenerKind();
                if (PrePersistListener.class == kind) {
                    l.addPrePersistListener(entityViewClass, (PrePersistListener<Object>) listener);
                } else if (PrePersistEntityListener.class == kind) {
                    l.addPrePersistEntityListener(entityViewClass, (PrePersistEntityListener<Object, Object>) listener);
                } else if (PostPersistListener.class == kind) {
                    l.addPostPersistListener(entityViewClass, (PostPersistListener<Object>) listener);
                } else if (PostPersistEntityListener.class == kind) {
                    l.addPostPersistEntityListener(entityViewClass, (PostPersistEntityListener<Object, Object>) listener);
                } else if (PreUpdateListener.class == kind) {
                    l.addPreUpdateListener(entityViewClass, (PreUpdateListener<Object>) listener);
                } else if (PostUpdateListener.class == kind) {
                    l.addPostUpdateListener(entityViewClass, (PostUpdateListener<Object>) listener);
                } else if (PreRemoveListener.class == kind) {
                    l.addPreRemoveListener(entityViewClass, (PreRemoveListener<Object>) listener);
                } else if (PostRemoveListener.class == kind) {
                    l.addPostRemoveListener(entityViewClass, (PostRemoveListener<Object>) listener);
                } else if (PostCommitListener.class == kind) {
                    l.addPostCommitListener(entityViewClass, (PostCommitListener<?>) listener, VIEW_TRANSITIONS);
                } else if (PostRollbackListener.class == kind) {
                    l.addPostRollbackListener(entityViewClass, (PostRollbackListener<?>) listener, VIEW_TRANSITIONS);
                }
            }
        }

        this.listeners = listeners;

        if (Boolean.valueOf(String.valueOf(config.getProperty(ConfigurationProperties.TEMPLATE_EAGER_LOADING)))) {
            for (ViewTypeImpl<?> view : metamodel.views()) {
                // TODO: Might be a good idea to let the view root be overridden or specified via the annotation
                String probableViewRoot = StringUtils.firstToLower(view.getEntityClass().getSimpleName());
                MacroConfigurationExpressionFactory macroAwareExpressionFactory = context.createMacroAwareExpressionFactory(probableViewRoot);
                ViewJpqlMacro viewJpqlMacro = (ViewJpqlMacro) macroAwareExpressionFactory.getDefaultMacroConfiguration().get("VIEW").getState()[0];
                EmbeddingViewJpqlMacro embeddingViewJpqlMacro = (EmbeddingViewJpqlMacro) macroAwareExpressionFactory.getDefaultMacroConfiguration().get("EMBEDDING_VIEW").getState()[0];
                getTemplate(macroAwareExpressionFactory, view, null, null, viewJpqlMacro, null, embeddingViewJpqlMacro);

                for (MappingConstructor<?> constructor : view.getConstructors()) {
                    getTemplate(macroAwareExpressionFactory, view, (MappingConstructorImpl) constructor, null, viewJpqlMacro, null, embeddingViewJpqlMacro);
                }
            }
        } else if (Boolean.valueOf(String.valueOf(config.getProperty(ConfigurationProperties.PROXY_EAGER_LOADING)))) {
            // Loading template will always involve also loading the proxies, so we use else if
            for (ViewType<?> view : metamodel.getViews()) {
                proxyFactory.getProxy(this, (ManagedViewTypeImplementor<Object>) view);
            }
        }

        if (Boolean.valueOf(String.valueOf(config.getProperty(ConfigurationProperties.UPDATER_EAGER_LOADING)))) {
            for (ManagedViewType<?> view : metamodel.getViews()) {
                getUpdater(null, (ManagedViewTypeImplementor<?>) view, null, null, null);
            }
        }
    }

    private static String getMetamodelClassName(Class<?> javaType) {
        return getGeneratedClassName(javaType, META_MODEL_CLASS_NAME_SUFFIX);
    }

    private static String getGeneratedClassName(Class<?> javaType, String suffix) {
        String fqcn = javaType.getName();
        StringBuilder sb = new StringBuilder(fqcn.length() + suffix.length());
        int i;
        if (javaType.getPackage() == null) {
            i = 0;
        } else {
            String packageName = javaType.getPackage().getName();
            sb.append(packageName).append('.');
            i = packageName.length() + 1;
        }
        for (; i < fqcn.length(); i++) {
            final char c = fqcn.charAt(i);
            if (c != '$') {
                sb.append(c);
            }
        }
        sb.append(suffix);
        return sb.toString();
    }

    private static String getRelationClassName(Class<?> javaType) {
        return getGeneratedClassName(javaType, RELATION_CLASS_NAME_SUFFIX);
    }

    private static String getMultiRelationClassName(Class<?> javaType) {
        return getGeneratedClassName(javaType, MULTI_RELATION_CLASS_NAME_SUFFIX);
    }

    private void initializeStaticMetamodel(Set<String> errors, ManagedViewType<?> managedView, Map<Class<?>, Constructor<?>> relationConstructors, Map<Class<?>, Constructor<?>> multiRelationConstructors) {
        Class<?> javaType = managedView.getJavaType();
        Class<?> metamodelClass;
        try {
            metamodelClass = javaType.getClassLoader().loadClass(getMetamodelClassName(javaType));
            StaticMetamodel annotation = metamodelClass.getAnnotation(StaticMetamodel.class);
            if (annotation != null) {
                if (annotation.value() != javaType) {
                    errors.add("The static metamodel class '" + metamodelClass.getName() + "' was expected to be defined for the entity view type '" + javaType.getName() + "' but was defined for: " + annotation.value().getName());
                    return;
                }
            }
            Class<?> relationClass = javaType.getClassLoader().loadClass(getRelationClassName(javaType));
            StaticRelation staticRelation = relationClass.getAnnotation(StaticRelation.class);
            if (staticRelation != null) {
                if (staticRelation.value() != javaType) {
                    errors.add("The static relation class '" + relationClass.getName() + "' was expected to be defined for the entity view type '" + javaType.getName() + "' but was defined for: " + annotation.value().getName());
                    return;
                }
            }
            Class<?> multiRelationClass = javaType.getClassLoader().loadClass(getMultiRelationClassName(javaType));
            StaticRelation staticMultiRelation = multiRelationClass.getAnnotation(StaticRelation.class);
            if (staticMultiRelation != null) {
                if (staticMultiRelation.value() != javaType) {
                    errors.add("The static relation class '" + multiRelationClass.getName() + "' was expected to be defined for the entity view type '" + javaType.getName() + "' but was defined for: " + annotation.value().getName());
                    return;
                }
            }
            if (!relationConstructors.containsKey(javaType)) {
                try {
                    relationConstructors.put(javaType, relationClass.getConstructor(AttributePath.class));
                } catch (NoSuchMethodException e) {
                    StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw));
                    errors.add("The initialization of the static relation class '" + relationClass.getName() + "' failed: " + sw.toString());
                }
            }
            if (!multiRelationConstructors.containsKey(javaType)) {
                try {
                    multiRelationConstructors.put(javaType, multiRelationClass.getConstructor(AttributePath.class));
                } catch (NoSuchMethodException e) {
                    StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw));
                    errors.add("The initialization of the static relation class '" + multiRelationClass.getName() + "' failed: " + sw.toString());
                }
            }
        } catch (ClassNotFoundException e) {
            // Ignore
            return;
        }
        try {
            for (MethodAttribute<?, ?> attribute : managedView.getAttributes()) {
                if (attribute.isSubview()) {
                    AttributePath<?, ?, ?> path;
                    Class<?> elementType;
                    if (attribute instanceof PluralAttribute<?, ?, ?>) {
                        elementType = ((PluralAttribute<?, ?, ?>) attribute).getElementType().getJavaType();
                        path = AttributePaths.of((MethodPluralAttribute<?, ?, ?>) attribute);
                    } else {
                        elementType = attribute.getJavaType();
                        path = AttributePaths.of((MethodSingularAttribute<?, ?>) attribute);
                    }
                    if (attribute instanceof MethodMultiListAttribute<?, ?, ?> || attribute instanceof MethodMultiMapAttribute<?, ?, ?, ?>) {
                        Constructor<?> multiRelationConstructor = multiRelationConstructors.get(elementType);
                        if (multiRelationConstructor == null) {
                            Class<?> relationClass = javaType.getClassLoader().loadClass(getMultiRelationClassName(elementType));
                            multiRelationConstructor = relationClass.getConstructor(AttributePath.class);
                            multiRelationConstructors.put(elementType, multiRelationConstructor);
                        }
                        metamodelClass.getDeclaredField(attribute.getName()).set(null, multiRelationConstructor.newInstance(path));
                    } else {
                        Constructor<?> relationConstructor = relationConstructors.get(elementType);
                        if (relationConstructor == null) {
                            Class<?> relationClass = javaType.getClassLoader().loadClass(getRelationClassName(elementType));
                            relationConstructor = relationClass.getConstructor(AttributePath.class);
                            relationConstructors.put(elementType, relationConstructor);
                        }
                        metamodelClass.getDeclaredField(attribute.getName()).set(null, relationConstructor.newInstance(path));
                    }
                } else if (!SYNTHETIC_VERSION_GETTER.equals(attribute.getJavaMethod())) {
                    // There is no field in the static metamodel for the synthetic version getter
                    metamodelClass.getDeclaredField(attribute.getName()).set(null, attribute);
                }
                for (AttributeFilterMapping<?, ?> filter : attribute.getFilters()) {
                    String name = attribute.getName() + "_";
                    if (filter.getName().isEmpty()) {
                        name += "filter";
                    } else {
                        name += filter.getName();
                    }
                    metamodelClass.getDeclaredField(name).set(null, filter);
                }
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            errors.add("The initialization of the static metamodel class '" + metamodelClass.getName() + "' failed: " + sw.toString());
        }
    }

    private static String getBuilderClassName(Class<?> javaType) {
        return getGeneratedClassName(javaType, BUILDER_CLASS_NAME_SUFFIX);
    }

    private static void initializeStaticBuilder(Set<String> errors, ManagedViewType<?> managedView, Map<ViewBuilderKey, Constructor<? extends EntityViewBuilder<?>>> viewBuilderConstructors) {
        Class<?> javaType = managedView.getJavaType();
        Class<?> builderClass;
        try {
            builderClass = javaType.getClassLoader().loadClass(getBuilderClassName(javaType));
            StaticBuilder annotation = builderClass.getAnnotation(StaticBuilder.class);
            if (annotation != null) {
                if (annotation.value() != javaType) {
                    errors.add("The static builder class '" + builderClass.getName() + "' was expected to be defined for the entity view type '" + javaType.getName() + "' but was defined for: " + annotation.value().getName());
                    return;
                }
            }
        } catch (ClassNotFoundException e) {
            // Ignore
            return;
        }
        try {
            int size = managedView.getConstructors().size();
            if (size == 0) {
                Class<?> constructorClass = javaType.getClassLoader().loadClass(builderClass.getName() + "$Init");
                viewBuilderConstructors.put(new ViewBuilderKey(managedView, null), (Constructor<? extends EntityViewBuilder<?>>) constructorClass.getDeclaredConstructor(Map.class));
            } else if (size == 1) {
                MappingConstructor<?> constructor = managedView.getConstructors().iterator().next();
                Class<?> constructorClass = javaType.getClassLoader().loadClass(builderClass.getName() + "$" + Character.toUpperCase(constructor.getName().charAt(0)) + constructor.getName().substring(1));
                viewBuilderConstructors.put(new ViewBuilderKey(managedView, null), (Constructor<? extends EntityViewBuilder<?>>) constructorClass.getDeclaredConstructor(Map.class));
                viewBuilderConstructors.put(new ViewBuilderKey(managedView, constructor), (Constructor<? extends EntityViewBuilder<?>>) constructorClass.getDeclaredConstructor(Map.class));
            } else {
                for (MappingConstructor<?> constructor : managedView.getConstructors()) {
                    Class<?> constructorClass = javaType.getClassLoader().loadClass(builderClass.getName() + "$" + Character.toUpperCase(constructor.getName().charAt(0)) + constructor.getName().substring(1));
                    viewBuilderConstructors.put(new ViewBuilderKey(managedView, constructor), (Constructor<? extends EntityViewBuilder<?>>) constructorClass.getDeclaredConstructor(Map.class));
                }
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            errors.add("The initialization of the static builder class '" + builderClass.getName() + "' failed: " + sw.toString());
        }
    }

    private boolean isConvertible(ManagedViewType<?> sourceType, ManagedViewType<?> targetType) {
        if (targetType.getJavaType().isAssignableFrom(sourceType.getJavaType())) {
            return true;
        }
        if (!targetType.getEntityClass().isAssignableFrom(sourceType.getEntityClass())) {
            return false;
        }
        Set<MethodAttribute<?, ?>> attributes = (Set<MethodAttribute<?, ?>>) targetType.getAttributes();
        if (attributes.size() > sourceType.getAttributes().size()) {
            return false;
        }
        for (MethodAttribute<?, ?> targetAttribute : attributes) {
            MethodAttribute<?, ?> sourceAttribute = sourceType.getAttribute(targetAttribute.getName());
            if (sourceAttribute == null) {
                return false;
            }
            if (targetAttribute.isCollection()) {
                if (!sourceAttribute.isCollection()) {
                    return false;
                }
                PluralAttribute<?, ?, ?> targetPluralAttribute = (PluralAttribute<?, ?, ?>) targetAttribute;
                PluralAttribute<?, ?, ?> sourcePluralAttribute = (PluralAttribute<?, ?, ?>) sourceAttribute;
                if (sourcePluralAttribute.getCollectionType() != targetPluralAttribute.getCollectionType()) {
                    return false;
                }
                if (targetAttribute.isSubview()) {
                    if (!sourceAttribute.isSubview() || !isConvertible((ManagedViewType<?>) sourcePluralAttribute.getElementType(), (ManagedViewType<?>) targetPluralAttribute.getElementType())) {
                        return false;
                    }

                    if (targetPluralAttribute.getCollectionType() == PluralAttribute.CollectionType.MAP) {
                        MapAttribute<?, ?, ?> targetMapAttr = (MapAttribute<?, ?, ?>) targetAttribute;
                        MapAttribute<?, ?, ?> sourceMapAttr = (MapAttribute<?, ?, ?>) sourceAttribute;
                        if (targetMapAttr.isKeySubview()) {
                            if (!sourceMapAttr.isKeySubview() || !isConvertible((ManagedViewType<?>) sourceMapAttr.getKeyType(), (ManagedViewType<?>) targetMapAttr.getKeyType())) {
                                return false;
                            }
                        } else {
                            if (sourceMapAttr.isKeySubview() || targetMapAttr.getKeyType().getConvertedType() != sourceMapAttr.getKeyType().getConvertedType()) {
                                return false;
                            }
                        }
                    }
                } else {
                    if (sourceAttribute.isSubview() || targetPluralAttribute.getElementType().getConvertedType() != sourcePluralAttribute.getElementType().getConvertedType()) {
                        return false;
                    }
                }
            } else if (targetAttribute.isSubview()) {
                return sourceAttribute.isSubview() && !sourceAttribute.isCollection() && isConvertible((ManagedViewType<?>) ((SingularAttribute<?, ?>) sourceAttribute).getType(), (ManagedViewType<?>) ((SingularAttribute<?, ?>) targetAttribute).getType());
            } else if (targetAttribute.getConvertedJavaType() != sourceAttribute.getConvertedJavaType()) {
                return false;
            }
        }
        return true;
    }

    public CriteriaBuilderFactory getCriteriaBuilderFactory() {
        return cbf;
    }

    public Map<Class<?>, Listeners> getListeners() {
        return listeners;
    }

    public Map<Class<?>, Set<Class<?>>> getConvertibleManagedViewTypes() {
        return convertibleManagedViewTypes;
    }

    public ManagedViewType<?> getListenerManagedView(Class<?> listenerClass, Class<?> listenerKindClass) {
        return getListenerTypeArguments(listenerClass, listenerKindClass).managedViewType;
    }

    public Class<?> getListenerEntityClass(Class<?> listenerClass, Class<?> listenerKindClass) {
        return getListenerTypeArguments(listenerClass, listenerKindClass).entityClass;
    }

    private ListenerTypeInfo getListenerTypeArguments(Class<?> listenerClass, Class<?> listenerKindClass) {
        ListenerTypeInfo listenerTypeInfo = listenerClassTypeInfo.get(listenerClass);
        if (listenerTypeInfo == null) {
            TypeVariable<? extends Class<?>>[] typeParameters = listenerKindClass.getTypeParameters();
            Class<?> entityViewClass = ReflectionUtils.resolveTypeVariable(listenerClass, typeParameters[0]);
            ManagedViewTypeImplementor<?> managedViewType = metamodel.managedView(entityViewClass);
            if (managedViewType == null) {
                throw new IllegalArgumentException("Lifecycle listener class " + listenerClass.getName() + " uses a non-registered entity view type: " + entityViewClass.getName());
            }
            Class<?> entityClass = null;
            if (typeParameters.length > 1) {
                entityClass = ReflectionUtils.resolveTypeVariable(listenerClass, typeParameters[1]);
                entityClass = getCommonClass(entityClass, managedViewType.getEntityClass());
            }

            listenerClassTypeInfo.putIfAbsent(listenerClass, listenerTypeInfo = new ListenerTypeInfo(managedViewType, entityClass));
        }
        return listenerTypeInfo;
    }

    private static Class<?> getCommonClass(Class<?> class1, Class<?> class2) {
        if (class2.isAssignableFrom(class1)) {
            return class2;
        } else if (class1.isAssignableFrom(class2)) {
            return class1;
        } else {
            throw new IllegalArgumentException("The classes [" + class1.getName() + ", " + class2.getName()
                    + "] are not in a inheritance relationship, so there is no common class!");
        }
    }

    public Set<Class<?>> getJavaTypeToManagedTypeJavaTypes(Class<?> javaType) {
        Set<Class<?>> classes = javaTypeToManagedTypeJavaTypes.get(javaType);
        if (classes == null) {
            return Collections.emptySet();
        }
        return classes;
    }

    @Override
    public <T> T getService(Class<T> serviceClass) {
        if (Metamodel.class.isAssignableFrom(serviceClass)) {
            return (T) metamodel.getEntityMetamodel();
        } else if (TransactionSupport.class.isAssignableFrom(serviceClass)) {
            return (T) transactionSupport;
        } else if (CriteriaBuilderFactory.class.isAssignableFrom(serviceClass)) {
            return (T) cbf;
        }
        return cbf.getService(serviceClass);
    }

    @Override
    public ViewMetamodelImpl getMetamodel() {
        return metamodel;
    }

    @Override
    public Map<String, Object> getOptionalParameters() {
        return optionalParameters;
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
        ViewTypeImpl<T> managedViewType = metamodel.viewOrError(entityViewSetting.getEntityViewClass());
        EntityType<?> entityType = (EntityType<?>) managedViewType.getJpaManagedType();
        jakarta.persistence.metamodel.SingularAttribute<?, ?> idAttribute = JpaMetamodelUtils.getSingleIdAttribute(entityType);
        CriteriaBuilder<?> cb = cbf.create(entityManager, managedViewType.getEntityClass())
                .where(idAttribute.getName()).eq(entityId);
        List<T> resultList = applySetting(entityViewSetting, cb).getResultList();
        return resultList.isEmpty() ? null : resultList.get(0);
    }

    @Override
    public <T> T getReference(Class<T> entityViewClass, Object id) {
        Constructor<T> constructor = (Constructor<T>) referenceConstructorCache.get(entityViewClass);
        try {
            ViewTypeImpl<T> managedViewType = metamodel.viewOrError(entityViewClass);
            if (constructor == null) {
                Class<? extends T> proxyClass = proxyFactory.getProxy(this, managedViewType);
                constructor = (Constructor<T>) proxyClass.getConstructor(managedViewType.getIdAttribute().getConvertedJavaType());
                referenceConstructorCache.put(entityViewClass, constructor);
            }
            if (!managedViewType.getIdAttribute().getJavaType().isInstance(id)) {
                id = getUpdater(null, managedViewType, null, null, null).getFullGraphNode().createViewIdByEntityId(id);
            }
            return constructor.newInstance(id);
        } catch (Exception e) {
            throw new IllegalArgumentException("Couldn't instantiate entity view object for type: " + entityViewClass.getName() + "\nDid you forget to add a no-args constructor to the view? Consider adding a no-args constructor annotated with @ViewConstructor(\"reference\").", e);
        }
    }

    public Object getEntityId(EntityManager entityManager, EntityViewProxy proxy) {
        UpdateContext context = new SimpleUpdateContext(this, entityManager);
        Class<?> entityViewClass = proxy.$$_getEntityViewClass();
        ManagedViewTypeImplementor<?> viewType = metamodel.managedViewOrError(entityViewClass);
        EntityViewUpdater updater = getUpdater(null, viewType, null, null, null);
        return ((CompositeAttributeFlusher) updater.getFullGraphNode()).getEntityIdCopy(context, proxy);
    }

    @Override
    public <T> T getEntityReference(EntityManager entityManager, Object view) {
        if (!(view instanceof EntityViewProxy)) {
            throw new IllegalArgumentException("Can't retrieve entity reference for non entity view object: " + view);
        }
        EntityViewProxy proxy = (EntityViewProxy) view;
        return (T) entityManager.getReference(proxy.$$_getJpaManagedClass(), getEntityId(entityManager, proxy));
    }

    @Override
    public <T> T create(Class<T> entityViewClass) {
        return create0(entityViewClass, optionalParameters);
    }

    @Override
    public <T> T create(Class<T> entityViewClass, Map<String, Object> optionalParameters) {
        Map<String, Object> map = new HashMap<>(this.optionalParameters);
        map.putAll(optionalParameters);
        return create0(entityViewClass, Collections.unmodifiableMap(map));
    }

    public <T> T create0(Class<T> entityViewClass, Map<String, Object> optionalParameters) {
        Constructor<T> constructor = (Constructor<T>) createConstructorCache.get(entityViewClass);
        CREATION: try {
            if (constructor == null) {
                ManagedViewTypeImplementor<T> managedViewType = metamodel.managedViewOrError(entityViewClass);
                if (!managedViewType.isCreatable()) {
                    break CREATION;
                }
                Class<? extends T> proxyClass = proxyFactory.getProxy(this, managedViewType);
                constructor = (Constructor<T>) proxyClass.getConstructor(proxyClass, Map.class);
                createConstructorCache.put(entityViewClass, constructor);
            }
            return constructor.newInstance(null, optionalParameters);
        } catch (Exception e) {
            throw new IllegalArgumentException("Couldn't instantiate entity view object for type: " + entityViewClass.getName() + "\nDid you forget to add a no-args constructor to the view? Consider adding a no-args constructor annotated with @ViewConstructor(\"create\").", e);
        }

        throw new IllegalArgumentException("Can't create instance of non-creatable view type: " + entityViewClass.getName() + "\nDid you forget to annotate it with @CreatableEntityView?");
    }

    @Override
    public <X> EntityViewBuilder<X> createBuilder(Class<X> clazz) {
        return createBuilder(clazz, null, null);
    }

    @Override
    public <X> EntityViewBuilder<X> createBuilder(Class<X> clazz, String constructorName) {
        return createBuilder(clazz, null, constructorName);
    }

    @Override
    public <X> EntityViewBuilder<X> createBuilder(Class<X> clazz, Map<String, Object> optionalParameters) {
        return createBuilder(clazz, optionalParameters, null);
    }

    @Override
    public <X> EntityViewBuilder<X> createBuilder(Class<X> clazz, Map<String, Object> optionalParameters, String constructorName) {
        ManagedViewTypeImplementor<X> managedViewTypeImplementor = metamodel.managedViewOrError(clazz);
        MappingConstructorImpl<X> mappingConstructor = null;
        if (constructorName != null) {
            mappingConstructor = (MappingConstructorImpl<X>) managedViewTypeImplementor.getConstructor(constructorName);
        }
        if (optionalParameters == null || optionalParameters.isEmpty()) {
            optionalParameters = this.optionalParameters;
        } else {
            Map<String, Object> tempOptionalParameters = optionalParameters;
            optionalParameters = new HashMap<>(optionalParameters.size() + this.optionalParameters.size());
            optionalParameters.putAll(this.optionalParameters);
            optionalParameters.putAll(tempOptionalParameters);
        }
        Constructor<? extends EntityViewBuilder<?>> viewBuilderConstructor = viewBuilderClasses.get(new ViewBuilderKey(managedViewTypeImplementor, mappingConstructor));
        if (viewBuilderConstructor == null) {
            return new EntityViewBuilderImpl<>(this, managedViewTypeImplementor, mappingConstructor, optionalParameters);
        }
        try {
            return (EntityViewBuilder<X>) viewBuilderConstructor.newInstance(optionalParameters);
        } catch (Exception e) {
            throw new RuntimeException("Could not construct view builder for entity view: " + clazz.getName(), e);
        }
    }

    @Override
    public <X> EntityViewBuilder<X> createBuilder(X view) {
        return createBuilder(view, null, null);
    }

    @Override
    public <X> EntityViewBuilder<X> createBuilder(X view, String constructorName) {
        return createBuilder(view, null, constructorName);
    }

    @Override
    public <X> EntityViewBuilder<X> createBuilder(X view, Map<String, Object> optionalParameters) {
        return createBuilder(view, optionalParameters, null);
    }

    @Override
    public <X> EntityViewBuilder<X> createBuilder(X view, Map<String, Object> optionalParameters, String constructorName) {
        @SuppressWarnings("unchecked")
        Class<X> clazz = (Class<X>) ((EntityViewProxy) view).$$_getEntityViewClass();
        EntityViewBuilder<X> builder = createBuilder(clazz, optionalParameters, constructorName);
        if (builder instanceof EntityViewBuilderImpl<?>) {
            EntityViewBuilderImpl<X> builderImpl = (EntityViewBuilderImpl<X>) builder;
            ManagedViewTypeImpl.InheritanceSubtypeConfiguration<X> inheritanceSubtypeConfiguration = builderImpl.getManagedViewType().getInheritanceSubtypeConfiguration(null);
            Object[] tuple = builderImpl.getTuple();
            for (ConstrainedAttribute<AbstractMethodAttribute<? super X, ?>> value : inheritanceSubtypeConfiguration.getAttributesClosure().values()) {
                tuple[value.getAttribute().getAttributeIndex()] = getValueForBuilder(value, view, optionalParameters);
            }
        } else {
            ManagedViewTypeImplementor<X> managedViewTypeImplementor = metamodel.managedViewOrError(clazz);
            ManagedViewTypeImpl.InheritanceSubtypeConfiguration<X> inheritanceSubtypeConfiguration = managedViewTypeImplementor.getInheritanceSubtypeConfiguration(null);
            for (ConstrainedAttribute<AbstractMethodAttribute<? super X, ?>> value : inheritanceSubtypeConfiguration.getAttributesClosure().values()) {
                builder.with(value.getAttribute().getName(), getValueForBuilder(value, view, optionalParameters));
            }
        }
        return builder;
    }

    private static <X> Object getValueForBuilder(ConstrainedAttribute<AbstractMethodAttribute<? super X, ?>> value, X view, Map<String, Object> optionalParameters) {
        Object newValue;
        if (value.getAttribute().isCollection()) {
            ContainerAccumulator<Object> containerAccumulator = (ContainerAccumulator<Object>) value.getAttribute().getContainerAccumulator();
            newValue = containerAccumulator.createContainer(value.getAttribute().needsDirtyTracker(), 0);
            containerAccumulator.addAll(newValue, value.getAttribute().getValue(view), value.getAttribute().needsDirtyTracker());
        } else {
            newValue = value.getAttribute().getValue(view);
            if (newValue == null && value.getAttribute().getMappingType() == Attribute.MappingType.PARAMETER) {
                newValue = optionalParameters.get(value.getAttribute().getMapping());
            }
        }
        return newValue;
    }

    @Override
    public <T> T convert(Object source, Class<T> entityViewClass, ConvertOption... convertOptions) {
        return getViewMapper(ViewMapper.Key.create(metamodel, source, entityViewClass, null, convertOptions)).map(source, optionalParameters);
    }

    @Override
    public <T> T convert(Object source, Class<T> entityViewClass, String constructorName, ConvertOption... convertOptions) {
        return getViewMapper(ViewMapper.Key.create(metamodel, source, entityViewClass, constructorName, convertOptions)).map(source, optionalParameters);
    }

    @Override
    public <T> T convert(Object source, Class<T> entityViewClass, Map<String, Object> optionalParameters, ConvertOption... convertOptions) {
        Map<String, Object> map = new HashMap<>(this.optionalParameters);
        map.putAll(optionalParameters);
        return getViewMapper(ViewMapper.Key.create(metamodel, source, entityViewClass, null, convertOptions)).map(source, Collections.unmodifiableMap(map));
    }

    @Override
    public <T> T convert(Object source, Class<T> entityViewClass, String constructorName, Map<String, Object> optionalParameters, ConvertOption... convertOptions) {
        Map<String, Object> map = new HashMap<>(this.optionalParameters);
        map.putAll(optionalParameters);
        return getViewMapper(ViewMapper.Key.create(metamodel, source, entityViewClass, constructorName, convertOptions)).map(source, Collections.unmodifiableMap(map));
    }

    @Override
    public <T> ConvertOperationBuilder<T> convertWith(Object source, Class<T> entityViewClass, ConvertOption... convertOptions) {
        return new ConvertOperationBuilderImpl<>(this, ViewMapper.Key.create(metamodel, source, entityViewClass, null, convertOptions), source, optionalParameters);
    }

    @Override
    public <T> ConvertOperationBuilder<T> convertWith(Object source, Class<T> entityViewClass, String constructorName, ConvertOption... convertOptions) {
        return new ConvertOperationBuilderImpl<>(this, ViewMapper.Key.create(metamodel, source, entityViewClass, constructorName, convertOptions), source, optionalParameters);
    }

    @Override
    public <T> ConvertOperationBuilder<T> convertWith(Object source, Class<T> entityViewClass, Map<String, Object> optionalParameters, ConvertOption... convertOptions) {
        Map<String, Object> map = new HashMap<>(this.optionalParameters);
        map.putAll(optionalParameters);
        return new ConvertOperationBuilderImpl<>(this, ViewMapper.Key.create(metamodel, source, entityViewClass, null, convertOptions), source, Collections.unmodifiableMap(map));
    }

    @Override
    public <T> ConvertOperationBuilder<T> convertWith(Object source, Class<T> entityViewClass, String constructorName, Map<String, Object> optionalParameters, ConvertOption... convertOptions) {
        Map<String, Object> map = new HashMap<>(this.optionalParameters);
        map.putAll(optionalParameters);
        return new ConvertOperationBuilderImpl<>(this, ViewMapper.Key.create(metamodel, source, entityViewClass, constructorName, convertOptions), source, Collections.unmodifiableMap(map));
    }

    @SuppressWarnings("unchecked")
    public final <S, T> ViewMapper<S, T> getViewMapper(ViewMapper.Key<Object, T> key) {
        ViewMapper<?, ?> viewMapper = entityViewMappers.get(key);
        if (viewMapper == null) {
            viewMapper = key.createViewMapper(this, proxyFactory, Collections.<String, ViewMapper.Key<Object, Object>>emptyMap());
            ViewMapper<?, ?> old = entityViewMappers.putIfAbsent(key, viewMapper);
            if (old != null) {
                viewMapper = old;
            }
        }
        return (ViewMapper<S, T>) viewMapper;
    }

    @SuppressWarnings("unchecked")
    public final <S, T> ViewMapper<S, T> getViewMapper(ViewMapper.Key<Object, T> viewMapperKey, Map<String, ViewMapper.Key<Object, Object>> subMappers) {
        ViewMapperConfigKey key = new ViewMapperConfigKey(viewMapperKey, subMappers);
        ViewMapper<?, ?> viewMapper = configuredEntityViewMappers.get(key);
        if (viewMapper == null) {
            viewMapper = key.key.createViewMapper(this, proxyFactory, key.subMappers);
            ViewMapper<?, ?> old = configuredEntityViewMappers.putIfAbsent(key, viewMapper);
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
        ManagedViewTypeImplementor<DirtyStateTrackable> viewType = (ManagedViewTypeImplementor<DirtyStateTrackable>) metamodel.managedViewOrError(entityViewClass);
        EntityViewUpdater updater = getUpdater(null, viewType, null, null, null);
        return (SingularChangeModel<T>) new ViewChangeModel<>(viewType, updatableProxy, updater.getDirtyChecker());
    }

    @Override
    public void update(EntityManager entityManager, Object view) {
        save(entityManager, view);
    }

    @Override
    public void updateFull(EntityManager entityManager, Object view) {
        saveFull(entityManager, view);
    }

    @Override
    public void save(EntityManager em, Object view) {
        update(em, view, false);
    }

    @Override
    public void saveFull(EntityManager em, Object view) {
        update(em, view, true);
    }

    @Override
    public void saveTo(EntityManager em, Object view, Object entity) {
        updateTo(em, view, entity, false);
    }

    @Override
    public void saveFullTo(EntityManager em, Object view, Object entity) {
        updateTo(em, view, entity, true);
    }

    @Override
    public void remove(EntityManager entityManager, Object view) {
        remove(new DefaultUpdateContext(this, entityManager, false, false, true, null, view, null), view);
    }

    public void remove(UpdateContext context, Object view) {
        if (!(view instanceof EntityViewProxy)) {
            throw new IllegalArgumentException("Can't remove non entity view object: " + view);
        }
        EntityViewProxy proxy = (EntityViewProxy) view;
        Class<?> entityViewClass = proxy.$$_getEntityViewClass();
        ManagedViewTypeImplementor<?> viewType = metamodel.managedViewOrError(entityViewClass);
        EntityViewUpdater updater = getUpdater(null, viewType, null, null, null);
        try {
            if (proxy.$$_isNew()) {
                MutableStateTrackable updatableProxy = (MutableStateTrackable) proxy;
                // If it has a parent, we can't just ignore this call
                if (updatableProxy.$$_hasParent()) {
                    throw new IllegalStateException("Can't remove not-yet-persisted object [" + view + "] that is referenced by: " + updatableProxy.$$_getParent());
                }
            } else {
                if (proxy instanceof MutableStateTrackable) {
                    MutableStateTrackable updatableProxy = (MutableStateTrackable) proxy;
                    if (updatableProxy.$$_hasParent()) {
                        throw new IllegalStateException("Can't remove object [" + view + "] that is still referenced by: " + updatableProxy.$$_getParent());
                    }
                }
                updater.remove(context, proxy);
            }
        } catch (Throwable t) {
            context.getTransactionAccess().markRollbackOnly();
            ExceptionUtils.doThrow(t);
        }
    }

    @Override
    public void remove(EntityManager entityManager, Class<?> entityViewClass, Object viewId) {
        remove(new DefaultUpdateContext(this, entityManager, false, false, true, entityViewClass, viewId, null), entityViewClass, viewId);
    }

    public void remove(UpdateContext context, Class<?> entityViewClass, Object viewId) {
        ManagedViewTypeImplementor<?> viewType = metamodel.managedView(entityViewClass);
        if (viewType == null) {
            throw new IllegalArgumentException("Can't remove non entity view object: " + entityViewClass.getName());
        }
        EntityViewUpdater updater = getUpdater(null, viewType, null, null, null);
        try {
            updater.remove(context, viewId);
        } catch (Throwable t) {
            context.getTransactionAccess().markRollbackOnly();
            ExceptionUtils.doThrow(t);
        }
    }

    @Override
    public FlushOperationBuilder removeWith(EntityManager entityManager, Object view) {
        return new DefaultUpdateContext(this, entityManager, false, false, true, null, view, null);
    }

    @Override
    public FlushOperationBuilder removeWith(EntityManager entityManager, Class<?> entityViewClass, Object viewId) {
        return new DefaultUpdateContext(this, entityManager, false, false, true, entityViewClass, viewId, null);
    }

    public void update(EntityManager em, Object view, boolean forceFull) {
        update(new DefaultUpdateContext(this, em, forceFull, false, false, null, view, null), view);
    }

    public void updateTo(EntityManager em, Object view, Object entity, boolean forceFull) {
        updateTo(new DefaultUpdateContext(this, em, forceFull, true, false, null, view, null), view, entity);
    }

    @Override
    public FlushOperationBuilder saveWith(EntityManager em, Object view) {
        return new DefaultUpdateContext(this, em, false, false, false, null, view, null);
    }

    @Override
    public FlushOperationBuilder saveFullWith(EntityManager em, Object view) {
        return new DefaultUpdateContext(this, em, true, false, false, null, view, null);
    }

    @Override
    public FlushOperationBuilder saveWithTo(EntityManager em, Object view, Object entity) {
        return new DefaultUpdateContext(this, em, false, true, false, null, view, entity);
    }

    @Override
    public FlushOperationBuilder saveFullWithTo(EntityManager em, Object view, Object entity) {
        return new DefaultUpdateContext(this, em, true, true, false, null, view, entity);
    }
    
    public void update(UpdateContext context, Object view) {
        if (!(view instanceof MutableStateTrackable)) {
            throw new IllegalArgumentException("Can't update non-updatable entity views: " + view);
        }

        MutableStateTrackable updatableProxy = (MutableStateTrackable) view;
        Class<?> entityViewClass = updatableProxy.$$_getEntityViewClass();
        ManagedViewTypeImplementor<?> viewType = metamodel.managedViewOrError(entityViewClass);
        EntityViewUpdater updater = getUpdater(null, viewType, null, null, null);
        try {
            if (updatableProxy.$$_isNew()) {
                updater.executePersist(context, updatableProxy);
            } else {
                updater.executeUpdate(context, updatableProxy);
            }
        } catch (Throwable t) {
            context.getTransactionAccess().markRollbackOnly();
            ExceptionUtils.doThrow(t);
        }
    }

    public void updateTo(UpdateContext context, Object view, Object entity) {
        if (!(view instanceof MutableStateTrackable)) {
            throw new IllegalArgumentException("Can't update non-updatable entity views: " + view);
        }

        MutableStateTrackable updatableProxy = (MutableStateTrackable) view;
        if (updatableProxy.$$_isNew()) {
            throw new IllegalArgumentException("Can't flush new entity view to existing entity: " + view);
        }
        Class<?> entityViewClass = updatableProxy.$$_getEntityViewClass();
        ManagedViewTypeImplementor<?> viewType = metamodel.managedViewOrError(entityViewClass);
        EntityViewUpdater updater = getUpdater(null, viewType, null, null, null);
        try {
            updater.executeUpdate(context, entity, updatableProxy);
        } catch (Throwable t) {
            context.getTransactionAccess().markRollbackOnly();
            ExceptionUtils.doThrow(t);
        }
    }

    public Object persist(UpdateContext context, Object view) {
        if (!(view instanceof MutableStateTrackable)) {
            throw new IllegalArgumentException("Can't persist non-updatable entity views: " + view);
        }

        MutableStateTrackable updatableProxy = (MutableStateTrackable) view;
        Class<?> entityViewClass = updatableProxy.$$_getEntityViewClass();
        ManagedViewTypeImplementor<?> viewType = metamodel.managedViewOrError(entityViewClass);
        EntityViewUpdater updater = getUpdater(null, viewType, null, null, null);
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
    public <T extends AttributeFilterProvider<?>> T createAttributeFilter(Class<T> filterClass, Class<?> expectedType, Object argument) {
        @SuppressWarnings("unchecked")
        Class<T> filterClassImpl = (Class<T>) filterMappings.get(filterClass.getName());

        if (filterClassImpl == null) {
            return createFilterInstance(filterClass, expectedType, argument);
        } else {
            return createFilterInstance(filterClassImpl, expectedType, argument);
        }
    }

    private <T extends AttributeFilterProvider<?>> T createFilterInstance(Class<T> filterClass, Class<?> expectedType, Object argument) {
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

    public ObjectBuilder<?> createObjectBuilder(ManagedViewTypeImplementor<?> viewType, MappingConstructorImpl<?> mappingConstructor, String entityViewRoot, String embeddingViewPath, FullQueryBuilder<?, ?> criteriaBuilder, EntityViewConfiguration configuration, int offset, int suffix, boolean nullFlatViewIfEmpty) {
        Path root = getPath(criteriaBuilder, entityViewRoot);
        return createObjectBuilder(viewType, mappingConstructor, root.getJavaType(), root.getPath(), embeddingViewPath, criteriaBuilder, configuration, offset, suffix, nullFlatViewIfEmpty);
    }

    public ObjectBuilder<?> createObjectBuilder(ManagedViewTypeImplementor<?> viewType, MappingConstructorImpl<?> mappingConstructor, Class<?> rootType, String entityViewRoot, String embeddingViewPath, FullQueryBuilder<?, ?> criteriaBuilder, EntityViewConfiguration configuration, int offset, int suffix, boolean nullFlatViewIfEmpty) {
        ExpressionFactory ef = criteriaBuilder.getService(ExpressionFactory.class);
        if (!viewType.getEntityClass().isAssignableFrom(rootType)) {
            if (rootType.isAssignableFrom(viewType.getEntityClass())) {
                entityViewRoot = "TREAT(" + entityViewRoot + " AS " + metamodel.getEntityMetamodel().getEntity(viewType.getEntityClass()).getName() + ")";
            } else {
                throw new IllegalArgumentException("The given view type with the entity type '" + viewType.getEntityClass().getName()
                        + "' can not be applied to the query builder with result type '" + rootType.getName() + "'");
            }
        }

        MacroConfiguration originalMacroConfiguration = ef.getDefaultMacroConfiguration();
        ExpressionFactory cachingExpressionFactory = ef.unwrap(AbstractCachingExpressionFactory.class);
        JpqlMacro viewRootJpqlMacro = new DefaultViewRootJpqlMacro(entityViewRoot);
        ViewJpqlMacro viewJpqlMacro = configuration.getViewJpqlMacro();
        EmbeddingViewJpqlMacro embeddingViewJpqlMacro = configuration.getEmbeddingViewJpqlMacro();
        viewJpqlMacro.setViewPath(entityViewRoot);
        Map<String, MacroFunction> macros = new HashMap<>();
        macros.put("view", new JpqlMacroAdapter(viewJpqlMacro, cachingExpressionFactory));
        macros.put("view_root", new JpqlMacroAdapter(viewRootJpqlMacro, cachingExpressionFactory));
        macros.put("embedding_view", new JpqlMacroAdapter(embeddingViewJpqlMacro, cachingExpressionFactory));
        MacroConfiguration macroConfiguration = originalMacroConfiguration.with(macros);
        MacroConfigurationExpressionFactory macroEf = new MacroConfigurationExpressionFactory(cachingExpressionFactory, macroConfiguration);
        criteriaBuilder.registerMacro("view_root", viewRootJpqlMacro);

        return getTemplate(macroEf, viewType, mappingConstructor, entityViewRoot, viewJpqlMacro, embeddingViewPath, embeddingViewJpqlMacro, offset)
            .createObjectBuilder(criteriaBuilder, configuration.getOptionalParameters(), configuration, suffix, false, nullFlatViewIfEmpty);
    }

    private static Path getPath(FullQueryBuilder<?, ?> queryBuilder, String entityViewRoot) {
        return queryBuilder.getRequiredPath(entityViewRoot);
    }

    @SuppressWarnings("unchecked")
    public ViewTypeObjectBuilderTemplate<?> getTemplate(MacroConfigurationExpressionFactory ef, ViewTypeImpl<?> viewType, MappingConstructorImpl<?> mappingConstructor, String entityViewRoot, ViewJpqlMacro viewJpqlMacro, String embeddingViewPath, EmbeddingViewJpqlMacro embeddingViewJpqlMacro) {
        return getTemplate(ef, viewType, mappingConstructor, entityViewRoot, viewJpqlMacro, embeddingViewPath, embeddingViewJpqlMacro, 0);
    }

    public ViewTypeObjectBuilderTemplate<?> getTemplate(MacroConfigurationExpressionFactory ef, ManagedViewTypeImplementor<?> viewType, MappingConstructorImpl<?> mappingConstructor, String entityViewRoot, ViewJpqlMacro viewJpqlMacro, String embeddingViewPath, EmbeddingViewJpqlMacro embeddingViewJpqlMacro, int offset) {
        ViewTypeObjectBuilderTemplate.Key key = new ViewTypeObjectBuilderTemplate.Key(ef, viewType, mappingConstructor, entityViewRoot, embeddingViewPath, offset);
        if (!key.isCacheable()) {
            return key.createValue(this, proxyFactory, viewJpqlMacro, embeddingViewJpqlMacro, ef);
        }
        ViewTypeObjectBuilderTemplate<?> value = objectBuilderCache.get(key);

        if (value == null) {
            value = key.createValue(this, proxyFactory, viewJpqlMacro, embeddingViewJpqlMacro, ef);
            ViewTypeObjectBuilderTemplate<?> oldValue = objectBuilderCache.putIfAbsent(key, value);

            if (oldValue != null) {
                value = oldValue;
            }
        }

        return value;
    }

    public void addUpdater(Map<Object, EntityViewUpdaterImpl> localCache, ManagedViewTypeImplementor<?> viewType, ManagedViewTypeImplementor<?> declaredViewType, EntityViewUpdaterImpl owner, String ownerMapping, EntityViewUpdaterImpl updater) {
        if (declaredViewType != null && declaredViewType != viewType || owner != null) {
            ContextAwareUpdaterKey key = new ContextAwareUpdaterKey(viewType, declaredViewType, owner, ownerMapping);
            localCache.put(key, updater);
        } else {
            localCache.put(viewType, updater);
        }
    }
    
    public EntityViewUpdaterImpl getUpdater(Map<Object, EntityViewUpdaterImpl> localCache, ManagedViewTypeImplementor<?> viewType, ManagedViewTypeImplementor<?> declaredViewType, EntityViewUpdaterImpl owner, String ownerMapping) {
        if (declaredViewType != null && declaredViewType != viewType || owner != null) {
            ContextAwareUpdaterKey key = new ContextAwareUpdaterKey(viewType, declaredViewType, owner, ownerMapping);
            EntityViewUpdaterImpl value;

            if (localCache == null || (value = localCache.get(key)) == null) {
                value = contextAwareEntityViewUpdaterCache.get(key);
                if (value == null) {
                    boolean store = localCache == null;
                    if (store) {
                        localCache = new HashMap<>();
                    }
                    value = new EntityViewUpdaterImpl(this, localCache, viewType, declaredViewType, owner, ownerMapping);
                    if (store) {
                        contextAwareEntityViewUpdaterCache.putAll((Map<? extends ContextAwareUpdaterKey, ? extends EntityViewUpdaterImpl>) (Map<?, ?>) localCache);
                    }
                }
            }

            return value;
        } else {
            EntityViewUpdaterImpl value;

            if (localCache == null || (value = localCache.get(viewType)) == null) {
                value = entityViewUpdaterCache.get(viewType);
                if (value == null) {
                    boolean store = localCache == null;
                    if (store) {
                        localCache = new HashMap<>();
                    }
                    value = new EntityViewUpdaterImpl(this, localCache, viewType, null, null, null);
                    if (store) {
                        entityViewUpdaterCache.putAll((Map<? extends ManagedViewType<?>, ? extends EntityViewUpdaterImpl>) (Map<?, ?>) localCache);
                    }
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
        filterMappings.put(BetweenFilter.class.getName(), BetweenFilterImpl.class);
    }

    public EntityViewManager getSerializableDelegate(Class<?> entityViewClass) {
        return serializableDelegates.get(proxyFactory.getProxy(this, metamodel.managedViewOrError(entityViewClass)));
    }

    @Override
    public void close() {
        proxyFactory.clear();
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    private static class ContextAwareUpdaterKey {
        private final ManagedViewTypeImplementor<?> viewType;
        private final ManagedViewTypeImplementor<?> declaredViewType;
        private final EntityViewUpdaterImpl owner;
        private final String ownerMapping;

        public ContextAwareUpdaterKey(ManagedViewTypeImplementor<?> viewType, ManagedViewTypeImplementor<?> declaredViewType, EntityViewUpdaterImpl owner, String ownerMapping) {
            this.viewType = viewType;
            this.declaredViewType = declaredViewType;
            this.owner = owner;
            this.ownerMapping = ownerMapping;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof ContextAwareUpdaterKey)) {
                return false;
            }

            ContextAwareUpdaterKey that = (ContextAwareUpdaterKey) o;

            if (!viewType.equals(that.viewType)) {
                return false;
            }
            if (declaredViewType != null ? !declaredViewType.equals(that.declaredViewType) : that.declaredViewType != null) {
                return false;
            }
            if (owner != that.owner) {
                return false;
            }
            return ownerMapping != null ? ownerMapping.equals(that.ownerMapping) : that.ownerMapping == null;
        }

        @Override
        public int hashCode() {
            int result = viewType.hashCode();
            result = 31 * result + (declaredViewType != null ? declaredViewType.hashCode() : 0);
            result = 31 * result + (owner != null ? owner.hashCode() : 0);
            result = 31 * result + (ownerMapping != null ? ownerMapping.hashCode() : 0);
            return result;
        }
    }

    /**
     * @author Christian Beikov
     * @since 1.4.0
     */
    private static class ListenerTypeInfo {
        private final ManagedViewType<?> managedViewType;
        private final Class<?> entityClass;

        public ListenerTypeInfo(ManagedViewType<?> managedViewType, Class<?> entityClass) {
            this.managedViewType = managedViewType;
            this.entityClass = entityClass;
        }
    }

    /**
     * @author Christian Beikov
     * @since 1.4.0
     */
    private static class ViewMapperConfigKey {
        private final ViewMapper.Key<?, ?> key;
        private final Map<String, ViewMapper.Key<Object, Object>> subMappers;

        public ViewMapperConfigKey(ViewMapper.Key<?, ?> key, Map<String, ViewMapper.Key<Object, Object>> subMappers) {
            this.key = key;
            this.subMappers = subMappers;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof ViewMapperConfigKey)) {
                return false;
            }

            ViewMapperConfigKey that = (ViewMapperConfigKey) o;

            if (!key.equals(that.key)) {
                return false;
            }
            return subMappers.equals(that.subMappers);
        }

        @Override
        public int hashCode() {
            int result = key.hashCode();
            result = 31 * result + subMappers.hashCode();
            return result;
        }
    }

    /**
     * @author Christian Beikov
     * @since 1.5.0
     */
    private static class ViewBuilderKey {
        private final ManagedViewType<?> managedViewType;
        private final MappingConstructor<?> constructor;

        public ViewBuilderKey(ManagedViewType<?> managedViewType, MappingConstructor<?> constructor) {
            this.managedViewType = managedViewType;
            this.constructor = constructor;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof ViewBuilderKey)) {
                return false;
            }

            ViewBuilderKey that = (ViewBuilderKey) o;

            if (!managedViewType.equals(that.managedViewType)) {
                return false;
            }
            return constructor != null ? constructor.equals(that.constructor) : that.constructor == null;
        }

        @Override
        public int hashCode() {
            int result = managedViewType.hashCode();
            result = 31 * result + (constructor != null ? constructor.hashCode() : 0);
            return result;
        }
    }
}
