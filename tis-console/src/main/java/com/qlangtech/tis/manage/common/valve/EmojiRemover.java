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

package com.qlangtech.tis.manage.common.valve;

/**
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2025/11/26
 */
public class EmojiRemover {
  /**
   * 移除字符串中的Emoji字符
   */
  public static String removeEmoji(String input) {
    if (input == null) {
      return null;
    }

    // 匹配Emoji字符的正则表达式
    String emojiRegex =
      "[\\uD83C-\\uDBFF\\uDC00-\\uDFFF]" +                // 基本Emoji
        "|[\\u2600-\\u26FF]" +                             // 杂项符号
        "|[\\u2700-\\u27BF]" +                             // 装饰符号
        "|[\\uE000-\\uF8FF]" +                             // 私有区域
        "|[\\uFE00-\\uFE0F]" +                             // 变体选择器
        "|[\\uD83C\\uDDE6-\\uD83C\\uDDFF]{1,2}" +          // 国旗
        "|[\\uD83E\\uDD10-\\uD83E\\uDDFF]" +               // 补充符号
        "|[\\uD83D\\uDE00-\\uD83D\\uDE4F]" +               // 表情符号
        "|[\\uD83D\\uDE80-\\uD83D\\uDEF6]";                // 交通和地图符号

    return input.replaceAll(emojiRegex, "");
  }

  /**
   * 更简洁的正则表达式版本
   */
  public static String removeEmojiSimple(String input) {
    if (input == null) {
      return null;
    }
    // 匹配所有4字节的UTF-8字符（包括Emoji）
    return input.replaceAll("[^\\u0000-\\uFFFF]", "");
  }

  /**
   * 使用Unicode范围的正则表达式
   */
  public static String removeEmojiByRange(String input) {
    if (input == null) {
      return null;
    }
    // 移除代理对字符（大部分Emoji使用代理对）
    return input.replaceAll("[\\uD800-\\uDFFF]", "");
  }
}
