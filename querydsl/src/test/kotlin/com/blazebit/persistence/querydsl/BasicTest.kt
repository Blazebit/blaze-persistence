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

package com.blazebit.persistence.querydsl

import com.blazebit.persistence.testsuite.AbstractCoreTest
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink
import com.blazebit.persistence.testsuite.base.jpa.category.NoMySQL
import com.blazebit.persistence.testsuite.base.jpa.category.NoOpenJPA
import com.blazebit.persistence.testsuite.entity.*
import com.blazebit.persistence.testsuite.entity.QPerson.person
import com.blazebit.persistence.testsuite.entity.QDocument.document
import com.blazebit.persistence.testsuite.entity.QTestCTE.testCTE
import com.blazebit.persistence.testsuite.entity.QRecursiveEntity.recursiveEntity
import com.blazebit.persistence.testsuite.entity.QNameObject.nameObject
import com.blazebit.persistence.testsuite.entity.QNameObjectContainer.nameObjectContainer
import com.mysema.query.types.ConstantImpl
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.experimental.categories.Category
import javax.persistence.Tuple

class BasicTest : AbstractCoreTest() {

    @Test
    fun stringOperators() {
        cbf.create(em, Tuple::class)
                .from(person)
                .select(person.name + " " + person.name)
                .resultList
    }


    @Test
    fun stringFunctions() {
        cbf.create(em, Tuple::class)
                .from(person)
                .select(person.name
                        .trim()
                        .lpad(2, "test")
                        .rpad(2, "test")
                        .ltrim()
                        .rtrim()
                        .upper().lower().substring(person.nameObject.intIdEntity.id))
                .resultList
    }

    @Test
    fun basicTest() {
        cbf.create(em, Tuple::class)
                .from(Document::class.java)
                .whereOr()
                    .where(document.name).betweenExpression(ConstantImpl.create("test")).andExpression(ConstantImpl.create("test"))
                    .where(document.name).eqExpression(ConstantImpl.create("test"))
                .endOr()
                .resultList
    }

    @Test
    fun dateExtractionFunctions() {
        cbf.create(em, Tuple::class)
                .from(document)
                .select(document.lastModified.dayOfMonth())
                .select(document.lastModified.dayOfWeek())
                .select(document.lastModified.dayOfYear())
                .select(document.lastModified.year())
                .select(document.lastModified.month())
                .select(document.lastModified.week())
                .select(document.lastModified.hour())
                .select(document.lastModified.minute())
                .select(document.lastModified.second())
                .resultList
    }

    @Test
    fun greatestFunction() {
        cbf.create(em, Tuple::class)
                .from(person)
                .select(person.age.greatest(1L).round())
                .resultList
    }

    @Test
    fun simplePathPredicate() {
        cbf.create(em, person)
            .where(person.name.eq("test").and(person.name.ne("bleh").or(person.name.eq("test").not())))
            .resultList
    }

    @Test
    fun startsWithPathPredicate() {
        cbf.create(em, person)
                .where(person.name.startsWith("test"))
                .resultList
    }

    @Test
    fun aggregateWithImplicitGroupBy() {
        val favoriteDocument = QDocument("favorite")

        cbf.create(em, Tuple::class)
                .from(person)
                .leftJoin(person.favoriteDocuments, favoriteDocument)
                .select(person.name)
                .select(favoriteDocument.count())
                .resultList
    }


    @Test
    fun aggregateWithExplicitGroupBy() {
        val favoriteDocument = QDocument("favorite")

        cbf.create(em, Tuple::class)
                .from(person)
                .leftJoin(person.favoriteDocuments, favoriteDocument)
                .select(person.name)
                .select(favoriteDocument.count())
                .groupBy(person.name, person.id)
                .resultList
    }

