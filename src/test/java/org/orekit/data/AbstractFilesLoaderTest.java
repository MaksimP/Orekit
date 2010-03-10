/* Copyright 2002-2010 CS Communication & Systèmes
 * Licensed to CS Communication & Systèmes (CS) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * CS licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.orekit.data;

import org.orekit.Utils;
import org.orekit.errors.OrekitException;
import org.orekit.frames.EOPHistory;
import org.orekit.time.TimeStamped;
import org.orekit.utils.Constants;

public abstract class AbstractFilesLoaderTest {

    protected void setRoot(String directoryName) throws OrekitException {
        Utils.setDataRoot(directoryName);
    }

    protected int getMaxGap(EOPHistory history) {
        double maxGap = 0;
        TimeStamped previous = null;
        for (final TimeStamped current : history) {
            if (previous != null) {
                maxGap = Math.max(maxGap, current.getDate().durationFrom(previous.getDate()));
            }
            previous = current;
        }
        return (int) Math.round(maxGap / Constants.JULIAN_DAY);
    }

}
