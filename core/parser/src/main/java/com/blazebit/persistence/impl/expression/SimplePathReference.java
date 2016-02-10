package com.blazebit.persistence.impl.expression;


public class SimplePathReference implements PathReference {

    private final Object baseNode;
    private final String field;
    
    public SimplePathReference(Object baseNode, String field) {
        this.baseNode = baseNode;
        this.field = field;
    }

    @Override
    public Object getBaseNode() {
        return baseNode;
    }

    @Override
    public String getField() {
        return field;
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
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof PathReference))
            return false;
        PathReference other = (PathReference) obj;
        if (baseNode == null) {
            if (other.getBaseNode() != null)
                return false;
        } else if (!baseNode.equals(other.getBaseNode()))
            return false;
        if (field == null) {
            if (other.getField() != null)
                return false;
        } else if (!field.equals(other.getField()))
            return false;
        return true;
    }
    
}
