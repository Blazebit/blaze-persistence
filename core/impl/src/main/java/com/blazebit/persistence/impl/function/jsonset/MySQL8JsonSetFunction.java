/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
package com.blazebit.persistence.impl.function.jsonset;

import com.blazebit.persistence.spi.FunctionRenderContext;
import java.util.List;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
public class MySQL8JsonSetFunction extends AbstractJsonSetFunction {

    @Override
    protected void render0(FunctionRenderContext context) {
        List<Object> jsonPathElements = AbstractJsonFunction.retrieveJsonPathElements(context, 2);
        String jsonPath =
            AbstractJsonFunction.toJsonPathTemplate(jsonPathElements, jsonPathElements.size(), true);

        context.addChunk("(select ");
        context.addChunk("case when lower(temp.val) = 'null' then json_set(");
        context.addArgument(0);
        context.addChunk(",'");
        context.addChunk(jsonPath);
        context.addChunk("', null) else ");
        context.addChunk("json_merge_patch(");
        context.addArgument(0);
        context.addChunk(", concat('");

        for (int i = 0; i < jsonPathElements.size(); i++) {
            startJsonPathElement(context, jsonPathElements, i);
        }
        context.addChunk("', ");
        context.addChunk("temp.val");
        context.addChunk(", '");
        for (int i = jsonPathElements.size() - 1; i >= 0; i--) {
            endJsonPathElement(context, jsonPathElements, i);
        }
        context.addChunk("')) end");

        context.addChunk(" from (values row(");
        context.addArgument(1);
        context.addChunk(")) temp(val))");
    }

    private void startJsonPathElement(FunctionRenderContext context, List<Object> pathElems, int curIndex) {
        Object pathElem = pathElems.get(curIndex);
        if (pathElem instanceof Integer) {
            context.addChunk("[', ");

            context.addChunk("(select GROUP_CONCAT(quoted_array_element.value SEPARATOR ',') from (");
            context.addChunk("select array_element.rownumber, COALESCE(array_element.complexvalue, COALESCE(CASE WHEN array_element.scalarvalue IS NOT NULL AND array_element.numbervalue IS NULL THEN concat('\"', array_element.scalarvalue, '\"') ELSE array_element.scalarvalue END, 'null')) as value from ");
            context.addChunk("json_table(");
            context.addArgument(0);
            context.addChunk(",'");
            String jsonPathTemplate = AbstractJsonFunction.toJsonPathTemplate(pathElems, curIndex, true);
            context.addChunk(jsonPathTemplate + "[*]");
            context.addChunk("' COLUMNS (");
            context.addChunk("rownumber FOR ORDINALITY,");
            context.addChunk("complexvalue JSON PATH '$',");
            context.addChunk("scalarvalue text PATH '$',");
            context.addChunk("numbervalue numeric PATH '$' null on error");
            context.addChunk(")) array_element ");
            context.addChunk("where array_element.rownumber != ");
            context.addChunk(pathElem.toString());
            context.addChunk("+1");
            context.addChunk(" union all ");
            context.addChunk("select ");
            context.addChunk(pathElem.toString());
            context.addChunk("+1, ");

            if (curIndex < pathElems.size() - 1) {
                context.addChunk("coalesce(json_merge_patch(");
                renderJsonGet(context, AbstractJsonFunction.toJsonPathTemplate(pathElems, curIndex + 1, true));
                context.addChunk(", concat('");
            } else {
                context.addChunk("concat('");
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
                context.addChunk(")), concat('");
                for (int i = curIndex + 1; i < pathElems.size(); i++) {
                    startJsonPathElement(context, pathElems, i);
                }
                context.addChunk("', temp.val, '");
                for (int i = pathElems.size() - 1; i >= curIndex + 1; i--) {
                    endJsonPathElement(context, pathElems, i);
                }
                context.addChunk("'))");
            } else {
                context.addChunk(")");
            }
            context.addChunk(" order by rownumber");
            context.addChunk(") quoted_array_element)");

            context.addChunk(", ']");
        } else {
            context.addChunk("}");
        }
    }

    private void renderJsonGet(FunctionRenderContext context, String jsonPath) {
        context.addChunk("json_value(");
        context.addArgument(0);
        context.addChunk(",'");
        context.addChunk(jsonPath);
        context.addChunk("')");
    }
}
