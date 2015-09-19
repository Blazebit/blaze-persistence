/*
 * Copyright 2015 Blazebit.
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
package com.blazebit.persistence.impl;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;

import com.blazebit.persistence.CTECriteriaBuilder;
import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.RecursiveCTECriteriaBuilder;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.1.0
 */
public class CTEManager<T> extends CTEBuilderListenerImpl {

	private final CriteriaBuilderFactoryImpl cbf;
	private final EntityManager em;
	private final Set<String> registeredFunctions;

    private final Set<CTEInfo> ctes;
    private boolean recursive = false;

    CTEManager(CriteriaBuilderFactoryImpl cbf, EntityManager em, Set<String> registeredFunctions) {
    	this.cbf = cbf;
    	this.em = em;
    	this.registeredFunctions = registeredFunctions;
        this.ctes = new LinkedHashSet<CTEInfo>();
    }

    void buildClause(StringBuilder sb) {
        if (ctes.isEmpty()) {
        	return;
        }

        sb.append("WITH ");
        
        if (recursive) {
        	sb.append("RECURSIVE ");
        }
        
        boolean first = true;
        for (CTEInfo cte : ctes) {
        	if (first) {
        		first = false;
        	} else {
        		sb.append(", ");
        	}
        	
        	sb.append(cte.name);
        	sb.append('(');

        	final List<String> attributes = cte.attributes; 
    		sb.append(attributes.get(0));
    		
        	for (int i = 1; i < attributes.size(); i++) {
        		sb.append(", ");
        		sb.append(attributes.get(i));
        	}

        	sb.append(')');
        	
        	sb.append(" AS(\n");
        	// TODO: need to reorder select infos first
        	sb.append(cte.criteriaBuilder.getQueryString());
        	sb.append("\n)");
        }
    }

	<X> CTECriteriaBuilder<X, T> with(Class<X> cteClass, CriteriaBuilderImpl<T> criteriaBuilderImpl) {
		CTECriteriaBuilderImpl<X, T> cteBuilder = new CTECriteriaBuilderImpl<X, T>(cbf, em, cteClass, registeredFunctions, criteriaBuilderImpl, this);
        this.onBuilderStarted(cteBuilder);
		return cteBuilder;
	}

	<X> RecursiveCTECriteriaBuilder<X, T> withRecursive(Class<X> cteClass, CriteriaBuilderImpl<T> criteriaBuilderImpl) {
		return null;
	}
	
    @Override
	public void onBuilderEnded(CTECriteriaBuilderImpl<?, ?> builder) {
		super.onBuilderEnded(builder);
		ctes.add(new CTEInfo(builder.getCteName(), builder.getAttributes(), builder));
	}

	private static class CTEInfo {
    	private final String name;
    	private final List<String> attributes;
    	private final CTECriteriaBuilderImpl<?, ?> criteriaBuilder;
    	
		public CTEInfo(String name, List<String> attributes, CTECriteriaBuilderImpl<?, ?> criteriaBuilder) {
			this.name = name;
			this.attributes = attributes;
			this.criteriaBuilder = criteriaBuilder;
		}
    }

}
