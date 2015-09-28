package com.blazebit.persistence.impl;

import java.util.List;

class CTEInfo {
	final String name;
	final List<String> attributes;
	final boolean recursive;
	final AbstractCommonQueryBuilder<?, ?> nonRecursiveCriteriaBuilder;
	final AbstractCommonQueryBuilder<?, ?> recursiveCriteriaBuilder;
	
	CTEInfo(String name, List<String> attributes, boolean recursive, AbstractCommonQueryBuilder<?, ?> nonRecursiveCriteriaBuilder, AbstractCommonQueryBuilder<?, ?> recursiveCriteriaBuilder) {
		this.name = name;
		this.attributes = attributes;
		this.recursive = recursive;
		this.nonRecursiveCriteriaBuilder = nonRecursiveCriteriaBuilder;
		this.recursiveCriteriaBuilder = recursiveCriteriaBuilder;
	}
}