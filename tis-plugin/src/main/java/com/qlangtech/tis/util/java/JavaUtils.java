/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 *   This program is free software: you can use, redistribute, and/or modify
 *   it under the terms of the GNU Affero General Public License, version 3
 *   or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *   FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.qlangtech.tis.util.java;

//import hudson.util.VersionNumber;
//import io.jenkins.lib.versionnumber.JavaSpecificationVersion;
//import org.kohsuke.accmod.Restricted;
//import org.kohsuke.accmod.restrictions.NoExternalUse;

/**
 * Utility class for Java environment management and checks.
 * @author Oleg Nenashev
 */
public class JavaUtils {

    private JavaUtils() {
        // Cannot construct
    }

    /**
     * Check whether the current JVM is running with Java 8 or below
     * @return {@code true} if it is Java 8 or older version
     */
    public static boolean isRunningWithJava8OrBelow() {
        String javaVersion = getCurrentRuntimeJavaVersion();
        return javaVersion.startsWith("1.");
    }

    /**
     * Check whether the current JVM is running with Java 9 or above.
     * @return {@code true} if it is Java 9 or above
     */
    public static boolean isRunningWithPostJava8() {
        String javaVersion = getCurrentRuntimeJavaVersion();
        return !javaVersion.startsWith("1.");
    }

//    /**
//     * Returns the JVM's current version as a {@link 'VersionNumber'} instance.
//     */
//    public static JavaSpecificationVersion getCurrentJavaRuntimeVersionNumber() {
//        return JavaSpecificationVersion.forCurrentJVM();
//    }

    /**
     * Returns the JVM's current version as a {@link String}.
     * See https://openjdk.java.net/jeps/223 for the expected format.
     * <ul>
     *     <li>Until Java 8 included, the expected format should be starting with {@code 1.x}</li>
     *     <li>Starting with Java 9, cf. JEP-223 linked above, the version got simplified in 9.x, 10.x, etc.</li>
     * </ul>
     *
     * @see System#getProperty(String)
     */
    public static String getCurrentRuntimeJavaVersion() {
        // TODO: leverage Runtime.version() once on Java 9+
        return System.getProperty("java.specification.version");
    }
}
