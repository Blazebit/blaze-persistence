package com.blazebit.persistence.impl.expression;

/**
 * TODO: documentation
 * 
 * @author Christian Beikov
 * @since 1.1.0
 *
 */
public interface PathReference {

    // Although this node will always be a JoinNode we will use casting at use site to be able to reuse the parser
    public Object getBaseNode();
    
    public String getField();
}
