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
package com.blazebit.persistence.impl.eclipselink.function;

import com.blazebit.persistence.spi.FunctionRenderContext;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.persistence.expressions.Expression;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class EclipseLinkFunctionRenderContext implements FunctionRenderContext {
    
    private final List<String> chunks = new ArrayList<String>();
    private final int[] argumentIndices;
    private final List<Expression> arguments;
    private int currentIndex;
    private Boolean chunkFirst;

    public EclipseLinkFunctionRenderContext(List<Expression> arguments) {
        this.argumentIndices = new int[arguments.size()];
        this.arguments = arguments;
    }

    @Override
    public int getArgumentsSize() {
        return arguments.size();
    }

    @Override
    public String getArgument(int index) {
        return arguments.get(index).toString();
    }

    @Override
    public void addArgument(int index) {
        if (chunkFirst == null) {
            chunkFirst = false;
        }
        argumentIndices[currentIndex++] = index;
    }

    @Override
    public void addChunk(String chunk) {
        if (chunkFirst == null) {
            chunkFirst = true;
        }
        chunks.add(chunk);
    }
    
    public boolean isChunkFirst() {
        return chunkFirst;
    }
    
    public List<String> getChunks() {
        return chunks;
    }
    
    public int[] getArgumentIndices() {
        return argumentIndices;
    }
    
}
