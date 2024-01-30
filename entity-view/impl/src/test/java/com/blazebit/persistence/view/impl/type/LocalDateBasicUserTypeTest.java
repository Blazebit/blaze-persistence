/*
 * Copyright 2014 - 2024 Blazebit.
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

package com.blazebit.persistence.view.impl.type;

import org.junit.Test;
import java.time.LocalDate;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Raja Kolli
 * @since 1.6.12
 */
public class LocalDateBasicUserTypeTest {

    @Test
    public void testFromString() {
        LocalDateBasicUserType userType = new LocalDateBasicUserType();
        CharSequence sequence = "2024-01-30";

        // Expected local date
        LocalDate expectedDate = LocalDate.of(2024, 1,30);

        // Call the method under test
        LocalDate result = userType.fromString(sequence);

        // Assert the result
        assertEquals(expectedDate, result, "Converted LocalDate should match expected LocalDate");    
  
    }
  
}
