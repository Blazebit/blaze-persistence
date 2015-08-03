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
package com.blazebit.persistence.impl.function.limit;

/**
 *
 * @author Christian Beikov
 * @since 1.0.1
 */
public class OracleLimitFunction extends LimitFunction {

    public OracleLimitFunction() {
        super(
            "select * from ( ?1 ) where rownum <= ?2",
            // TODO: This is selecting the rownum too...
            // TODO: See the following
            // https://groups.google.com/forum/#!topic/jooq-user/G9Op6cQwMkY/discussion
            // http://www.inf.unideb.hu/~gabora/pagination/results.html
            "select * from ( select row_.*, rownum rownum_ from ( ?1 ) row_ ) where rownum_ <= ?2 and rownum_ > ?3"
        );
    }
    
}
