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
package javax.servlet.jsp;

import java.io.IOException;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public abstract class JspWriter extends java.io.Writer {

    /**
     * Constant indicating that the Writer is not buffering output.
     */
    public static final int NO_BUFFER = 0;

    /**
     * Constant indicating that the Writer is buffered and is using the
     * implementation default buffer size.
     */
    public static final int DEFAULT_BUFFER = -1;

    /**
     * Constant indicating that the Writer is buffered and is unbounded; this
     * is used in BodyContent.
     */
    public static final int UNBOUNDED_BUFFER = -2;

    protected JspWriter(int bufferSize, boolean autoFlush) {
        this.bufferSize = bufferSize;
        this.autoFlush = autoFlush;
    }

    /**
     * Write a line separator.  The line separator string is defined by the
     * system property <tt>line.separator</tt>, and is not necessarily a single
     * newline ('\n') character.
     *
     * @exception  IOException  If an I/O error occurs
     */
    public abstract void newLine() throws IOException;

    /**
     * Print a boolean value.  The string produced by <code>{@link
     * java.lang.String#valueOf(boolean)}</code> is written to the
     * JspWriter's buffer or, if no buffer is used, directly to the
     * underlying writer.
     *
     * @param      b   The <code>boolean</code> to be printed
     * @throws	   java.io.IOException If an error occured while writing
     */
    public abstract void print(boolean b) throws IOException;

    /**
     * Print a character.  The character is written to the
     * JspWriter's buffer or, if no buffer is used, directly to the
     * underlying writer.
     *
     * @param      c   The <code>char</code> to be printed
     * @throws	   java.io.IOException If an error occured while writing
     */
    public abstract void print(char c) throws IOException;

    /**
     * Print an integer.  The string produced by <code>{@link
     * java.lang.String#valueOf(int)}</code> is written to the
     * JspWriter's buffer or, if no buffer is used, directly to the
     * underlying writer.
     *
     * @param      i   The <code>int</code> to be printed
     * @see        java.lang.Integer#toString(int)
     * @throws	   java.io.IOException If an error occured while writing
     */
    public abstract void print(int i) throws IOException;

    /**
     * Print a long integer.  The string produced by <code>{@link
     * java.lang.String#valueOf(long)}</code> is written to the
     * JspWriter's buffer or, if no buffer is used, directly to the
     * underlying writer.
     *
     * @param      l   The <code>long</code> to be printed
     * @see        java.lang.Long#toString(long)
     * @throws	   java.io.IOException If an error occured while writing
     */
    public abstract void print(long l) throws IOException;

    /**
     * Print a floating-point number.  The string produced by <code>{@link
     * java.lang.String#valueOf(float)}</code> is written to the
     * JspWriter's buffer or, if no buffer is used, directly to the
     * underlying writer.
     *
     * @param      f   The <code>float</code> to be printed
     * @see        java.lang.Float#toString(float)
     * @throws	   java.io.IOException If an error occured while writing
     */
    public abstract void print(float f) throws IOException;

    /**
     * Print a double-precision floating-point number.  The string produced by
     * <code>{@link java.lang.String#valueOf(double)}</code> is written to
     * the JspWriter's buffer or, if no buffer is used, directly to the
     * underlying writer.
     *
     * @param      d   The <code>double</code> to be printed
     * @see        java.lang.Double#toString(double)
     * @throws	   java.io.IOException If an error occured while writing
     */
    public abstract void print(double d) throws IOException;

    /**
     * Print an array of characters.  The characters are written to the
     * JspWriter's buffer or, if no buffer is used, directly to the
     * underlying writer.
     *
     * @param      s   The array of chars to be printed
     *
     * @throws  NullPointerException  If <code>s</code> is <code>null</code>
     * @throws	   java.io.IOException If an error occured while writing
     */
    public abstract void print(char[] s) throws IOException;

    /**
     * Print a string.  If the argument is <code>null</code> then the string
     * <code>"null"</code> is printed.  Otherwise, the string's characters are
     * written to the JspWriter's buffer or, if no buffer is used, directly
     * to the underlying writer.
     *
     * @param      s   The <code>String</code> to be printed
     * @throws	   java.io.IOException If an error occured while writing
     */
    public abstract void print(String s) throws IOException;

    /**
     * Print an object.  The string produced by the <code>{@link
     * java.lang.String#valueOf(Object)}</code> method is written to the
     * JspWriter's buffer or, if no buffer is used, directly to the
     * underlying writer.
     *
     * @param      obj   The <code>Object</code> to be printed
     * @see        java.lang.Object#toString()
     * @throws	   java.io.IOException If an error occured while writing
     */
    public abstract void print(Object obj) throws IOException;

    /**
     * Terminate the current line by writing the line separator string.  The
     * line separator string is defined by the system property
     * <code>line.separator</code>, and is not necessarily a single newline
     * character (<code>'\n'</code>).
     * @throws	   java.io.IOException If an error occured while writing
     */
    public abstract void println() throws IOException;

    /**
     * Print a boolean value and then terminate the line.  This method behaves
     * as though it invokes <code>{@link #print(boolean)}</code> and then
     * <code>{@link #println()}</code>.
     *
     * @param      x the boolean to write
     * @throws	   java.io.IOException If an error occured while writing
     */
    public abstract void println(boolean x) throws IOException;

    /**
     * Print a character and then terminate the line.  This method behaves as
     * though it invokes <code>{@link #print(char)}</code> and then <code>{@link
     * #println()}</code>.
     *
     * @param      x the char to write
     * @throws	   java.io.IOException If an error occured while writing
     */
    public abstract void println(char x) throws IOException;

    /**
     * Print an integer and then terminate the line.  This method behaves as
     * though it invokes <code>{@link #print(int)}</code> and then <code>{@link
     * #println()}</code>.
     *
     * @param      x the int to write
     * @throws	   java.io.IOException If an error occured while writing
     */
    public abstract void println(int x) throws IOException;

    /**
     * Print a long integer and then terminate the line.  This method behaves
     * as though it invokes <code>{@link #print(long)}</code> and then
     * <code>{@link #println()}</code>.
     *
     * @param      x the long to write
     * @throws	   java.io.IOException If an error occured while writing
     */
    public abstract void println(long x) throws IOException;

    /**
     * Print a floating-point number and then terminate the line.  This method
     * behaves as though it invokes <code>{@link #print(float)}</code> and then
     * <code>{@link #println()}</code>.
     *
     * @param      x the float to write
     * @throws	   java.io.IOException If an error occured while writing
     */
    public abstract void println(float x) throws IOException;

    /**
     * Print a double-precision floating-point number and then terminate the
     * line.  This method behaves as though it invokes <code>{@link
     * #print(double)}</code> and then <code>{@link #println()}</code>.
     *
     * @param      x the double to write
     * @throws	   java.io.IOException If an error occured while writing
     */
    public abstract void println(double x) throws IOException;

    /**
     * Print an array of characters and then terminate the line.  This method
     * behaves as though it invokes <code>print(char[])</code> and then
     * <code>println()</code>.
     *
     * @param      x the char[] to write
     * @throws	   java.io.IOException If an error occured while writing
     */
    public abstract void println(char[] x) throws IOException;

    /**
     * Print a String and then terminate the line.  This method behaves as
     * though it invokes <code>{@link #print(String)}</code> and then
     * <code>{@link #println()}</code>.
     *
     * @param      x the String to write
     * @throws	   java.io.IOException If an error occured while writing
     */
    public abstract void println(String x) throws IOException;

    /**
     * Print an Object and then terminate the line.  This method behaves as
     * though it invokes <code>{@link #print(Object)}</code> and then
     * <code>{@link #println()}</code>.
     *
     * @param      x the Object to write
     * @throws	   java.io.IOException If an error occured while writing
     */
    public abstract void println(Object x) throws IOException;

    /**
     * Clear the contents of the buffer. If the buffer has been already
     * been flushed then the clear operation shall throw an IOException
     * to signal the fact that some data has already been irrevocably
     * written to the client response stream.
     *
     * @throws IOException		If an I/O error occurs
     */
    public abstract void clear() throws IOException;

    /**
     * Clears the current contents of the buffer. Unlike clear(), this
     * method will not throw an IOException if the buffer has already been
     * flushed. It merely clears the current content of the buffer and
     * returns.
     *
     * @throws IOException		If an I/O error occurs
     */
    public abstract void clearBuffer() throws IOException;

    /**
     * Flush the stream.  If the stream has saved any characters from the
     * various write() methods in a buffer, write them immediately to their
     * intended destination.  Then, if that destination is another character or
     * byte stream, flush it.  Thus one flush() invocation will flush all the
     * buffers in a chain of Writers and OutputStreams.
     * <p>
     * The method may be invoked indirectly if the buffer size is exceeded.
     * <p>
     * Once a stream has been closed,
     * further write() or flush() invocations will cause an IOException to be
     * thrown.
     *
     * @exception  IOException  If an I/O error occurs
     */
    public abstract void flush() throws IOException;

    /**
     * Close the stream, flushing it first.
     * <p>
     * This method needs not be invoked explicitly for the initial JspWriter
     * as the code generated by the JSP container will automatically
     * include a call to close().
     * <p>
     * Closing a previously-closed stream, unlike flush(), has no effect.
     *
     * @exception  IOException  If an I/O error occurs
     */
    public abstract void close() throws IOException;

    /**
     * This method returns the size of the buffer used by the JspWriter.
     *
     * @return the size of the buffer in bytes, or 0 is unbuffered.
     */
    public int getBufferSize() {
        return bufferSize;
    }

    /**
     * This method returns the number of unused bytes in the buffer.
     *
     * @return the number of bytes unused in the buffer
     */
    public abstract int getRemaining();

    /**
     * This method indicates whether the JspWriter is autoFlushing.
     *
     * @return if this JspWriter is auto flushing or throwing IOExceptions
     *     on buffer overflow conditions
     */
    public boolean isAutoFlush() {
        return autoFlush;
    }

    /*
     * fields
     */
    /**
     * The size of the buffer used by the JspWriter.
     */
    protected int bufferSize;

    /**
     * Whether the JspWriter is autoflushing.
     */
    protected boolean autoFlush;
}
