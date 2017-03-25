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

import java.util.regex.Pattern;

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
import com.blazebit.persistence.view.impl.ScalarTargetResolvingExpressionVisitor.TargetType;
import com.blazebit.persistence.view.impl.UpdatableExpressionVisitor;
import com.blazebit.persistence.view.metamodel.Attribute;
import com.blazebit.persistence.view.metamodel.PluralAttribute;
import com.blazebit.persistence.view.metamodel.Type;
import com.blazebit.reflection.ReflectionUtils;

import javax.persistence.metamodel.ManagedType;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public abstract class AbstractAttribute<X, Y> implements Attribute<X, Y> {

    private static final String[] EMPTY = new String[0];
    private static final String THIS = "this";
    private static final Pattern PREFIX_THIS_REPLACE_PATTERN = Pattern.compile("([^a-zA-Z0-9\\.])this\\.");

    protected final ManagedViewTypeImpl<X> declaringType;
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
    protected final MappingType mappingType;
    protected final boolean id;

    @SuppressWarnings("unchecked")
    public AbstractAttribute(ManagedViewTypeImpl<X> declaringType, AttributeMapping mapping, MetamodelBuildingContext context) {
        if (mapping.getJavaType() == null) {
            context.addError("The attribute type is not resolvable " + mapping.getErrorLocation());
        }

        BatchFetch batchFetch = mapping.getBatchFetch();
        int batchSize;
        if (batchFetch == null || batchFetch.size() == -1) {
            batchSize = -1;
        } else if (batchFetch.size() < 1) {
            context.addError("Illegal batch fetch size lower than 1 defined at '" + mapping.getErrorLocation() + "'!");
            batchSize = Integer.MIN_VALUE;
        } else {
            batchSize = batchFetch.size();
        }

        this.declaringType = declaringType;
        this.javaType = (Class<Y>) mapping.getJavaType();
        Annotation mappingAnnotation = mapping.getMapping();

        if (mappingAnnotation instanceof IdMapping) {
            this.mapping = ((IdMapping) mappingAnnotation).value();
            this.fetches = EMPTY;
            this.fetchStrategy = FetchStrategy.JOIN;
            this.batchSize = -1;
            this.subqueryProvider = null;
            this.id = true;
            this.mappingType = MappingType.BASIC;
            this.subqueryExpression = null;
            this.subqueryAlias = null;
            this.correlationBasis = null;
            this.correlationResult = null;
            this.correlationProvider = null;
            this.correlated = null;
            this.correlationKeyAlias = null;
            this.correlationExpression = null;
        } else if (mappingAnnotation instanceof Mapping) {
            Mapping m = (Mapping) mappingAnnotation;
            this.mapping = m.value();
            this.fetches = m.fetches();
            this.fetchStrategy = m.fetch();
            this.batchSize = batchSize;
            this.subqueryProvider = null;
            this.id = false;
            this.mappingType = MappingType.BASIC;
            this.subqueryExpression = null;
            this.subqueryAlias = null;
            this.correlationBasis = null;
            this.correlationResult = null;
            this.correlationProvider = null;
            this.correlated = null;
            this.correlationKeyAlias = null;
            this.correlationExpression = null;
        } else if (mappingAnnotation instanceof MappingParameter) {
            this.mapping = ((MappingParameter) mappingAnnotation).value();
            this.fetches = EMPTY;
            this.fetchStrategy = FetchStrategy.JOIN;
            this.batchSize = -1;
            this.subqueryProvider = null;
            this.id = false;
            this.mappingType = MappingType.PARAMETER;
            this.subqueryExpression = null;
            this.subqueryAlias = null;
            this.correlationBasis = null;
            this.correlationResult = null;
            this.correlationProvider = null;
            this.correlated = null;
            this.correlationKeyAlias = null;
            this.correlationExpression = null;
        } else if (mappingAnnotation instanceof MappingSubquery) {
            MappingSubquery mappingSubquery = (MappingSubquery) mappingAnnotation;
            this.mapping = null;
            this.fetches = EMPTY;
            this.subqueryProvider = mappingSubquery.value();
            this.fetchStrategy = FetchStrategy.JOIN;
            this.batchSize = -1;
            this.id = false;
            this.mappingType = MappingType.SUBQUERY;
            this.subqueryExpression = mappingSubquery.expression();
            this.subqueryAlias = mappingSubquery.subqueryAlias();
            this.correlationBasis = null;
            this.correlationResult = null;
            this.correlationProvider = null;
            this.correlated = null;
            this.correlationKeyAlias = null;
            this.correlationExpression = null;

            if (!subqueryExpression.isEmpty() && subqueryAlias.isEmpty()) {
                context.addError("The subquery alias is empty although the subquery expression is not " + mapping.getErrorLocation());
            }
            if (subqueryProvider.getEnclosingClass() != null && !Modifier.isStatic(subqueryProvider.getModifiers())) {
                context.addError("The subquery provider is defined as non-static inner class. Make it static, otherwise it can't be instantiated: " + mapping.getErrorLocation());
            }
        } else if (mappingAnnotation instanceof MappingCorrelated) {
            MappingCorrelated mappingCorrelated = (MappingCorrelated) mappingAnnotation;
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
            this.mappingType = MappingType.CORRELATED;
            this.subqueryExpression = null;
            this.subqueryAlias = null;
            this.correlationBasis = mappingCorrelated.correlationBasis();
            this.correlationResult = mappingCorrelated.correlationResult();
            this.correlationProvider = mappingCorrelated.correlator();
            this.correlated = null;
            this.correlationKeyAlias = null;
            this.correlationExpression = null;

            if (correlationProvider.getEnclosingClass() != null && !Modifier.isStatic(correlationProvider.getModifiers())) {
                context.addError("The correlation provider is defined as non-static inner class. Make it static, otherwise it can't be instantiated: " + mapping.getErrorLocation());
            }
        } else if (mappingAnnotation instanceof MappingCorrelatedSimple) {
            MappingCorrelatedSimple mappingCorrelated = (MappingCorrelatedSimple) mappingAnnotation;
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
            this.mappingType = MappingType.CORRELATED;
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
            context.addError("No mapping annotation could be found " + mapping.getErrorLocation());
            this.mapping = null;
            this.fetches = EMPTY;
            this.fetchStrategy = null;
            this.batchSize = Integer.MIN_VALUE;
            this.subqueryProvider = null;
            this.id = false;
            this.mappingType = null;
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
        if (mapping == null || isQueryParameter()) {
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

    public boolean hasJoinFetchedCollections() {
        return isCollection() && getFetchStrategy() == FetchStrategy.JOIN
                || getElementType() instanceof ManagedViewTypeImpl<?> && ((ManagedViewTypeImpl) getElementType()).hasJoinFetchedCollections();
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
                    // Either possibleTargetType is a subtype of target type, or it is a subtype of map and the target type a subtype of Collection
                    // This allows mapping Map<?, Entity> to List<Subview>
                    // Anyway the possibleTargetElementType must be a subtype of the targetElementType
                    return (targetType.isAssignableFrom(possibleTargetType) || Map.class.isAssignableFrom(possibleTargetType) && Collection.class.isAssignableFrom(targetType))
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
            context.addError(typeCompatibilityError(
                    Arrays.<TargetType>asList(new ScalarTargetResolvingExpressionVisitor.TargetTypeImpl(
                            false, null, managedType.getJavaType(), null
                    )),
                    expressionType,
                    targetElementType,
                    expressionLocation,
                    location
            ));
            return;
        }

        ScalarTargetResolvingExpressionVisitor visitor = new ScalarTargetResolvingExpressionVisitor(managedType, context.getEntityMetamodel(), context.getJpqlFunctions());

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
                context.addError(typeCompatibilityError(possibleTargets, expressionType, targetElementType, expressionLocation, location));
            }
        }
    }

    private static String typeCompatibilityError(List<TargetType> possibleTargets, Class<?> targetType, Class<?> targetElementType, ExpressionLocation expressionLocation, String location) {
        StringBuilder sb = new StringBuilder();
        sb.append("The resolved possible types ");
        sb.append('[');
        for (TargetType t : possibleTargets) {
            sb.append(t.getLeafBaseClass().getName());
            if (t.getLeafBaseValueClass() != null && t.getLeafBaseClass() != t.getLeafBaseValueClass()) {
                sb.append('<');
                sb.append(t.getLeafBaseValueClass().getName());
                sb.append('>');
            }
            sb.append(", ");
        }

        sb.setLength(sb.length() - 2);
        sb.append(']');
        sb.append(" are not assignable to the given expression type '");
        sb.append(targetType.getName());
        if (targetElementType != null && targetElementType != targetType) {
            sb.append('<');
            sb.append(targetElementType.getName());
            sb.append('>');
        }
        sb.append("' of the ");
        sb.append(expressionLocation);
        sb.append(" declared by the ");
        sb.append(location);
        sb.append("!");
        return sb.toString();
    }

    public void checkAttribute(ManagedType<?> managedType, MetamodelBuildingContext context) {
        Class<?> expressionType = getJavaType();
        Class<?> elementType = null;

        if (fetches.length != 0) {
            ManagedType<?> entityType = context.getEntityMetamodel().getManagedType(getElementType().getJavaType());
            if (entityType == null) {
                context.addError("Specifying fetches for non-entity attribute type [" + Arrays.toString(fetches) + "] at the " + getLocation() + " is not allowed!");
            } else {
                ScalarTargetResolvingExpressionVisitor visitor = new ScalarTargetResolvingExpressionVisitor(entityType, context.getEntityMetamodel(), context.getJpqlFunctions());
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
            elementType = getElementType().getJavaType();

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
            ManagedViewTypeImpl<?> subviewType = (ManagedViewTypeImpl<?>) getElementType();

            if (isCollection()) {
                elementType = subviewType.getEntityClass();
            } else {
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

    public void checkNestedAttribute(List<AbstractAttribute<?, ?>> parents, ManagedType<?> managedType, MetamodelBuildingContext context) {
        if (!parents.isEmpty()) {
            if (getDeclaringType().getMappingType() == Type.MappingType.FLAT_VIEW) {
                // When this attribute is part of a flat view
                if (isCollection() && getFetchStrategy() == FetchStrategy.JOIN) {
                    // And is a join fetched collection

                    // Traverse up and check if it has at least one non-embedded parent
                    for (int i = parents.size() - 1; i >= 0; i--) {
                        AbstractAttribute<?, ?> parentAttribute = parents.get(i);
                        if (parentAttribute.getDeclaringType().getMappingType() == Type.MappingType.FLAT_VIEW && !parentAttribute.isEmbedded()) {
                            String path = parentAttribute.getDeclaringType().getJavaType().getName();
                            for (i = i + 1; i < parents.size(); i++) {
                                path += " > " + parents.get(i).getLocation();
                            }
                            context.addError("Illegal mapping of join fetched collection in the " + getLocation() + ". The flat view '" + getJavaType().getName() + "' was via the path: " + path);
                            break;
                        }
                    }
                }
            }
        }

        // Go into subtypes for nested checking
        if (isSubview()) {
            ManagedViewTypeImpl<?> subviewType = (ManagedViewTypeImpl<?>) getElementType();
            parents.add(this);
            subviewType.checkNestedAttributes(parents, context);
            parents.remove(parents.size() - 1);
        }
    }

    protected boolean isEmbedded() {
        return getDeclaringType().getMappingType() == Type.MappingType.FLAT_VIEW && "this".equals(mapping);
    }

    protected abstract Class[] getTypeArguments();

    protected abstract String getLocation();

    protected abstract boolean isUpdatable();

    protected abstract boolean isIndexed();

    protected abstract PluralAttribute.CollectionType getCollectionType();

    protected abstract Type<?> getElementType();

    @Override
    public final MappingType getMappingType() {
        return mappingType;
    }

    public final boolean isQueryParameter() {
        return mappingType == MappingType.PARAMETER;
    }

    public final boolean isId() {
        return id;
    }

    public final Class<? extends SubqueryProvider> getSubqueryProvider() {
        return subqueryProvider;
    }

    public final String getSubqueryExpression() {
        return subqueryExpression;
    }

    public final String getSubqueryAlias() {
        return subqueryAlias;
    }

    public final Class<? extends CorrelationProvider> getCorrelationProvider() {
        return correlationProvider;
    }

    public final String getCorrelationBasis() {
        return correlationBasis;
    }

    public final String getCorrelationResult() {
        return correlationResult;
    }

    public final FetchStrategy getFetchStrategy() {
        return fetchStrategy;
    }

    public final int getBatchSize() {
        return batchSize;
    }

    public final String getMapping() {
        return mapping;
    }

    @Override
    public final boolean isSubquery() {
        return mappingType == MappingType.SUBQUERY;
    }

    @Override
    public final ManagedViewTypeImpl<X> getDeclaringType() {
        return declaringType;
    }

    @Override
    public final Class<Y> getJavaType() {
        return javaType;
    }

    @Override
    public final String[] getFetches() {
        return fetches;
    }
}
