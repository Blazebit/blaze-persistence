package com.blazebit.persistence.impl;

import java.util.List;

class CTEInfo {
	final String name;
	final List<String> attributes;
	final boolean recursive;
	final AbstractCTECriteriaBuilder<?, ?, ?> nonRecursiveCriteriaBuilder;
	final AbstractCTECriteriaBuilder<?, ?, ?> recursiveCriteriaBuilder;
	
	public CTEInfo(String name, List<String> attributes, boolean recursive, AbstractCTECriteriaBuilder<?, ?, ?> nonRecursiveCriteriaBuilder, AbstractCTECriteriaBuilder<?, ?, ?> recursiveCriteriaBuilder) {
		this.name = name;
		this.attributes = attributes;
		this.recursive = recursive;
		this.nonRecursiveCriteriaBuilder = nonRecursiveCriteriaBuilder;
		this.recursiveCriteriaBuilder = recursiveCriteriaBuilder;
	}
}