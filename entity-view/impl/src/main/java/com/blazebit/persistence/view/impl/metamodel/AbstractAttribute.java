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
package com.blazebit.persistence.view.impl.metamodel;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;

import com.blazebit.persistence.impl.expression.ExpressionFactory;
import com.blazebit.persistence.impl.expression.SyntaxErrorException;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.MappingParameter;
import com.blazebit.persistence.view.MappingSubquery;
import com.blazebit.persistence.view.SubqueryProvider;
import com.blazebit.persistence.view.impl.CollectionJoinMappingGathererExpressionVisitor;
import com.blazebit.persistence.view.impl.MetamodelTargetResolvingExpressionVisitor;
import com.blazebit.persistence.view.impl.UpdatableExpressionVisitor;
import com.blazebit.persistence.view.impl.MetamodelTargetResolvingExpressionVisitor.TargetType;
import com.blazebit.persistence.view.metamodel.Attribute;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.PluralAttribute;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public abstract class AbstractAttribute<X, Y> implements Attribute<X, Y> {

    protected final ManagedViewType<X> declaringType;
    protected final Class<Y> javaType;
    protected final String mapping;
    protected final Class<? extends SubqueryProvider> subqueryProvider;
    protected final String subqueryExpression;
    protected final String subqueryAlias;
    protected final boolean queryParameter;
    protected final boolean id;
    protected final boolean subqueryMapping;
    protected final boolean subview;

    public AbstractAttribute(ManagedViewType<X> declaringType, Class<Y> javaType, Annotation mapping, Set<Class<?>> entityViews, String errorLocation) {
        if (javaType == null) {
            throw new IllegalArgumentException("The attribute type is not resolvable " + errorLocation);
        }
        
        this.declaringType = declaringType;
        this.javaType = javaType;
        this.subview = entityViews.contains(javaType);

        if (mapping instanceof IdMapping) {
            this.mapping = ((IdMapping) mapping).value();
            this.subqueryProvider = null;
            this.id = true;
            this.queryParameter = false;
            this.subqueryMapping = false;
            this.subqueryExpression = null;
            this.subqueryAlias = null;
        } else if (mapping instanceof Mapping) {
            this.mapping = ((Mapping) mapping).value();
            this.subqueryProvider = null;
            this.id = false;
            this.queryParameter = false;
            this.subqueryMapping = false;
            this.subqueryExpression = null;
            this.subqueryAlias = null;
        } else if (mapping instanceof MappingParameter) {
            this.mapping = ((MappingParameter) mapping).value();
            this.subqueryProvider = null;
            this.id = false;
            this.queryParameter = true;
            this.subqueryMapping = false;
            this.subqueryExpression = null;
            this.subqueryAlias = null;
        } else if (mapping instanceof MappingSubquery) {
            MappingSubquery mappingSubquery = (MappingSubquery) mapping;
            this.mapping = null;
            this.subqueryProvider = mappingSubquery.value();
            this.id = false;
            this.queryParameter = false;
            this.subqueryMapping = true;
            this.subqueryExpression = mappingSubquery.expression();
            this.subqueryAlias = mappingSubquery.subqueryAlias();

            if (!subqueryExpression.isEmpty() && subqueryAlias.isEmpty()) {
                throw new IllegalArgumentException("The subquery alias is empty although the subquery expression is not " + errorLocation);
            }
        } else {
            throw new IllegalArgumentException("No mapping annotation could be found " + errorLocation);
        }
    }
    
    public Set<String> getCollectionJoinMappings(ManagedType<?> managedType, Metamodel metamodel, ExpressionFactory expressionFactory) {
        if (mapping == null || queryParameter) {
            // Subqueries and parameters can't be checked
            return Collections.emptySet();
        }
        
    	CollectionJoinMappingGathererExpressionVisitor visitor = new CollectionJoinMappingGathererExpressionVisitor(managedType, metamodel);
        expressionFactory.createSimpleExpression(mapping, false).accept(visitor);
        Set<String> mappings = new HashSet<String>();
        
        for (String s : visitor.getPaths()) {
        	mappings.add(s);
        }
        
        return mappings;
    }

    public String checkAttribute(ManagedType<?> managedType, Map<Class<?>, ManagedViewType<?>> managedViews, ExpressionFactory expressionFactory, Metamodel metamodel) {
        if (mapping == null || queryParameter) {
            // Subqueries and parameters can't be checked
            return null;
        }
        
        if (isUpdatable()) {
            UpdatableExpressionVisitor visitor = new UpdatableExpressionVisitor(managedType.getJavaType());
            try {
                expressionFactory.createPathExpression(mapping).accept(visitor);
                Map<Method, Class<?>[]> possibleTargets = visitor.getPossibleTargets();
                
                if (possibleTargets.size() > 1) {
                    return "Multiple possible target type for the mapping in the " + getLocation() + ": " + possibleTargets;
                } else {
                    // TODO: further type checks like
                    // * collection type is same
                    // * collection value type is compatible
                }
            } catch (SyntaxErrorException ex) {
                return "Syntax error in mapping expression '" + mapping + "' of the " + getLocation() + ": " + ex.getMessage();
            } catch (IllegalArgumentException ex) {
                return "There is an error for the " + getLocation() + ": " + ex.getMessage();
            }
        }
        
        Class<?> expressionType = getJavaType();
        Class<?> elementType = null;
        
        if (isCollection()) {
            elementType = getElementType();
        }
        
        // TODO: check if collection value types are compatible => subview is view for entity class, or class is super type of entity class
        
        // Updatable collection attributes must have the same collection type
        if (!isUpdatable() && isCollection() && !isIndexed() && Collection.class.isAssignableFrom(expressionType)) {
            // We can assign e.g. a Set to a List, so let's use the common supertype
            expressionType = Collection.class;
        } else if (!isCollection() && isSubview()) {
            ManagedViewType<?> subviewType = managedViews.get(expressionType);
            
            if (subviewType == null) {
                throw new IllegalStateException("Expected subview '" + expressionType.getName() + "' to exist but couldn't find it!");
            }
            
            expressionType = subviewType.getEntityClass();
        }

        MetamodelTargetResolvingExpressionVisitor visitor = new MetamodelTargetResolvingExpressionVisitor(managedType, metamodel);
        
        try {
            expressionFactory.createSimpleExpression(mapping, false).accept(visitor);
        } catch (SyntaxErrorException ex) {
            return "Syntax error in mapping expression '" + mapping + "' of the " + getLocation() + ": " + ex.getMessage();
        } catch (IllegalArgumentException ex) {
            return "An error occurred while trying to resolve " + getLocation() + ": " + ex.getMessage();
        }
        
        List<TargetType> possibleTargets = visitor.getPossibleTargets();
        
        if (!possibleTargets.isEmpty()) {
            boolean error = true;
            for (TargetType t : possibleTargets) {
                Class<?> possibleTargetType = t.getLeafBaseClass();
                
                // Null is the marker for ANY TYPE
                if (possibleTargetType == null || expressionType.isAssignableFrom(possibleTargetType)
                    || Map.class.isAssignableFrom(possibleTargetType) && expressionType.isAssignableFrom(t.getLeafBaseValueClass())) {
                    error = false;
                    break;
                } else if (t.hasCollectionJoin() && elementType != null && elementType.isAssignableFrom(t.getLeafBaseValueClass())) {
                	error = false;
                    break;
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
                return "The resolved possible types " + sb.toString() + " are not assignable to the given expression type '" + getJavaType().getName() + "' of the expression declared by the " + getLocation() + "!";
            }
        }
        
        return null;
    }
    
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

    @Override
    public boolean isSubquery() {
        return subqueryMapping;
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
}
