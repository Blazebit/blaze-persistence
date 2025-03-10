/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.metamodel;

import com.blazebit.persistence.parser.AliasReplacementVisitor;
import com.blazebit.persistence.parser.EntityMetamodel;
import com.blazebit.persistence.parser.expression.AbstractCachingExpressionFactory;
import com.blazebit.persistence.parser.expression.EntityLiteral;
import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.ExpressionFactory;
import com.blazebit.persistence.parser.expression.MacroConfiguration;
import com.blazebit.persistence.parser.expression.MacroFunction;
import com.blazebit.persistence.parser.expression.NullExpression;
import com.blazebit.persistence.spi.DbmsDialect;
import com.blazebit.persistence.spi.JpaProvider;
import com.blazebit.persistence.spi.JpqlFunction;
import com.blazebit.persistence.view.CTEProvider;
import com.blazebit.persistence.view.ConfigurationProperties;
import com.blazebit.persistence.view.CorrelationProviderFactory;
import com.blazebit.persistence.view.FetchStrategy;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.MappingCorrelated;
import com.blazebit.persistence.view.MappingCorrelatedSimple;
import com.blazebit.persistence.view.MappingIndex;
import com.blazebit.persistence.view.MappingSubquery;
import com.blazebit.persistence.view.impl.CorrelationProviderHelper;
import com.blazebit.persistence.view.impl.JpqlMacroAdapter;
import com.blazebit.persistence.view.impl.MacroConfigurationExpressionFactory;
import com.blazebit.persistence.view.impl.ScalarTargetResolvingExpressionVisitor;
import com.blazebit.persistence.view.impl.TypeExtractingCorrelationBuilder;
import com.blazebit.persistence.view.impl.macro.DefaultViewRootJpqlMacro;
import com.blazebit.persistence.view.impl.macro.FunctionPassthroughJpqlMacro;
import com.blazebit.persistence.view.impl.macro.MutableEmbeddingViewJpqlMacro;
import com.blazebit.persistence.view.impl.macro.MutableViewJpqlMacro;
import com.blazebit.persistence.view.impl.proxy.ProxyFactory;
import com.blazebit.persistence.view.impl.type.BasicUserTypeRegistry;
import com.blazebit.persistence.view.metamodel.Type;
import com.blazebit.persistence.view.spi.type.BasicUserType;
import com.blazebit.persistence.view.spi.type.TypeConverter;
import com.blazebit.reflection.ReflectionUtils;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class MetamodelBuildingContextImpl implements MetamodelBuildingContext {

    private static final Comparator<Class<?>> CLASS_NAME_COMPARATOR = new Comparator<Class<?>>() {
        @Override
        public int compare(Class<?> o1, Class<?> o2) {
            return o1.getName().compareTo(o2.getName());
        }
    };

    private final Map<TypeRegistryKey, Type<?>> basicTypeRegistry = new HashMap<>();
    private final Map<TypeRegistryKey, Map<Class<?>, Type<?>>> convertedTypeRegistry = new HashMap<>();
    private final Map<BasicUserType<?>, Boolean> multisetSupport = new HashMap<>();
    private final BasicUserTypeRegistry basicUserTypeRegistry;
    private final EntityMetamodel entityMetamodel;
    private final JpaProvider jpaProvider;
    private final DbmsDialect dbmsDialect;
    private final Map<String, JpqlFunction> jpqlFunctions;
    private final ExpressionFactory expressionFactory;
    private final MacroConfigurationExpressionFactory typeExtractionExpressionFactory;
    private final MacroConfigurationExpressionFactory typeValidationExpressionFactory;
    private final ProxyFactory proxyFactory;
    private final Map<Class<?>, ViewMapping> viewMappings;
    private final Map<ViewMappingInitializationKey, ManagedViewTypeImplementor<?>> initializingManagedViews;
    private final Map<ManagedViewTypeImplementor<?>, List<Runnable>> managedViewFinishListeners;
    private final Set<String> errors;

    private final boolean disallowOwnedUpdatableSubview;
    private final boolean strictCascadingCheck;
    private final boolean errorOnInvalidPluralSetter;
    private final boolean createEmptyFlatViews;
    private final FlushMode flushModeOverride;
    private final Map<String, FlushMode> flushModeOverrides;
    private final FlushStrategy flushStrategyOverride;
    private final Map<String, FlushStrategy> flushStrategyOverrides;

    private final Map<Class<?>, CTEProvider> cteProviders = new LinkedHashMap<>();

    public MetamodelBuildingContextImpl(Properties properties, BasicUserTypeRegistry basicUserTypeRegistry, EntityMetamodel entityMetamodel, JpaProvider jpaProvider, DbmsDialect dbmsDialect, Map<String, JpqlFunction> jpqlFunctions, ExpressionFactory expressionFactory, ProxyFactory proxyFactory, Map<Class<?>, ViewMapping> viewMappings, Set<String> errors) {
        this.basicUserTypeRegistry = basicUserTypeRegistry;
        this.entityMetamodel = entityMetamodel;
        this.jpaProvider = jpaProvider;
        this.dbmsDialect = dbmsDialect;
        this.jpqlFunctions = jpqlFunctions;
        this.expressionFactory = expressionFactory;
        this.typeExtractionExpressionFactory = createMacroAwareExpressionFactory("this");
        this.typeValidationExpressionFactory = createTypeValidationExpressionFactory();
        this.proxyFactory = proxyFactory;
        this.viewMappings = viewMappings;
        this.initializingManagedViews = new HashMap<>();
        this.managedViewFinishListeners = new HashMap<>();
        this.errors = errors;
        this.disallowOwnedUpdatableSubview = "true".equals(properties.getProperty(ConfigurationProperties.UPDATER_DISALLOW_OWNED_UPDATABLE_SUBVIEW));
        this.strictCascadingCheck = Boolean.valueOf(String.valueOf(properties.getProperty(ConfigurationProperties.UPDATER_STRICT_CASCADING_CHECK)));
        this.errorOnInvalidPluralSetter = Boolean.valueOf(String.valueOf(properties.getProperty(ConfigurationProperties.UPDATER_ERROR_ON_INVALID_PLURAL_SETTER)));
        this.createEmptyFlatViews = Boolean.valueOf(String.valueOf(properties.getProperty(ConfigurationProperties.CREATE_EMPTY_FLAT_VIEWS)));
        this.flushModeOverride = getFlushMode(properties.getProperty(ConfigurationProperties.UPDATER_FLUSH_MODE), "global property '" + ConfigurationProperties.UPDATER_FLUSH_MODE + "'");
        this.flushModeOverrides = getFlushModeOverrides(properties);
        this.flushStrategyOverride = getFlushStrategy(properties.getProperty(ConfigurationProperties.UPDATER_FLUSH_STRATEGY), "global property '" + ConfigurationProperties.UPDATER_FLUSH_STRATEGY + "'");
        this.flushStrategyOverrides = getFlushStrategyOverrides(properties);
    }

    private FlushMode getFlushMode(String property, String location) {
        if (property == null || property.isEmpty()) {
            return null;
        }

        if ("partial".equals(property)) {
            return FlushMode.PARTIAL;
        } else if ("lazy".equals(property)) {
            return FlushMode.LAZY;
        } else if ("full".equals(property)) {
            return FlushMode.FULL;
        }

        throw new IllegalArgumentException("Invalid flush mode defined for " + location + ": " + property);
    }

    private Map<String, FlushMode> getFlushModeOverrides(Properties properties) {
        String prefix = ConfigurationProperties.UPDATER_FLUSH_MODE + ".";
        Map<String, FlushMode> flushModes = new HashMap<>();
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            String key = (String) entry.getKey();
            if (key.startsWith(prefix) && entry.getValue() != null) {
                Object value = entry.getValue();
                FlushMode mode;
                if (value instanceof FlushMode) {
                    mode = (FlushMode) value;
                } else {
                    mode = getFlushMode(entry.getValue().toString(), "property '" + key + "'");
                }
                flushModes.put(key.substring(prefix.length()), mode);
            }
        }
        return flushModes;
    }

    private FlushStrategy getFlushStrategy(String property, String location) {
        if (property == null || property.isEmpty()) {
            return null;
        }

        if ("query".equalsIgnoreCase(property)) {
            return FlushStrategy.QUERY;
        } else if ("entity".equalsIgnoreCase(property)) {
            return FlushStrategy.ENTITY;
        }

        throw new IllegalArgumentException("Invalid flush strategy defined for " + location + ": " + property);
    }

    private Map<String, FlushStrategy> getFlushStrategyOverrides(Properties properties) {
        String prefix = ConfigurationProperties.UPDATER_FLUSH_STRATEGY + ".";
        Map<String, FlushStrategy> flushStrategies = new HashMap<>();
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            String key = (String) entry.getKey();
            if (key.startsWith(prefix) && entry.getValue() != null) {
                Object value = entry.getValue();
                FlushStrategy strategy;
                if (value instanceof FlushStrategy) {
                    strategy = (FlushStrategy) value;
                } else {
                    strategy = getFlushStrategy(entry.getValue().toString(), "property '" + key + "'");
                }
                flushStrategies.put(key.substring(prefix.length()), strategy);
            }
        }
        return flushStrategies;
    }

    @Override
    public BasicUserTypeRegistry getBasicUserTypeRegistry() {
        return basicUserTypeRegistry;
    }

    @Override
    public Collection<ViewMapping> getViewMappings() {
        return viewMappings.values();
    }

    @Override
    public void addManagedViewType(ViewMapping viewMapping, EmbeddableOwner embeddableMapping, ManagedViewTypeImplementor<?> managedViewType) {
        ViewMappingInitializationKey key = new ViewMappingInitializationKey(viewMapping, embeddableMapping);
        if (initializingManagedViews.get(key) == null) {
            initializingManagedViews.put(key, managedViewType);
        }
    }

    @Override
    public ManagedViewTypeImplementor<?> getManagedViewType(ViewMapping viewMapping, EmbeddableOwner embeddableMapping) {
        ViewMappingInitializationKey key = new ViewMappingInitializationKey(viewMapping, embeddableMapping);
        ManagedViewTypeImplementor<?> managedViewTypeImplementor = initializingManagedViews.get(key);
        if (managedViewTypeImplementor == null) {
            return viewMapping.getManagedViewType(this, embeddableMapping);
        }
        return managedViewTypeImplementor;
    }

    @Override
    public void finishViewType(ManagedViewTypeImplementor<?> managedViewType) {
        List<Runnable> finishListeners = managedViewFinishListeners.get(managedViewType);
        if (finishListeners == null) {
            managedViewFinishListeners.put(managedViewType, Collections.<Runnable>emptyList());
        } else {
            for (Runnable finishListener : finishListeners) {
                finishListener.run();
            }
            finishListeners.clear();
        }
    }

    @Override
    public void onViewTypeFinished(ManagedViewTypeImplementor<?> managedViewType, Runnable listener) {
        List<Runnable> finishListeners = managedViewFinishListeners.get(managedViewType);
        if (finishListeners == null) {
            finishListeners = new ArrayList<>();
            finishListeners.add(listener);
            managedViewFinishListeners.put(managedViewType, finishListeners);
        } else {
            if (finishListeners.isEmpty()) {
                listener.run();
            } else {
                finishListeners.add(listener);
            }
        }
    }

    @Override
    public ViewMapping getViewMapping(Class<?> entityViewClass) {
        return viewMappings.get(entityViewClass);
    }

    @Override
    public <X> Map<Class<?>, TypeConverter<?, X>> getTypeConverter(Class<X> type) {
        return basicUserTypeRegistry.getTypeConverter(type);
    }

    @Override
    public List<ScalarTargetResolvingExpressionVisitor.TargetType> getPossibleTargetTypes(Class<?> entityClass, Attribute<?, ?> rootAttribute, Annotation mapping, Map<String, javax.persistence.metamodel.Type<?>> rootTypes) {
        ManagedType<?> managedType = entityMetamodel.getManagedType(entityClass);
        String expression;
        if (mapping instanceof Mapping) {
            expression = ((Mapping) mapping).value();
        } else if (mapping instanceof IdMapping) {
            expression = ((IdMapping) mapping).value();
        } else if (mapping instanceof MappingIndex) {
            expression = ((MappingIndex) mapping).value();
        } else if (mapping instanceof MappingCorrelatedSimple) {
            MappingCorrelatedSimple m = (MappingCorrelatedSimple) mapping;
            managedType = entityMetamodel.getManagedType(m.correlated());
            expression = m.correlationResult();
            // Correlation result is the correlated type, so the possible target type is the managed type
            if (expression.isEmpty()) {
                return Collections.<ScalarTargetResolvingExpressionVisitor.TargetType>singletonList(new ScalarTargetResolvingExpressionVisitor.TargetTypeImpl(
                        false,
                        null,
                        managedType.getJavaType(),
                        null,
                        null
                ));
            }
        } else if (mapping instanceof MappingCorrelated) {
            MappingCorrelated m = (MappingCorrelated) mapping;
            CorrelationProviderFactory correlationProviderFactory = CorrelationProviderHelper.getFactory(m.correlator());
            ScalarTargetResolvingExpressionVisitor resolver = new ScalarTargetResolvingExpressionVisitor(entityClass, getEntityMetamodel(), getJpqlFunctions(), rootTypes);
            javax.persistence.metamodel.Type<?> type = TypeExtractingCorrelationBuilder.extractType(correlationProviderFactory, "_alias", this, resolver);
            if (type == null) {
                // We can't determine the managed type
                return Collections.emptyList();
            }
            managedType = (ManagedType<?>) type;
            expression = m.correlationResult();
            // Correlation result is the correlated type, so the possible target type is the managed type
            if (expression.isEmpty()) {
                return Collections.<ScalarTargetResolvingExpressionVisitor.TargetType>singletonList(new ScalarTargetResolvingExpressionVisitor.TargetTypeImpl(
                        false,
                        null,
                        managedType.getJavaType(),
                        null,
                        null
                ));
            }
        } else if (mapping instanceof MappingSubquery) {
            MappingSubquery mappingSubquery = (MappingSubquery) mapping;
            if (!mappingSubquery.expression().isEmpty()) {
                Expression simpleExpression = typeValidationExpressionFactory.createSimpleExpression(((MappingSubquery) mapping).expression(), false, true, false);
                AliasReplacementVisitor aliasReplacementVisitor = new AliasReplacementVisitor(NullExpression.INSTANCE, mappingSubquery.subqueryAlias());
                simpleExpression.accept(aliasReplacementVisitor);
                ScalarTargetResolvingExpressionVisitor visitor = new ScalarTargetResolvingExpressionVisitor(entityClass, entityMetamodel, jpqlFunctions, rootTypes);
                simpleExpression.accept(visitor);
                return visitor.getPossibleTargetTypes();
            }
            // We can't determine the managed type
            return Collections.emptyList();
        } else {
            // There is no entity model type for other mappings
            return Collections.emptyList();
        }

        // Don't bother with empty expressions or missing managed type, let the validation process handle this
        if (expression.isEmpty() || managedType == null) {
            return Collections.emptyList();
        }

        expression = AbstractAttribute.stripThisFromMapping(expression);
        // The result is "THIS" apparently, so the possible target type is the managed type
        if (expression.isEmpty()) {
            Class<?> leafBaseClass = managedType.getJavaType();
            Class<?> leafBaseKeyClass = null;
            Class<?> leafBaseValueClass = null;
            if (rootAttribute instanceof PluralAttribute<?, ?, ?>) {
                leafBaseClass = rootAttribute.getJavaType();
                leafBaseValueClass = ((PluralAttribute<?, ?, ?>) rootAttribute).getElementType().getJavaType();
                if (rootAttribute instanceof MapAttribute<?, ?, ?>) {
                    leafBaseKeyClass = ((MapAttribute<?, ?, ?>) rootAttribute).getKeyJavaType();
                }
            } else if (rootAttribute instanceof SingularAttribute<?, ?>) {
                leafBaseClass = rootAttribute.getJavaType();
            }
            return Collections.<ScalarTargetResolvingExpressionVisitor.TargetType>singletonList(new ScalarTargetResolvingExpressionVisitor.TargetTypeImpl(
                    leafBaseValueClass != null,
                    rootAttribute,
                    leafBaseClass,
                    leafBaseKeyClass,
                    leafBaseValueClass
            ));
        }
        Expression simpleExpression = typeValidationExpressionFactory.createSimpleExpression(expression, false, false, true);
        if (simpleExpression instanceof EntityLiteral) {
            // This is a special case where the mapping i.e. attribute name matches an entity name
            try {
                Attribute<?, ?> attribute = managedType.getAttribute(expression);
                return Collections.<ScalarTargetResolvingExpressionVisitor.TargetType>singletonList(new ScalarTargetResolvingExpressionVisitor.TargetTypeImpl(false, attribute, attribute.getJavaType(), null, attribute.getJavaType()));
            } catch (IllegalArgumentException ex) {
                // Apparently it's not an attribute, so let it run through
            }
        }
        ScalarTargetResolvingExpressionVisitor visitor = new ScalarTargetResolvingExpressionVisitor(managedType, rootAttribute, entityMetamodel, jpqlFunctions, rootTypes);
        simpleExpression.accept(visitor);
        return visitor.getPossibleTargetTypes();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <X> Type<X> getBasicType(ViewMapping viewMapping, java.lang.reflect.Type type, Class<?> classType, Set<Class<?>> possibleTypes) {
        if (type == null) {
            return null;
        }

        // First try find a normal type
        TypeRegistryKey key = new TypeRegistryKey(type, possibleTypes);
        Type<X> t = (Type<X>) basicTypeRegistry.get(key);
        if (t == null) {
            // If none found, try find a converted type
            Map<Class<?>, ? extends TypeConverter<?, ?>> typeConverterMap = basicUserTypeRegistry.getTypeConverter(classType);
            TypeConverter<?, ?> typeConverter = null;
            Class<?> convertedType = null;
            Map<Class<?>, Type<?>> convertedTypeMap = null;

            for (Class<?> entityModelType : possibleTypes) {
                if (entityModelType != null && !typeConverterMap.isEmpty()) {
                    convertedTypeMap = convertedTypeRegistry.get(key);

                    if (convertedTypeMap != null) {
                        // Try to find an existing type, by default check boxed types first
                        if ((t = (Type<X>) convertedTypeMap.get(ReflectionUtils.getObjectClassOfPrimitve(entityModelType))) != null) {
                            return t;
                        }
                        // If the type is a primitive type, also try to find an existing converter there
                        if (entityModelType.isPrimitive()) {
                            if ((t = (Type<X>) convertedTypeMap.get(entityModelType)) != null) {
                                return t;
                            }
                        }
                        // Then try find a match for a "self" type
                        if ((t = (Type<X>) convertedTypeMap.get(classType)) != null) {
                            return t;
                        }
                        // Then try find a default
                        if ((t = (Type<X>) convertedTypeMap.get(Object.class)) != null) {
                            return t;
                        }
                    }
                    // Try find a converter matching the entity model type
                    typeConverter = typeConverterMap.get(ReflectionUtils.getObjectClassOfPrimitve(entityModelType));

                    // Optionally try to find a converter for the primitive type
                    if (typeConverter == null) {
                        if (entityModelType.isPrimitive()) {
                            typeConverter = typeConverterMap.get(entityModelType);
                        }

                        // Then try find a match for a "self" type
                        if (typeConverter == null) {
                            typeConverter = typeConverterMap.get(classType);

                            // Then try find a default
                            if (typeConverter == null) {
                                typeConverter = typeConverterMap.get(Object.class);
                                if (typeConverter != null) {
                                    convertedType = Object.class;
                                }
                            } else {
                                convertedType = classType;
                            }
                        } else {
                            convertedType = entityModelType;
                        }
                    } else {
                        convertedType = ReflectionUtils.getObjectClassOfPrimitve(entityModelType);
                    }
                }

                if (typeConverter != null) {
                    // Ask the converter for the "real" type and create user types for that
                    classType = typeConverter.getUnderlyingType(viewMapping.getEntityViewClass(), type);
                    BasicUserType<X> userType = (BasicUserType<X>) basicUserTypeRegistry.getBasicUserType(ReflectionUtils.resolveType(viewMapping.getEntityViewClass(), type));
                    ManagedType<X> managedType = (ManagedType<X>) entityMetamodel.getManagedType(classType);
                    registerMultisetSupport(userType);
                    t = new BasicTypeImpl<>((Class<X>) classType, managedType, userType, type, (TypeConverter<X, ?>) typeConverter);
                    if (convertedTypeMap == null) {
                        convertedTypeMap = new HashMap<>();
                        convertedTypeRegistry.put(key, convertedTypeMap);
                    }
                    convertedTypeMap.put(convertedType, t);
                    return t;
                } else {
                    // Create a normal type
                    BasicUserType<X> userType = (BasicUserType<X>) basicUserTypeRegistry.getBasicUserType(classType);
                    ManagedType<X> managedType = (ManagedType<X>) entityMetamodel.getManagedType(classType);
                    registerMultisetSupport(userType);
                    t = new BasicTypeImpl<>((Class<X>) classType, managedType, userType);
                    basicTypeRegistry.put(key, t);
                    return t;
                }
            }
        }
        return t;
    }

    private void registerMultisetSupport(BasicUserType<?> userType) {
        if (multisetSupport.get(userType) == null) {
            boolean supportsMultiset = false;
            try {
                userType.toStringExpression("NULL");
                supportsMultiset = true;
            } catch (Exception ex) {
                // Ignore
            }
            multisetSupport.put(userType, supportsMultiset);
        }
    }

    @Override
    public void checkMultisetSupport(List<AbstractAttribute<?, ?>> parents, AbstractAttribute<?, ?> attribute, BasicUserType<?> userType) {
        if (!multisetSupport.get(userType)) {
            AbstractAttribute<?, ?> multisetAttribute = null;
            for (AbstractAttribute<?, ?> parent : parents) {
                if (parent.getFetchStrategy() == FetchStrategy.MULTISET) {
                    multisetAttribute = parent;
                    break;
                }
            }

            addError("The basic type " + userType.getClass().getName() + " of the " + attribute.getLocation() + " does not support MULTISET fetching! Please switch to a different fetch strategy at " + multisetAttribute.getLocation() +
                    " or register a custom user type as described in https://persistence.blazebit.com/documentation/entity-view/manual/en_US/index.html#entity-view-basic-user-type-spi");
        }
    }

    @Override
    public void checkMultisetSupport(AbstractAttribute<?, ?> attribute, BasicUserType<?> userType) {
        if (!multisetSupport.get(userType)) {
            addError("The basic type " + userType.getClass().getName() + " of the " + attribute.getLocation() + " does not support MULTISET fetching! Please switch to a different fetch strategy " +
                    " or register a custom user type as described in https://persistence.blazebit.com/documentation/entity-view/manual/en_US/index.html#entity-view-basic-user-type-spi");
        }
    }

    @Override
    public Map<String, JpqlFunction> getJpqlFunctions() {
        return jpqlFunctions;
    }

    @Override
    public EntityMetamodel getEntityMetamodel() {
        return entityMetamodel;
    }

    @Override
    public JpaProvider getJpaProvider() {
        return jpaProvider;
    }

    @Override
    public DbmsDialect getDbmsDialect() {
        return dbmsDialect;
    }

    @Override
    public ExpressionFactory getExpressionFactory() {
        return expressionFactory;
    }

    @Override
    public MacroConfigurationExpressionFactory getTypeValidationExpressionFactory() {
        return typeValidationExpressionFactory;
    }

    @Override
    public ExpressionFactory getTypeExtractionExpressionFactory() {
        return typeExtractionExpressionFactory;
    }

    @Override
    public MacroConfigurationExpressionFactory createMacroAwareExpressionFactory(String viewRoot) {
        MacroConfiguration originalMacroConfiguration = expressionFactory.getDefaultMacroConfiguration();
        ExpressionFactory cachingExpressionFactory = expressionFactory.unwrap(AbstractCachingExpressionFactory.class);
        Map<String, MacroFunction> macros = new HashMap<>();
        macros.put("view", new JpqlMacroAdapter(new MutableViewJpqlMacro(viewRoot), cachingExpressionFactory));
        macros.put("view_root", new JpqlMacroAdapter(new DefaultViewRootJpqlMacro(viewRoot), cachingExpressionFactory));
        macros.put("embedding_view", new JpqlMacroAdapter(new MutableEmbeddingViewJpqlMacro(), cachingExpressionFactory));
        MacroConfiguration macroConfiguration = originalMacroConfiguration.with(macros);
        return new MacroConfigurationExpressionFactory(cachingExpressionFactory, macroConfiguration);
    }

    private MacroConfigurationExpressionFactory createTypeValidationExpressionFactory() {
        MacroConfiguration originalMacroConfiguration = expressionFactory.getDefaultMacroConfiguration();
        ExpressionFactory cachingExpressionFactory = expressionFactory.unwrap(AbstractCachingExpressionFactory.class);
        Map<String, MacroFunction> macros = new HashMap<>();
        macros.put("view", new JpqlMacroAdapter(new FunctionPassthroughJpqlMacro("VIEW"), cachingExpressionFactory));
        macros.put("view_root", new JpqlMacroAdapter(new FunctionPassthroughJpqlMacro("VIEW_ROOT"), cachingExpressionFactory));
        macros.put("embedding_view", new JpqlMacroAdapter(new FunctionPassthroughJpqlMacro("EMBEDDING_VIEW"), cachingExpressionFactory));
        MacroConfiguration macroConfiguration = originalMacroConfiguration.with(macros);
        return new MacroConfigurationExpressionFactory(cachingExpressionFactory, macroConfiguration);
    }

    @Override
    public boolean isDisallowOwnedUpdatableSubview() {
        return disallowOwnedUpdatableSubview;
    }

    @Override
    public boolean isStrictCascadingCheck() {
        return strictCascadingCheck;
    }

    @Override
    public boolean isErrorOnInvalidPluralSetter() {
        return errorOnInvalidPluralSetter;
    }

    @Override
    public boolean isCreateEmptyFlatViews() {
        return createEmptyFlatViews;
    }

    @Override
    public ProxyFactory getProxyFactory() {
        return proxyFactory;
    }

    @Override
    public FlushMode getFlushMode(Class<?> clazz, FlushMode defaultValue) {
        if (flushModeOverride != null) {
            return flushModeOverride;
        }
        FlushMode mode = flushModeOverrides.get(clazz.getName());
        if (mode != null) {
            return mode;
        } else {
            return defaultValue;
        }
    }

    @Override
    public FlushStrategy getFlushStrategy(Class<?> clazz, FlushStrategy defaultValue) {
        if (flushStrategyOverride != null) {
            return flushStrategyOverride;
        }
        FlushStrategy mode = flushStrategyOverrides.get(clazz.getName());
        if (mode != null) {
            return mode;
        } else {
            return defaultValue;
        }
    }

    @Override
    public void addError(String error) {
        errors.add(error);
    }

    @Override
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    @Override
    public boolean isEntityView(Class<?> clazz) {
        return viewMappings.containsKey(clazz);
    }

    @Override
    public Set<Class<?>> findSubtypes(Class<?> entityViewClass) {
        // Deterministic order of classes
        Set<Class<?>> subtypes = new TreeSet<>(CLASS_NAME_COMPARATOR);
        for (Class<?> clazz : viewMappings.keySet()) {
            if (entityViewClass.isAssignableFrom(clazz) && entityViewClass != clazz) {
                subtypes.add(clazz);
            }
        }

        return subtypes;
    }

    @Override
    public Set<Class<?>> findSupertypes(Class<?> entityViewClass) {
        // Deterministic order of classes
        Set<Class<?>> supertypes = new TreeSet<>(CLASS_NAME_COMPARATOR);
        for (Class<?> clazz : viewMappings.keySet()) {
            if (clazz.isAssignableFrom(entityViewClass) && entityViewClass != clazz) {
                supertypes.add(clazz);
            }
        }

        return supertypes;
    }

    @Override
    public Map<Class<?>, CTEProvider> getCteProviders() {
        return cteProviders;
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    private static final class TypeRegistryKey {
        private final java.lang.reflect.Type type;
        private final Set<Class<?>> possibleTypes;

        private TypeRegistryKey(java.lang.reflect.Type type, Set<Class<?>> possibleTypes) {
            this.type = type;
            this.possibleTypes = possibleTypes;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            TypeRegistryKey that = (TypeRegistryKey) o;

            if (type != null ? !type.equals(that.type) : that.type != null) {
                return false;
            }
            return possibleTypes != null ? possibleTypes.equals(that.possibleTypes) : that.possibleTypes == null;
        }

        @Override
        public int hashCode() {
            int result = type != null ? type.hashCode() : 0;
            result = 31 * result + (possibleTypes != null ? possibleTypes.hashCode() : 0);
            return result;
        }
    }

    /**
     * @author Christian Beikov
     * @since 1.3.0
     */
    private static class ViewMappingInitializationKey {
        private final ViewMapping viewMapping;
        private final EmbeddableOwner embeddableOwner;

        public ViewMappingInitializationKey(ViewMapping viewMapping, EmbeddableOwner embeddableOwner) {
            this.viewMapping = viewMapping;
            this.embeddableOwner = viewMapping.getIdAttribute() == null ? embeddableOwner : null;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof ViewMappingInitializationKey)) {
                return false;
            }

            ViewMappingInitializationKey that = (ViewMappingInitializationKey) o;

            if (!viewMapping.equals(that.viewMapping)) {
                return false;
            }
            return embeddableOwner != null ? embeddableOwner.equals(that.embeddableOwner) : that.embeddableOwner == null;
        }

        @Override
        public int hashCode() {
            int result = viewMapping.hashCode();
            result = 31 * result + (embeddableOwner != null ? embeddableOwner.hashCode() : 0);
            return result;
        }
    }
}
