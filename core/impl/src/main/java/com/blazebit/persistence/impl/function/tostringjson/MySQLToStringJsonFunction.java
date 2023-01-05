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

package com.blazebit.persistence.impl.function.tostringjson;

import com.blazebit.persistence.impl.util.SqlUtils;
import com.blazebit.persistence.spi.FunctionRenderContext;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public class MySQLToStringJsonFunction extends AbstractToStringJsonFunction {

    private static final String START_CHUNK = "(select json_arrayagg(json_object('";

    @Override
    public void render(FunctionRenderContext context, String[] fields, String[] selectItemExpressions, String subquery, int fromIndex) {
        context.addChunk(START_CHUNK);

        int orderByIndex = SqlUtils.indexOfOrderBy(subquery, fromIndex);
        if (orderByIndex == -1) {
            renderJsonObjectArguments(context, fields, selectItemExpressions);
            context.addChunk("))");
            context.addChunk(subquery.substring(fromIndex));
        } else {
            int limitIndex = SqlUtils.indexOfLimit(subquery, orderByIndex);
            if (limitIndex == -1) {
                renderJsonObjectArguments(context, fields, selectItemExpressions);
                context.addChunk("))");
                context.addChunk(" OVER (");
                context.addChunk(subquery.substring(orderByIndex));
                context.addChunk(")");
                context.addChunk(subquery.substring(fromIndex, orderByIndex));
            } else {
                renderJsonObjectArguments(context, fields, fields);
                context.addChunk("))");
                context.addChunk(" from (select ");
                for (int i = 0; i < fields.length; i++) {
                    if (i != 0) {
                        context.addChunk(",");
                    }
                    context.addChunk(selectItemExpressions[i]);
                    context.addChunk(" ");
                    context.addChunk(fields[i]);
                }
                context.addChunk(subquery.substring(fromIndex));
                context.addChunk(" tmp)");
            }
        }
    }

    private void renderJsonObjectArguments(FunctionRenderContext context, String[] fields, String[] selectItemExpressions) {
        for (int i = 0; i < fields.length; i++) {
            if (i != 0) {
                context.addChunk(",'");
            }
            context.addChunk(fields[i]);
            context.addChunk("',");
            if (selectItemExpressions[i].startsWith(START_CHUNK)) {
                context.addChunk(selectItemExpressions[i]);
            } else {
                context.addChunk("cast(");
                context.addChunk(selectItemExpressions[i]);
                context.addChunk(" as char)");
            }
        }
    }

}