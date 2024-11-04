/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl;


import com.blazebit.persistence.From;
import com.blazebit.persistence.Path;
import com.blazebit.persistence.parser.expression.BaseNode;
import com.blazebit.persistence.parser.expression.PathReference;

import javax.persistence.metamodel.Type;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class SimplePathReference implements PathReference, Path {

    private final JoinNode baseNode;
    private final String field;
    private final Type<?> type;

    public SimplePathReference(JoinNode baseNode, String field, Type<?> type) {
        this.baseNode = baseNode;
        this.field = field;
        this.type = type;
    }

    @Override
    public BaseNode getBaseNode() {
        return baseNode;
    }

    @Override
    public String getField() {
        return field;
    }

    @Override
    public Type<?> getType() {
        return type;
    }

    @Override
    public From getFrom() {
        return baseNode;
    }

    @Override
    public String getPath() {
        StringBuilder sb = new StringBuilder();
        baseNode.appendDeReference(sb, field, true, false, false);
        return sb.toString();
    }

    @Override
    public Class<?> getJavaType() {
        return type.getJavaType();
    }

    @Override
    public String toString() {
        return getPath();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((baseNode == null) ? 0 : baseNode.hashCode());
        result = prime * result + ((field == null) ? 0 : field.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof PathReference)) {
            return false;
        }
        PathReference other = (PathReference) obj;
        if (baseNode == null) {
            if (other.getBaseNode() != null) {
                return false;
            }
        } else if (!baseNode.equals(other.getBaseNode())) {
            return false;
        }
        if (field == null) {
            if (other.getField() != null) {
                return false;
            }
        } else if (!field.equals(other.getField())) {
            return false;
        }
        return true;
    }
    
}
