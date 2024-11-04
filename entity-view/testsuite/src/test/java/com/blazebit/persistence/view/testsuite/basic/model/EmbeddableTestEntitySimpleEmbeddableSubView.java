/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.basic.model;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.MappingParameter;
import com.blazebit.persistence.view.testsuite.entity.EmbeddableTestEntitySimpleEmbeddable2;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
@EntityView(EmbeddableTestEntitySimpleEmbeddable2.class)
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
