/*
 * Copyright 2017-2020 Axway Software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
// ********RoostGPT********
/*
Test generated by RoostGPT for test axway-java-test using AI Type Azure Open AI and AI Model roostgpt-4-32k
ROOST_METHOD_HASH=getTablesWithDifferentNumberOfRows_b758c29416
ROOST_METHOD_SIG_HASH=getTablesWithDifferentNumberOfRows_f4d8628b7c
"""
  Scenario 1: Validate that an empty List is returned when no snapshot name is found in the map 'differentNumberOfRows'.
  Details:
    TestName: testEmptyListWhenSnapshotNotFound
    Description: This test will confirm that an empty list is returned when no match is found for the snapshot name in the map 'differentNumberOfRows'.

    Execution:
    Arrange: Set up 'differentNumberOfRows' to contain entries that do not match the snapshot name used in test.
    Act: Invoke getTablesWithDifferentNumberOfRows() with a snapshot name that does not exist in the map.
    Assert: Use JUnit assertions to validate if an empty list is returned.

    Validation:
    The absence of a snapshot in 'differentNumberOfRows' should result in the return of an empty list as there are no tables to add, and this test will validate the same.
  Scenario 2: Validate that correct list of tables is returned for a given snapshot name.
  Details:
    TestName: testCorrectTablesListReturned
    Description: This test will confirm that a list of tables corresponding to a given snapshot name is correctly returned.
    Execution:
    Arrange: Set up 'differentNumberOfRows' to contain multiple entries including the snapshot name used in the test.
    Act: Invoke getTablesWithDifferentNumberOfRows() with a snapshot name that matches entries in the map.
    Assert: Use JUnit assertions to validate the return list exactly matching the expected list of tables.
    Validation:
    If a snapshot name is found in 'differentNumberOfRows', all corresponding tables should be returned. This test ensures the method correctly returns all tables corresponding to a given snapshot name.
  Scenario 3: Validate that an empty list is returned for a null snapshot name.
  Details:
    TestName: testNullSnapshotName
    Description: This test will confirm that an empty list is returned when the snapshot name is null.
    Execution:
    Arrange: No arrangement is required as a null snapshot name will be used.
    Act: Invoke getTablesWithDifferentNumberOfRows() with a null name.
    Assert: Use JUnit assertions to validate if an empty list is returned.
    Validation:
    For a null snapshot name, an empty list should be returned as null cannot exist as a key in 'differentNumberOfRows' and this test will ensure the same is practiced.
"""
*/
// ********RoostGPT********
package com.axway.ats.common.dbaccess.snapshot.equality;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import java.util.TreeMap;
import com.axway.ats.common.PublicAtsApi;
import org.junit.jupiter.api.*;

@Tag("com.axway.ats.common.dbaccess.snapshot.equality")
@Tag("com.axway.ats.common.dbaccess.snapshot.equality.getTablesWithDifferentNumberOfRows")
class DatabaseEqualityStateGetTablesWithDifferentNumberOfRowsTest {

	private final String firstSnapshotName = "Snapshot1";

	private final String secondSnapshotName = "Snapshot2";

	private DatabaseEqualityState databaseEqualityState;

	@BeforeEach
	void setUp() {
		this.databaseEqualityState = new DatabaseEqualityState(firstSnapshotName, secondSnapshotName);
	}

	@Test
    @Tag('valid')
    void testEmptyListWhenSnapshotNotFound(){
        Map<String, Integer> snapshotMap = new HashMap<>();
        snapshotMap.put("Table1", 5);
        databaseEqualityState.getDifferentNumberOfRows().put(firstSnapshotName,snapshotMap);
        List<String> result = databaseEqualityState.getTablesWithDifferentNumberOfRows(secondSnapshotName);
        assertTrue(result.isEmpty());
    }

	@Test
	@Tag
	('valid')void testCorrectTablesListReturned() {
		List<String> expectedTables = Arrays.asList("Table1", "Table2");
		Map<String, Integer> snapshotMap = new HashMap<>();
		for (String tableName : expectedTables) {
			snapshotMap.put(tableName, 5);
		}
		databaseEqualityState.getDifferentNumberOfRows().put(firstSnapshotName, snapshotMap);
		List<String> result = databaseEqualityState.getTablesWithDifferentNumberOfRows(firstSnapshotName);
		assertEquals(expectedTables, result);
	}

	@Test
	@Tag
	('invalid')void testNullSnapshotName() {
		Map<String, Integer> snapshotMap = new HashMap<>();
		snapshotMap.put("Table1", 5);
		databaseEqualityState.getDifferentNumberOfRows().put(firstSnapshotName, snapshotMap);
		List<String> result = databaseEqualityState.getTablesWithDifferentNumberOfRows(null);
		assertTrue(result.isEmpty());
	}

}