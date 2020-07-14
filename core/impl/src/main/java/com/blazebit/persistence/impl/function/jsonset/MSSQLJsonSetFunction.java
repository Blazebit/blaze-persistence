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
package com.blazebit.persistence.impl.function.jsonset;

import com.blazebit.persistence.impl.function.jsonget.AbstractJsonGetFunction;
import com.blazebit.persistence.spi.FunctionRenderContext;

import java.util.List;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
public class MSSQLJsonSetFunction extends AbstractJsonGetFunction {

    @Override
    protected void render0(FunctionRenderContext context) {
        List<Object> jsonPathElements = AbstractJsonGetFunction.retrieveJsonPathElements(context, 2);
        String jsonPath = AbstractJsonGetFunction.toJsonPath(jsonPathElements, jsonPathElements.size(), true);

        context.addChunk("(select case when isjson(temp.val) = 0 then (case ");

        context.addChunk("when TRY_CONVERT(bigint , json_value(concat('{\"val\": ', temp.val, '}'), '$.val')) is not null then json_modify(");
        context.addArgument(0);
        context.addChunk(", '" + jsonPath + "', CONVERT(bigint, json_value(concat('{\"val\": ', temp.val, '}'), '$.val'))) ");

        context.addChunk("when TRY_CONVERT(float , json_value(concat('{\"val\": ', temp.val, '}'), '$.val')) is not null then json_modify(");
        context.addArgument(0);
        context.addChunk(", '" + jsonPath + "', CONVERT(float, json_value(concat('{\"val\": ', temp.val, '}'), '$.val'))) ");

        context.addChunk("when TRY_CONVERT(bit, json_value(concat('{\"val\": ', temp.val, '}'), '$.val')) is not null then json_modify(");
        context.addArgument(0);
        context.addChunk(", '" + jsonPath + "', CONVERT(bit, json_value(concat('{\"val\": ', temp.val, '}'), '$.val'))) ");

        context.addChunk("else case when LOWER(temp.val) = 'null' ");
        context.addChunk("then ");
        context.addChunk("json_modify(");
        context.addArgument(0);
        context.addChunk(", 'strict " + jsonPath + "', json_value(concat('{\"val\": ', temp.val, '}'), '$.val')) ");
        context.addChunk("else ");
        context.addChunk("json_modify(");
        context.addArgument(0);
        context.addChunk(", '" + jsonPath + "', json_value(concat('{\"val\": ', temp.val, '}'), '$.val')) end end");

        context.addChunk(") else json_modify(");
        context.addArgument(0);
        context.addChunk(", '" + jsonPath + "', json_query(concat('{\"val\": ', temp.val, '}'), '$.val')) end ");

        context.addChunk("from (values(");
        context.addArgument(1);
        context.addChunk(")) temp(val))");
    }
}
