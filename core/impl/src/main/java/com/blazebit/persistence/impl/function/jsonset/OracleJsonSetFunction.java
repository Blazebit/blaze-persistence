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
package com.blazebit.persistence.impl.function.jsonset;

import com.blazebit.persistence.impl.function.jsonget.AbstractJsonGetFunction;
import com.blazebit.persistence.spi.FunctionRenderContext;

import java.util.List;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
public class OracleJsonSetFunction extends AbstractJsonSetFunction {

    @Override
    protected void render0(FunctionRenderContext context) {
        List<Object> jsonPathElements = AbstractJsonGetFunction.retrieveJsonPathElements(context, 2);

        context.addChunk("(select ");
        context.addChunk("json_mergepatch(");
        context.addArgument(0);
        context.addChunk(",'");

        for (int i = 0; i < jsonPathElements.size(); i++) {
            startJsonPathElement(context, jsonPathElements, i);
        }
        context.addChunk("' || ");
        context.addChunk("column_value");
        context.addChunk(" || '");
        for (int i = jsonPathElements.size() - 1; i >= 0; i--) {
            endJsonPathElement(context, jsonPathElements, i);
        }
        context.addChunk("')");

        context.addChunk(" from table(sys.ODCIVARCHAR2LIST(");
        context.addArgument(1);
        context.addChunk(")))");
    }

    private void startJsonPathElement(FunctionRenderContext context, List<Object> pathElems, int curIndex) {
        Object pathElem = pathElems.get(curIndex);
        if (pathElem instanceof Integer) {
            context.addChunk("[' || ");

            context.addChunk("(select (dbms_xmlgen.convert(substr(xmlagg(xmlelement(e,to_clob(',') || quoted_array_element.value).extract('//text()')).getClobVal(),2),1)) from (");
            context.addChunk("select array_element.row_number, COALESCE(array_element.\"complex_value\", COALESCE(CASE WHEN array_element.\"scalar_value\" IS NOT NULL AND array_element.\"number_value\" IS NULL THEN '\"' || array_element.\"scalar_value\" || '\"' ELSE array_element.\"scalar_value\" END, 'null')) as value from ");
            context.addChunk("json_table(");
            context.addArgument(0);
            context.addChunk(",'");
            context.addChunk(AbstractJsonGetFunction.toJsonPath(pathElems, curIndex, true) + "[*]");
            context.addChunk("' COLUMNS (");
            context.addChunk("row_number FOR ORDINALITY,");
            context.addChunk("\"complex_value\" varchar2 FORMAT JSON PATH '$',");
            context.addChunk("\"scalar_value\" varchar2 PATH '$',");
            context.addChunk("\"number_value\" number PATH '$' null on error");
            context.addChunk(")) array_element where array_element.row_number != ");
            context.addChunk(pathElem.toString());
            context.addChunk("+1");
            context.addChunk(" union all ");
            context.addChunk("select ");
            context.addChunk(pathElem.toString());
            context.addChunk("+1, ");

            if (curIndex < pathElems.size() - 1) {
                context.addChunk("coalesce(json_mergepatch(");
                renderJsonGet(context, AbstractJsonGetFunction.toJsonPath(pathElems, curIndex + 1, true));
                context.addChunk(",'");
            } else {
                context.addChunk("'");
            }
        } else {
            context.addChunk("{\"");
            context.addChunk((String) pathElem);
            context.addChunk("\":");
        }
    }

    private void endJsonPathElement(FunctionRenderContext context, List<Object> pathElems, int curIndex) {
        Object pathElem = pathElems.get(curIndex);
        if (pathElem instanceof Integer) {
            context.addChunk("'");
            if (curIndex < pathElems.size() - 1) {
                context.addChunk("), '");
                for (int i = curIndex + 1; i < pathElems.size(); i++) {
                    startJsonPathElement(context, pathElems, i);
                }
                context.addChunk("' || column_value || '");
                for (int i = pathElems.size() - 1; i >= curIndex + 1; i--) {
                    endJsonPathElement(context, pathElems, i);
                }
                context.addChunk("')");
            }

            context.addChunk("from table (sys.ODCIVARCHAR2LIST(column_value)) ");
            context.addChunk("order by row_number");
            context.addChunk(") quoted_array_element) ");

            context.addChunk(" || ']");
        } else {
            context.addChunk("}");
        }
    }

    private void renderJsonGet(FunctionRenderContext context, String jsonPath) {
        context.addChunk("coalesce(json_value(");
        context.addArgument(0);
        context.addChunk(" format json,'");
        context.addChunk(jsonPath);
        context.addChunk("'),json_query(");
        context.addArgument(0);
        context.addChunk(" format json,'");
        context.addChunk(jsonPath);
        context.addChunk("'))");
    }
}
