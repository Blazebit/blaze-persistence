/*
 * Copyright (c) 2008, 2023 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0,
 * or the Eclipse Distribution License v. 1.0 which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: EPL-2.0 OR BSD-3-Clause
 */

// Contributors:
//     Lukas Jungmann  - 2.2
//     Linda DeMichiel - 2.1
//     Linda DeMichiel - 2.0

package jakarta.persistence.spi;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Holds the global {@link PersistenceProviderResolver} instance.
 * If no {@code PersistenceProviderResolver} is set by the environment,
 * the default {@code PersistenceProviderResolver} is used.
 *
 * <p>Enable {@code "jakarta.persistence.spi"} logger to show diagnostic
 * information.
 * 
 * <p>Implementations must be thread-safe.
 * 
 * @since 2.0
 */
public class PersistenceProviderResolverHolder {

    private static PersistenceProviderResolver singleton = new DefaultPersistenceProviderResolver();

    /**
     * Returns the current persistence provider resolver.
     * 
     * @return the current persistence provider resolver
     */
    public static PersistenceProviderResolver getPersistenceProviderResolver() {
        return singleton;
    }

    /**
     * Defines the persistence provider resolver used.
     * 
     * @param resolver persistence provider resolver to be used.
     */
    public static void setPersistenceProviderResolver(PersistenceProviderResolver resolver) {
        if (resolver == null) {
            singleton = new DefaultPersistenceProviderResolver();
        } else {
            singleton = resolver;
        }
    }

    /**
     * Default provider resolver class to use when none is explicitly set.
     * 
     * <p>Uses service loading mechanism as described in the Jakarta Persistence
     * specification. A ServiceLoader.load() call is made with the current context
     * classloader to find the service provider files on the classpath.
     */
    private static class DefaultPersistenceProviderResolver implements PersistenceProviderResolver {

        /**
         * Cached list of available providers cached by CacheKey to ensure
         * there is not potential for provider visibility issues. 
         */
        private volatile HashMap<CacheKey, PersistenceProviderReference> providers = new HashMap<CacheKey, PersistenceProviderReference>();
        
        /**
         * Queue for reference objects referring to class loaders or persistence providers.
         */
        private static final ReferenceQueue referenceQueue = new ReferenceQueue();

        public List<PersistenceProvider> getPersistenceProviders() {
            // Before we do the real loading work, see whether we need to
            // do some cleanup: If references to class loaders or
            // persistence providers have been nulled out, remove all related
            // information from the cache.
            processQueue();
            
            ClassLoader loader = getContextClassLoader();
            CacheKey cacheKey = new CacheKey(loader);
            PersistenceProviderReference providersReferent = this.providers.get(cacheKey);
            List<PersistenceProvider> loadedProviders = null;
            
            if (providersReferent != null) {
                loadedProviders = providersReferent.get();
            }

            if (loadedProviders == null) {
                loadedProviders = new ArrayList<>();
                Iterator<PersistenceProvider> ipp = ServiceLoader.load(PersistenceProvider.class, loader).iterator();
                try {
                    while (ipp.hasNext()) {
                        try {
                            PersistenceProvider pp = ipp.next();
                            loadedProviders.add(pp);
                        } catch (ServiceConfigurationError sce) {
                            log(Level.FINEST, sce.toString());
                        }
                    }
                } catch (ServiceConfigurationError sce) {
                    log(Level.FINEST, sce.toString());
                }

                // If none are found we'll log the provider names for diagnostic
                // purposes.
                if (loadedProviders.isEmpty()) {
                    log(Level.WARNING, "No valid providers found.");
                }
                
                providersReferent = new PersistenceProviderReference(loadedProviders, referenceQueue, cacheKey);

                this.providers.put(cacheKey, providersReferent);
            }

            return loadedProviders;
        }
        
        /**
         * Remove garbage collected cache keys & providers.
         */
        private void processQueue() {
            CacheKeyReference ref;
            while ((ref = (CacheKeyReference) referenceQueue.poll()) != null) {
                providers.remove(ref.getCacheKey());
            }            
        }

