/*
 * Copyright (C) 2018 The Android Open Source Project
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


package android.app.usage;

import static org.junit.Assert.assertTrue;

import android.os.SystemClock;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class TimeSparseArrayTest {
    @Test
    public void testDuplicateKeysNotDropped() {
        final TimeSparseArray<Integer> testTimeSparseArray = new TimeSparseArray<>();
        final long key = SystemClock.elapsedRealtime();
        for (int i = 0; i < 5; i++) {
            testTimeSparseArray.put(key, i);
        }
        for (int i = 0; i < 5; i++) {
            final int valueIndex = testTimeSparseArray.indexOfValue(i);
            assertTrue("Value " + i + " not found; intended key: " + key , valueIndex >= 0);
            final long keyForValue = testTimeSparseArray.keyAt(valueIndex);
            assertTrue("Value " + i + " stored too far (at " + keyForValue + ") from intended key "
                    + key, Math.abs(keyForValue - key) < 100);
        }
    }
}
