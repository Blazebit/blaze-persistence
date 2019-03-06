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

@QueryEntities({
        IntIdEntity.class,
        LongSequenceEntity.class,
        Ownable.class,
        Version.class,
        Person.class,
        Document.class,
        IdHolderCTE.class,
        TestCTE.class,
        TestAdvancedCTE1.class,
        TestAdvancedCTE2.class,
        TestCTEEmbeddable.class,
        RecursiveEntity.class,
        NameObject.class,
        NameObjectContainer.class,
        NameObjectContainer2.class
})
package com.blazebit.persistence.querydsl;

import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.IdHolderCTE;
import com.blazebit.persistence.testsuite.entity.IntIdEntity;
import com.blazebit.persistence.testsuite.entity.LongSequenceEntity;
import com.blazebit.persistence.testsuite.entity.NameObject;
import com.blazebit.persistence.testsuite.entity.NameObjectContainer;
import com.blazebit.persistence.testsuite.entity.NameObjectContainer2;
import com.blazebit.persistence.testsuite.entity.Ownable;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.entity.RecursiveEntity;
import com.blazebit.persistence.testsuite.entity.TestAdvancedCTE1;
import com.blazebit.persistence.testsuite.entity.TestAdvancedCTE2;
import com.blazebit.persistence.testsuite.entity.TestCTE;
import com.blazebit.persistence.testsuite.entity.TestCTEEmbeddable;
import com.blazebit.persistence.testsuite.entity.Version;
import com.mysema.query.annotations.QueryEntities;
