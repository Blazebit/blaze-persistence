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

package com.blazebit.persistence.view.testsuite.basic.model;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.MappingParameter;
import com.blazebit.persistence.view.testsuite.entity.EmbeddableTestEntitySimpleEmbeddable;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
@EntityView(EmbeddableTestEntitySimpleEmbeddable.class)
public interface EmbeddableTestEntitySimpleEmbeddableSubView {

    @Mapping("name")
    public String getName();
    
    // NOTE: we kind of need one value to be a long, otherwise the int operation will fail because of the overflow
    // For the ORM we also need to provide the expected type so that it will be fetched with the appropriate JDBC type 
    @Mapping("FUNCTION('TREAT_LONG', " + 0x80000000L + "L + 1)")
    public Long getStaticLong();
    
    @Mapping("1 + 1")
    public Integer getStaticInteger();
    
    @Mapping("'test'")
    public String getStaticString();
    
    @Mapping("NULL")
    public String getStaticStringNull();
    
    @Mapping("NULL")
    public Integer getStaticIntegerNull();
    
    @Mapping("CASE WHEN 1=1 THEN true ELSE false END")
    public Boolean getStaticBoolean();
    
    @MappingParameter("optionalInteger")
    public Integer getOptionalInteger();
    
    
    // Works for blaze-persistence but apparently hibernate can't handle the literals
//    @Mapping("FUNCTION('TREAT_CALENDAR', {ts '2016-01-01 00:00:00.0'})")
//    public Calendar getStaticTimestamp();

}
