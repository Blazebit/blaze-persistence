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
package com.blazebit.persistence.impl.function.pageposition;

import com.blazebit.persistence.impl.function.TemplateRenderer;
import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.JpqlFunction;

/**
 *
 * @author Christian
 */
public class PagePositionFunction implements JpqlFunction {

    private final TemplateRenderer renderer;

    public PagePositionFunction() {
        this.renderer = new TemplateRenderer("(select base1_.rownumber_ from (select " + getRownumFunction() + " as rownumber_, base_.* from ?1 as base_) as base1_ where ?2 = base1_.?3)");
    }
    
    public PagePositionFunction(String template) {
        this.renderer = new TemplateRenderer(template);
    }
    
    protected String getRownumFunction() {
        return "row_number() over ()";
    }
    
    @Override
    public void render(FunctionRenderContext context) {
        if (context.getArgumentsSize() != 2) {
            throw new RuntimeException("The page position function needs exactly two arguments <base_query> and <entity_id>! args=" + context);
        }
        
        String subquery = context.getArgument(0);
        String subqueryStart = "(select ";
        int fromIndex;
        
        if (!startsWithIgnoreCase(subquery, subqueryStart)) {
            throw new IllegalArgumentException("Expected a subquery as the second parameter but was: " + subquery);
        } else if ((fromIndex = subquery.indexOf(" from ")) < 1) {
            throw new IllegalArgumentException("Expected a subquery as the second parameter but was: " + subquery);
        }
        
        String id = subquery.substring(subqueryStart.length(), fromIndex);
        
        if (id.indexOf(',') > -1) {
            throw new IllegalArgumentException("Expected a subquery with a simple id but it was composite: " + subquery);
        }
        
        int dotIndex = id.indexOf('.');
        
        if (dotIndex < 0) {
            throw new IllegalArgumentException("Expected that the id is fully qualified but it isn't: " + id);
        }
        
        String idName = id.substring(dotIndex + 1);
        renderer.start(context)
                .addArgument(0)
                .addArgument(1)
                .addParameter(idName)
                .build();
    }
    
    private boolean startsWithIgnoreCase(String s1, String s2) {
        return s1.regionMatches(true, 0, s2, 0, s2.length());
    }
}