    @Test
    fun aggregateWithHavingClause() {
        val favoriteDocument = QDocument("favorite")

        cbf.create(em, Tuple::class)
                .from(person)
                .leftJoin(person.favoriteDocuments, favoriteDocument)
                .select(person.name)
                .groupBy(person.name)
                .having(favoriteDocument.count().gt(1))
                .resultList
    }

    @Test
    fun innerJoinCollection() {
        val ownedDocument = QDocument("ownedDocument")

        cbf.create(em, Tuple::class)
                .from(person)
                .innerJoin(person.ownedDocuments, ownedDocument)
                .select(person)
                .select(ownedDocument)
                .resultList
    }

    @Test
    fun implicitJoinAssociation() {
        cbf.create(em, Tuple::class)
                .from(person)
                .select(person)
                .select(person.friend)
                .resultList
    }

    @Test
    fun innerJoinAssociation() {
        val friend = QPerson("friend")

        cbf.create(em, Tuple::class)
                .from(person)
                .innerJoin(person.friend, friend)
                .select(person)
                .select(friend)
                .resultList
    }

    @Test
    fun innerJoinAssociationOn() {
        val friend = QPerson("friend")

        cbf.create(em, Tuple::class)
                .from(person)
                .innerJoinOn(person.friend, friend)
                    .on(friend.name.eq("Fred"))
                .select(person)
                .select(friend)
                .resultList
    }

    @Test
    fun innerJoinAssociationOnStub() {
        val friend = QPerson("friend")

        cbf.create(em, Tuple::class)
                .from(person)
                .innerJoinOn(person.friend, friend)
                    .on(friend.name).eqExpression(ConstantImpl.create("test")).end()
                .select(person)
                .select(friend)
                .resultList
    }


    @Test
    fun innerJoinCollectionOn() {
        val ownedDocument = QDocument("ownedDocument")

        cbf.create(em, Tuple::class)
                .from(person)
                .innerJoinOn(person.ownedDocuments, ownedDocument)
                    .on(ownedDocument.archived.isFalse)
                .select(person)
                .select(ownedDocument)
                .resultList
    }

    @Test
    fun innerJoinRoot() {
        cbf.create(em, Tuple::class)
                .from(person)
                .innerJoin(document)
                .select(person)
                .select(document)
                .resultList
    }

    @Test
    fun innerJoinRootOn() {
        cbf.create(em, Tuple::class)
                .from(person)
                .innerJoinOn(document)
                    .on(document.owner.eq(person))
                .select(person)
                .select(document)
                .resultList
    }

    @Test
    fun leftJoinCollection() {
        val ownedDocument = QDocument("ownedDocument")

        cbf.create(em, Tuple::class)
                .from(person)
                .leftJoin(person.ownedDocuments, ownedDocument)
                .select(person)
                .select(ownedDocument)
                .resultList
    }

    @Test
    fun leftJoinAssociation() {
        val friend = QPerson("friend")

        cbf.create(em, Tuple::class)
                .from(person)
                .leftJoin(person.friend, friend)
                .select(person)
                .select(friend)
                .resultList
    }


    @Test
    fun leftJoinAssociationOn() {
        val friend = QPerson("friend")

        cbf.create(em, Tuple::class)
                .from(person)
                .leftJoinOn(person.friend, friend)
                    .on(friend.name.eq("Fred"))
                .select(person)
                .select(friend)
                .resultList
    }

    @Test
    fun leftJoinCollectionOn() {
        val ownedDocument = QDocument("ownedDocument")

        cbf.create(em, Tuple::class)
                .from(person)
                .leftJoinOn(person.ownedDocuments, ownedDocument)
                .on(ownedDocument.archived.isFalse)
                .select(person)
                .select(ownedDocument)
                .resultList
    }


    @Test
    fun leftJoinRoot() {
        cbf.create(em, Tuple::class)
                .from(person)
                .leftJoin(document)
                .select(person)
                .select(document)
                .resultList
    }

    @Test
    fun leftJoinRootOn() {
        cbf.create(em, Tuple::class)
                .from(person)
                .leftJoinOn(document)
                .on(document.owner.eq(person))
                .select(person)
                .select(document)
                .resultList
    }


