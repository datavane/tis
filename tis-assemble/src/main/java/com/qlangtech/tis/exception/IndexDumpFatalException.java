/* 
 * The MIT License
 *
 * Copyright (c) 2018-2022, qinglangtech Ltd
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.qlangtech.tis.exception;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class IndexDumpFatalException extends Exception {

    private static final long serialVersionUID = 3294989715647914155L;

    public IndexDumpFatalException() {
        super();
    }

    public IndexDumpFatalException(String message) {
        super(message);
    }

    public IndexDumpFatalException(Throwable t) {
        super(t);
    }

    public IndexDumpFatalException(String message, Throwable t) {
        super(message, t);
    }

    public static class InitDataProviderException extends IndexDumpFatalException {

        private static final long serialVersionUID = -4255435884947599591L;

        public InitDataProviderException() {
            super();
        }

        public InitDataProviderException(String message, Throwable t) {
            super(message, t);
        }

        public InitDataProviderException(String message) {
            super(message);
        }

        public InitDataProviderException(Throwable t) {
            super(t);
        }
    }

    public static class GetDataException extends IndexDumpFatalException {

        private static final long serialVersionUID = 6625284878435741927L;

        public GetDataException() {
            super();
        }

        public GetDataException(String message, Throwable t) {
            super(message, t);
        }

        public GetDataException(String message) {
            super(message);
        }

        public GetDataException(Throwable t) {
            super(t);
        }
    }

    public static class ReleaseSourceException extends IndexDumpFatalException {

        private static final long serialVersionUID = -3250911784262386705L;

        public ReleaseSourceException() {
            super();
        }

        public ReleaseSourceException(String message, Throwable t) {
            super(message, t);
        }

        public ReleaseSourceException(String message) {
            super(message);
        }

        public ReleaseSourceException(Throwable t) {
            super(t);
        }
    }
}
