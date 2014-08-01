/*
 * Copyright 2014 Blazebit.
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

package com.blazebit.persistence.view.impl.objectbuilder;

/**
 *
 * @author cpbec
 */
public class AbstractOffsetViewTypeObjectBuilder<T> extends AbstractViewTypeObjectBuilder<T> {
    
    private final Object[] tempTuple;
    private final int startIndex;
    private final int length;

    public AbstractOffsetViewTypeObjectBuilder(ViewTypeObjectBuilderTemplate<T> template, int startIndex) {
        super(template);
        
        this.tempTuple = new Object[template.getEffectiveTupleSize()];
        this.startIndex = startIndex;
        this.length = template.getEffectiveTupleSize();
    }

    @Override
    public T build(Object[] tuple, String[] aliases) {
        T resultObject;
        
        // TODO: maybe we can do something smarter here
        System.arraycopy(tuple, startIndex, tempTuple, 0, length);
        resultObject = super.build(tempTuple, aliases);
        
        Object[] resultTuple = new Object[tuple.length - (length - 1)];
        System.arraycopy(tuple, 0, resultTuple, 0, startIndex);
        resultTuple[startIndex] = resultObject;
        
        int nextStartIndex = startIndex + length;
        if (nextStartIndex < tuple.length) {
            System.arraycopy(tuple, nextStartIndex, resultTuple, startIndex + 1, tuple.length - nextStartIndex);
        }
        
        return (T) resultTuple;
    }
}
