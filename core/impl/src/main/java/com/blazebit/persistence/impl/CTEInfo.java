package com.blazebit.persistence.impl;

import java.util.List;

import javax.persistence.metamodel.EntityType;

class CTEInfo {
	final String name;
	final EntityType<?> cteType;
	final List<String> attributes;
	final boolean recursive;
	final boolean unionAll;
	final AbstractCommonQueryBuilder<?, ?, ?, ?, ?> nonRecursiveCriteriaBuilder;
	final AbstractCommonQueryBuilder<?, ?, ?, ?, ?> recursiveCriteriaBuilder;
	
	CTEInfo(String name, EntityType<?> cteType, List<String> attributes, boolean recursive, boolean unionAll, AbstractCommonQueryBuilder<?, ?, ?, ?, ?> nonRecursiveCriteriaBuilder, AbstractCommonQueryBuilder<?, ?, ?, ?, ?> recursiveCriteriaBuilder) {
		this.name = name;
		this.cteType = cteType;
		this.attributes = attributes;
		this.recursive = recursive;
		this.unionAll = unionAll;
		this.nonRecursiveCriteriaBuilder = nonRecursiveCriteriaBuilder;
		this.recursiveCriteriaBuilder = recursiveCriteriaBuilder;
	}

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CTEInfo other = (CTEInfo) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }
}