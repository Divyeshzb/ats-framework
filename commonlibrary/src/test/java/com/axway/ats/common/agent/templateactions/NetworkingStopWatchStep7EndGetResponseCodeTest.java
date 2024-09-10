// ********RoostGPT********
/*
Test generated by RoostGPT for test axway-java-test using AI Type  and AI Model

ROOST_METHOD_HASH=step7_EndGetResponseCode_c88fd10e86
ROOST_METHOD_SIG_HASH=step7_EndGetResponseCode_96bd90ea8c

"""
Scenario 1: Test to check the suspension of network and server processing timer and the resumption of timer between request and response

Details:
  TestName: testEndGetResponseCode_ResumedAndSuspendedTimers
  Description: The test is intended to verify that the method step7_EndGetResponseCode correctly suspends the network and server processing timer and resumes the timer between request and response.

Execution:
  Arrange: Initialize a new NetworkingStopWatch object and invoke the methods step1_OpenConnectionForRequest, step2_OpenedConnectionForRequest, step3_StartSendingRequest, step5_StartInterimTimer, and step6_StartGetResponseCode in the given order.
  Act: Invoke step7_EndGetResponseCode to suspend the network and server processing timer and to resume the timer between request and response.
  Assert: Assert that the network and server processing timer is suspended and that the timer between request and response is resumed.

Validation:
  The assertion verifies that the timers are correctly manipulated by the step7_EndGetResponseCode method. The correct operation of the timers is critical to the accurate measurement and tracking of network communication and server processing times.

Scenario 2: Test to validate the behavior of step7_EndGetResponseCode when no timers have been started

Details:
  TestName: testEndGetResponseCode_WithoutStartingTimers
  Description: This test is intended to check if any exception is thrown when step7_EndGetResponseCode is invoked without initiating any timers.

Execution:
  Arrange: Initialize a new NetworkingStopWatch object without starting any timers.
  Act: Invoke step7_EndGetResponseCode method.
  Assert: Assert that an exception is thrown.

Validation:
  The purpose of this test is to verify the method's behavior in error scenarios, such as when the timers have not been initiated. This test is significant as the method's robustness and error handling capability can directly affect the accuracy of operations involving timers.
"""
*/

// ********RoostGPT********

package com.axway.ats.common.agent.templateactions;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.log4j.Logger;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;

public class NetworkingStopWatchStep7EndGetResponseCodeTest {

	// Instantiate necessary objects for tests
	private final Logger logTimer = Logger.getLogger("com.axway.ats.common.agent.templateactions.wireTimer");

	private final StopWatch timerNetAndServerProcessingTime = new StopWatch();

	private final StopWatch timerBetweenReqAndResp = new StopWatch();

	@Test
	@Tag("valid")
	public void testEndGetResponseCode_ResumedAndSuspendedTimers() {
		// Arrange
		NetworkingStopWatch netStopWatch = new NetworkingStopWatch("templateActionName");
		netStopWatch.step1_OpenConnectionForRequest();
		netStopWatch.step2_OpenedConnectionForRequest();
		netStopWatch.step3_StartSendingRequest();
		netStopWatch.step5_StartInterimTimer();
		netStopWatch.step6_StartGetResponseCode();
		// Act
		netStopWatch.step7_EndGetResponseCode();

		// Assert
		assertFalse(timerNetAndServerProcessingTime.isStarted());
		assertTrue(timerBetweenReqAndResp.isStarted());
	}

	@Test
	@Tag("invalid")
	public void testEndGetResponseCode_WithoutStartingTimers() {
		// Arrange
		NetworkingStopWatch netStopWatch = new NetworkingStopWatch("templateActionName");
		// Act and Assert
		assertThrows(IllegalStateException.class, () -> netStopWatch.step7_EndGetResponseCode());
	}

}