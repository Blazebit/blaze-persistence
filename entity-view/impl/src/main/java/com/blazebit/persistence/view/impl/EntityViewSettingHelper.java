/*
 * Copyright 2014 - 2021 Blazebit.
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

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.KeysetPage;
import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.parser.expression.ExpressionFactory;
import com.blazebit.persistence.spi.ServiceProvider;
import com.blazebit.persistence.view.AttributeFilterProvider;
import com.blazebit.persistence.view.ConfigurationProperties;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.Sorter;
import com.blazebit.persistence.view.ViewFilterProvider;
import com.blazebit.persistence.view.impl.macro.MutableEmbeddingViewJpqlMacro;
import com.blazebit.persistence.view.impl.macro.MutableViewJpqlMacro;
import com.blazebit.persistence.view.impl.metamodel.AbstractMethodAttribute;
import com.blazebit.persistence.view.impl.metamodel.ManagedViewTypeImplementor;
import com.blazebit.persistence.view.impl.metamodel.MappingConstructorImpl;
import com.blazebit.persistence.view.impl.metamodel.ViewTypeImplementor;
import com.blazebit.persistence.view.metamodel.AttributeFilterMapping;
import com.blazebit.persistence.view.metamodel.FlatViewType;
import com.blazebit.persistence.view.metamodel.MappingAttribute;
import com.blazebit.persistence.view.metamodel.MethodAttribute;
import com.blazebit.persistence.view.metamodel.PluralAttribute;
import com.blazebit.persistence.view.metamodel.SingularAttribute;
import com.blazebit.persistence.view.metamodel.ViewFilterMapping;
import com.blazebit.persistence.view.metamodel.ViewMetamodel;
import com.blazebit.persistence.view.metamodel.ViewType;

import javax.persistence.metamodel.Metamodel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;

/**
 * @author Christian Beikov
 * @since 1.0.0
 */
public final class EntityViewSettingHelper {

    private EntityViewSettingHelper() {
    }

