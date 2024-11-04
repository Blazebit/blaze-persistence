/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser.expression;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public class PathExpression extends AbstractExpression implements Expression {

    private List<PathElementExpression> pathProperties;
    private PathReference pathReference;
    private boolean usedInCollectionFunction = false;
    private boolean collectionQualifiedPath;

    public PathExpression() {
        this(new ArrayList<PathElementExpression>(), false);
    }

    public PathExpression(PathElementExpression pathElementExpression) {
        this(list(pathElementExpression), false);
    }

    public PathExpression(List<PathElementExpression> pathProperties) {
        this(pathProperties, false);
    }

    public PathExpression(List<PathElementExpression> pathProperties, boolean isCollectionKeyPath) {
        this.pathProperties = pathProperties;
        this.collectionQualifiedPath = isCollectionKeyPath;
    }

    public PathExpression(List<PathElementExpression> pathProperties, PathReference pathReference, boolean usedInCollectionFunction, boolean collectionQualifiedPath) {
        this.pathProperties = pathProperties;
        this.pathReference = pathReference;
        this.usedInCollectionFunction = usedInCollectionFunction;
        this.collectionQualifiedPath = collectionQualifiedPath;
    }

    private static List<PathElementExpression> list(PathElementExpression elementExpression) {
        List<PathElementExpression> list = new ArrayList<>(1);
        list.add(elementExpression);
        return list;
    }

    @Override
    public Expression copy(ExpressionCopyContext copyContext) {
        if (pathProperties.size() == 1 && pathProperties.get(0) instanceof PropertyExpression) {
            Expression expression = copyContext.getExpressionForAlias(pathProperties.get(0).toString());
            if (expression != null) {
                return expression;
            }
        }
        int size = pathProperties.size();
        List<PathElementExpression> newPathProperties = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            newPathProperties.add(pathProperties.get(i).copy(copyContext));
        }
        return new PathExpression(newPathProperties, copyContext.isCopyResolved() ? pathReference : null, usedInCollectionFunction, collectionQualifiedPath);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T accept(ResultVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public List<PathElementExpression> getExpressions() {
        return pathProperties;
    }

    public void setExpressions(List<PathElementExpression> expressions) {
        this.pathProperties = expressions;
    }

    public PathReference getPathReference() {
        return pathReference;
    }
    
    public void setPathReference(PathReference pathReference) {
        this.pathReference = pathReference;
    }
    
    public BaseNode getBaseNode() {
        if (pathReference == null) {
            return null;
        }
        
        return pathReference.getBaseNode();
    }
    
    public String getField() {
        if (pathReference == null) {
            return null;
        }
        
        return pathReference.getField();
    }

    public boolean isUsedInCollectionFunction() {
        return usedInCollectionFunction;
    }

    public void setUsedInCollectionFunction(boolean collectionValued) {
        this.usedInCollectionFunction = collectionValued;
    }

    public String getPath() {
        return toString();
    }

    public boolean isCollectionQualifiedPath() {
        return collectionQualifiedPath;
    }

    public void setCollectionQualifiedPath(boolean collectionQualifiedPath) {
        this.collectionQualifiedPath = collectionQualifiedPath;
    }

    public PathExpression withoutFirst() {
        int size = pathProperties.size();
        if (size == 0) {
            return this;
        }
        List<PathElementExpression> list = new ArrayList<>(size - 1);
        for (int i = 1; i < size; i++) {
            list.add(pathProperties.get(i));
        }
        return new PathExpression(list);
    }

    /*
     * The following equals and hashCode implementation makes it possible that expressions which have different path properties but
     * reference the same object, are equal.
     */
    @Override
    public int hashCode() {
        int hash = 3;
        if (this.pathReference != null) {
            hash = 31 * hash + this.pathReference.hashCode();
        } else {
            hash = 31 * hash + (this.pathProperties != null ? this.pathProperties.hashCode() : 0);
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PathExpression other = (PathExpression) obj;
        if (this.pathReference != null || other.pathReference != null) {
            if (this.pathReference == null) {
                // First try to match with path properties
                if (this.pathProperties == other.pathProperties || this.pathProperties != null && this.pathProperties.equals(other.pathProperties)) {
                    return true;
                }
                // If that doesn't work out, try to match paths
                return this.getPath().equals(other.getPath());
            } else if (other.pathReference == null) {
                // First try to match with path properties
                if (other.pathProperties == this.pathProperties || other.pathProperties != null && other.pathProperties.equals(this.pathProperties)) {
                    return true;
                }

                // If that doesn't work out, try to match paths
                return this.getPath().equals(other.getPath());
            } else {
                if (this.pathReference == other.pathReference) {
                    return true;
                }
                return this.pathReference.equals(other.pathReference);
            }
        } else {
            if (this.pathProperties != other.pathProperties && (this.pathProperties == null || !this.pathProperties.equals(other.pathProperties))) {
                return false;
            }
        }
        return true;
    }
}
