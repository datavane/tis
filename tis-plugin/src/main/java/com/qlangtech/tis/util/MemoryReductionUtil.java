/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.qlangtech.tis.util;


import java.util.HashMap;
import java.util.Map;

/**
 * Utilities to reduce memory footprint
 * @author Sam Van Oort
 */
public class MemoryReductionUtil {
    /** Returns the capacity we need to allocate for a HashMap so it will hold all elements without needing to resize. */
    public static int preallocatedHashmapCapacity(int elementsToHold) {
        if (elementsToHold <= 0) {
            return 0;
        } else if (elementsToHold < 3) {
            return elementsToHold + 1;
        } else {
            return elementsToHold + elementsToHold / 3; // Default load factor is 0.75, so we want to fill that much.
        }
    }

    /** Returns a mutable HashMap presized to hold the given number of elements without needing to resize. */
    public static Map getPresizedMutableMap(int elementCount) {
        return new HashMap(preallocatedHashmapCapacity(elementCount));
    }

    /** Empty string array, exactly what it says on the tin. Avoids repeatedly created empty array when calling "toArray." */
    public static final String[] EMPTY_STRING_ARRAY = new String[0];

    /** Returns the input strings, but with all values interned. */
    public static String[] internInPlace(String[] input) {
        if (input == null) {
            return null;
        } else if (input.length == 0) {
            return EMPTY_STRING_ARRAY;
        }
        for (int i = 0; i < input.length; i++) {
            input[i] = Util.intern(input[i]);
        }
        return input;
    }

}
