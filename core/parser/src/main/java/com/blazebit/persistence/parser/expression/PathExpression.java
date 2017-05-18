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
    private boolean collectionKeyPath;

    public PathExpression() {
        this(new ArrayList<PathElementExpression>(), false);
    }

    public PathExpression(List<PathElementExpression> pathProperties) {
        this(pathProperties, false);
    }

    public PathExpression(List<PathElementExpression> pathProperties, boolean isCollectionKeyPath) {
        this.pathProperties = pathProperties;
        this.collectionKeyPath = isCollectionKeyPath;
    }

    public PathExpression(List<PathElementExpression> pathProperties, PathReference pathReference, boolean usedInCollectionFunction, boolean collectionKeyPath) {
        this.pathProperties = pathProperties;
        this.pathReference = pathReference;
        this.usedInCollectionFunction = usedInCollectionFunction;
        this.collectionKeyPath = collectionKeyPath;
    }

    @Override
    public PathExpression clone(boolean resolved) {
        if (resolved && pathReference != null) {
            return (PathExpression) pathReference.getBaseNode().createExpression(pathReference.getField());
        }

        int size = pathProperties.size();
        List<PathElementExpression> newPathProperties = new ArrayList<PathElementExpression>(size);

        for (int i = 0; i < size; i++) {
            newPathProperties.add(pathProperties.get(i).clone(resolved));
        }

        // NOTE: don't copy the path reference, it has to be set manually on the copy
        return new PathExpression(newPathProperties, null, usedInCollectionFunction, collectionKeyPath);
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

    public boolean isCollectionKeyPath() {
        return collectionKeyPath;
    }

    public void setCollectionKeyPath(boolean collectionKeyPath) {
        this.collectionKeyPath = collectionKeyPath;
    }

    /*
     * The following equals and hashCode implementation makes it possible that expressions which have different path properties but
     * reference the same object, are equal.
     */
    @Override
    public int hashCode() {
        int hash = 3;
        if (this.pathReference != null) {
            hash = 31 * hash + (this.pathReference != null ? this.pathReference.hashCode() : 0);
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
