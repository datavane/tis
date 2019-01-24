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
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class CreateInsertSql {

    private static final Pattern p = Pattern.compile("(.*?),(.*?),(\\d*?),(.+?),(.*?),(.+?)");

    private static final Connection conn;

    static {
        try {
            // MySQ
            String user = "terminatorhome";
            String password = "terminatorhome";
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://10.232.31.36:3306/terminatorhome?useUnicode=yes&amp;characterEncoding=GBK", user, password);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] arg) throws Exception {
        // if (matcher.matches()) {
        // System.out.println(matcher.group(1));
        // System.out.println(matcher.group(2));
        // System.out.println(matcher.group(3));
        // System.out.println(matcher.group(4));
        // System.out.println(matcher.group(5));
        // System.out.println(matcher.group(6));
        // }
        Statement statement = null;
        PreparedStatement prep = null;
        ResultSet result = null;
        BufferedReader reader = new BufferedReader(new InputStreamReader(CreateInsertSql.class.getResourceAsStream("terminator.csv")));
        String line = null;
        Matcher matcher = null;
        String appName = null;
        Integer group = null;
        String hostName = null;
        Integer appid = null;
        Integer groupid = null;
        String ipAddress = null;
        int appcount = 0;
        while ((line = reader.readLine()) != null) {
            matcher = p.matcher(line);
            if (!matcher.matches()) {
                continue;
            }
            if (isNotEmpty(matcher.group(2))) {
                appcount++;
                appName = matcher.group(2);
                statement = conn.createStatement();
                result = statement.executeQuery("select app_id from application where project_name = '" + appName + "'");
                if (result.next()) {
                    appid = result.getInt(1);
                } else {
                    throw new IllegalStateException("appName:" + appName + " has not match any app record");
                }
                result.close();
                statement.close();
            }
            group = isNotEmpty(matcher.group(3)) ? Integer.parseInt(matcher.group(3)) : 0;
            hostName = matcher.group(4);
            ipAddress = matcher.group(6);
            statement = conn.createStatement();
            result = statement.executeQuery("select gid from server_group where app_id =" + appid + " and runt_environment = 2 and group_index =" + group);
            if (result.next()) {
                groupid = result.getInt(1);
            }
            statement.close();
            result.close();
            if (groupid == null) {
                // create group
                prep = conn.prepareStatement("insert server_group(app_id,runt_environment,group_index,create_time)values(?,2,?,now())");
                prep.setInt(1, appid);
                prep.setInt(2, group);
                prep.execute();
                // if (result.next()) {
                groupid = getInsertId(conn);
                // }
                prep.close();
            // result.close();
            }
            if (groupid == null) {
                throw new IllegalStateException("appid:" + appid + " group:" + group + " can not create group index");
            }
            prep = conn.prepareStatement("insert server(gid,server_name,ip_address,create_time)values(?,?,?,now())");
            prep.setInt(1, groupid);
            prep.setString(2, hostName);
            prep.setString(3, ipAddress);
            prep.execute();
            prep.close();
            System.out.println(appName + group + " " + hostName + " " + ipAddress);
        }
        System.out.println("appcount:" + appcount);
        reader.close();
    }

    private static Integer getInsertId(Connection conn) {
        Statement statement = null;
        ResultSet result = null;
        try {
            statement = conn.createStatement();
            result = statement.executeQuery("select LAST_INSERT_ID();");
            if (result.next()) {
                return result.getInt(1);
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                result.close();
            } catch (Throwable e) {
            }
            try {
                statement.close();
            } catch (Throwable e) {
            }
        }
    }

    private static boolean isNotEmpty(String value) {
        return value != null && value.length() > 0;
    }
}
