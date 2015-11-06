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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.blazebit.persistence.FullSelectCTECriteriaBuilder;
import com.blazebit.persistence.LeafOngoingSetOperationCTECriteriaBuilder;
import com.blazebit.persistence.ReturningModificationCriteriaBuilderFactory;
import com.blazebit.persistence.SelectRecursiveCTECriteriaBuilder;
import com.blazebit.persistence.StartOngoingSetOperationCTECriteriaBuilder;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.1.0
 */
public class CTEManager extends CTEBuilderListenerImpl {

	private final MainQuery mainQuery;
	// CteFinalName -> CTEInfo
	private final Map<String, CTEInfo> cteMap;
	// EntityAlias -> CteFinalName
    private final Map<String, String> namedCteUsages;
    private final Set<CTEInfo> ctes;
    private boolean recursive = false;

    CTEManager(MainQuery mainQuery) {
    	this.mainQuery = mainQuery;
    	this.cteMap = new HashMap<String, CTEInfo>(0);
    	this.namedCteUsages = new HashMap<String, String>(0);
        this.ctes = new LinkedHashSet<CTEInfo>();
    }
    
    Set<CTEInfo> getCtes() {
    	return ctes;
    }

    public boolean hasCtes() {
        return ctes.size() > 0;
    }

	boolean isRecursive() {
		return recursive;
	}

    void addCteUsage(String finalAlias, String cteName) {
        namedCteUsages.put(finalAlias, cteName);
    }
    
    Map<String, String> getNamedCteUsages() {
        return namedCteUsages;
    }
    
    List<String> getCteEntityAliases(String cteName) {
        List<String> aliases = new ArrayList<String>();
        
        // Since the count will probably be small, this is ok
        for (Map.Entry<String, String> entry : namedCteUsages.entrySet()) {
            if (cteName.equals(entry.getValue())) {
                aliases.add(entry.getKey());
            }
        }
        
        return aliases;
    }

    String getFinalCteName(String cteName) {
        CTEInfo info = cteMap.get(cteName);
        
        if (info == null) {
            return null;
        }
        
        return getFinalCteName(cteName, info.cteType.getJavaType());
    }

    String getFinalCteName(String cteName, Class<?> cteClass) {
        if (cteClass.getSimpleName().equals(cteName)) {
            return cteName;
        }
        
        CTEInfo cteInfo = cteMap.get(cteName);
        if (cteInfo == null) {
            cteInfo = cteMap.get(cteClass.getName());
            
            if (cteInfo == null) {
                throw new IllegalArgumentException("Could not find cte '" + cteClass.getName() + "' with name '" + cteName + "' in the current query!");
            }
            
            return cteInfo.name + "_" + cteClass.getSimpleName();
        }
        
        if (!cteInfo.cteType.getJavaType().equals(cteClass)) {
            throw new IllegalArgumentException("Invalid expected class '" + cteClass.getName() + "' for cte with name '" + cteName + "'. Actual type is: " + cteInfo.cteType.getJavaType().getName());
        }
        
        return cteInfo.name + "_" + cteClass.getSimpleName();
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
        	
        	sb.append(getFinalCteName(cte.name, cte.cteType.getJavaType()));
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
        	
        	if (cte.recursive) {
        	    sb.append("\nUNION ALL\n");
        	    sb.append(cte.recursiveCriteriaBuilder.getQueryString());
        	}
        	
        	sb.append("\n)");
        }
        
        sb.append("\n");
    }

    @SuppressWarnings("unchecked")
    <Y> StartOngoingSetOperationCTECriteriaBuilder<Y, LeafOngoingSetOperationCTECriteriaBuilder<Y>> withStartSet(Class<?> cteClass, String cteName, Y result) {
        cteName = getCteName(cteClass, cteName);
        FinalSetOperationCTECriteriaBuilderImpl<Y> parentFinalSetOperationBuilder = new FinalSetOperationCTECriteriaBuilderImpl<Y>(mainQuery, (Class<Y>) cteClass, result, null, false, this, null);
        OngoingFinalSetOperationCTECriteriaBuilderImpl<Y> subFinalSetOperationBuilder = new OngoingFinalSetOperationCTECriteriaBuilderImpl<Y>(mainQuery, (Class<Y>) cteClass, null, null, true, parentFinalSetOperationBuilder.getSubListener(), null);
        this.onBuilderStarted(parentFinalSetOperationBuilder);
        
        LeafOngoingSetOperationCTECriteriaBuilderImpl<Y> leafCb = new LeafOngoingSetOperationCTECriteriaBuilderImpl<Y>(mainQuery, cteName, (Class<Object>) cteClass, result, parentFinalSetOperationBuilder.getSubListener(), (FinalSetOperationCTECriteriaBuilderImpl<Object>) parentFinalSetOperationBuilder);
        OngoingSetOperationCTECriteriaBuilderImpl<Y, LeafOngoingSetOperationCTECriteriaBuilder<Y>> cb = new OngoingSetOperationCTECriteriaBuilderImpl<Y, LeafOngoingSetOperationCTECriteriaBuilder<Y>>(mainQuery, cteName, (Class<Object>) cteClass, result, subFinalSetOperationBuilder.getSubListener(), (OngoingFinalSetOperationCTECriteriaBuilderImpl<Object>) subFinalSetOperationBuilder, leafCb);
        
        subFinalSetOperationBuilder.setOperationManager.setStartQueryBuilder(cb);
        parentFinalSetOperationBuilder.setOperationManager.setStartQueryBuilder(subFinalSetOperationBuilder);

        subFinalSetOperationBuilder.getSubListener().onBuilderStarted(cb);
        parentFinalSetOperationBuilder.getSubListener().onBuilderStarted(leafCb);
        
        return cb;
    }

    @SuppressWarnings("unchecked")
	<Y> FullSelectCTECriteriaBuilder<Y> with(Class<?> cteClass, String cteName, Y result) {
        cteName = getCteName(cteClass, cteName);
		FullSelectCTECriteriaBuilderImpl<Y> cteBuilder = new FullSelectCTECriteriaBuilderImpl<Y>(mainQuery, cteName, (Class<Object>) cteClass, result, this);
        this.onBuilderStarted(cteBuilder);
		return cteBuilder;
	}

	@SuppressWarnings("unchecked")
    <Y> SelectRecursiveCTECriteriaBuilder<Y> withRecursive(Class<?> cteClass, String cteName, Y result) {
	    cteName = getCteName(cteClass, cteName);
		recursive = true;
		RecursiveCTECriteriaBuilderImpl<Y> cteBuilder = new RecursiveCTECriteriaBuilderImpl<Y>(mainQuery, cteName, (Class<Object>) cteClass, result, this);
        this.onBuilderStarted(cteBuilder);
		return cteBuilder;
	}

	<Y> ReturningModificationCriteriaBuilderFactory<Y> withReturning(Class<?> cteClass, String cteName, Y result) {
	    cteName = getCteName(cteClass, cteName);
	    ReturningModificationCriteraBuilderFactoryImpl<Y> factory = new ReturningModificationCriteraBuilderFactoryImpl<Y>(mainQuery, cteName, cteClass, result, this);
		return factory;
	}
	
    private String getCteName(Class<?> cteClass, String cteName) {
        if (cteName == null) {
            return cteClass.getSimpleName();
        }
        
        return cteName;
    }

    @Override
	public void onBuilderEnded(CTEInfoBuilder builder) {
		super.onBuilderEnded(builder);
		CTEInfo cteInfo = builder.createCTEInfo();
		ctes.add(cteInfo);
		cteMap.put(cteInfo.name, cteInfo);
	}

}
