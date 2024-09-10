// ********RoostGPT********
/*
Test generated by RoostGPT for test axway-java-test using AI Type  and AI Model

ROOST_METHOD_HASH=FileSystemOperationException_ee13150cfd
ROOST_METHOD_SIG_HASH=FileSystemOperationException_60e5457309

"""
  Scenario 1: Test to verify if FileSystemOperationException is properly initialized with the provided message and exception

  Details:
    TestName: testExceptionInitialization.
    Description: The aim of this test is to validate if FileSystemOperationException is constructed correctly with the provided parameters. This will include passing a predefined message and exception to the constructor and verifying the received message and exception from FileSystemOperationException.
  Execution:
    Arrange: Prepare a predefined string as the message and create a new Exception instance.
    Act: Create a new FileSystemOperationException using the prepared string and Exception instance.
    Assert: Assert that the received message and exception from FileSystemOperationException are same as the ones that were passed.
  Validation:
    This asserts that FileSystemOperationException is correctly associating the provided message and exception with the instance. This is vital as it ensures that the proper error message and Exception trace would be available for debugging.


  Scenario 2: Test to confirm an empty message and non-null exception is correctly associated with FileSystemOperationException instance.

  Details:
    TestName: testInitializationWithEmptyMessage.
    Description: The test is planned to check the FileSystemOperationException's ability to handle the scenario when an empty message and a not null exception is provided for initialization.
  Execution:
    Arrange: Define an empty string as the message and create a new Exception instance.
    Act: Instantiate the FileSystemOperationException using the empty string and the Exception instance.
    Assert: Assert that the received message from FileSystemOperationException instance is empty and the exception trace is not null.
  Validation:
    This will validate that even if an empty message is provided during initialization, the exception trace is still correctly associated with the FileSystemOperationException instance. This is important as it ensures that the exception trace is not lost in the scenario of an empty error message.


  Scenario 3: Test creation of FileSystemOperationException instance with Null as both message and exception.

  Details:
    TestName: testInitializationWithNullValues.
    Description: This test validates the ability of FileSystemOperationException to handle a scenario in which both the message and exception provided during initialization are null.
  Execution:
    Arrange: Define a null String as the message and a null Exception instance.
    Act: Instantiate the FileSystemOperationException using null values.
    Assert: Assert that the received message and exception from the FileSystemOperationException instance are null.
  Validation:
    This validation verifies that the FileSystemOperationException is capable to handle null references without crashing or behaving inconsistently. This is crucial to ensuring the robustness of the error handling system.
"""
*/

// ********RoostGPT********

package com.axway.ats.common.filesystem;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.*;
import com.axway.ats.common.PublicAtsApi;

public class FileSystemOperationExceptionFileSystemOperationException236Test {

	private static final long serialVersionUID = 6354018478615561666L;

	@Test
	@Tag("valid")
	public void testExceptionInitialization() {
		String expectedMessage = "This is a test message";
		Exception expectedException = new Exception("This is a test exception");
		FileSystemOperationException exception = new FileSystemOperationException(expectedMessage, expectedException);
		assertEquals(expectedMessage, exception.getMessage());
		assertEquals(expectedException, exception.getCause());
	}

	@Test
	@Tag("boundary")
	public void testInitializationWithEmptyMessage() {
		String expectedMessage = "";
		Exception expectedException = new Exception("This is a test exception");
		FileSystemOperationException exception = new FileSystemOperationException(expectedMessage, expectedException);
		assertEquals(expectedMessage, exception.getMessage());
		assertEquals(expectedException, exception.getCause());
	}

	@Test
	@Tag("invalid")
	public void testInitializationWithNullValues() {
		String expectedMessage = null;
		Exception expectedException = null;
		FileSystemOperationException exception = new FileSystemOperationException(expectedMessage, expectedException);
		assertNull(exception.getMessage());
		assertNull(exception.getCause());
	}

}