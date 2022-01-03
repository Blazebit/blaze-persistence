/*
 * Copyright 2014 - 2022 Blazebit.
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

package com.blazebit.persistence.impl.function.tostringxml;

import com.blazebit.persistence.impl.function.cast.CastFunction;
import com.blazebit.persistence.spi.FunctionRenderContext;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public class ForXmlPathToStringXmlFunction extends AbstractToStringXmlFunction {

    private final CastFunction castFunction;

    public ForXmlPathToStringXmlFunction(CastFunction castFunction) {
        this.castFunction = castFunction;
    }

    @Override
    public void render(FunctionRenderContext context, String[] fields, String[] selectItemExpressions, String subquery, int fromIndex) {
        context.addChunk("(select ");

        for (int i = 0; i < fields.length; i++) {
            if (i != 0) {
                context.addChunk(",");
            }
            context.addChunk(castFunction.getCastExpression(selectItemExpressions[i]));
            context.addChunk(" as ");
            context.addChunk(fields[i]);
        }

        context.addChunk(subquery.substring(fromIndex, subquery.lastIndexOf(')')));
        context.addChunk(" for xml path ('e'))");
    }
}