        /**
         * Wraps {@code Thread.currentThread().getContextClassLoader()} into a
         * doPrivileged block if security manager is present
         */
        private static ClassLoader getContextClassLoader() {
            if (System.getSecurityManager() == null) {
                return Thread.currentThread().getContextClassLoader();
            } else {
                return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
                    public ClassLoader run() {
                        return Thread.currentThread().getContextClassLoader();
                    }
                });
            }
        }

        private static final String LOGGER_SUBSYSTEM = "jakarta.persistence.spi";

        private Logger logger;

        private void log(Level level, String message) {
            if (this.logger == null) {
                this.logger = Logger.getLogger(LOGGER_SUBSYSTEM);
            }
            this.logger.log(level, LOGGER_SUBSYSTEM + "::" + message);
        }

        /**
         * Clear all cached providers
         */
        public void clearCachedProviders() {
            this.providers.clear();
        }

        
        /**
         * The common interface to get a CacheKey implemented by
         * LoaderReference and PersistenceProviderReference.
         */
        private interface CacheKeyReference {
            CacheKey getCacheKey();
        }        
        
        /**
          * Key used for cached persistence providers. The key checks
          * the class loader to determine if the persistence providers
          * is a match to the requested one. The loader may be null.
          */
        private class CacheKey implements Cloneable {
            
            /* Weak Reference to ClassLoader */
            private LoaderReference loaderRef;
            
            /* Cached Hashcode */
            private int hashCodeCache;

            CacheKey(ClassLoader loader) {
                if (loader == null) {
                    this.loaderRef = null;
                } else {
                    loaderRef = new LoaderReference(loader, referenceQueue, this);
                }
                calculateHashCode();
            }

            ClassLoader getLoader() {
                return (loaderRef != null) ? loaderRef.get() : null;
            }

            public boolean equals(Object other) {
                if (this == other) {
                    return true;
                }
                try {
                    final CacheKey otherEntry = (CacheKey) other;
                    // quick check to see if they are not equal
                    if (hashCodeCache != otherEntry.hashCodeCache) {
                        return false;
                    }
                    // are refs (both non-null) or (both null)?
                    if (loaderRef == null) {
                        return otherEntry.loaderRef == null;
                    }
                    ClassLoader loader = loaderRef.get();
                    return (otherEntry.loaderRef != null)
                    // with a null reference we can no longer find
                    // out which class loader was referenced; so
                    // treat it as unequal
                    && (loader != null) && (loader == otherEntry.loaderRef.get());
                } catch (NullPointerException e) {
                } catch (ClassCastException e) {
                }

                return false;
            }

            public int hashCode() {
                return hashCodeCache;
            }

            private void calculateHashCode() {
                ClassLoader loader = getLoader();
                if (loader != null) {
                    hashCodeCache = loader.hashCode();
                }
            }

            public Object clone() {
                try {
                    CacheKey clone = (CacheKey) super.clone();
                    if (loaderRef != null) {
                        clone.loaderRef = new LoaderReference(loaderRef.get(), referenceQueue, clone);
                    }
                    return clone;
                } catch (CloneNotSupportedException e) {
                    // this should never happen
                    throw new InternalError();
                }
            }

            public String toString() {
                return "CacheKey[" + getLoader() + ")]";
            }
        }
       
       /**
         * References to class loaders are weak references, so that they can be
         * garbage collected when nobody else is using them. The DefaultPersistenceProviderResolver 
         * class has no reason to keep class loaders alive.
         */
        private class LoaderReference extends WeakReference<ClassLoader> 
                implements CacheKeyReference {
            private CacheKey cacheKey;

            @SuppressWarnings("unchecked")
            LoaderReference(ClassLoader referent, ReferenceQueue q, CacheKey key) {
                super(referent, q);
                cacheKey = key;
            }

            public CacheKey getCacheKey() {
                return cacheKey;
            }
        }

        /**
         * References to persistence provider are soft references so that they can be garbage
         * collected when they have no hard references.
         */
        private class PersistenceProviderReference extends SoftReference<List<PersistenceProvider>>
                implements CacheKeyReference {
            private CacheKey cacheKey;

            @SuppressWarnings("unchecked")
            PersistenceProviderReference(List<PersistenceProvider> referent, ReferenceQueue q, CacheKey key) {
                super(referent, q);
                cacheKey = key;
            }

            public CacheKey getCacheKey() {
                return cacheKey;
            }
        }
    }
}
