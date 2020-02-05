/*
 * Copyright 2014 - 2020 Blazebit.
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

package com.blazebit.persistence.impl.function.stringjsonagg;

import com.blazebit.persistence.spi.FunctionRenderContext;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public class MySQLStringJsonAggFunction extends AbstractStringJsonAggFunction {

    @Override
    public void render(FunctionRenderContext context) {
        if ((context.getArgumentsSize() & 1) == 1) {
            throw new RuntimeException("The string_json_agg function needs an even amount of arguments <key1>, <value1>, ..., <keyN>, <valueN>! args=" + context);
        }
        context.addChunk("json_arrayagg(json_object(");
        context.addArgument(0);
        for (int i = 1; i < context.getArgumentsSize(); i++) {
            context.addChunk(",");
            if ((i & 1) == 1) {
                context.addChunk("cast(");
                context.addArgument(i);
                context.addChunk(" as char)");
            } else {
                context.addArgument(i);
            }
        }
        context.addChunk("))");
    }

}