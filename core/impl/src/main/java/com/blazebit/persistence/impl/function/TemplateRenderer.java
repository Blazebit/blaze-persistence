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
package com.blazebit.persistence.impl.function;

import com.blazebit.persistence.spi.FunctionRenderContext;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class TemplateRenderer {

    private final String[] chunks;
    private final Integer[] parameterIndices;

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
    
    public Context start(FunctionRenderContext context) {
        return new Context(this, context);
    }
    
    public static class Context {
        
        private final TemplateRenderer template;
        private final FunctionRenderContext context;
        private final Object[] boundValues;
        private int boundValueIndex = 0;

        public Context(TemplateRenderer template, FunctionRenderContext context) {
            this.template = template;
            this.context = context;
            this.boundValues = new Object[template.parameterIndices.length];
        }
        
        public Context addArgument(int index) {
            if (boundValueIndex >= boundValues.length) {
                throw new IllegalArgumentException("The index " + boundValueIndex + " is invalid since all parameters have already been bound.");
            }
            
            boundValues[boundValueIndex++] = index;
            return this;
        }
        
        public Context addParameter(String chunk) {
            if (boundValueIndex >= boundValues.length) {
                throw new IllegalArgumentException("The index " + boundValueIndex + " is invalid since all parameters have already been bound.");
            }
            
            boundValues[boundValueIndex++] = chunk;
            return this;
        }
        
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
