package com.blazebit.persistence.impl.hibernate;

import org.hibernate.mapping.Collection;
import org.hibernate.mapping.OneToMany;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.persister.collection.OneToManyPersister;

import java.util.Iterator;
import java.util.logging.Logger;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CommonHibernateUtils {

    private static final Logger LOG = Logger.getLogger(CommonHibernateUtils.class.getName());

    public static void setCollectionPersisterClass(Iterator<PersistentClass> iter, Class<?> customOneToManyPersisterClass) {
        while (iter.hasNext()) {
            PersistentClass clazz = iter.next();
            Iterator<Property> propertyIter = clazz.getPropertyClosureIterator();
            while (propertyIter.hasNext()) {
                Property p = propertyIter.next();
                if (!(p.getValue() instanceof Collection)) {
                    continue;
                }
                Collection c = (Collection) p.getValue();
                if (c.getElement() instanceof OneToMany) {
                    if (c.getCollectionPersisterClass() == null || c.getCollectionPersisterClass() == OneToManyPersister.class || c.getCollectionPersisterClass() == customOneToManyPersisterClass) {
                        c.setCollectionPersisterClass(customOneToManyPersisterClass);
                    } else {
                        LOG.warning("Could not set a custom one-to-many persister on '" + clazz.getClassName() + "." + p.getName() + "' because it has set a non-default persister: " + c.getCollectionPersisterClass().getName());
                    }
                }
            }
        }
    }
}
