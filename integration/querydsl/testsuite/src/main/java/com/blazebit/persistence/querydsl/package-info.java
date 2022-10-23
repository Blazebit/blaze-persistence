/*
 * Copyright 2014 - 2022 Blazebit.
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

@QueryEntities(value = {
        com.blazebit.persistence.testsuite.entity.BlobEntity.class,
        com.blazebit.persistence.testsuite.entity.BookEntity.class,
        com.blazebit.persistence.testsuite.entity.BookISBNReferenceEntity.class,
        com.blazebit.persistence.testsuite.entity.DeletePersonCTE.class,
        com.blazebit.persistence.testsuite.entity.Document.class,
        com.blazebit.persistence.testsuite.entity.DocumentForEntityKeyMaps.class,
        com.blazebit.persistence.testsuite.entity.DocumentForOneToOne.class,
        com.blazebit.persistence.testsuite.entity.DocumentForOneToOneJoinTable.class,
        com.blazebit.persistence.testsuite.entity.DocumentForSimpleOneToOne.class,
        com.blazebit.persistence.testsuite.entity.DocumentInfo.class,
        com.blazebit.persistence.testsuite.entity.DocumentInfoSimple.class,
        com.blazebit.persistence.testsuite.entity.DocumentNodeCTE.class,
        com.blazebit.persistence.testsuite.entity.DocumentTupleEntity.class,
        com.blazebit.persistence.testsuite.entity.DocumentType.class,
        com.blazebit.persistence.testsuite.entity.DocumentWithNullableName.class,
        com.blazebit.persistence.testsuite.entity.EmbeddableTestEntity.class,
        com.blazebit.persistence.testsuite.entity.EmbeddableTestEntityContainer.class,
        com.blazebit.persistence.testsuite.entity.EmbeddableTestEntitySub.class,
        com.blazebit.persistence.testsuite.entity.EmbeddedDocumentTupleEntity.class,
        com.blazebit.persistence.testsuite.entity.EmbeddedDocumentTupleEntityId.class,
        com.blazebit.persistence.testsuite.entity.IdClassEntity.class,
        com.blazebit.persistence.testsuite.entity.IdHolderCTE.class,
        com.blazebit.persistence.testsuite.entity.IndexedNode.class,
        com.blazebit.persistence.testsuite.entity.IndexedNode2.class,
        com.blazebit.persistence.testsuite.entity.IntIdEntity.class,
        com.blazebit.persistence.testsuite.entity.JuniorProjectLeader.class,
        com.blazebit.persistence.testsuite.entity.KeyedNode.class,
        com.blazebit.persistence.testsuite.entity.KeyedNode2.class,
        com.blazebit.persistence.testsuite.entity.KeysetEntity.class,
        com.blazebit.persistence.testsuite.entity.KeysetEntity2.class,
        com.blazebit.persistence.testsuite.entity.LargeProject.class,
        com.blazebit.persistence.testsuite.entity.LocalizedEntity.class,
        com.blazebit.persistence.testsuite.entity.LongSequenceEntity.class,
        com.blazebit.persistence.testsuite.entity.NameObject.class,
        com.blazebit.persistence.testsuite.entity.NameObjectContainer.class,
        com.blazebit.persistence.testsuite.entity.NameObjectContainer2.class,
        com.blazebit.persistence.testsuite.entity.NaturalIdJoinTableEntity.class,
        com.blazebit.persistence.testsuite.entity.Order.class,
        com.blazebit.persistence.testsuite.entity.OrderPosition.class,
        com.blazebit.persistence.testsuite.entity.OrderPositionId.class,
        com.blazebit.persistence.testsuite.entity.OrderPositionElement.class,
        com.blazebit.persistence.testsuite.entity.OrderPositionHead.class,
        com.blazebit.persistence.testsuite.entity.IndexedEmbeddable.class,
        com.blazebit.persistence.testsuite.entity.KeyedEmbeddable.class,
        com.blazebit.persistence.testsuite.entity.EmbeddableTestEntityEmbeddable.class,
        com.blazebit.persistence.testsuite.entity.EmbeddedDocumentTupleEntity.class,
        com.blazebit.persistence.testsuite.entity.EmbeddableTestEntityNestedEmbeddable.class,
        com.blazebit.persistence.testsuite.entity.EmbeddableTestEntityId.class,
        com.blazebit.persistence.testsuite.entity.OrderPositionHeadId.class,
        com.blazebit.persistence.testsuite.entity.Ownable.class,
        com.blazebit.persistence.testsuite.entity.ParameterOrderCte.class,
        com.blazebit.persistence.testsuite.entity.ParameterOrderCteB.class,
        com.blazebit.persistence.testsuite.entity.ParameterOrderEntity.class,
        com.blazebit.persistence.testsuite.entity.Parent.class,
        com.blazebit.persistence.testsuite.entity.Person.class,
        com.blazebit.persistence.testsuite.entity.PersonCTE.class,
        com.blazebit.persistence.testsuite.entity.PersonForEntityKeyMaps.class,
        com.blazebit.persistence.testsuite.entity.PolymorphicBase.class,
        com.blazebit.persistence.testsuite.entity.PolymorphicBaseContainer.class,
        com.blazebit.persistence.testsuite.entity.PolymorphicPropertyBase.class,
        com.blazebit.persistence.testsuite.entity.PolymorphicPropertyMapBase.class,
        com.blazebit.persistence.testsuite.entity.PolymorphicPropertySub1.class,
        com.blazebit.persistence.testsuite.entity.PolymorphicPropertySub2.class,
        com.blazebit.persistence.testsuite.entity.PolymorphicSub1.class,
        com.blazebit.persistence.testsuite.entity.PolymorphicSub2.class,
        com.blazebit.persistence.testsuite.entity.PrimitiveDocument.class,
        com.blazebit.persistence.testsuite.entity.PrimitivePerson.class,
        com.blazebit.persistence.testsuite.entity.PrimitiveVersion.class,
        com.blazebit.persistence.testsuite.entity.Project.class,
        com.blazebit.persistence.testsuite.entity.ProjectLeader.class,
        // TODO doesn't compile in QueryDSL, file a bug there...
//        com.blazebit.persistence.testsuite.entity.RawTypeEntity.class,
        com.blazebit.persistence.testsuite.entity.RecursiveEntity.class,
        com.blazebit.persistence.testsuite.entity.Root.class,
        com.blazebit.persistence.testsuite.entity.Root2.class,
        com.blazebit.persistence.testsuite.entity.SchemaEntity.class,
        com.blazebit.persistence.testsuite.entity.SecondaryTableEntityBase.class,
        com.blazebit.persistence.testsuite.entity.SecondaryTableEntitySub.class,
        com.blazebit.persistence.testsuite.entity.SeniorProjectLeader.class,
        com.blazebit.persistence.testsuite.entity.SequenceBaseEntity.class,
        com.blazebit.persistence.testsuite.entity.SmallProject.class,
        com.blazebit.persistence.testsuite.entity.StringIdCTE.class,
        com.blazebit.persistence.testsuite.entity.Sub1.class,
        com.blazebit.persistence.testsuite.entity.Sub1Sub1.class,
        com.blazebit.persistence.testsuite.entity.Sub1Sub2.class,
        com.blazebit.persistence.testsuite.entity.Sub2.class,
        com.blazebit.persistence.testsuite.entity.Sub2Sub1.class,
        com.blazebit.persistence.testsuite.entity.Sub2Sub2.class,
        com.blazebit.persistence.testsuite.entity.TestAdvancedCTE1.class,
        com.blazebit.persistence.testsuite.entity.TestAdvancedCTE2.class,
        com.blazebit.persistence.testsuite.entity.TestCTE.class,
        com.blazebit.persistence.testsuite.entity.TestCTEEmbeddable.class,
        com.blazebit.persistence.testsuite.entity.TPCBase.class,
        com.blazebit.persistence.testsuite.entity.TPCSub1.class,
        com.blazebit.persistence.testsuite.entity.TPCSub2.class,
        com.blazebit.persistence.testsuite.entity.Version.class,
        com.blazebit.persistence.testsuite.entity.Workflow.class
})
package com.blazebit.persistence.querydsl;

import com.querydsl.core.annotations.QueryEntities;