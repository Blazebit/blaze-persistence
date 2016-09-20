package com.blazebit.persistence.impl.hibernate;

import org.hibernate.mapping.Table;
import org.hibernate.service.Service;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface Database extends Service {

    public Table getTable(String name);

}
