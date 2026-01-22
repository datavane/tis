/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qlangtech.tis.extension;

/**
 * Multi-step plugin related exception
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/1/21
 */
public class MultiStepPluginException extends RuntimeException {

    public MultiStepPluginException(String message) {
        super(message);
    }

    public MultiStepPluginException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Exception thrown when a previous step plugin instance is not found in the context
     */
    public static class StepPluginNotFoundException extends MultiStepPluginException {
        public StepPluginNotFoundException(String pluginClass) {
            super("Previous step plugin not found for class: " + pluginClass);
        }
    }

    /**
     * Exception thrown when step processing fails
     */
    public static class StepProcessingException extends MultiStepPluginException {
        public StepProcessingException(String message, Throwable cause) {
            super("Failed to process multi-step plugin: " + message, cause);
        }
    }

    /**
     * Exception thrown when step data parsing fails
     */
    public static class StepDataParsingException extends MultiStepPluginException {
        public StepDataParsingException(String message, Throwable cause) {
            super("Failed to parse step data: " + message, cause);
        }
    }
}
