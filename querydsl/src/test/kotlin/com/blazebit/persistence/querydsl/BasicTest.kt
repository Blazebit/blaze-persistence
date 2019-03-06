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
import com.blazebit.persistence.testsuite.base.jpa.category.*
import com.blazebit.persistence.testsuite.entity.*
import com.blazebit.persistence.testsuite.entity.QPerson.person
import com.blazebit.persistence.testsuite.entity.QDocument.document
import com.blazebit.persistence.testsuite.entity.QTestCTE.testCTE
import com.blazebit.persistence.testsuite.entity.QRecursiveEntity.recursiveEntity
import com.blazebit.persistence.testsuite.entity.QIdHolderCTE.idHolderCTE
import com.mysema.query.jpa.impl.JPAQuery
import com.mysema.query.types.ConstantImpl
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.experimental.categories.Category
import javax.persistence.Tuple

class BasicTest : AbstractCoreTest() {

    @Test
    fun basicTest() {
        cbf.create(em, Tuple::class)
                .from(person)
                .where(person.name.eq("alex"))
                .select(person)
                .resultList
    }

    @Test
    fun stringOperators() {
        cbf.create(em, Tuple::class)
                .from(person)
                .where(! person.name.eq("alex"))
                .select(person.name.concat(" ") + " " + person.name)
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
    fun criteriaBuilderExtensions() {
        cbf.create(em, Tuple::class)
                .from(Document::class.java)
                .whereOr()
                    .where(document.name).betweenExpression(ConstantImpl.create("test")).andExpression(ConstantImpl.create("test"))
                    .where(document.name).notBetweenExpression(ConstantImpl.create("test")).andExpression(ConstantImpl.create("test"))
                    .where(document.name).eqExpression(ConstantImpl.create("test"))
                    .where(document.name).notEqExpression(ConstantImpl.create("test"))
                    .where(document.name).eq().expression(ConstantImpl.create("test"))
                .endOr()
                .resultList
    }

    @Test
    fun innerJoinFetchTest() {
        cbf.create(em, person)
                .innerJoinFetch(person.favoriteDocuments, document)
                .fetch(person.friend)
                .resultList
    }

    @Test
    fun leftJoinFetchTest() {
        cbf.create(em, person)
                .leftJoinFetch(person.favoriteDocuments, document)
                .fetch(person.friend)
                .resultList
    }


    @Test
    fun rightJoinFetchTest() {
        cbf.create(em, person)
                .rightJoinFetch(person.favoriteDocuments, document)
                .fetch(person.friend)
                .resultList
    }


    @Test
    fun basicCaseWhenThen() {
        cbf.create(em, Tuple::class)
                .from(document)
                .selectCase()
                .`when`(document.name).eq("A").then(1)
                .`when`(document.name).eq("B").then(2)
                .whenOr()
                .or(document.name).eq("B")
        cbf.create(em, Tuple::class)
                .from(document)
                .selectCase()
                    .`when`(document.name).eq("A").then(1)
                    .`when`(document.name).eq("B").then(2)
                    .whenOr()
                        .or(document.name).eq("B")
                        .or(document.name).eq("B")
                        .then(3)
                    .otherwise(3)
                .selectCase()
                    .whenAnd()
                        .and(document.name).eq("B")
                        .and(document.name).eq("B")
                        .or()
                            .or(document.name).eq("B")
                            .and()
                                .and(document.name).eq("B").endAnd().endOr()
                        .then(3)
                    .otherwiseExpression(document.age)
                .selectSimpleCase(document.name)
                    .`when`(document.name.lower(), "LOWER")
                    .`when`(document.name.upper(), "UPPER")
                    .otherwiseValue("MIXED")
                .whereSimpleCase(document.name)
                    .`when`(document.name.lower(), "LOWER")
                    .`when`(document.name.upper(), "UPPER")
                    .otherwiseValue("MIXED")
                    .eq("BERT")
                .resultList
    }


    @Test
    fun dateExtractionFunctions() {
        cbf.create(em, Tuple::class)
                .from(document)
                .select(document.lastModified.dayOfMonth())
//                .select(document.lastModified.dayOfWeek())
//                .select(document.lastModified.dayOfYear())
                .select(document.lastModified.year())
                .select(document.lastModified.month())
//                .select(document.lastModified.week())
                .select(document.lastModified.hour(), "hour")
                .select(document.lastModified.minute())
                .select(document.lastModified.second())
                .resultList
    }

    @Test
    fun greatestFunction() {
        cbf.create(em, Tuple::class)
                .from(person)
                .select(person.age.greatest(1L).round())
                .select(person.age.least(1L).round())
                .select(person.age.greatest(person.age * -1).round(2))
                .select(person.age.least(person.age * -1).round(2))
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
                .select(person.nameObject.intIdEntity.id.sum())
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
                .having(person.id.gt(1))
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
                .orderBy(person.name.asc().nullsLast(), person.age.desc())
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
    fun innerJoinMap() {
        cbf.create(em, Tuple::class)
                .from(document)
                .innerJoin(document.contacts, person)
                .select(document)
                .select(person)
                .resultList
    }

    @Test
    fun innerJoinMapOn() {
        cbf.create(em, Tuple::class)
                .from(document)
                .innerJoinOn(document.contacts, person)
                    .on(person.age.gt(1))
                .select(document)
                .select(person)
                .resultList
    }

    @Test
    fun innerFetchJoinMap() {
        cbf.create(em, Tuple::class)
                .from(document)
                .innerJoinFetch(document.contacts, person)
                .select(document)
                .select(person)
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
    fun leftJoinMap() {
        cbf.create(em, Tuple::class)
                .from(document)
                .leftJoin(document.contacts, person)
                .select(document)
                .select(person)
                .resultList
    }

    @Test
    fun leftJoinMapOn() {
        cbf.create(em, Tuple::class)
                .from(document)
                .leftJoinOn(document.contacts, person)
                    .on(person.age.gt(0))
                .select(document)
                .select(person)
                .resultList
    }

    @Test
    fun leftFetchJoinMap() {
        cbf.create(em, Tuple::class)
                .from(document)
                .leftJoinFetch(document.contacts, person)
                .select(document)
                .select(person)
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
    fun rightJoinMap() {
        cbf.create(em, Tuple::class)
                .from(document)
                .rightJoin(document.contacts, person)
                .select(document)
                .select(person)
                .resultList
    }

    @Test
    fun rightJoinMapOn() {
        cbf.create(em, Tuple::class)
                .from(document)
                .rightJoinOn(document.contacts, person)
                    .on(person.age.gt(0))
                .select(document)
                .select(person)
                .resultList
    }

    @Test
    fun rightFetchJoinMap() {
        cbf.create(em, Tuple::class)
                .from(document)
                .rightJoinFetch(document.contacts, person)
                .select(document)
                .select(person)
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

    @Test
    fun testUpdate() {
        val resultList = cbf.update(em, person)
                .set(person.name, "Frank")
                .setExpression(person.age, person.age + 1)
                .where(person.name.like("Billy"))
                .executeUpdate()
    }

    @Test
    fun testDelete() {
        val resultList = cbf.delete(em, person)
                .where(person.name.like("Billy"))
                .executeUpdate()
    }

    @Test
    // NOTE: H2 and MySQL only support returning generated keys
    @Category(NoH2::class, NoMySQL::class, NoDatanucleus::class, NoEclipselink::class, NoOpenJPA::class)
    fun testReturningDelete() {
        val resultList = cbf.create(em, TestCTE::class.java)
                .withReturning(testCTE)
                    .delete(person)
                    .where(person.name).eq("Bert")
                    .returning(testCTE.id, person.id)
                    .returning(testCTE.name, person.name)
                    .returning(testCTE.level, person.nameObject.intIdEntity.id)
                .end()
                .resultList
    }


    // NOTE: H2 does not seem to support set operations in CTEs properly
    @Test
    @Category(NoH2::class, NoMySQL::class, NoFirebird::class, NoDatanucleus::class, NoEclipselink::class, NoOpenJPA::class)
    fun testCTELeftNesting() {
        val simpleExpression = document.`as`("test")


        val d = QDocument("d")
        val d1 = QDocument("d1")
        val d2 = QDocument("d2")
        val d3 = QDocument("d3")
        val d4 = QDocument("d4")
        val d5 = QDocument("d5")
        val d6 = QDocument("d6")
        val idHolder = QIdHolderCTE("idHolder")

        val cb = cbf.create(em, Document::class)
                .withStartSet(idHolderCTE)
                .startSet()
                    .from(d1)
                    .bind(idHolderCTE.id).select("d1.id")
                    .where(d1.name).eq("D1")
                .except()
                    .from(d2)
                    .bind(idHolderCTE.id).select("d2.id")
                    .where(d2.name).eq("D2")
                .endSet()
                    .startExcept()
                        .from(d3)
                        .bind(idHolderCTE.id).select("d3.id")
                        .where(d3.name).eq("D3")
                    .union()
                        .from(d4)
                        .bind(idHolderCTE.id).select("d4.id")
                        .where(d4.name).eq("D4")
                    .endSet()
                .endSet()
                .startExcept()
                    .startSet()
                        .from(d5)
                        .bind(idHolderCTE.id).select("d5.id")
                        .where(d5.name).eq("D5")
                    .union()
                        .from(d6)
                        .bind(idHolderCTE.id).select("d6.id")
                        .where(d6.name).eq("D6")
                    .endSet()
                    .endSet()
                .endSet()
                .end()
                .from(d)
                .from(idHolder)
                .select(d)
                .where(d.id).eqExpression(idHolder.id)
        val expected = (""
                + "WITH IdHolderCTE(id) AS(\n" +
                "((SELECT d1.id FROM Document d1 WHERE d1.name = :param_0\n"
                + "EXCEPT\n"
                + "SELECT d2.id FROM Document d2 WHERE d2.name = :param_1)\n"
                + "EXCEPT\n"
                + "(SELECT d3.id FROM Document d3 WHERE d3.name = :param_2\n"
                + "UNION\n"
                + "SELECT d4.id FROM Document d4 WHERE d4.name = :param_3))\n"
                + "EXCEPT\n"
                + "((SELECT d5.id FROM Document d5 WHERE d5.name = :param_4\n"
                + "UNION\n"
                + "SELECT d6.id FROM Document d6 WHERE d6.name = :param_5))\n"
                + ")\n"
                + "SELECT d FROM Document d, IdHolderCTE idHolder WHERE d.id = idHolder.id")
        assertEquals(expected, cb.queryString)
        val resultList = cb.resultList
        assertEquals(1, resultList.size.toLong())
        assertEquals("D1", resultList[0].name)
    }


    override fun getEntityClasses(): Array<Class<*>> {
        return arrayOf(
            IdHolderCTE::class.java,
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