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

package com.blazebit.persistence.view.impl.metamodel;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.regex.Pattern;

import javax.persistence.metamodel.ManagedType;

import com.blazebit.persistence.impl.expression.SyntaxErrorException;
import com.blazebit.persistence.view.BatchFetch;
import com.blazebit.persistence.view.CorrelationProvider;
import com.blazebit.persistence.view.FetchStrategy;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.MappingCorrelated;
import com.blazebit.persistence.view.MappingCorrelatedSimple;
import com.blazebit.persistence.view.MappingParameter;
import com.blazebit.persistence.view.MappingSubquery;
import com.blazebit.persistence.view.SubqueryProvider;
import com.blazebit.persistence.view.impl.CollectionJoinMappingGathererExpressionVisitor;
import com.blazebit.persistence.view.impl.CorrelationProviderHelper;
import com.blazebit.persistence.view.impl.ScalarTargetResolvingExpressionVisitor;
import com.blazebit.persistence.view.impl.UpdatableExpressionVisitor;
import com.blazebit.persistence.view.impl.ScalarTargetResolvingExpressionVisitor.TargetType;
import com.blazebit.persistence.view.metamodel.Attribute;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.MappingConstructor;
import com.blazebit.persistence.view.metamodel.PluralAttribute;
import com.blazebit.reflection.ReflectionUtils;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public abstract class AbstractAttribute<X, Y> implements Attribute<X, Y> {

    private static final String[] EMPTY = new String[0];
    private static final String THIS = "this";
    private static final Pattern PREFIX_THIS_REPLACE_PATTERN = Pattern.compile("([^a-zA-Z0-9\\.])this\\.");

    protected final ManagedViewType<X> declaringType;
    protected final Class<Y> javaType;
    protected final String mapping;
    protected final String[] fetches;
    protected final FetchStrategy fetchStrategy;
    protected final int batchSize;
    protected final Class<? extends SubqueryProvider> subqueryProvider;
    protected final String subqueryExpression;
    protected final String subqueryAlias;
    protected final Class<? extends CorrelationProvider> correlationProvider;
    protected final String correlationBasis;
    protected final String correlationResult;
    protected final Class<?> correlated;
    protected final String correlationKeyAlias;
    protected final String correlationExpression;
    protected final boolean queryParameter;
    protected final boolean id;
    protected final boolean subview;

    public AbstractAttribute(ManagedViewType<X> declaringType, Class<Y> javaType, Annotation mapping, BatchFetch batchFetch, String errorLocation, MetamodelBuildingContext context) {
        if (javaType == null) {
            context.addError("The attribute type is not resolvable " + errorLocation);
        }

        int batchSize;
        if (batchFetch == null || batchFetch.size() == -1) {
            batchSize = -1;
        } else if (batchFetch.size() < 1) {
            context.addError("Illegal batch fetch size defined at '" + errorLocation + "'! Use a value greater than 0!");
            batchSize = Integer.MIN_VALUE;
        } else {
            batchSize = batchFetch.size();
        }

        this.declaringType = declaringType;
        this.javaType = javaType;
        this.subview = context.isEntityView(javaType);

        if (mapping instanceof IdMapping) {
            this.mapping = ((IdMapping) mapping).value();
            this.fetches = EMPTY;
            this.fetchStrategy = FetchStrategy.JOIN;
            this.batchSize = -1;
            this.subqueryProvider = null;
            this.id = true;
            this.queryParameter = false;
            this.subqueryExpression = null;
            this.subqueryAlias = null;
            this.correlationBasis = null;
            this.correlationResult = null;
            this.correlationProvider = null;
            this.correlated = null;
            this.correlationKeyAlias = null;
            this.correlationExpression = null;
        } else if (mapping instanceof Mapping) {
            Mapping m = (Mapping) mapping;
            this.mapping = m.value();
            this.fetches = m.fetches();
            this.fetchStrategy = m.fetch();
            this.batchSize = batchSize;
            this.subqueryProvider = null;
            this.id = false;
            this.queryParameter = false;
            this.subqueryExpression = null;
            this.subqueryAlias = null;
            this.correlationBasis = null;
            this.correlationResult = null;
            this.correlationProvider = null;
            this.correlated = null;
            this.correlationKeyAlias = null;
            this.correlationExpression = null;
        } else if (mapping instanceof MappingParameter) {
            this.mapping = ((MappingParameter) mapping).value();
            this.fetches = EMPTY;
            this.fetchStrategy = FetchStrategy.JOIN;
            this.batchSize = -1;
            this.subqueryProvider = null;
            this.id = false;
            this.queryParameter = true;
            this.subqueryExpression = null;
            this.subqueryAlias = null;
            this.correlationBasis = null;
            this.correlationResult = null;
            this.correlationProvider = null;
            this.correlated = null;
            this.correlationKeyAlias = null;
            this.correlationExpression = null;
        } else if (mapping instanceof MappingSubquery) {
            MappingSubquery mappingSubquery = (MappingSubquery) mapping;
            this.mapping = null;
            this.fetches = EMPTY;
            this.subqueryProvider = mappingSubquery.value();
            this.fetchStrategy = FetchStrategy.JOIN;
            this.batchSize = -1;
            this.id = false;
            this.queryParameter = false;
            this.subqueryExpression = mappingSubquery.expression();
            this.subqueryAlias = mappingSubquery.subqueryAlias();
            this.correlationBasis = null;
            this.correlationResult = null;
            this.correlationProvider = null;
            this.correlated = null;
            this.correlationKeyAlias = null;
            this.correlationExpression = null;

            if (!subqueryExpression.isEmpty() && subqueryAlias.isEmpty()) {
                context.addError("The subquery alias is empty although the subquery expression is not " + errorLocation);
            }
            if (subqueryProvider.getEnclosingClass() != null && !Modifier.isStatic(subqueryProvider.getModifiers())) {
                context.addError("The subquery provider is defined as non-static inner class. Make it static, otherwise it can't be instantiated: " + errorLocation);
            }
        } else if (mapping instanceof MappingCorrelated) {
            MappingCorrelated mappingCorrelated = (MappingCorrelated) mapping;
            this.mapping = null;
            this.fetches = mappingCorrelated.fetches();
            this.fetchStrategy = mappingCorrelated.fetch();

            if (fetchStrategy == FetchStrategy.SELECT) {
                this.batchSize = batchSize;
            } else {
                this.batchSize = -1;
            }

            this.subqueryProvider = null;
            this.id = false;
            this.queryParameter = false;
            this.subqueryExpression = null;
            this.subqueryAlias = null;
            this.correlationBasis = mappingCorrelated.correlationBasis();
            this.correlationResult = mappingCorrelated.correlationResult();
            this.correlationProvider = mappingCorrelated.correlator();
            this.correlated = null;
            this.correlationKeyAlias = null;
            this.correlationExpression = null;

            if (correlationProvider.getEnclosingClass() != null && !Modifier.isStatic(correlationProvider.getModifiers())) {
                context.addError("The correlation provider is defined as non-static inner class. Make it static, otherwise it can't be instantiated: " + errorLocation);
            }
            if (mappingCorrelated.correlationBasis().isEmpty()) {
                context.addError("Illegal empty correlation basis in the " + getLocation());
            }
        } else if (mapping instanceof MappingCorrelatedSimple) {
            MappingCorrelatedSimple mappingCorrelated = (MappingCorrelatedSimple) mapping;
            this.mapping = null;
            this.fetches = mappingCorrelated.fetches();
            this.fetchStrategy = mappingCorrelated.fetch();

            if (fetchStrategy == FetchStrategy.SELECT) {
                this.batchSize = batchSize;
            } else {
                this.batchSize = -1;
            }

            this.subqueryProvider = null;
            this.id = false;
            this.queryParameter = false;
            this.subqueryExpression = null;
            this.subqueryAlias = null;
            this.correlationProvider = CorrelationProviderHelper.createCorrelationProvider(mappingCorrelated.correlated(), mappingCorrelated.correlationKeyAlias(), mappingCorrelated.correlationExpression(), context);
            this.correlationBasis = mappingCorrelated.correlationBasis();
            this.correlationResult = mappingCorrelated.correlationResult();
            this.correlated = mappingCorrelated.correlated();
            this.correlationKeyAlias = mappingCorrelated.correlationKeyAlias();
            this.correlationExpression = mappingCorrelated.correlationExpression();

            if (mappingCorrelated.correlationBasis().isEmpty()) {
                context.addError("Illegal empty correlation basis in the " + getLocation());
            }
        } else {
            context.addError("No mapping annotation could be found " + errorLocation);
            this.mapping = null;
            this.fetches = EMPTY;
            this.fetchStrategy = null;
            this.batchSize = Integer.MIN_VALUE;
            this.subqueryProvider = null;
            this.id = false;
            this.queryParameter = false;
            this.subqueryExpression = null;
            this.subqueryAlias = null;
            this.correlationBasis = null;
            this.correlationResult = null;
            this.correlationProvider = null;
            this.correlated = null;
            this.correlationKeyAlias = null;
            this.correlationExpression = null;
        }
    }

    public static String stripThisFromMapping(String mapping) {
        return replaceThisFromMapping(mapping, "");
    }

    public static String replaceThisFromMapping(String mapping, String root) {
        if (mapping == null) {
            return null;
        }
        mapping = mapping.trim();
        if (mapping.startsWith(THIS)) {
            // Special case when the mapping start with "this"
            if (mapping.length() == THIS.length()) {
                // Return the empty string if it essentially equals "this"
                return root;
            }
            if (root.isEmpty()) {
                char nextChar = mapping.charAt(THIS.length());
                if (nextChar == '.') {
                    // Only replace if it isn't a prefix
                    mapping = mapping.substring(THIS.length() + 1);
                }
            } else {
                mapping = root + mapping.substring(THIS.length());
            }
        }

        String replacement;
        if (root.isEmpty()) {
            replacement = "$1";
        } else {
            replacement = "$1" + root + ".";
        }
        mapping = PREFIX_THIS_REPLACE_PATTERN.matcher(mapping)
                .replaceAll(replacement);

        return mapping;
    }

    /**
     * Collects all mappings that involve the use of a collection attribute for duplicate usage checks.
     *
     * @param managedType The JPA type against which to evaluate the mapping
     * @param context The metamodel context
     * @return The mappings which contain collection attribute uses
     */
    public Set<String> getCollectionJoinMappings(ManagedType<?> managedType, MetamodelBuildingContext context) {
        if (mapping == null || queryParameter) {
            // Subqueries and parameters can't be checked
            return Collections.emptySet();
        }
        
        CollectionJoinMappingGathererExpressionVisitor visitor = new CollectionJoinMappingGathererExpressionVisitor(managedType, context.getEntityMetamodel());
        String expression = stripThisFromMapping(mapping);
        if (expression.isEmpty()) {
            return Collections.emptySet();
        }

        context.getExpressionFactory().createSimpleExpression(expression, false).accept(visitor);
        Set<String> mappings = new HashSet<String>();
        
        for (String s : visitor.getPaths()) {
            mappings.add(s);
        }
        
        return mappings;
    }

    public void checkAttributeCorrelationUsage(Map<Class<?>, ManagedViewTypeImpl<?>> managedViews, Set<ManagedViewType<?>> seenViewTypes, Set<MappingConstructor<?>> seenConstructors, MetamodelBuildingContext context) {
        if (isSubview()) {
            ManagedViewTypeImpl<?> subviewType;
            if (isCollection()) {
                subviewType = managedViews.get(((PluralAttribute<?, ?, ?>) this).getElementType());
            } else {
                subviewType = managedViews.get(javaType);
            }
            subviewType.checkAttributesCorrelationUsage(managedViews, seenViewTypes, seenConstructors, context);
        }
    }

    private static enum ExpressionLocation {
        MAPPING("mapping expression"),
        CORRELATION_BASIS("correlation basis"),
        CORRELATION_RESULT("correlation result"),
        CORRELATION_EXPRESSION("correlation expression");

        private final String location;

        ExpressionLocation(String location) {
            this.location = location;
        }

        @Override
        public String toString() {
            return location;
        }
    }

    private static boolean isCompatible(TargetType t, Class<?> targetType, Class<?> targetElementType, boolean subtypesAllowed) {
        if (t.hasCollectionJoin()) {
            return isCompatible(t.getLeafBaseClass(), t.getLeafBaseValueClass(), targetType, targetElementType, subtypesAllowed);
        } else {
            return isCompatible(t.getLeafBaseClass(), null, targetType, targetElementType, subtypesAllowed);
        }
    }

    /**
     * Checks if <code>possibleTargetType</code> with an optional element type <code>possibleTargetElementType</code>
     * can be mapped to <code>targetType</code> with the optional element type <code>targetElementType</code> and the given <code>subtypesAllowed</code> config.
     *
     * A <code>possibleTargetType</code> of <code>NULL</code> represents the <i>any type</i> which makes it always compatible i.e. returning <code>true</code>.
     * A type is compatible if the source types given by <code>possibleTargetType</code>/<code>possibleTargetElementType</code>
     * are subtypes of the target types <code>targetType</code>/<code>targetElementType</code>.
     *
     * A source collection type it is also compatible with non-collection targets if the source element type is a subtype of the target type.
     * A source non-collection type is also compatible with a collection target if the source type is a subtype of the target element type.
     *
     * @param possibleTargetType The source type
     * @param possibleTargetElementType The optional source element type
     * @param targetType The target type
     * @param targetElementType The optional target element type
     * @param subtypesAllowed Whether a more specific source type is allowed to map to a general target type
     * @return True if mapping from <code>possibleTargetType</code>/<code>possibleTargetElementType</code> to <code>targetType</code>/<code>targetElementType</code> is possible
     */
    private static boolean isCompatible(Class<?> possibleTargetType, Class<?> possibleTargetElementType, Class<?> targetType, Class<?> targetElementType, boolean subtypesAllowed) {
        // Null is the marker for ANY TYPE
        if (possibleTargetType == null) {
            return true;
        }

        if (subtypesAllowed) {
            if (possibleTargetElementType != null) {
                if (targetElementType != null) {
                    // Mapping a plural entity attribute to a plural view attribute
                    return targetType.isAssignableFrom(possibleTargetType)
                            && targetElementType.isAssignableFrom(possibleTargetElementType);
                } else {
                    // Mapping a plural entity attribute to a singular view attribute
                    return targetType.isAssignableFrom(possibleTargetElementType);
                }
            } else {
                if (targetElementType != null) {
                    // Mapping a singular entity attribute to a plural view attribute
                    return targetElementType.isAssignableFrom(possibleTargetType);
                } else {
                    // Mapping a singular entity attribute to a singular view attribute
                    return targetType.isAssignableFrom(possibleTargetType);
                }
            }
        } else {
            if (possibleTargetElementType != null) {
                if (targetElementType != null) {
                    // Mapping a plural entity attribute to a plural view attribute
                    return targetType == possibleTargetType
                            && targetElementType == possibleTargetElementType;
                } else {
                    // Mapping a plural entity attribute to a singular view attribute
                    return targetType == possibleTargetElementType;
                }
            } else {
                if (targetElementType != null) {
                    // Mapping a singular entity attribute to a plural view attribute
                    return targetElementType == possibleTargetType;
                } else {
                    // Mapping a singular entity attribute to a singular view attribute
                    return targetType == possibleTargetType;
                }
            }
        }
    }

    private static void validateTypesCompatible(ManagedType<?> managedType, String expression, Class<?> targetType, Class<?> targetElementType, boolean subtypesAllowed, MetamodelBuildingContext context, ExpressionLocation expressionLocation, String location) {
        final Class<?> expressionType = targetType;
        if (expression.isEmpty()) {
            if (isCompatible(managedType.getJavaType(), null, targetType, targetElementType, subtypesAllowed)) {
                return;
            }
            StringBuilder sb = new StringBuilder();
            sb.append('[');
            sb.append(managedType.getJavaType().getName());
            sb.append(']');
            context.addError("The resolved possible types " + sb.toString() + " are not assignable to the given expression type '" + expressionType.getName() + "' of the " + expressionLocation + " declared by the " + location + "!");
            return;
        }

        ScalarTargetResolvingExpressionVisitor visitor = new ScalarTargetResolvingExpressionVisitor(managedType, context.getEntityMetamodel());

        try {
            context.getExpressionFactory().createSimpleExpression(expression, false).accept(visitor);
        } catch (SyntaxErrorException ex) {
            context.addError("Syntax error in " + expressionLocation + " '" + expression + "' of the " + location + ": " + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            context.addError("An error occurred while trying to resolve the " + expressionLocation + " of the " + location + ": " + ex.getMessage());
        }

        List<TargetType> possibleTargets = visitor.getPossibleTargets();

        if (!possibleTargets.isEmpty()) {
            boolean error = true;
            for (TargetType t : possibleTargets) {
                if (isCompatible(t, targetType, targetElementType, subtypesAllowed)) {
                    error = false;
                    break;
                }
            }

            if (error) {
                if (targetType.isPrimitive()) {
                    targetType = ReflectionUtils.getObjectClassOfPrimitve(targetType);
                } else {
                    targetType = ReflectionUtils.getPrimitiveClassOfWrapper(targetType);
                }

                if (targetType != null) {
                    for (TargetType t : possibleTargets) {
                        if (isCompatible(t, targetType, targetElementType, subtypesAllowed)) {
                            error = false;
                            break;
                        }
                    }
                }
            }

            if (error) {
                StringBuilder sb = new StringBuilder();
                sb.append('[');
                for (TargetType t : possibleTargets) {
                    sb.append(t.getLeafBaseClass().getName());
                    sb.append(", ");
                }

                sb.setLength(sb.length() - 2);
                sb.append(']');
                context.addError("The resolved possible types " + sb.toString() + " are not assignable to the given expression type '" + expressionType.getName() + "' of the " + expressionLocation + " declared by the " + location + "!");
            }
        }
    }

    public void checkAttribute(ManagedType<?> managedType, Map<Class<?>, ManagedViewTypeImpl<?>> managedViews, MetamodelBuildingContext context) {
        Class<?> expressionType = getJavaType();
        Class<?> elementType = null;

        if (fetches.length != 0) {
            ManagedType<?> entityType = context.getEntityMetamodel().getManagedType(getElementType());
            if (entityType == null) {
                context.addError("Specifying fetches for non-entity attribute type [" + Arrays.toString(fetches) + "] at the " + getLocation() + " is not allowed!");
            } else {
                ScalarTargetResolvingExpressionVisitor visitor = new ScalarTargetResolvingExpressionVisitor(entityType, context.getEntityMetamodel());
                for (int i = 0; i < fetches.length; i++) {
                    final String fetch = fetches[i];
                    final String errorLocation;
                    if (fetches.length == 1) {
                        errorLocation = "the fetch expression";
                    } else {
                        errorLocation = "the " + (i + 1) + ". fetch expression";
                    }
                    visitor.clear();

                    try {
                        // Validate the fetch expression parses
                        context.getExpressionFactory().createPathExpression(fetch).accept(visitor);
                    } catch (SyntaxErrorException ex) {
                        context.addError("Syntax error in " + errorLocation + " '" + fetch + "' of the " + getLocation() + ": " + ex.getMessage());
                    } catch (IllegalArgumentException ex) {
                        context.addError("An error occurred while trying to resolve the " + errorLocation + " '" + fetch + "' of the " + getLocation() + ": " + ex.getMessage());
                    }
                }
            }
        }

        if (isCollection()) {
            elementType = getElementType();

            if (isUpdatable()) {
                // Updatable collection attributes currently must have the same collection type
            } else {
                if (isIndexed()) {
                    if (getCollectionType() == PluralAttribute.CollectionType.MAP) {
                        // All map types can be sourced from a map
                        expressionType = Map.class;
                    } else {
                        // An indexed list can only be sourced from an indexed list
                        expressionType = List.class;
                    }
                } else {
                    // We can assign e.g. a Set to a List, so let's use the common supertype
                    expressionType = Collection.class;
                }
            }
        }

        if (isSubview()) {
            if (isCollection()) {
                ManagedViewType<?> subviewType = managedViews.get(elementType);

                if (subviewType == null) {
                    throw new IllegalStateException("Expected subview '" + elementType.getName() + "' to exist but couldn't find it!");
                }
                elementType = subviewType.getEntityClass();
            } else {
                ManagedViewType<?> subviewType = managedViews.get(expressionType);

                if (subviewType == null) {
                    throw new IllegalStateException("Expected subview '" + expressionType.getName() + "' to exist but couldn't find it!");
                }
                expressionType = subviewType.getEntityClass();
            }
        }

        if (isCorrelated()) {
            if (isUpdatable()) {
                context.addError("Illegal updatable correlated attribute " + getLocation());
            }
            // Validate that resolving "correlationBasis" on "managedType" is valid
            validateTypesCompatible(managedType, stripThisFromMapping(correlationBasis), Object.class, null, true, context, ExpressionLocation.CORRELATION_BASIS, getLocation());

            if (correlated != null) {
                // Validate that resolving "correlationResult" on "correlated" is compatible with "expressionType" and "elementType"
                validateTypesCompatible(context.getEntityMetamodel().managedType(correlated), stripThisFromMapping(correlationResult), expressionType, elementType, true, context, ExpressionLocation.CORRELATION_RESULT, getLocation());

                // TODO: Validate the "correlationExpression" when https://github.com/Blazebit/blaze-persistence/issues/212 is implemented
                try {
                    // Validate the expression parses
                    context.createMacroAwareExpressionFactory().createBooleanExpression(correlationExpression, false);
                } catch (SyntaxErrorException ex) {
                    context.addError("Syntax error in " + ExpressionLocation.CORRELATION_EXPRESSION + " '" + correlationExpression + "' of the " + getLocation() + ": " + ex.getMessage());
                } catch (IllegalArgumentException ex) {
                    context.addError("An error occurred while trying to resolve the " + ExpressionLocation.CORRELATION_EXPRESSION + " of the " + getLocation() + ": " + ex.getMessage());
                }
            }
        } else if (isSubquery() || isQueryParameter()) {
            // Subqueries and parameters can't be checked
        } else {
            boolean subtypesAllowed = !isUpdatable();

            // Forcing singular via @MappingSingular
            if (!isCollection() && Collection.class.isAssignableFrom(expressionType)) {
                Class<?>[] typeArguments = getTypeArguments();
                elementType = typeArguments[typeArguments.length - 1];
            }

            String mapping = stripThisFromMapping(this.mapping);
            // Validate that resolving "mapping" on "managedType" is compatible with "expressionType" and "elementType"
            validateTypesCompatible(managedType, mapping, expressionType, elementType, subtypesAllowed, context, ExpressionLocation.MAPPING, getLocation());

            if (isUpdatable()) {
                UpdatableExpressionVisitor visitor = new UpdatableExpressionVisitor(managedType.getJavaType());
                try {
                    // NOTE: Not supporting "this" here because it doesn't make sense to have an updatable mapping that refers to this
                    // The only thing that might be interesting is supporting "this" when we support cascading as properties could be nested
                    // But not sure yet if the embeddable attributes would then be modeled as "updatable".
                    // I guess these attributes are not "updatable" but that probably depends on the decision regarding collections as they have a similar problem
                    // A collection itself might not be "updatable" but it's elements could be. This is roughly the same problem
                    context.getExpressionFactory().createPathExpression(mapping).accept(visitor);
                    Map<Method, Class<?>[]> possibleTargets = visitor.getPossibleTargets();

                    if (possibleTargets.size() > 1) {
                        context.addError("Multiple possible target type for the mapping in the " + getLocation() + ": " + possibleTargets);
                    }
                } catch (SyntaxErrorException ex) {
                    context.addError("Syntax error in mapping expression '" + mapping + "' of the " + getLocation() + ": " + ex.getMessage());
                } catch (IllegalArgumentException ex) {
                    context.addError("There is an error for the " + getLocation() + ": " + ex.getMessage());
                }
            }
        }
    }

    protected abstract Class[] getTypeArguments();
    
    public boolean isUpdatable() {
        return false;
    }

    public boolean isIndexed() {
        return false;
    }

    protected abstract String getLocation();

    public Class<?> getElementType() {
        return getJavaType();
    }

    public PluralAttribute.CollectionType getCollectionType() {
        throw new UnsupportedOperationException("This method should be overridden or not be publicly exposed.");
    }

    public boolean isQueryParameter() {
        return queryParameter;
    }

    public boolean isId() {
        return id;
    }

    public Class<? extends SubqueryProvider> getSubqueryProvider() {
        return subqueryProvider;
    }

    public String getSubqueryExpression() {
        return subqueryExpression;
    }

    public String getSubqueryAlias() {
        return subqueryAlias;
    }

    public Class<? extends CorrelationProvider> getCorrelationProvider() {
        return correlationProvider;
    }

    public String getCorrelationBasis() {
        return correlationBasis;
    }

    public String getCorrelationResult() {
        return correlationResult;
    }

    public FetchStrategy getFetchStrategy() {
        return fetchStrategy;
    }

    public int getBatchSize() {
        return batchSize;
    }

    @Override
    public boolean isSubquery() {
        return false;
    }

    @Override
    public boolean isSubview() {
        return subview;
    }

    @Override
    public ManagedViewType<X> getDeclaringType() {
        return declaringType;
    }

    @Override
    public Class<Y> getJavaType() {
        return javaType;
    }

    public String getMapping() {
        return mapping;
    }

    @Override
    public String[] getFetches() {
        return fetches;
    }
}
