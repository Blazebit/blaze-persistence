package com.blazebit.persistence.examples.base.bean;

import javax.persistence.EntityManager;

/**
 * @author Moritz Becker (moritz.becker@gmx.at)
 * @since 1.2
 */
public interface EntityManagerHolder {

    EntityManager getEntityManager();

}
