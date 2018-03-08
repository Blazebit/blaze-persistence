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

package com.blazebit.persistence.impl;

/**
 *
 * @author Moritz Becker
 * @since 1.0.0
 */
public class ExternalAliasDereferencingException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ExternalAliasDereferencingException() {
    }

    public ExternalAliasDereferencingException(String msg) {
        super(msg);
    }

    public ExternalAliasDereferencingException(Throwable t) {
        super(t);
    }

    public ExternalAliasDereferencingException(String msg, Throwable t) {
        super(msg, t);
    }
}
