/*
 * Copyright 2014 - 2023 Blazebit.
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
package com.blazebit.persistence.impl.function.jsonget;

import com.blazebit.persistence.spi.FunctionRenderContext;

import java.util.List;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
public class DB2JsonGetFunction extends AbstractJsonGetFunction {

    @Override
    protected void render0(FunctionRenderContext context) {
        List<Object> jsonPathElements = AbstractJsonGetFunction.retrieveJsonPathElements(context, 1);
        jsonPathElements.add(0, "val");
        String jsonPath = AbstractJsonGetFunction.toJsonPath(jsonPathElements, jsonPathElements.size(), false);

        context.addChunk("json_query(concat('{\"val\":', concat(");
        context.addArgument(0);
        context.addChunk(", '}'))");
        context.addChunk(",'");
        context.addChunk(jsonPath);
        context.addChunk("' OMIT QUOTES)");
    }
}
