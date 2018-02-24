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

package com.blazebit.persistence.impl.function.pageposition;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class OraclePagePositionFunction extends PagePositionFunction {

    private static final String ROWNUM = "rownum";

    public OraclePagePositionFunction() {
        // Oracle doesn't treat the subquery as constant...
        super("MAX((select base1_.rownumber_ from (select " + ROWNUM
                + " as rownumber_, base_.* from ?1 base_) base1_ where ?2 = base1_.?3))");
    }

    @Override
    protected String getRownumFunction() {
        return ROWNUM;
    }

}
