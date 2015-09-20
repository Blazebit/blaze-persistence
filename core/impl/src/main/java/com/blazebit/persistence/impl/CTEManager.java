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

import javax.persistence.EntityManager;

import com.blazebit.persistence.CTECriteriaBuilder;
import com.blazebit.persistence.RecursiveCTECriteriaBuilder;
import com.blazebit.persistence.spi.DbmsDialect;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.1.0
 */
public class CTEManager<T> extends CTEBuilderListenerImpl {

	private final CriteriaBuilderFactoryImpl cbf;
	private final EntityManager em;
	private final DbmsDialect dbmsDialect;
	private final Set<String> registeredFunctions;

    private final Set<CTEInfo> ctes;
    private boolean recursive = false;

    CTEManager(CriteriaBuilderFactoryImpl cbf, EntityManager em, DbmsDialect dbmsDialect, Set<String> registeredFunctions) {
    	this.cbf = cbf;
    	this.em = em;
    	this.dbmsDialect = dbmsDialect;
    	this.registeredFunctions = registeredFunctions;
        this.ctes = new LinkedHashSet<CTEInfo>();
    }
    
    Set<CTEInfo> getCtes() {
    	return ctes;
    }

	boolean isRecursive() {
		return recursive;
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
        	sb.append(cte.nonRecursiveCriteriaBuilder.getQueryString());
        	sb.append("\n)");
        }
        
        sb.append("\n");
    }

	<X> CTECriteriaBuilder<X, T> with(Class<X> cteClass, CriteriaBuilderImpl<T> criteriaBuilderImpl) {
		CTECriteriaBuilderImpl<X, T> cteBuilder = new CTECriteriaBuilderImpl<X, T>(cbf, em, dbmsDialect, cteClass, registeredFunctions, criteriaBuilderImpl, this);
        this.onBuilderStarted(cteBuilder);
		return cteBuilder;
	}

	<X> RecursiveCTECriteriaBuilder<X, T> withRecursive(Class<X> cteClass, CriteriaBuilderImpl<T> criteriaBuilderImpl) {
		recursive = true;
		RecursiveCTECriteriaBuilderImpl<X, T> cteBuilder = new RecursiveCTECriteriaBuilderImpl<X, T>(cbf, em, dbmsDialect, cteClass, registeredFunctions, criteriaBuilderImpl, this);
        this.onBuilderStarted(cteBuilder);
		return cteBuilder;
	}
	
    @Override
	public void onBuilderEnded(AbstractCTECriteriaBuilder<?, ?, ?> builder) {
		super.onBuilderEnded(builder);
		ctes.add(builder.createCTEInfo());
	}

}
