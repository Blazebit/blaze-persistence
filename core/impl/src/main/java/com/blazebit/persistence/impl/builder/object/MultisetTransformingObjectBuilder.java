/*
 * Copyright 2014 - 2021 Blazebit.
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

package com.blazebit.persistence.impl.builder.object;

import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.SelectBuilder;
import com.blazebit.persistence.impl.SelectInfo;
import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.FunctionExpression;
import com.blazebit.persistence.spi.JpqlFunctionProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
public class MultisetTransformingObjectBuilder implements ObjectBuilder<Object[]> {

    private final ProcessorEntry[] processorEntries;

    public MultisetTransformingObjectBuilder(Map<Integer, JpqlFunctionProcessor<?>> jpqlFunctionProcessors, List<SelectInfo> arguments) {
        List<ProcessorEntry> processorEntries = new ArrayList<>(jpqlFunctionProcessors.size());
        for (Map.Entry<Integer, JpqlFunctionProcessor<?>> entry : jpqlFunctionProcessors.entrySet()) {
            processorEntries.add(new ProcessorEntry(entry.getValue(), entry.getKey(), ((FunctionExpression) arguments.get(entry.getKey()).getExpression()).getExpressions()));
        }
        this.processorEntries = processorEntries.toArray(new ProcessorEntry[0]);
    }

    public MultisetTransformingObjectBuilder(List<Expression> arguments, Map<Integer, JpqlFunctionProcessor<?>> jpqlFunctionProcessors) {
        List<ProcessorEntry> processorEntries = new ArrayList<>(jpqlFunctionProcessors.size());
        for (Map.Entry<Integer, JpqlFunctionProcessor<?>> entry : jpqlFunctionProcessors.entrySet()) {
            processorEntries.add(new ProcessorEntry(entry.getValue(), entry.getKey(), ((FunctionExpression) arguments.get(entry.getKey())).getExpressions()));
        }
        this.processorEntries = processorEntries.toArray(new ProcessorEntry[0]);
    }

    @Override
    public <X extends SelectBuilder<X>> void applySelects(X selectBuilder) {
    }

    @Override
    public Object[] build(Object[] tuple) {
        for (int i = 0; i < processorEntries.length; i++) {
            ProcessorEntry processorEntry = processorEntries[i];
            int index = processorEntry.index;
            tuple[index] = processorEntry.processor.process(tuple[index], processorEntry.arguments);
        }

        return tuple;
    }

    @Override
    public List<Object[]> buildList(List<Object[]> list) {
        return list;
    }

    /**
     *
     * @author Christian Beikov
     * @since 1.5.0
     */
    private static final class ProcessorEntry {
        private final JpqlFunctionProcessor<Object> processor;
        private final int index;
        private final List<Object> arguments;

        public ProcessorEntry(JpqlFunctionProcessor<?> processor, int index, List<? extends Object> arguments) {
            this.processor = (JpqlFunctionProcessor<Object>) processor;
            this.index = index;
            this.arguments = (List<Object>) arguments;
        }
    }
}