    @SuppressWarnings("unchecked")
    public static <T, Q extends FullQueryBuilder<T, Q>> Q apply(EntityViewSetting<T, Q> setting, EntityViewManagerImpl evm, CriteriaBuilder<?> criteriaBuilder, String entityViewRoot) {
        ManagedViewTypeImplementor<?> managedView = evm.getMetamodel().managedView(setting.getEntityViewClass());
        if (managedView == null) {
            throw new IllegalArgumentException("There is no entity view for the class '" + setting.getEntityViewClass().getName() + "' registered!");
        }
        MappingConstructorImpl<?> mappingConstructor = (MappingConstructorImpl<?>) managedView.getConstructor(setting.getViewConstructorName());
        if (managedView instanceof FlatViewType<?>) {
            if (managedView.hasJoinFetchedCollections()) {
                throw new IllegalArgumentException("Can't use the flat view '" + managedView.getJavaType().getName() + "' as view root because it contains join fetched collections! " +
                        "Consider adding a @IdMapping to the entity view or use a different fetch strategy for the collections!");
            }
            if (mappingConstructor == null) {
                if (managedView.getConstructors().size() > 1) {
                    mappingConstructor = (MappingConstructorImpl<T>) managedView.getConstructor("init");
                } else if (managedView.getConstructors().size() == 1) {
                    mappingConstructor = (MappingConstructorImpl<T>) managedView.getConstructors().toArray()[0];
                }
            }
            if (mappingConstructor != null && mappingConstructor.hasJoinFetchedCollections()) {
                throw new IllegalArgumentException("Can't use the flat view '" + managedView.getJavaType().getName() + "' with the mapping constructor '" + mappingConstructor.getName() + "' as view root because it contains join fetched collections! " +
                        "Consider adding a @IdMapping to the entity view or use a different fetch strategy for the collections!");
            }
        }

        if (managedView.isUpdatable() && !setting.getFetches().isEmpty()) {
            throw new IllegalArgumentException("Specifying fetches for @UpdatableEntityViews is currently disallowed. Remove the fetches!");
        }

        ExpressionFactory ef = criteriaBuilder.getService(ExpressionFactory.class);
        Map<String, Object> optionalParameters;
        if (setting.getOptionalParameters().isEmpty()) {
            optionalParameters = evm.getOptionalParameters();
        } else {
            optionalParameters = new HashMap<>(evm.getOptionalParameters());
            optionalParameters.putAll(setting.getOptionalParameters());
            optionalParameters = Collections.unmodifiableMap(optionalParameters);
        }
        EntityViewConfiguration configuration = new EntityViewConfiguration(criteriaBuilder, ef, new MutableViewJpqlMacro(), new MutableEmbeddingViewJpqlMacro(), optionalParameters, setting.getProperties(), setting.getFetches(), managedView);
        entityViewRoot = evm.applyObjectBuilder(managedView, mappingConstructor, entityViewRoot, configuration.getCriteriaBuilder(), configuration, 0);
        applyAttributeFilters(setting, evm, criteriaBuilder, ef, managedView);
        applyAttributeSorters(setting, evm, criteriaBuilder, ef, managedView);
        applyOptionalParameters(optionalParameters, criteriaBuilder);
        Map<String, Object> properties = setting.getProperties();

        if (setting.isPaginated()) {
            KeysetPage keysetPage = setting.getKeysetPage();
            boolean forceUseKeyset = keysetPage != null && getBooleanProperty(properties, ConfigurationProperties.PAGINATION_FORCE_USE_KEYSET, false);
            if (forceUseKeyset) {
                setting.withKeysetPage(null);
            }
            PaginatedCriteriaBuilder<?> builder;
            if (managedView instanceof FlatViewType<?>) {
                if (setting.isKeysetPaginated()) {
                    if (setting.getFirstResult() == -1) {
                        builder = criteriaBuilder.pageAndNavigate(setting.getEntityId(), setting.getMaxResults()).withKeysetExtraction(true);
                    } else {
                        builder = criteriaBuilder.page(setting.getKeysetPage(), setting.getFirstResult(), setting.getMaxResults());
                    }
                } else {
                    if (setting.getFirstResult() == -1) {
                        builder = criteriaBuilder.page(0, setting.getMaxResults());
                    } else {
                        builder = criteriaBuilder.page(setting.getFirstResult(), setting.getMaxResults());
                    }
                }
            } else {
                // When the result should be paginated, we have to properly paginate by the identifier of the view
                MethodAttribute<?, ?> idAttribute = ((ViewTypeImplementor<?>) managedView).getIdAttribute();
                String firstExpression;
                List<String> expressions;
                if (idAttribute.isSubview()) {
                    String prefix = getMapping(entityViewRoot, idAttribute, criteriaBuilder);
                    ManagedViewTypeImplementor<?> type = (ManagedViewTypeImplementor<?>) ((SingularAttribute<?, ?>) idAttribute).getType();
                    Set<MethodAttribute<?, ?>> attributes = (Set) type.getAttributes();
                    Iterator<MethodAttribute<?, ?>> iterator = attributes.iterator();
                    firstExpression = getMapping(prefix, iterator.next(), criteriaBuilder);
                    if (iterator.hasNext()) {
                        expressions = new ArrayList<>(attributes.size() - 1);
                        while (iterator.hasNext()) {
                            expressions.add(getMapping(prefix, iterator.next(), criteriaBuilder));
                        }
                    } else {
                        expressions = null;
                    }
                } else {
                    expressions = null;
                    firstExpression = getMapping(entityViewRoot, idAttribute, criteriaBuilder);
                }

                if (setting.isKeysetPaginated()) {
                    if (setting.getFirstResult() == -1) {
                        builder = criteriaBuilder.pageByAndNavigate(setting.getEntityId(), setting.getMaxResults(), firstExpression, getExpressionArray(expressions)).withKeysetExtraction(true);
                    } else {
                        builder = criteriaBuilder.pageBy(setting.getKeysetPage(), setting.getFirstResult(), setting.getMaxResults(), firstExpression, getExpressionArray(expressions));
                    }
                } else {
                    if (setting.getFirstResult() == -1) {
                        builder = criteriaBuilder.pageBy(0, setting.getMaxResults(), firstExpression, getExpressionArray(expressions));
                    } else {
                        builder = criteriaBuilder.pageBy(setting.getFirstResult(), setting.getMaxResults(), firstExpression, getExpressionArray(expressions));
                    }
                }
            }

            if (forceUseKeyset) {
                if (keysetPage.getLowest() != null) {
                    builder.beforeKeyset(keysetPage.getLowest());
                } else if (keysetPage.getHighest() != null) {
                    builder.afterKeyset(keysetPage.getHighest());
                }
            }
            boolean disableCountQuery = getBooleanProperty(properties, ConfigurationProperties.PAGINATION_DISABLE_COUNT_QUERY, false);
            if (disableCountQuery) {
                builder.withCountQuery(false);
            } else {
                Integer boundedCount = null;
                Object o = properties.get(ConfigurationProperties.PAGINATION_BOUNDED_COUNT);
                if (o != null) {
                    if (o instanceof Integer || o instanceof Long) {
                        boundedCount = ((Number) o).intValue();
                    } else if (o instanceof String) {
                        boundedCount = Integer.parseInt((String) o);
                    } else {
                        throw new IllegalArgumentException("Invalid value of type " + o.getClass().getName() + " given for the integer property: " + ConfigurationProperties.PAGINATION_BOUNDED_COUNT);
                    }
                }
                if (boundedCount != null) {
                    builder.withBoundedCount(boundedCount);
                }
            }
            Integer highestKeyOffset = null;
            Object o = properties.get(ConfigurationProperties.PAGINATION_HIGHEST_KEYSET_OFFSET);
            if (o != null) {
                if (o instanceof Integer) {
                    highestKeyOffset = ((Number) o).intValue();
                } else if (o instanceof String) {
                    highestKeyOffset = Integer.parseInt((String) o);
                } else {
                    throw new IllegalArgumentException("Invalid value of type " + o.getClass().getName() + " given for the integer property: " + ConfigurationProperties.PAGINATION_HIGHEST_KEYSET_OFFSET);
                }
            }
            if (highestKeyOffset != null) {
                builder.withHighestKeysetOffset(highestKeyOffset);
            }
            boolean extractAllKeysets = getBooleanProperty(properties, ConfigurationProperties.PAGINATION_EXTRACT_ALL_KEYSETS, false);
            if (extractAllKeysets) {
                builder.withExtractAllKeysets(true);
            }

            return (Q) builder;
        } else {
            return (Q) criteriaBuilder;
        }
    }

