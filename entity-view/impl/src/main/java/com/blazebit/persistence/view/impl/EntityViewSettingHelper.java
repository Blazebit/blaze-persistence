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

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.parser.SimpleQueryGenerator;
import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.ExpressionFactory;
import com.blazebit.persistence.view.AttributeFilterProvider;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.FetchStrategy;
import com.blazebit.persistence.view.Sorter;
import com.blazebit.persistence.view.SubqueryProvider;
import com.blazebit.persistence.view.ViewFilterProvider;
import com.blazebit.persistence.view.impl.macro.MutableEmbeddingViewJpqlMacro;
import com.blazebit.persistence.view.impl.metamodel.AbstractAttribute;
import com.blazebit.persistence.view.impl.metamodel.ManagedViewTypeImplementor;
import com.blazebit.persistence.view.impl.metamodel.ViewTypeImplementor;
import com.blazebit.persistence.view.metamodel.AttributeFilterMapping;
import com.blazebit.persistence.view.metamodel.CorrelatedAttribute;
import com.blazebit.persistence.view.metamodel.FlatViewType;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.MappingAttribute;
import com.blazebit.persistence.view.metamodel.MethodAttribute;
import com.blazebit.persistence.view.metamodel.PluralAttribute;
import com.blazebit.persistence.view.metamodel.SingularAttribute;
import com.blazebit.persistence.view.metamodel.SubqueryAttribute;
import com.blazebit.persistence.view.metamodel.ViewFilterMapping;
import com.blazebit.persistence.view.metamodel.ViewMetamodel;
import com.blazebit.persistence.view.metamodel.ViewType;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
        if (managedView instanceof FlatViewType<?>) {
            if (managedView.hasJoinFetchedCollections()) {
                throw new IllegalArgumentException("Can't use the flat view '" + managedView.getJavaType().getName() + "' as view root because it contains join fetched collections!");
            }
        }

        ExpressionFactory ef = criteriaBuilder.getService(ExpressionFactory.class);
        EntityViewConfiguration configuration = new EntityViewConfiguration(criteriaBuilder, ef, new MutableEmbeddingViewJpqlMacro(), setting.getOptionalParameters(), setting.getProperties());
        boolean isQueryRoot = entityViewRoot == null || entityViewRoot.isEmpty();
        entityViewRoot = evm.applyObjectBuilder(setting.getEntityViewClass(), setting.getViewConstructorName(), entityViewRoot, configuration);
        applyAttributeFilters(setting, evm, criteriaBuilder, ef, entityViewRoot, isQueryRoot);
        applyAttributeSorters(setting, evm, criteriaBuilder, ef, entityViewRoot);
        applyOptionalParameters(setting, criteriaBuilder);

        if (setting.isPaginated()) {
            if (managedView instanceof FlatViewType<?>) {
                if (setting.isKeysetPaginated()) {
                    if (setting.getFirstResult() == -1) {
                        return (Q) criteriaBuilder.page(setting.getEntityId(), setting.getMaxResults()).withKeysetExtraction(true);
                    } else {
                        return (Q) criteriaBuilder.page(setting.getKeysetPage(), setting.getFirstResult(), setting.getMaxResults());
                    }
                } else {
                    if (setting.getFirstResult() == -1) {
                        return (Q) criteriaBuilder.page(0, setting.getMaxResults());
                    } else {
                        return (Q) criteriaBuilder.page(setting.getFirstResult(), setting.getMaxResults());
                    }
                }
            } else {
                // When the result should be paginated, we have to properly paginate by the identifier of the view
                MethodAttribute<?, ?> idAttribute = ((ViewTypeImplementor<?>) managedView).getIdAttribute();
                String firstExpression;
                List<String> expressions;
                if (idAttribute.isSubview()) {
                    String prefix = getMapping(entityViewRoot, idAttribute, ef);
                    ManagedViewTypeImplementor<?> type = (ManagedViewTypeImplementor<?>) ((SingularAttribute<?, ?>) idAttribute).getType();
                    Set<MethodAttribute<?, ?>> attributes = (Set) type.getAttributes();
                    Iterator<MethodAttribute<?, ?>> iterator = attributes.iterator();
                    firstExpression = getMapping(prefix, iterator.next(), ef);
                    if (iterator.hasNext()) {
                        expressions = new ArrayList<>(attributes.size() - 1);
                        while (iterator.hasNext()) {
                            expressions.add(getMapping(prefix, iterator.next(), ef));
                        }
                    } else {
                        expressions = null;
                    }
                } else {
                    expressions = null;
                    firstExpression = getMapping(entityViewRoot, idAttribute, ef);
                }

                if (setting.isKeysetPaginated()) {
                    if (setting.getFirstResult() == -1) {
                        return (Q) criteriaBuilder.pageByAndNavigate(setting.getEntityId(), setting.getMaxResults(), firstExpression, getExpressionArray(expressions)).withKeysetExtraction(true);
                    } else {
                        return (Q) criteriaBuilder.pageBy(setting.getKeysetPage(), setting.getFirstResult(), setting.getMaxResults(), firstExpression, getExpressionArray(expressions));
                    }
                } else {
                    if (setting.getFirstResult() == -1) {
                        return (Q) criteriaBuilder.pageBy(0, setting.getMaxResults(), firstExpression, getExpressionArray(expressions));
                    } else {
                        return (Q) criteriaBuilder.pageBy(setting.getFirstResult(), setting.getMaxResults(), firstExpression, getExpressionArray(expressions));
                    }
                }
            }
        } else {
            return (Q) criteriaBuilder;
        }
    }

    private static String getMapping(String prefix, MethodAttribute<?, ?> attribute, ExpressionFactory ef) {
        String mapping = ((MappingAttribute<?, ?>) attribute).getMapping();
        // Id attributes are normally simple mappings, so we try to improve that case
        if (mapping.indexOf('(') == -1) {
            return prefix + "." + mapping;
        } else {
            // Since we have functions in here, we have to parse an properly prefix the mapping
            Expression expression = ef.createSimpleExpression(mapping, false);
            SimpleQueryGenerator generator = new PrefixingQueryGenerator(Arrays.asList(prefix));
            StringBuilder sb = new StringBuilder();
            generator.setQueryBuffer(sb);
            expression.accept(generator);
            return sb.toString();
        }
    }

    private static String[] getExpressionArray(List<String> expressions) {
        if (expressions == null || expressions.isEmpty()) {
            return null;
        }
        return expressions.toArray(new String[expressions.size()]);
    }

    private static void applyOptionalParameters(EntityViewSetting<?, ?> setting, CriteriaBuilder<?> normalCb) {
        // Add optional parameters
        if (setting.hasOptionalParameters()) {
            for (Map.Entry<String, Object> paramEntry : setting.getOptionalParameters().entrySet()) {
                if (normalCb.containsParameter(paramEntry.getKey()) && !normalCb.isParameterSet(paramEntry.getKey())) {
                    normalCb.setParameter(paramEntry.getKey(), paramEntry.getValue());
                }
            }
        }
    }

    private static void applyFilter(String entityViewRoot, Object key, boolean correlated, AttributeFilterProvider filterProvider, EntityViewSetting<?, ?> setting, CriteriaBuilder<?> cb, ExpressionFactory ef) {
        if (key instanceof SubqueryAttribute<?, ?>) {
            SubqueryAttribute<?, ?> subqueryAttribute = (SubqueryAttribute<?, ?>) key;
            SubqueryProvider provider = SubqueryProviderHelper.getFactory(subqueryAttribute.getSubqueryProvider()).create(cb, setting.getOptionalParameters());

            if (subqueryAttribute.getSubqueryExpression().isEmpty()) {
                filterProvider.apply(cb, null, null, provider);
            } else {
                if (entityViewRoot != null && entityViewRoot.length() > 0) {
                    // TODO: need to prefix the subqueryExpression with entityViewRoot
                    throw new UnsupportedOperationException("Filtering by a subquery that is applied on an entity view with a custom root is not yet supported!");
                }

                Expression expr = ef.createSimpleExpression(subqueryAttribute.getSubqueryExpression(), false);
                SimpleQueryGenerator generator = new PrefixingQueryGenerator(Arrays.asList(entityViewRoot));
                StringBuilder sb = new StringBuilder();
                generator.setQueryBuffer(sb);
                expr.accept(generator);
                filterProvider.apply(cb, subqueryAttribute.getSubqueryAlias(), sb.toString(), provider);
            }
        } else {
            String mapping = (String) key;
            if (entityViewRoot != null && entityViewRoot.length() > 0 && !correlated) {
                if (mapping.isEmpty()) {
                    filterProvider.apply(cb, entityViewRoot);
                } else {
                    Expression expr = ef.createSimpleExpression(mapping, false);
                    SimpleQueryGenerator generator = new PrefixingQueryGenerator(Arrays.asList(entityViewRoot));
                    StringBuilder sb = new StringBuilder();
                    generator.setQueryBuffer(sb);
                    expr.accept(generator);
                    filterProvider.apply(cb, sb.toString());
                }
            } else {
                filterProvider.apply(cb, mapping);
            }
        }
    }

    private static void applyAttributeFilters(EntityViewSetting<?, ?> setting, EntityViewManagerImpl evm, CriteriaBuilder<?> cb, ExpressionFactory ef, String entityViewRoot, boolean isQueryRoot) {
        ViewMetamodel metamodel = evm.getMetamodel();
        Metamodel jpaMetamodel = cb.getMetamodel();
        ViewType<?> viewType = metamodel.view(setting.getEntityViewClass());

        applyAttributeFilters(setting, evm, cb, ef, metamodel, jpaMetamodel, viewType, entityViewRoot);
        applyViewFilters(setting, evm, cb, viewType, entityViewRoot, isQueryRoot);
    }

    private static void applyViewFilters(EntityViewSetting<?, ?> setting, EntityViewManagerImpl evm, CriteriaBuilder<?> cb, ViewType<?> viewType, String entityViewRoot, boolean isQueryRoot) {
        // Add named view filter
        for (String filterName : setting.getViewFilters()) {
            ViewFilterMapping filterMapping = viewType.getViewFilter(filterName);

            if (filterMapping == null) {
                throw new IllegalArgumentException("Could not find view filter mapping with the name '" + filterName
                    + "' in the entity view type '" + viewType.getJavaType()
                        .getName() + "'");
            }

            if (!isQueryRoot && entityViewRoot != null && entityViewRoot.length() > 0) {
                // TODO: need to prefix all paths with entityViewRoot
                throw new UnsupportedOperationException("Applying a view filter on an entity view with a custom root is not yet supported!");
            }

            // TODO: allow parameter injection
            ViewFilterProvider provider = evm.createViewFilter(filterMapping.getFilterClass());
            provider.apply(cb);
        }
    }

    private static void applyAttributeFilters(EntityViewSetting<?, ?> setting, EntityViewManagerImpl evm, CriteriaBuilder<?> cb, ExpressionFactory ef, ViewMetamodel metamodel, Metamodel jpaMetamodel, ViewType<?> viewType, String entityViewRoot) throws IllegalArgumentException {
        for (Map.Entry<String, EntityViewSetting.AttributeFilterActivation> attributeFilterEntry : setting.getAttributeFilters().entrySet()) {
            String attributeName = attributeFilterEntry.getKey();
            EntityViewSetting.AttributeFilterActivation filterActivation = attributeFilterEntry.getValue();
            AttributeInfo attributeInfo = resolveAttributeInfo(metamodel, jpaMetamodel, viewType, attributeName);

            Class<? extends AttributeFilterProvider> filterClass;
            Class<?> expectedType;

            if (attributeInfo.entityAttribute) {
                throw new IllegalArgumentException("Attribute filter on entity attributes are not allowed!");
            } else {
                MethodAttribute<?, ?> attribute = attributeInfo.attribute;
                AttributeFilterMapping filterMapping = attribute.getFilter(filterActivation.getAttributeFilterName());

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
            }

            if (filterClass == null) {
                throw new IllegalArgumentException("No filter mapping given for the attribute '" + attributeName
                    + "' in the entity view type '" + viewType.getJavaType()
                        .getName() + "'");
            }
            
            Object expression;
            
            if (attributeInfo.mapping instanceof SubqueryAttribute) {
                expression = attributeInfo.mapping;
            } else {
                expression = getPrefixedExpression(ef, attributeInfo.subviewPrefixParts, attributeInfo.mapping.toString());
            }

            AttributeFilterProvider filter = evm.createAttributeFilter(filterClass, expectedType, filterActivation.getFilterValue());
            applyFilter(entityViewRoot, expression, attributeInfo.correlated, filter, setting, cb, ef);
        }
    }

    private static void applyAttributeSorters(EntityViewSetting<?, ?> setting, EntityViewManagerImpl evm, CriteriaBuilder<?> cb, ExpressionFactory ef, String entityViewRoot) {
        ViewMetamodel metamodel = evm.getMetamodel();
        Metamodel jpaMetamodel = cb.getMetamodel();
        ViewType<?> viewType = metamodel.view(setting.getEntityViewClass());

        for (Map.Entry<String, Sorter> attributeSorterEntry : setting.getAttributeSorters().entrySet()) {
            String attributeName = attributeSorterEntry.getKey();
            Sorter sorter = attributeSorterEntry.getValue();
            AttributeInfo attributeInfo = resolveAttributeInfo(metamodel, jpaMetamodel, viewType, attributeName);
            String mapping;

            if (attributeInfo.entityAttribute) {
                mapping = getPrefixedExpression(ef, attributeInfo.subviewPrefixParts, attributeInfo.mapping.toString());

                if (entityViewRoot != null && entityViewRoot.length() > 0) {
                    if (mapping.isEmpty()) {
                        mapping = entityViewRoot;
                    } else {
                        mapping = entityViewRoot + '.' + mapping;
                    }
                }
            } else {
                mapping = resolveAttributeAlias(viewType, attributeSorterEntry.getKey());
            }

            // TODO: order by subquery? #195
            sorter.apply(cb, mapping);
        }
    }

    private static String resolveAttributeAlias(ViewType<?> viewType, String attributeName) {
        String viewTypeName = viewType.getName();
        StringBuilder sb = new StringBuilder(viewTypeName.length() + attributeName.length() + 1);
        sb.append(viewTypeName)
            .append('_');

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
    
    private static MethodAttribute<?, ?> getAttribute(ManagedViewType<?> viewType, String attributeName, ViewType<?> baseViewType, String attributePath) {
        MethodAttribute<?, ?> attribute = viewType.getAttribute(attributeName);
        
        if (attribute == null) {
            throw new IllegalArgumentException("The attribute with the name '" + attributeName + "' couldn't be found on the view type '" + viewType.getJavaType().getName() + "' during resolving the attribute path '" + attributePath + "' on the view type '" + baseViewType.getName() + "'");
        }
        
        return attribute;
    }

    private static MethodAttribute<?, ?> getAttribute(ViewType<?> viewType, String attributeName) {
        MethodAttribute<?, ?> attribute = viewType.getAttribute(attributeName);
        
        if (attribute == null) {
            throw new IllegalArgumentException("The attribute with the name '" + attributeName + "' couldn't be found on the view type '" + viewType.getName() + "'");
        }
        
        return attribute;
    }
    
    private static AttributeInfo resolveAttributeInfo(ViewMetamodel metamodel, Metamodel jpaMetamodel, ViewType<?> viewType, String attributePath) {
        if (attributePath.indexOf('.') == -1) {
            MethodAttribute<?, ?> attribute = getAttribute(viewType, attributePath);
            List<String> subviewPrefixParts = null;
            boolean correlated = false;
            Object mapping;

            if (attribute.isSubquery()) {
                mapping = attribute;
            } else if (attribute.isCorrelated()) {
                subviewPrefixParts = new ArrayList<>();
                mapping = handleCorrelatedAttribute(viewType, attribute, subviewPrefixParts, attributePath, new String[]{ attributePath }, 0);
                correlated = true;
            } else {
                mapping = ((MappingAttribute<?, ?>) attribute).getMapping();
            }

            return new AttributeInfo(attribute, null, mapping, subviewPrefixParts, false, correlated);
        }

        String[] parts = attributePath.split("\\.");
        ManagedViewType<?> currentViewType = viewType;
        MethodAttribute<?, ?> currentAttribute = null;
        List<String> subviewPrefixParts = new ArrayList<String>();
        Object mapping = null;
        Attribute<?, ?> jpaAttribute = null;
        boolean foundEntityAttribute = false;
        boolean correlated = false;

        for (int i = 0; i < parts.length; i++) {
            currentAttribute = getAttribute(currentViewType, parts[i], viewType, attributePath);

            if (currentAttribute.isSubquery()) {
                // Note that if a subquery filtering is done, we ignore the mappings we gathered in the StringBuilder
                mapping = currentAttribute;

                if (i + 1 != parts.length) {
                    // Since subqueries can't return objects, it makes no sense to further navigate
                    throw new IllegalArgumentException("The given attribute path '" + attributePath
                            + "' is accessing the property '" + parts[i + 1] + "' of a subquery attribute in the type '"
                            + currentAttribute.getJavaType().getName() + "' which is illegal!");
                }
            } else if (currentAttribute.isSubview()) {
                if (currentAttribute.isCorrelated()) {
                    String correlationResult = handleCorrelatedAttribute(currentViewType, currentAttribute, subviewPrefixParts, attributePath, parts, i);

                    if (correlationResult != null && !correlationResult.isEmpty()) {
                        subviewPrefixParts.add(correlationResult);
                    }

                    correlated = true;
                    mapping = null;

                    if (currentAttribute.isCollection()) {
                        PluralAttribute<?, ?, ?> pluralAttribute = (PluralAttribute<?, ?, ?>) currentAttribute;
                        currentViewType = (ManagedViewType<?>) pluralAttribute.getElementType();
                    } else {
                        currentViewType = (ManagedViewType<?>) ((SingularAttribute<?, ?>) currentAttribute).getType();
                    }
                } else {
                    subviewPrefixParts.add(((MappingAttribute<?, ?>) currentAttribute).getMapping());

                    if (currentAttribute.isCollection()) {
                        PluralAttribute<?, ?, ?> pluralAttribute = (PluralAttribute<?, ?, ?>) currentAttribute;
                        currentViewType = (ManagedViewType<?>) pluralAttribute.getElementType();
                    } else {
                        currentViewType = (ManagedViewType<?>) ((SingularAttribute<?, ?>) currentAttribute).getType();
                    }
                }
            } else if (i + 1 != parts.length) {
                Class<?> maybeUnmanagedType = currentAttribute.getJavaType();
                i++;

                try {
                    ManagedType<?> type;
                    currentAttribute = null;
                    foundEntityAttribute = true;
                    StringBuilder newSb = new StringBuilder();
                    // If we get here, the attribute type is a managed type and we can copy the rest of the parts
                    for (; i < parts.length; i++) {
                        type = jpaMetamodel.managedType(maybeUnmanagedType);
                        newSb.append('.');
                        newSb.append(parts[i]);
                        jpaAttribute = type.getAttribute(parts[i]);
                        maybeUnmanagedType = jpaAttribute.getJavaType();
                    }

                    mapping = newSb.toString();
                    // At this point we consumed all parts
                    break;
                } catch (IllegalArgumentException ex) {
                    throw new IllegalArgumentException("The given attribute path '" + attributePath
                        + "' is accessing the possibly unknown property '" + parts[i] + "' of the type '" + maybeUnmanagedType
                        .getName() + "' which is illegal!", ex);
                }
            } else {
                // This is the last mapping
                if (currentAttribute.isCorrelated()) {
                    mapping = handleCorrelatedAttribute(currentViewType, currentAttribute, subviewPrefixParts, attributePath, parts, i);
                    correlated = true;
                } else {
                    mapping = ((MappingAttribute<?, ?>) currentAttribute).getMapping();
                }
                // Make it explicit, that if this branch is entered, the loop will be exited
                break;
            }
        }

        if (mapping == null) {
            throw new IllegalStateException("The mapping should never be null! This must be a bug.");
        }

        return new AttributeInfo(currentAttribute, jpaAttribute, mapping, subviewPrefixParts, foundEntityAttribute, correlated);
    }

    private static String handleCorrelatedAttribute(ManagedViewType<?> currentViewType, MethodAttribute<?, ?> currentAttribute, List<String> subviewPrefixParts, String attributePath, String[] parts, int i) {
        if (currentAttribute.getFetchStrategy() != FetchStrategy.JOIN) {
            // Since we can't filter or sort non-join fetched correlated attributes, it makes no sense to further navigate
            throw new IllegalArgumentException("The given attribute path '" + attributePath
                    + "' is accessing the correlated attribute '" + parts[i] + "' in the type '"
                    + currentViewType.getJavaType().getName() + "' that uses a fetch strategy other than 'FetchStrategy.JOIN' which is illegal!");
        }

        // We rest this here since the correlation alias is the new base
        subviewPrefixParts.clear();
        // Build the attribute path to generate the correlation alias
        StringBuilder sb = new StringBuilder(attributePath.length());
        for (int j = 0; j <= i; j++) {
            sb.append(parts[j]).append('.');
        }
        subviewPrefixParts.add(CorrelationProviderHelper.getDefaultCorrelationAlias(sb.substring(0, sb.length() - 1)));
        CorrelatedAttribute<?, ?> correlatedAttribute = (CorrelatedAttribute<?, ?>) currentAttribute;
        return correlatedAttribute.getCorrelationResult();
    }

    private static String getPrefixedExpression(ExpressionFactory ef, List<String> subviewPrefixParts, String mappingExpression) {
        String expression = AbstractAttribute.stripThisFromMapping(mappingExpression);
        if (expression.isEmpty()) {
            if (subviewPrefixParts != null && subviewPrefixParts.size() > 0) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < subviewPrefixParts.size(); i++) {
                    String s = AbstractAttribute.stripThisFromMapping(subviewPrefixParts.get(i));
                    if (!s.isEmpty()) {
                        sb.append(s);
                        sb.append('.');
                    }
                }
                if (sb.length() != 0) {
                    return sb.substring(0, sb.length() - 1);
                }
            }

            return "";
        }
        if (subviewPrefixParts != null && subviewPrefixParts.size() > 0) {
            Expression expr = ef.createSimpleExpression(expression, false);
            List<String> prefixes = new ArrayList<>(subviewPrefixParts.size());

            // Strip off all "this" parts
            for (String prefix : subviewPrefixParts) {
                String s = AbstractAttribute.stripThisFromMapping(prefix);
                if (!s.isEmpty()) {
                    prefixes.add(s);
                }
            }

            SimpleQueryGenerator generator = new PrefixingQueryGenerator(prefixes);
            StringBuilder sb = new StringBuilder();
            generator.setQueryBuffer(sb);
            expr.accept(generator);
            return sb.toString();
        }
        
        return AbstractAttribute.stripThisFromMapping(mappingExpression);
    }

    /**
     * @author Christian Beikov
     * @since 1.0.0
     */
    private static class AttributeInfo {

        private final MethodAttribute<?, ?> attribute;
//        private final Attribute<?, ?> jpaAttribute;
        private final Object mapping;
        private final List<String> subviewPrefixParts;
        private final boolean entityAttribute;
        private final boolean correlated;

        public AttributeInfo(MethodAttribute<?, ?> attribute, Attribute<?, ?> jpaAttribute, Object mapping, List<String> subviewPrefixParts, boolean entityAttribute, boolean correlated) {
            this.attribute = attribute;
//            this.jpaAttribute = jpaAttribute;
            this.mapping = mapping;
            this.subviewPrefixParts = subviewPrefixParts;
            this.entityAttribute = entityAttribute;
            this.correlated = correlated;
        }

    }
}
