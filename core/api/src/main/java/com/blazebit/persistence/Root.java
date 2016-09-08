package com.blazebit.persistence;

/**
 * CAREFUL, this is an experimental API and will change!
 *
 * TODO: documentation
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface Root {

    public String getAlias();

    public Class<?> getType();

}
