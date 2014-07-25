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

import com.blazebit.persistence.impl.JoinNode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Moritz Becker
 */
public class PathExpression implements Expression {

    private final List<PathElementExpression> pathProperties;
    private JoinNode baseNode;
    private String field;
    private boolean collectionValued = false;

    public PathExpression() {
        this.pathProperties = new ArrayList<PathElementExpression>();
    }

    public PathExpression(List<PathElementExpression> pathProperties) {
        this.pathProperties = pathProperties;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public List<PathElementExpression> getExpressions() {
        return pathProperties;
    }

    public JoinNode getBaseNode() {
        return baseNode;
    }

    public void setBaseNode(JoinNode baseNode) {
        this.baseNode = baseNode;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public boolean isCollectionValued() {
        return collectionValued;
    }

    public void setCollectionValued(boolean collectionValued) {
        this.collectionValued = collectionValued;
    }

    public String getPath() {
        StringBuilder sb = new StringBuilder();
        Iterator<PathElementExpression> iter = pathProperties.iterator();

        if (iter.hasNext()) {
            sb.append(iter.next());
        }

        while (iter.hasNext()) {
            sb.append(".")
                .append(iter.next());
        }

        return sb.toString();
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 73 * hash + (this.pathProperties != null ? this.pathProperties.hashCode() : 0);
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
        if (this.pathProperties != other.pathProperties && (this.pathProperties == null || !this.pathProperties.equals(
            other.pathProperties))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        Iterator<PathElementExpression> iter = pathProperties.iterator();
        StringBuilder sb = new StringBuilder();
        if (iter.hasNext()) {
            sb.append(iter.next());
        }

        while (iter.hasNext()) {
            sb.append('.')
                .append(iter.next());
        }
        return sb.toString();
    }

}
