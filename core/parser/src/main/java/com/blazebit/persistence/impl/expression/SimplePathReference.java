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

package com.blazebit.persistence.impl.expression;


public class SimplePathReference implements PathReference {

    private final BaseNode baseNode;
    private final String field;
    private final String typeName;

    public SimplePathReference(BaseNode baseNode, String field, String typeName) {
        this.baseNode = baseNode;
        this.field = field;
        this.typeName = typeName;
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
    public String getTreatTypeName() {
        return typeName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((baseNode == null) ? 0 : baseNode.hashCode());
        result = prime * result + ((field == null) ? 0 : field.hashCode());
        result = prime * result + ((typeName == null) ? 0 : typeName.hashCode());
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
        if (typeName == null) {
            if (other.getTreatTypeName() != null) {
                return false;
            }
        } else if (!typeName.equals(other.getTreatTypeName())) {
            return false;
        }
        return true;
    }
    
}
