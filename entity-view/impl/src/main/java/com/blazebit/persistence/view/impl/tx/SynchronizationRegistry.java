/*
 * Copyright 2014 - 2019 Blazebit.
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

package com.blazebit.persistence.view.impl.tx;

import javax.transaction.Status;
import javax.transaction.Synchronization;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * The main purpose of a custom registry is to invoke synchronizations in reverse order when rolling back.
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
public class SynchronizationRegistry implements Synchronization {

    private static final ThreadLocal<Object> THREAD_LOCAL_KEY = new ThreadLocal<Object>() {
        @Override
        protected Object initialValue() {
            return new Object();
        }
    };
    private static final ConcurrentMap<Object, SynchronizationRegistry> REGISTRY = new ConcurrentHashMap<>();
    private final List<Synchronization> synchronizations;
    private final Object key;

    public SynchronizationRegistry() {
        this(THREAD_LOCAL_KEY.get());
    }

    public SynchronizationRegistry(Object key) {
        this.synchronizations = new ArrayList<>(1);
        this.key = key;
        REGISTRY.put(key, this);
    }

    public static SynchronizationRegistry getRegistry(Object key) {
        return REGISTRY.get(key);
    }

    public void addSynchronization(Synchronization synchronization) {
        synchronizations.add(synchronization);
    }

    @Override
    public void beforeCompletion() {
        List<Exception> suppressedExceptions = null;
        for (int i = 0; i < synchronizations.size(); i++) {
            Synchronization synchronization = synchronizations.get(i);
            try {
                synchronization.beforeCompletion();
            } catch (Exception ex) {
                if (suppressedExceptions == null) {
                    suppressedExceptions = new ArrayList<>();
                }
                suppressedExceptions.add(ex);
            }
        }
        if (suppressedExceptions != null) {
            if (suppressedExceptions.size() == 1) {
                if (suppressedExceptions.get(0) instanceof RuntimeException) {
                    throw (RuntimeException) suppressedExceptions.get(0);
                }
                throw new RuntimeException("Error during beforeCompletion invocation of synchronizations", suppressedExceptions.get(0));
            }
            RuntimeException runtimeException = new RuntimeException("Error during beforeCompletion invocation of synchronizations");
            for (Exception supressedException : suppressedExceptions) {
                runtimeException.addSuppressed(supressedException);
            }
            throw runtimeException;
        }
    }

    @Override
    public void afterCompletion(int status) {
        THREAD_LOCAL_KEY.remove();
        List<Exception> suppressedExceptions = null;
        switch (status) {
            // We don't care about these statuses, only about committed and rolled back
            case Status.STATUS_ACTIVE:
            case Status.STATUS_COMMITTING:
            case Status.STATUS_MARKED_ROLLBACK:
            case Status.STATUS_NO_TRANSACTION:
            case Status.STATUS_PREPARED:
            case Status.STATUS_PREPARING:
                break;
            case Status.STATUS_COMMITTED:
                REGISTRY.remove(key);
                for (int i = 0; i < synchronizations.size(); i++) {
                    Synchronization synchronization = synchronizations.get(i);
                    try {
                        synchronization.afterCompletion(status);
                    } catch (Exception ex) {
                        if (suppressedExceptions == null) {
                            suppressedExceptions = new ArrayList<>();
                        }
                        suppressedExceptions.add(ex);
                    }
                }
                break;
            case Status.STATUS_ROLLING_BACK:
            case Status.STATUS_ROLLEDBACK:
            // We assume unknown means rolled back as Hibernate behaves this way with a local transaction coordinator
            case Status.STATUS_UNKNOWN:
            default:
                if (REGISTRY.remove(key) != null) {
                    for (int i = synchronizations.size() - 1; i >= 0; i--) {
                        Synchronization synchronization = synchronizations.get(i);
                        try {
                            synchronization.afterCompletion(status);
                        } catch (Exception ex) {
                            if (suppressedExceptions == null) {
                                suppressedExceptions = new ArrayList<>();
                            }
                            suppressedExceptions.add(ex);
                        }
                    }
                }
                break;
        }

        if (suppressedExceptions != null) {
            if (suppressedExceptions.size() == 1) {
                if (suppressedExceptions.get(0) instanceof RuntimeException) {
                    throw (RuntimeException) suppressedExceptions.get(0);
                }
                throw new RuntimeException("Error during afterCompletion invocation of synchronizations", suppressedExceptions.get(0));
            }
            RuntimeException runtimeException = new RuntimeException("Error during afterCompletion invocation of synchronizations");
            for (Exception supressedException : suppressedExceptions) {
                runtimeException.addSuppressed(supressedException);
            }
            throw runtimeException;
        }
    }
}
