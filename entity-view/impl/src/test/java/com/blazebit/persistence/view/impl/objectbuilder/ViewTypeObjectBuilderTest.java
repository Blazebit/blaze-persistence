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
package com.blazebit.persistence.view.impl.objectbuilder;

import java.util.TreeSet;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Christian Beikov
 * @since 1.6.11
 */
public class ViewTypeObjectBuilderTest {

	// Test for #1832
	@Test
	public void hasSubFetches() {
		TreeSet<String> fetches = new TreeSet<>();
		fetches.add( "id" );
		fetches.add( "bbbbbb" );
		fetches.add( "a.name" );
		assertTrue(ViewTypeObjectBuilder.hasSubFetches(fetches, "id"));
		assertTrue(ViewTypeObjectBuilder.hasSubFetches(fetches, "bbbbbb"));
		assertFalse(ViewTypeObjectBuilder.hasSubFetches(fetches, "aaa.id"));
		assertFalse(ViewTypeObjectBuilder.hasSubFetches(fetches, "ccc.id"));
		assertFalse(ViewTypeObjectBuilder.hasSubFetches(fetches, "bbbbb"));
		assertFalse(ViewTypeObjectBuilder.hasSubFetches(fetches, "bbbbbbb"));
		assertTrue(ViewTypeObjectBuilder.hasSubFetches(fetches, "a"));
		assertTrue(ViewTypeObjectBuilder.hasSubFetches(fetches, "a.name"));
		assertFalse(ViewTypeObjectBuilder.hasSubFetches(fetches, "a.id"));
	}
}