    private static boolean getBooleanProperty(Map<String, Object> properties, String key, boolean defaultValue) {
        Object o = properties.get(key);
        if (o == null) {
            return defaultValue;
        } else if (o instanceof Boolean) {
            return (boolean) o;
        } else if (o instanceof String) {
            return Boolean.parseBoolean(o.toString());
        }

        throw new IllegalArgumentException("Invalid value of type " + o.getClass().getName() + " given for the boolean property: " + key);
    }

    private static String getMapping(String prefix, MethodAttribute<?, ?> attribute, ServiceProvider serviceProvider) {
        MappingAttribute<?, ?> mappingAttribute = (MappingAttribute<?, ?>) attribute;
        StringBuilder sb = new StringBuilder(prefix.length() + mappingAttribute.getMapping().length() + 1);
        mappingAttribute.renderMapping(prefix, serviceProvider, sb);
        return sb.toString();
    }

    private static String[] getExpressionArray(List<String> expressions) {
        if (expressions == null || expressions.isEmpty()) {
            return null;
        }
        return expressions.toArray(new String[expressions.size()]);
    }

    private static void applyOptionalParameters(Map<String, Object> optionalParameters, CriteriaBuilder<?> normalCb) {
        // Add optional parameters
        if (!optionalParameters.isEmpty()) {
            for (Map.Entry<String, Object> paramEntry : optionalParameters.entrySet()) {
                if (normalCb.containsParameter(paramEntry.getKey()) && !normalCb.isParameterSet(paramEntry.getKey())) {
                    normalCb.setParameter(paramEntry.getKey(), paramEntry.getValue());
                }
            }
        }
    }

    private static void applyAttributeFilters(EntityViewSetting<?, ?> setting, EntityViewManagerImpl evm, CriteriaBuilder<?> cb, ExpressionFactory ef, ManagedViewTypeImplementor<?> managedView) {
        ViewMetamodel metamodel = evm.getMetamodel();
        Metamodel jpaMetamodel = cb.getMetamodel();

        applyAttributeFilters(setting, evm, cb, ef, metamodel, jpaMetamodel, managedView);
        applyViewFilters(setting, evm, cb, managedView);
    }

    private static void applyViewFilters(EntityViewSetting<?, ?> setting, EntityViewManagerImpl evm, CriteriaBuilder<?> cb, ManagedViewTypeImplementor<?> viewType) {
        // Add named view filter
        for (String filterName : setting.getViewFilters()) {
            ViewFilterMapping filterMapping = ((ViewType<?>) viewType).getViewFilter(filterName);

            if (filterMapping == null) {
                throw new IllegalArgumentException("Could not find view filter mapping with the name '" + filterName
                    + "' in the entity view type '" + viewType.getJavaType()
                        .getName() + "'");
            }

            // TODO: allow parameter injection
            ViewFilterProvider provider = evm.createViewFilter(filterMapping.getFilterClass());
            provider.apply(cb);
        }
    }

