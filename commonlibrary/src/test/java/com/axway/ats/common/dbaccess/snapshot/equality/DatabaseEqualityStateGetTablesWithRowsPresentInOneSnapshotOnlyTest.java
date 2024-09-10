// ********RoostGPT********
/*
Test generated by RoostGPT for test axway-java-test using AI Type  and AI Model

ROOST_METHOD_HASH=getTablesWithRowsPresentInOneSnapshotOnly_a140bcad39
ROOST_METHOD_SIG_HASH=getTablesWithRowsPresentInOneSnapshotOnly_fe2c8cdc18

"""
Scenario 1: Test to verify if the method returns the expected list of tables with unique rows for a given snapshot
Details:
  TestName: getTablesWithRowsPresentInOneSnapshotOnly
  Description: The test aims to confirm that the method correctly fetches and returns a list of tables with unique rows that are present in one snapshot only.
Execution:
  Arrange: Setup the DatabaseEqualityState entity with data. Generate snapshot named 'snapshot1'.
  Act: Invoke the method getTablesWithRowsPresentInOneSnapshotOnly() with 'snapshot1' as the parameter.
  Assert: Compare the returned list of tables against the expected outcomes.
Validation:
  The assert statement strives to verify that the returned list of tables matches the expected list of tables.
  This test scenario is essential to assure that the unique rows are tied to the correct snapshot and returned correctly by the method.

Scenario 2: Test to validate empty list return when snapshot is not found
Details:
  TestName: validateEmptyReturnForUnknownSnapshot
  Description: The test intends to validate that the method handles instances when the provided snapshot does not exist.
Execution:
  Arrange: Setup the DatabaseEqualityState entity with data. Do not generate a snapshot named 'snapshotUnknown'.
  Act: Invoke the method getTablesWithRowsPresentInOneSnapshotOnly() with 'snapshotUnknown' as the parameter.
  Assert: Ensure that an empty list is returned.
Validation:
  The assert statement targets to confirm that when a snapshot is not found an empty list is returned.
  This test scenario examines if the method accurately handles and responds to instances where a snapshot is not present.

Scenario 3: Test exception thrown when a null snapshot is passed
Details:
  TestName: validateNullExceptionForNullSnapshot
  Description: This test checks whether the method throws an expected exception when a null snapshot is passed as an argument.
Execution:
  Arrange: Do not arrange any setup data.
  Act: Invoke the method getTablesWithRowsPresentInOneSnapshotOnly() with null as the parameter.
  Assert: Check whether a specific exception (like IllegalArgumentException or NullPointerException) is thrown.
Validation:
  The assert statement aims to verify that when a null snapshot is passed as an argument, an exception is thrown.
  This test scenario validates if the method correctly handles null input parameters.

Scenario 4: Test to validate function when called multiple times
Details:
  TestName: validateFunctionForMultipleCalls
  Description: The test intends to validate that the method works accurately even when called consecutively multiple times.
Execution:
  Arrange: Setup the DatabaseEqualityState entity with data. Generate snapshots named 'snapshot1', 'snapshot2', and 'snapshot3'.
  Act: Invoke the method getTablesWithRowsPresentInOneSnapshotOnly() consecutively with 'snapshot1', 'snapshot2', and 'snapshot3' as parameters.
  Assert: Compare the returned lists of tables for each snapshot against the expected outcomes.
Validation:
  The assert statements aim to ensure that the method can accurately extract the tables with unique rows for different snapshots even when called multiple times and deliver the correct results.
  This test scenario is crucial to ensure that the implementation of the method can handle and maintain its consistency and accuracy over multiple function calls.
"""
*/

// ********RoostGPT********

package com.axway.ats.common.dbaccess.snapshot.equality;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import org.junit.jupiter.api.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import com.axway.ats.common.PublicAtsApi;

class DatabaseEqualityStateGetTablesWithRowsPresentInOneSnapshotOnlyTest {

	DatabaseEqualityState databaseEqualityState;

	@BeforeEach
	void setUp() {
		databaseEqualityState = new DatabaseEqualityState("snapshot1", "snapshot2");
	}

	@Test
	@Tag("valid")
	void getTablesWithRowsPresentInOneSnapshotOnly() {
		// arrange
		databaseEqualityState.addRowPresentInOneSnapshotOnly("snapshot1", "table1", "row1");
		databaseEqualityState.addRowPresentInOneSnapshotOnly("snapshot1", "table2", "row2");

		List<String> expectedTables = Arrays.asList("table1", "table2");

		// act
		List<String> actualTables = databaseEqualityState.getTablesWithRowsPresentInOneSnapshotOnly("snapshot1");
		// assert
		assertArrayEquals(expectedTables.toArray(), actualTables.toArray());
	}

	@Test
	@Tag("invalid")
	void validateEmptyReturnForUnknownSnapshot() {
		List<String> actualTables = databaseEqualityState.getTablesWithRowsPresentInOneSnapshotOnly("unknownSnapshot");
		assertTrue(actualTables.isEmpty());
	}

	@Test
	@Tag("invalid")
	void validateNullExceptionForNullSnapshot() {
		assertThrows(NullPointerException.class,
				() -> databaseEqualityState.getTablesWithRowsPresentInOneSnapshotOnly(null));
	}

	@Test
	@Tag("valid")
	void validateFunctionForMultipleCalls() {
		// arrange
		databaseEqualityState.addRowPresentInOneSnapshotOnly("snapshot1", "table1", "row1");
		databaseEqualityState.addRowPresentInOneSnapshotOnly("snapshot1", "table2", "row2");
		databaseEqualityState.addRowPresentInOneSnapshotOnly("snapshot2", "table3", "row3");
		List<String> expectedTablesSnapshot1 = Arrays.asList("table1", "table2");
		List<String> expectedTablesSnapshot2 = Collections.singletonList("table3");
		// act & assert
		assertArrayEquals(expectedTablesSnapshot1.toArray(),
				databaseEqualityState.getTablesWithRowsPresentInOneSnapshotOnly("snapshot1").toArray());
		assertArrayEquals(expectedTablesSnapshot2.toArray(),
				databaseEqualityState.getTablesWithRowsPresentInOneSnapshotOnly("snapshot2").toArray());
	}

}