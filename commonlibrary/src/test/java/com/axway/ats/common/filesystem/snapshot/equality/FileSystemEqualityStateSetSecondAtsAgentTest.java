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
package com.axway.ats.common.filesystem.snapshot.equality;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.axway.ats.common.PublicAtsApi;

class FileSystemEqualityStateSetSecondAtsAgentTest {
    private static final long serialVersionUID = 1L;
    private String firstSnapshotName = null;
    private String secondSnapshotName = null;
    private List<FileTrace> differences = null;
    private String firstAtsAgent = null;
    private String secondAtsAgent = null;
    @Test
    @Tag("valid")
    void setValidSecondAtsAgent() {
        String testValue = "testValue";
        setSecondAtsAgent(testValue);
        assertEquals(testValue, secondAtsAgent);
    }
    @Test
    @Tag("invalid")
    void setNullSecondAtsAgent() {
        setSecondAtsAgent(null);
        assertNull(secondAtsAgent);
    }
    @Test
    @Tag("valid")
    void replaceExistingSecondAtsAgentValue() {
        String initialValue = "initialValue";
        setSecondAtsAgent(initialValue);
        String newValue = "newValue";
        setSecondAtsAgent(newValue);
        assertNotEquals(initialValue, secondAtsAgent);
        assertEquals(newValue, secondAtsAgent);
    }
    public void setSecondAtsAgent(String secondAtsAgent) {
        this.secondAtsAgent = secondAtsAgent;
    }
}