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
package com.blazebit.persistence.impl.function.groupconcat;

import com.blazebit.persistence.spi.FunctionRenderContext;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class OracleGroupConcatFunction extends AbstractGroupConcatFunction {

    public OracleGroupConcatFunction() {
        super("listagg(?1)");
    }

    @Override
    public void render(FunctionRenderContext context) {
        GroupConcat groupConcat = getGroupConcat(context);
        StringBuilder sb = new StringBuilder();
        
        if (groupConcat.isDistinct()) {
            sb.append("distinct ");
        }
        
        sb.append(groupConcat.getExpression());
        sb.append(", ");
        sb.append(groupConcat.getSeparator());
        
        if (!groupConcat.getOrderByExpression().isEmpty()) {
            sb.append(") within group (order by ");
            sb.append(groupConcat.getOrderByExpression());
        }
        
        renderer.start(context)
                .addParameter(sb.toString())
                .build();
    }
}
