/*
 * Copyright 2014 Blazebit.
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
package com.blazebit.persistence.impl.hibernate.function.pageposition;

import java.util.ArrayList;
import java.util.List;
import org.hibernate.QueryException;
import org.hibernate.dialect.function.SQLFunction;
import org.hibernate.dialect.function.TemplateRenderer;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.type.LongType;
import org.hibernate.type.Type;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class PagePositionFunction implements SQLFunction {
    
    private final TemplateRenderer renderer;
    
    protected PagePositionFunction(String template) {
        this.renderer = new TemplateRenderer(template);
    }
    
    public PagePositionFunction() {
        //PAGE_POSITION(ID_SUBQUERY, ID_VALUE)
        this.renderer = new TemplateRenderer("(select base1_.rownumber_ from (select " + getRownumFunction() + " as rownumber_, base_.* from ?1 as base_) as base1_ where ?2 = base1_.?3)");
    }
    
    protected String getRownumFunction() {
        return "row_number() over ()";
    }

    @Override
    public boolean hasArguments() {
        return true;
    }

    @Override
    public boolean hasParenthesesIfNoArguments() {
        return true;
    }

    @Override
    public Type getReturnType(Type firstArgumentType, Mapping mapping) throws QueryException {
        return LongType.INSTANCE;
    }

    @Override
    public String render(Type firstArgumentType, List args, SessionFactoryImplementor factory) throws QueryException {
        if (args.size() != 2) {
            throw new RuntimeException("The page position function needs exactly two arguments <base_query> and <entity_id>! args=" + args);
        }
        
        String subquery = args.get(0).toString();
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
        List<Object> newArgs = new ArrayList<Object>(3);
        newArgs.add(subquery);
        newArgs.add(args.get(1));
        newArgs.add(idName);
        
        return renderer.render(newArgs, factory);
    }
    
    private boolean startsWithIgnoreCase(String s1, String s2) {
        return s1.regionMatches(true, 0, s2, 0, s2.length());
    }
}
