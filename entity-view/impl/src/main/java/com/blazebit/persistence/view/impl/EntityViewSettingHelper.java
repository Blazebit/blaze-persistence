/*
 * Copyright 2014 Blazebit.
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
import com.blazebit.persistence.QueryBuilder;
import com.blazebit.persistence.view.AttributeFilterProvider;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.Sorter;
import com.blazebit.persistence.view.SubqueryProvider;
import com.blazebit.persistence.view.ViewFilterProvider;
import com.blazebit.persistence.view.metamodel.AttributeFilterMapping;
import com.blazebit.persistence.view.metamodel.MappingAttribute;
import com.blazebit.persistence.view.metamodel.MethodAttribute;
import com.blazebit.persistence.view.metamodel.PluralAttribute;
import com.blazebit.persistence.view.metamodel.SubqueryAttribute;
import com.blazebit.persistence.view.metamodel.ViewFilterMapping;
import com.blazebit.persistence.view.metamodel.ViewMetamodel;
import com.blazebit.persistence.view.metamodel.ViewType;
import java.util.Map;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;

/**
 * @author Christian Beikov
 * @since 1.0
 */
public final class EntityViewSettingHelper {

    public static <T, Q extends QueryBuilder<T, Q>> Q apply(EntityViewSetting<T, Q> setting, EntityViewManagerImpl evm, CriteriaBuilder<?> criteriaBuilder) {
        applyAttributeFilters(setting, evm, criteriaBuilder);
        applyAttributeSorters(setting, evm, criteriaBuilder);
        CriteriaBuilder<T> normalCb = evm.applyObjectBuilder(setting.getEntityViewClass(), criteriaBuilder);
        applyOptionalParameters(setting, normalCb);

        if (setting.isPaginated()) {
            return (Q) normalCb.page(setting.getFirstResult(), setting.getMaxResults());
        } else {
            return (Q) criteriaBuilder;
        }
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

    private static void applyFilter(Object key, AttributeFilterProvider filterProvider, CriteriaBuilder<?> cb) {
        if (key instanceof SubqueryAttribute<?, ?>) {
            SubqueryAttribute<?, ?> subqueryAttribute = (SubqueryAttribute<?, ?>) key;
            SubqueryProvider provider;

            try {
                provider = subqueryAttribute.getSubqueryProvider()
                    .newInstance();
            } catch (Exception ex) {
                throw new IllegalArgumentException("Could not instantiate the subquery provider: " + subqueryAttribute
                    .getSubqueryProvider()
                    .getName(), ex);
            }

            if (subqueryAttribute.getSubqueryExpression().isEmpty()) {
                filterProvider.apply(cb, null, null, provider);
            } else {
                filterProvider.apply(cb, subqueryAttribute.getSubqueryAlias(), subqueryAttribute.getSubqueryExpression(), provider);
            }
        } else {
            filterProvider.apply(cb, (String) key);
        }
    }

    private static void applyAttributeFilters(EntityViewSetting<?, ?> setting, EntityViewManagerImpl evm, CriteriaBuilder<?> cb) {
        ViewMetamodel metamodel = evm.getMetamodel();
        Metamodel jpaMetamodel = cb.getMetamodel();
        ViewType<?> viewType = metamodel.view(setting.getEntityViewClass());

        applyAttributeFilters(setting, evm, cb, metamodel, jpaMetamodel, viewType);
        applyAttributeNamedFilters(setting, evm, cb, metamodel, jpaMetamodel, viewType);
        applyViewFilters(setting, evm, cb, viewType);
    }

    private static void applyViewFilters(EntityViewSetting<?, ?> setting, EntityViewManagerImpl evm, CriteriaBuilder<?> cb, ViewType<?> viewType) throws IllegalArgumentException {
        // Add named view filter
        for (String filterName : setting.getViewFilters()) {
            ViewFilterMapping filterMapping = viewType.getViewFilter(filterName);

            if (filterMapping == null) {
                throw new IllegalArgumentException("Could not find view filter mapping with the name '" + filterName
                    + "' in the entity view type '" + viewType.getJavaType()
                        .getName() + "'");
            }
            
            ViewFilterProvider provider = evm.createViewFilter(filterMapping.getFilterClass());
            provider.apply(cb);
        }
    }

    private static void applyAttributeFilters(EntityViewSetting<?, ?> setting, EntityViewManagerImpl evm, CriteriaBuilder<?> cb, ViewMetamodel metamodel, Metamodel jpaMetamodel, ViewType<?> viewType) throws IllegalArgumentException {
        for (Map.Entry<String, Object> attributeFilterEntry : setting.getAttributeFilters().entrySet()) {
            String attributeName = attributeFilterEntry.getKey();
            Object filterValue = attributeFilterEntry.getValue();
            AttributeInfo attributeInfo = resolveAttributeInfo(metamodel, jpaMetamodel, viewType, attributeName);

            Class<? extends AttributeFilterProvider> filterClass;
            Class<?> expectedType;

            if (attributeInfo.entityAttribute) {
                // No filters available
                filterClass = null;
                expectedType = null;
            } else {
                MethodAttribute<?, ?> attribute = attributeInfo.attribute;
                AttributeFilterMapping filterMapping = attribute.getFilter(attribute.getName());

                if (filterMapping == null) {
                    throw new IllegalArgumentException("Could not find view filter mapping with the name '" + attribute.getName()
                        + "' in the entity view type '" + attribute.getDeclaringType().getJavaType()
                            .getName() + "'");
                }
                
                filterClass = filterMapping.getFilterClass();
                expectedType = attribute.getJavaType();
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
                expression = getPrefixedExpression(attributeInfo.subviewPrefix, attributeInfo.mapping.toString());
            }

            AttributeFilterProvider filter = evm.createAttributeFilter(filterClass, expectedType, filterValue);
            applyFilter(expression, filter, cb);
        }
    }

    private static void applyAttributeNamedFilters(EntityViewSetting<?, ?> setting, EntityViewManagerImpl evm, CriteriaBuilder<?> cb, ViewMetamodel metamodel, Metamodel jpaMetamodel, ViewType<?> viewType) throws IllegalArgumentException {
        for (String filterName : setting.getAttributeNamedFilters()) {
            AttributeFilterMapping filterMapping = viewType.getAttributeFilter(filterName);

            if (filterMapping == null) {
                throw new IllegalArgumentException("Could not find attribute filter mapping with the name '" + filterName
                    + "' in the entity view type '" + viewType.getJavaType()
                        .getName() + "'");
            }
            
            MethodAttribute<?, ?> attribute = filterMapping.getDeclaringAttribute();
            AttributeInfo attributeInfo = resolveAttributeInfo(metamodel, jpaMetamodel, viewType, attribute.getName());
            AttributeFilterProvider provider;
            
            if (attributeInfo.entityAttribute) {
                throw new IllegalArgumentException("Attribute filter on entity attributes are not allowed!");
            } else {
                provider = evm.createAttributeFilter(filterMapping.getFilterClass(), attributeInfo.attribute.getJavaType(), null);
            }
            
            Object expression;
            
            if (attributeInfo.mapping instanceof SubqueryAttribute) {
                expression = attributeInfo.mapping;
            } else {
                expression = getPrefixedExpression(attributeInfo.subviewPrefix, attributeInfo.mapping.toString());
            }
            
            applyFilter(expression, provider, cb);
        }
    }
    
    private static void applyAttributeSorters(EntityViewSetting<?, ?> setting, EntityViewManagerImpl evm, CriteriaBuilder<?> cb) {
        ViewMetamodel metamodel = evm.getMetamodel();
        Metamodel jpaMetamodel = cb.getMetamodel();
        ViewType<?> viewType = metamodel.view(setting.getEntityViewClass());

        for (Map.Entry<String, Sorter> attributeSorterEntry : setting.getAttributeSorters().entrySet()) {
            String attributeName = attributeSorterEntry.getKey();
            Sorter sorter = attributeSorterEntry.getValue();
            AttributeInfo attributeInfo = resolveAttributeInfo(metamodel, jpaMetamodel, viewType, attributeName);
            String mapping;

            if (attributeInfo.entityAttribute) {
                mapping = getPrefixedExpression(attributeInfo.subviewPrefix, attributeInfo.mapping.toString());
            } else {
                mapping = resolveAttributeAlias(viewType, attributeSorterEntry.getKey());
            }

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

    private static AttributeInfo resolveAttributeInfo(ViewMetamodel metamodel, Metamodel jpaMetamodel, ViewType<?> viewType, String attributePath) {
        if (attributePath.indexOf('.') == -1) {
            MethodAttribute<?, ?> attribute = viewType.getAttribute(attributePath);
            Object mapping;

            if (attribute.isSubquery()) {
                mapping = attribute;
            } else {
                mapping = ((MappingAttribute<?, ?>) attribute).getMapping();
            }

            return new AttributeInfo(attribute, null, mapping, null, false);
        }

        String[] parts = attributePath.split("\\.");
        ViewType<?> currentViewType = viewType;
        MethodAttribute<?, ?> currentAttribute = null;
        StringBuilder sb = new StringBuilder();
        Object mapping = null;
        Attribute<?, ?> jpaAttribute = null;
        boolean foundEntityAttribute = false;

        for (int i = 0; i < parts.length; i++) {
            currentAttribute = currentViewType.getAttribute(parts[i]);

            if (currentAttribute.isSubquery()) {
                // Note that if a subquery filtering is done, we ignore the mappings we gathered in the StringBuilder
                mapping = currentAttribute;

                if (i + 1 != parts.length) {
                    // Since subqueries can't return objects, it makes no sense to further navigate
                    throw new IllegalArgumentException("The given attribute path '" + attributePath
                        + "' is accessing the property '" + parts[i + 1] + "' of a subquery attribute in the type '"
                        + currentAttribute.getJavaType()
                        .getName() + "' which is illegal!");
                }
            } else if (currentAttribute.isSubview()) {
                if (i != 0) {
                    sb.append('.');
                }

                sb.append(((MappingAttribute<?, ?>) currentAttribute).getMapping());

                if (currentAttribute.isCollection()) {
                    PluralAttribute<?, ?, ?> pluralAttribute = (PluralAttribute<?, ?, ?>) currentAttribute;
                    currentViewType = metamodel.view(pluralAttribute.getElementType());
                } else {
                    currentViewType = metamodel.view(currentAttribute.getJavaType());
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
                if (i != 0) {
                    sb.append('.');
                }

                // This is the last mapping
                mapping = ((MappingAttribute<?, ?>) currentAttribute).getMapping();
                // Make it explicit, that if this branch is entered, the loop will be exited
                break;
            }
        }

        if (mapping == null) {
            throw new IllegalStateException("The mapping should never be null! This must be a bug.");
        }

        return new AttributeInfo(currentAttribute, jpaAttribute, mapping, sb.toString(), foundEntityAttribute);
    }

    private static String getPrefixedExpression(String subviewPrefix, String mappingExpression) {
        if (subviewPrefix != null && subviewPrefix.length() > 0) {
            StringBuilder sb = new StringBuilder(subviewPrefix.length() + mappingExpression.length());
            sb.append(subviewPrefix);
            sb.append(mappingExpression);
            return sb.toString();
        }
        
        return mappingExpression;
    }

    private static class AttributeInfo {

        private final MethodAttribute<?, ?> attribute;
        private final Attribute<?, ?> jpaAttribute;
        private final Object mapping;
        private final String subviewPrefix;
        private final boolean entityAttribute;

        public AttributeInfo(MethodAttribute<?, ?> attribute, Attribute<?, ?> jpaAttribute, Object mapping, String subviewPrefix, boolean entityAttribute) {
            this.attribute = attribute;
            this.jpaAttribute = jpaAttribute;
            this.mapping = mapping;
            this.subviewPrefix = subviewPrefix;
            this.entityAttribute = entityAttribute;
        }

    }
}