    private static void applyAttributeFilters(EntityViewSetting<?, ?> setting, EntityViewManagerImpl evm, CriteriaBuilder<?> cb, ExpressionFactory ef, ViewMetamodel metamodel, Metamodel jpaMetamodel, ManagedViewTypeImplementor<?> entityViewRoot) throws IllegalArgumentException {
        String name = entityViewRoot.getJavaType().getSimpleName();
        StringBuilder sb = null;
        for (Map.Entry<String, List<EntityViewSetting.AttributeFilterActivation>> attributeFilterEntry : setting.getAttributeFilterActivations().entrySet()) {
            String attributeName = attributeFilterEntry.getKey();
            NavigableMap<String, AbstractMethodAttribute<?, ?>> recursiveAttributes = (NavigableMap<String, AbstractMethodAttribute<?, ?>>) entityViewRoot.getRecursiveAttributes();
            Map.Entry<String, AbstractMethodAttribute<?, ?>> entry = recursiveAttributes.floorEntry(attributeName);
            if (entry == null || !attributeName.startsWith(entry.getKey())) {
                throw new IllegalArgumentException("The attribute with the name '" + attributeName + "' couldn't be found on the view type '" + name + "'");
            }
            if (attributeName.length() != entry.getKey().length()) {
                throw new IllegalArgumentException("No support yet for entity attribute filtering!");
            }
            for (EntityViewSetting.AttributeFilterActivation filterActivation : attributeFilterEntry.getValue()) {
                Class<? extends AttributeFilterProvider> filterClass;
                Class<?> expectedType;

                MethodAttribute<?, ?> attribute = entry.getValue();
                AttributeFilterMapping<?, ?> filterMapping = attribute.getFilter(filterActivation.getAttributeFilterName());

                if (filterMapping == null) {
                    throw new IllegalArgumentException("Could not find attribute filter mapping with filter name '" + filterActivation.getAttributeFilterName()
                            + "' for attribute '" + attributeName + "' in the entity view type '" + attribute.getDeclaringType().getJavaType()
                            .getName() + "'");
                }

                filterClass = filterMapping.getFilterClass();
                // TODO: determining the expected type probably should be the job of the filter
                // Consider a filter that implements intersection between sets, in that case a collection might be expected
                // TODO: support converters here
                if (attribute.isCollection()) {
                    expectedType = ((PluralAttribute<?, ?, ?>) attribute).getElementType().getJavaType();
                } else {
                    expectedType = attribute.getJavaType();
                }

                if (filterClass == null) {
                    throw new IllegalArgumentException("No filter mapping given for the attribute '" + attributeName
                            + "' in the entity view type '" + entityViewRoot.getJavaType()
                            .getName() + "'");
                }

                AttributeFilterProvider<?> filter = evm.createAttributeFilter(filterClass, expectedType, filterActivation.getFilterValue());
                if (sb == null) {
                    sb = new StringBuilder(name.length() + attributeName.length() + 1);
                } else {
                    sb.setLength(0);
                }
                filter.apply(cb, buildAlias(sb, name, attributeName));
            }
        }
    }

    private static String buildAlias(StringBuilder sb, String name, String attributeName) {
        sb.append(name).append('_');
        for (int i = 0; i < attributeName.length(); i++) {
            char c = attributeName.charAt(i);
            if (c == '.') {
                sb.append('_');
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static void applyAttributeSorters(EntityViewSetting<?, ?> setting, EntityViewManagerImpl evm, CriteriaBuilder<?> cb, ExpressionFactory ef, ManagedViewTypeImplementor<?> entityViewRoot) {
        String name = entityViewRoot.getJavaType().getSimpleName();
        StringBuilder sb = null;

        for (Map.Entry<String, Sorter> attributeSorterEntry : setting.getAttributeSorters().entrySet()) {
            String attributeName = attributeSorterEntry.getKey();
            NavigableMap<String, AbstractMethodAttribute<?, ?>> recursiveAttributes = (NavigableMap<String, AbstractMethodAttribute<?, ?>>) entityViewRoot.getRecursiveAttributes();
            Map.Entry<String, AbstractMethodAttribute<?, ?>> entry = recursiveAttributes.floorEntry(attributeName);
            if (entry == null || !attributeName.startsWith(entry.getKey())) {
                throw new IllegalArgumentException("The attribute with the name '" + attributeName + "' couldn't be found on the view type '" + name + "'");
            }
            if (attributeName.length() != entry.getKey().length()) {
                throw new UnsupportedOperationException("No support yet for entity attribute filtering!");

//                // If we get here, the attribute type is a managed type and we can copy the rest of the parts
//                for (; i < parts.length; i++) {
//                    type = jpaMetamodel.managedType(maybeUnmanagedType);
//                    newSb.append('.');
//                    newSb.append(parts[i]);
//                    jpaAttribute = type.getAttribute(parts[i]);
//                    maybeUnmanagedType = jpaAttribute.getJavaType();
//                }
            }
            Sorter sorter = attributeSorterEntry.getValue();
            if (sb == null) {
                sb = new StringBuilder(name.length() + attributeName.length() + 1);
            } else {
                sb.setLength(0);
            }
            sorter.apply(cb, buildAlias(sb, name, attributeName));
        }
    }

}
