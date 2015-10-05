package com.blazebit.persistence.impl;

import java.util.List;

import javax.persistence.Query;
import javax.persistence.metamodel.EntityType;

class CTEInfo {
	final String name;
	final EntityType<?> cteType;
	final List<String> attributes;
	final boolean recursive;
	final AbstractCommonQueryBuilder<?, ?> nonRecursiveCriteriaBuilder;
	final AbstractCommonQueryBuilder<?, ?> recursiveCriteriaBuilder;
	Query cachedNonRecursiveQuery;
	Query cachedRecursiveQuery;
	
	CTEInfo(String name, EntityType<?> cteType, List<String> attributes, boolean recursive, AbstractCommonQueryBuilder<?, ?> nonRecursiveCriteriaBuilder, AbstractCommonQueryBuilder<?, ?> recursiveCriteriaBuilder) {
		this.name = name;
		this.cteType = cteType;
		this.attributes = attributes;
		this.recursive = recursive;
		this.nonRecursiveCriteriaBuilder = nonRecursiveCriteriaBuilder;
		this.recursiveCriteriaBuilder = recursiveCriteriaBuilder;
	}
}