    @Test
    fun rightJoinCollection() {
        val ownedDocument = QDocument("ownedDocument")

        cbf.create(em, Tuple::class)
                .from(person)
                .rightJoin(person.ownedDocuments, ownedDocument)
                .select(person)
                .select(ownedDocument)
                .resultList
    }

    @Test
    fun rightJoinCollectionOn() {
        val ownedDocument = QDocument("ownedDocument")

        cbf.create(em, Tuple::class)
                .from(person)
                .rightJoinOn(person.ownedDocuments, ownedDocument)
                .on(ownedDocument.archived.isFalse)
                .select(person)
                .select(ownedDocument)
                .resultList
    }

    @Test
    fun rightJoinAssociation() {
        val friend = QPerson("friend")

        cbf.create(em, Tuple::class)
                .from(person)
                .rightJoin(person.friend, friend)
                .select(person)
                .select(friend)
                .resultList
    }

    @Test
    fun rightJoinAssociationOn() {
        val friend = QPerson("friend")

        cbf.create(em, Tuple::class)
                .from(person)
                .rightJoinOn(person.friend, friend)
                    .on(friend.name.eq("Fred"))
                .select(person)
                .select(friend)
                .resultList
    }

    @Test
    fun rightJoinRoot() {
        cbf.create(em, Tuple::class)
                .from(person)
                .rightJoin(document)
                .select(person)
                .select(document)
                .resultList
    }

    @Test
    fun rightJoinRootOn() {
        cbf.create(em, Tuple::class)
                .from(person)
                .rightJoinOn(document)
                .on(document.owner.eq(person))
                .select(person)
                .select(document)
                .resultList
    }

    @Test
    @Category(NoDatanucleus::class, NoEclipselink::class, NoOpenJPA::class, NoMySQL::class)
    fun testCTE() {
        val cb = cbf.create(em, testCTE)
                .with(testCTE)
                .from(recursiveEntity)
                    .where(recursiveEntity.parent.isNull)
                    .bind(testCTE.id).select(recursiveEntity.id)
                    .bind(testCTE.name).select(recursiveEntity.name)
                    .bind(testCTE.level).select("0")
                .end()
                .where(testCTE.level.lt(2))

        cb.resultList
    }

    @Test
    @Category(NoDatanucleus::class, NoEclipselink::class, NoOpenJPA::class)
    fun testValuesEntityFunction() {
        val collection : Collection<NameObject> = listOf(NameObject("abc", "bcd"))

        val doc = QDocument("doc")
        val embeddable = QNameObject("embeddable")

        val cb = cbf.create(em, Tuple::class)
                .fromValues(embeddable, collection)
                .from(doc)
                .where(doc.nameObject.primaryName.eq(embeddable.secondaryName)
                        .and(doc.nameObject.secondaryName.eq(embeddable.primaryName)))
                .select(doc.name)
                .select(embeddable)

        val resultList = cb.resultList

        val expected = "SELECT doc.name, embeddable FROM Document doc, NameObject(1 VALUES) embeddable" +
                " WHERE doc.nameObject.primaryName = embeddable.secondaryName AND doc.nameObject.secondaryName = embeddable.primaryName"

        assertEquals(expected, cb.queryString)
    }

    override fun getEntityClasses(): Array<Class<*>> {
        return arrayOf(
            IntIdEntity::class.java,
            LongSequenceEntity::class.java,
            Ownable::class.java,
            Version::class.java,
            Person::class.java,
            Document::class.java,
            TestCTE::class.java,
            TestAdvancedCTE1::class.java,
            TestAdvancedCTE2::class.java,
            TestCTEEmbeddable::class.java,
            RecursiveEntity::class.java,
            NameObject::class.java,
            NameObjectContainer::class.java,
            NameObjectContainer2::class.java
        )
    }
}