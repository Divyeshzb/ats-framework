/*
 * Copyright 2017 Axway Software
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
package com.axway.ats.common.agent.templateactions;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.log4j.Logger;

class NetworkingStopWatchGetNetworkingTimeTest {

	private NetworkingStopWatch networkingStopWatch;

	private final String templateActionName = "Test Template Action";

	@BeforeEach
	void initializeNetworkingStopWatch() {
		networkingStopWatch = new NetworkingStopWatch(templateActionName);
	}

	@Tag('invalid')
    @Test
    void getNetworkingTimeWithoutStartingTimer() {
        long networkingTime = networkingStopWatch.getNetworkingTime();
        assertEquals(0, networkingTime, "Networking time is not zero but Timer is not started yet.");
    }

	@Tag
	('valid')@Test void getNetworkingTimeDuringTimerRunning() throws InterruptedException {
		networkingStopWatch.timerNetAndServerProcessingTime.start();
		Thread.sleep(1500);
		long networkingTime = networkingStopWatch.getNetworkingTime();
		assertNotEquals(0, networkingTime, "Networking time is zero but Timer was running.");
	}

	@Tag
	('integration')@Test void getNetworkingTimeAfterTimerStopped() throws InterruptedException {
		networkingStopWatch.timerNetAndServerProcessingTime.start();
		Thread.sleep(1500);
		networkingStopWatch.timerNetAndServerProcessingTime.stop();
		long networkingTime = networkingStopWatch.getNetworkingTime();
		assertEquals(1500, networkingTime,
				"Networking time is not equivalent to the exact timer manually started and stopped.");
	}

}