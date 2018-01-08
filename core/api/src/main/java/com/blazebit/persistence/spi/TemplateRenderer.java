/*
 * Copyright 2014 - 2018 Blazebit.
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

package com.blazebit.persistence.spi;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * A template renderer is a thread safe string renderer that can bind values to parameters.
 * It is not as sophisticated as {@link MessageFormat} but in contrast can be shared between threads
 * because it is immutable. Parameters are denoted by a question mark directly followed by the parameter index.
 * 
 * The following example should illustrate the usage.
 * 
 * <code>
 * new TemplateRenderer("?1 limit ?2")
 *         .start(context)
 *         .addArgument(1)
 *         .addArgument(2)
 *         .build();
 * </code>
 *
 * @author Christian Beikov
 * @since 1.0.6
 */
public class TemplateRenderer {

    private final String[] chunks;
    private final Integer[] parameterIndices;

    /**
     * Creates a new template renderer from the given template.
     * 
     * @param template The template on which this renderer is based.
     */
    public TemplateRenderer(String template) {
        List<String> chunkList = new ArrayList<String>();
        List<Integer> parameterIndexList = new ArrayList<Integer>();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < template.length(); i++) {
            char c = template.charAt(i);

            if (c == '?') {
                chunkList.add(sb.toString());
                sb.setLength(0);

                while (++i < template.length()) {
                    c = template.charAt(i);
                    if (Character.isDigit(c)) {
                        sb.append(c);
                    } else {
                        parameterIndexList.add(Integer.valueOf(sb.toString()) - 1);
                        sb.setLength(0);
                        sb.append(c);
                        break;
                    }
                }

                if (i == template.length()) {
                    parameterIndexList.add(Integer.valueOf(sb.toString()) - 1);
                    sb.setLength(0);
                }
            } else {
                sb.append(c);
            }
        }

        if (sb.length() > 0) {
            chunkList.add(sb.toString());
        }

        this.chunks = chunkList.toArray(new String[chunkList.size()]);
        this.parameterIndices = parameterIndexList.toArray(new Integer[parameterIndexList.size()]);
    }

    /**
     * Starts a new context for the given {@linkplain FunctionRenderContext} for building parameter bindings.
     * 
     * @param context The render context for the function
     * @return A context for binding parameter values
     */
    public Context start(FunctionRenderContext context) {
        return new Context(this, context);
    }

    /**
     * A context for a template renderer that supports binding function arguments or plain strings as values for placeholders.
     */
    public static class Context {

        private final TemplateRenderer template;
        private final FunctionRenderContext context;
        private final Object[] boundValues;
        private int boundValueIndex = 0;

        /**
         * Constructs a context for a template renderer and a render context.
         *
         * @param template The template renderer
         * @param context The function render context
         */
        public Context(TemplateRenderer template, FunctionRenderContext context) {
            this.template = template;
            this.context = context;
            this.boundValues = new Object[template.parameterIndices.length];
        }

        /**
         * Uses the value of the argument at the given index as value to be bound to the current parameter.
         * 
         * @param index The index of the argument
         * @return This context for chaining
         */
        public Context addArgument(int index) {
            if (boundValueIndex >= boundValues.length) {
                throw new IllegalArgumentException("The index " + boundValueIndex + " is invalid since all parameters have already been bound.");
            }

            boundValues[boundValueIndex++] = index;
            return this;
        }

        /**
         * Uses the given chunk as value to be bound to the current parameter.
         * 
         * @param chunk The chunk to use as value
         * @return This context for chaining
         */
        public Context addParameter(String chunk) {
            if (boundValueIndex >= boundValues.length) {
                throw new IllegalArgumentException("The index " + boundValueIndex + " is invalid since all parameters have already been bound.");
            }

            boundValues[boundValueIndex++] = chunk;
            return this;
        }

        /**
         * Binds the values to the underlying {@link FunctionRenderContext}.
         */
        public void build() {
            String[] chunks = template.chunks;
            Integer[] parameterIndices = template.parameterIndices;
            for (int i = 0; i < chunks.length; i++) {
                context.addChunk(chunks[i]);

                if (i < parameterIndices.length) {
                    int parameterIndex = parameterIndices[i];
                    Object boundValue = boundValues[parameterIndex];

                    if (boundValue instanceof Integer) {
                        context.addArgument((Integer) boundValue);
                    } else {
                        context.addChunk(boundValue.toString());
                    }
                }
            }
        }
    }
}
