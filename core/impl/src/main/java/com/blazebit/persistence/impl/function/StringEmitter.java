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

import java.util.List;

import com.blazebit.persistence.spi.FunctionRenderContext;

/**
 *
 * @author Christian
 */
public class StringEmitter implements TemplateEmitter {

    private final String string;

    public StringEmitter(String string) {
        this.string = string;
    }
    
    @Override
    public void emit(FunctionRenderContext context, List<?> parameters) {
        context.addChunk(string);
    }
    
}
