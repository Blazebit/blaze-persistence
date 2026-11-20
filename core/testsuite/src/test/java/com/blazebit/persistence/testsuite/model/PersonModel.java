/*
 * Copyright 2014 - 2023 Blazebit.
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

package com.blazebit.persistence.testsuite.model;

/**
 * @author Christian Graefe
 * @since 1.6.9
 */
public class PersonModel
{

    private final String name;

    private final Long documentCount;

    public PersonModel(String name, Long documentCount)
    {
        this.name = name;
        this.documentCount = documentCount;
    }

    public PersonModel(Long documentCount, String name)
    {
        this.name = name;
        this.documentCount = documentCount;
    }

    public String getName()
    {
        return name;
    }

    public Long getDocumentCount()
    {
        return documentCount;
    }
}
