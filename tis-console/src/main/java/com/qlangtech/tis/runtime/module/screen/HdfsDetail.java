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
package com.qlangtech.tis.runtime.module.screen;

import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.fs.Path;
import com.alibaba.citrus.turbine.Context;

/*
 * 显示hadoop 中的文本内容
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class HdfsDetail extends HdfsView {

    private static final long serialVersionUID = 1L;

    private static final Pattern cellPattern = Pattern.compile("[^\t]+");

    @Override
    public void execute(Context context) throws Exception {
        // super.execute(context);
        this.enableChangeDomain(context);
        this.disableNavigationBar(context);
        final Path path = new Path(this.getString("path"));
        // BufferedReader reader = new BufferedReader(new InputStreamReader());
        InputStream inputStream = null;
        StringBuffer result = new StringBuffer();
        try {
            inputStream = this.getFilesystem().open(path);
            LineIterator lineIterator = IOUtils.lineIterator(inputStream, getEncode());
            int i = 0;
            // 最多显示300行
            result.append("<table width='100%' border='1' cellspacing='0'>");
            final String header = lineIterator.nextLine();
            final int colSize = addHeader(result, header);
            int tableWidth = colSize > 25 ? 1800 : 1300;
            int cellWidth = tableWidth / colSize;
            context.put("tableWidth", tableWidth);
            context.put("cellWidth", cellWidth);
            while (lineIterator.hasNext() && (i++ < 300)) {
                result.append("<tr>");
                // result.append(StringUtils.replace(lineIterator.nextLine(),
                // "\t", "</td><td>"));
                // Matcher m = cellPattern.matcher(lineIterator.nextLine());
                String[] cells = StringUtils.splitPreserveAllTokens(lineIterator.nextLine(), "\t");
                int filledCell = 0;
                for (String cell : cells) {
                    result.append("<td class='cell" + filledCell + "' cellcol='cell" + filledCell + "'> <div style='position:relative'><div class='celldetail'></div></div><div>").append(StringUtils.isEmpty(cell) ? "&nbsp;" : StringEscapeUtils.escapeHtml(cell)).append("</div></td>");
                    filledCell++;
                }
                final int leave = colSize - filledCell;
                for (int j = 0; j < leave; j++) {
                    result.append("<td class='cell" + (filledCell++) + "' cellcol='cell" + (filledCell) + "'>").append("&nbsp;").append("</td>");
                }
                result.append("</tr>");
            }
            result.append("</table>");
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
        context.put("hdfscontent", result);
    }

    /**
     * @param result
     * @param header
     * @return column size
     */
    private int addHeader(StringBuffer result, String header) {
        result.append("<thead><tr>");
        // result.append(StringUtils.replace(lineIterator.nextLine(),
        // "\t", "</td><td>"));
        Matcher m = cellPattern.matcher(header);
        int columnSize = 0;
        while (m.find()) {
            result.append("<th class='hdfs_cell'><strong>").append(m.group()).append("</strong></th>");
            columnSize++;
        }
        result.append("</tr></thead>");
        return columnSize;
    }

    @Override
    public boolean isEnableDomainView() {
        return false;
    }

    public static void main(String[] arg) {
        Matcher matcher = cellPattern.matcher("adfasdfas\tasdfasdasd");
        while (matcher.find()) {
            System.out.println(matcher.group());
        }
    }
}
