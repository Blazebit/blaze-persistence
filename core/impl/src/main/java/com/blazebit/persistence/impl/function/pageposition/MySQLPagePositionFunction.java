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
public class MySQLPagePositionFunction extends PagePositionFunction {

    public MySQLPagePositionFunction() {
        super("(select rownumber_ from (select @i:=@i+1 as rownumber_, base_.* from (?1) as base_, (SELECT @i:=0) as iter_) as base1_ where ?2 = base1_.?3)");
    }

}
