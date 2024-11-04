/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
package com.blazebit.persistence.impl.function.jsonset;

import static com.blazebit.persistence.impl.util.JpqlFunctionUtil.quoteSingle;
import static com.blazebit.persistence.impl.util.JpqlFunctionUtil.unquoteSingleQuotes;

import com.blazebit.persistence.impl.function.concat.ConcatFunction;
import com.blazebit.persistence.impl.util.JpqlFunctionUtil;
import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.JpqlFunction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractJsonFunction implements JpqlFunction {

    private static final String PARAMETER_PLACEHOLDER = "??";

    private final ConcatFunction concatFunction;

    protected AbstractJsonFunction(ConcatFunction concatFunction) {
        this.concatFunction = concatFunction;
    }

    protected static boolean isJsonPathTemplate(String argument) {
        return argument.startsWith("$");
    }

    /**
     * Introduce JSON path template as optional first parameter
     * If first parameter is a literal string, interpret it as JSON path. The remaining arguments are interpreted
     * as varargs named parameters that are used as positional parameters for the json path template.
     * For postgres we need to parse the json path template and reformulate it to the postgres syntax.
     */
    protected static String toJsonPathTemplate(List<Object> pathElements, int to, boolean quotePathElements) {
        Object firstArgument = pathElements.get(0);
        if (firstArgument instanceof String && isJsonPathTemplate((String) firstArgument)) {
            return (String) firstArgument;
        } else {
            StringBuilder jsonPathBuilder = new StringBuilder("$");
            for (int i = 0; i < to; i++) {
                Object currentPathElement = pathElements.get(i);
                if (currentPathElement instanceof Integer) {
                    jsonPathBuilder.append('[');
                    jsonPathBuilder.append((int) currentPathElement);
                    jsonPathBuilder.append(']');
                } else {
                    jsonPathBuilder.append('.');
                    if (quotePathElements) {
                        jsonPathBuilder.append("\"");
                    }
                    jsonPathBuilder.append((String) currentPathElement);
                    if (quotePathElements) {
                        jsonPathBuilder.append("\"");
                    }
                }
            }
            return jsonPathBuilder.toString();
        }
    }

    protected void renderJsonPathTemplate(FunctionRenderContext context, String jsonPathTemplate,
                                                 int templateParameterOffset) {
        List<String> concatenationParts = splitByParameterPlaceholder(jsonPathTemplate);
        if (concatenationParts.size() == 1) {
            context.addChunk(quoteSingle(jsonPathTemplate));
        } else {
            context.addChunk(concatFunction.startConcat());
            context.addChunk(quoteSingle(concatenationParts.get(0)));
            for (int i = 1; i < concatenationParts.size(); i++) {
                context.addChunk(concatFunction.concatSeparator());
                renderJsonPathTemplateParameter(context, templateParameterOffset++);
                context.addChunk(concatFunction.concatSeparator());
                context.addChunk(quoteSingle(concatenationParts.get(i)));
            }
            context.addChunk(concatFunction.endConcat());
        }
    }

    protected void renderJsonPathTemplateParameter(FunctionRenderContext context, int parameterIdx) {
        context.addArgument(parameterIdx);
    }

    private static List<String> splitByParameterPlaceholder(String str) {
        List<String> parts = new ArrayList<>();
        int previousIdx = 0;
        int currentIdx;
        while ((currentIdx = str.indexOf(PARAMETER_PLACEHOLDER, previousIdx)) != -1) {
            parts.add(str.substring(previousIdx, currentIdx));
            previousIdx = currentIdx + PARAMETER_PLACEHOLDER.length();
        }
        parts.add(str.substring(previousIdx));
        return parts;
    }

    protected static List<Object> retrieveJsonPathElements(FunctionRenderContext context, int pathStartOffset) {
        String firstArgument = context.getArgument(pathStartOffset);
        if (isJsonPathTemplate(unquoteSingleQuotes(firstArgument))) {
            return Collections.singletonList((Object)
                JpqlFunctionUtil.unquoteDoubleQuotes(JpqlFunctionUtil.unquoteSingleQuotes(firstArgument)));
        }
        List<Object> jsonPathElements = new ArrayList<>(context.getArgumentsSize() - pathStartOffset);
        for (int i = pathStartOffset; i < context.getArgumentsSize(); i++) {
            try {
                jsonPathElements.add(Integer.parseInt(JpqlFunctionUtil.unquoteSingleQuotes(context.getArgument(i))));
            } catch (NumberFormatException e) {
                jsonPathElements.add(JpqlFunctionUtil.unquoteDoubleQuotes(JpqlFunctionUtil.unquoteSingleQuotes(context.getArgument(i))));
            }
        }
        return jsonPathElements;
    }
}
