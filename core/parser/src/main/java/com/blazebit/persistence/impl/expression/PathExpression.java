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
package com.blazebit.persistence.impl.expression;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
public class PathExpression extends AbstractExpression implements Expression {

    private final List<PathElementExpression> pathProperties;
    // Although this node will always be a JoinNode we will use casting at use site to be able to reuse the parser
    private Object baseNode;
    private String field;
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

    public PathExpression(List<PathElementExpression> pathProperties, Object baseNode, String field, boolean usedInCollectionFunction, boolean collectionKeyPath) {
        this.pathProperties = pathProperties;
        this.baseNode = baseNode;
        this.field = field;
        this.usedInCollectionFunction = usedInCollectionFunction;
        this.collectionKeyPath = collectionKeyPath;
    }

    @Override
    public Expression clone() {
        int size = pathProperties.size();
        List<PathElementExpression> newPathProperties = new ArrayList<PathElementExpression>(size);
        
        for (int i = 0; i < size; i++) {
            newPathProperties.add(pathProperties.get(i).clone());
        }
        
        return new PathExpression(newPathProperties, baseNode, field, usedInCollectionFunction, collectionKeyPath);
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

    public Object getBaseNode() {
        return baseNode;
    }

    public void setBaseNode(Object baseNode) {
        this.baseNode = baseNode;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
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
     * The following equals and hashCode implementation makes it possible that expressions which have different path properties but reference the same object, are equal.
     */
    @Override
    public int hashCode() {
        int hash = 3;
        if (this.baseNode != null || this.field != null) {
            hash = 31 * hash + (this.baseNode != null ? this.baseNode.hashCode() : 0);
            hash = 31 * hash + (this.field != null ? this.field.hashCode() : 0);
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
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PathExpression other = (PathExpression) obj;
        if (this.baseNode != null || this.field != null || other.baseNode != null || other.field != null) {
            if (this.baseNode != other.baseNode && (this.baseNode == null || !this.baseNode.equals(other.baseNode))) {
                return false;
            }
            if ((this.field == null) ? (other.field != null) : !this.field.equals(other.field)) {
                return false;
            }
        } else {
            if (this.pathProperties != other.pathProperties && (this.pathProperties == null || !this.pathProperties.equals(other.pathProperties))) {
                return false;
            }
        }
        return true;
    }
}
