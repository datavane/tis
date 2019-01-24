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
package com.qlangtech.tis.common.utils;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class JstConfigFetcher {
    // private static final String CONFIG_JST_ADDRESS = "jst_address";
    // public static final String TERMINATOR_GROUP = "Terminator-Group";
    // public static final String JST_CONFIG = "jst_config";
    // 
    // public static final String CONFIG_runenvironment = "runenvironment";
    // // private static JstConfigFetcher configFetcher;
    // 
    // private final String jstAddress;
    // 
    // private JstConfigFetcher(String serviceName) {
    // 
    // // 获取diamond上的配置
    // DefaultDiamondManager diamondManager = null;
    // DefaultDiamondManager overwriterDiamond = null;
    // 
    // try {
    // diamondManager = getGlobalDiamond();
    // 
    // overwriterDiamond = new DefaultDiamondManager(TERMINATOR_GROUP,
    // JST_CONFIG + "_" + serviceName,
    // new ManagerListenerAdapter() {
    // @Override
    // public void receiveConfigInfo(String value) {
    // }
    // });
    // 
    // try {
    // 
    // ServiceConfig servceConfig = new ServiceConfig(diamondManager,
    // overwriterDiamond);
    // 
    // // final String jsonConfig = diamondManager
    // // .getConfigureInfomation(6000);
    // // Assert.assertNotNull("jsonConfig can not be null",
    // // jsonConfig);
    // // JSONTokener tokener = new JSONTokener(jsonConfig);
    // // JSONObject json = new JSONObject(tokener);
    // 
    // 
    // jstAddress = servceConfig.getString(CONFIG_JST_ADDRESS);
    // 
    // 
    // } catch (JSONException e) {
    // throw new RuntimeException(e);
    // }
    // 
    // Assert.assertNotNull("jstAddress can not be null", this.jstAddress);
    // 
    // } finally {
    // try {
    // diamondManager.close();
    // } catch (Throwable e) {
    // 
    // }
    // 
    // try {
    // overwriterDiamond.close();
    // } catch (Throwable e) {
    // 
    // }
    // }
    // }
    // 
    // 
    // 
    // private static DefaultDiamondManager getGlobalDiamond() {
    // // DefaultDiamondManager diamondManager;
    // return new DefaultDiamondManager(TERMINATOR_GROUP,
    // JST_CONFIG, new ManagerListenerAdapter() {
    // @Override
    // public void receiveConfigInfo(String value) {
    // }
    // });
    // // return diamondManager;
    // }
    // 
    // private static class ServiceConfig {
    // 
    // private final JSONObject globalConfig;
    // private final JSONObject localConfig;
    // 
    // ServiceConfig(DefaultDiamondManager globalManager,
    // DefaultDiamondManager localDiamond) throws JSONException {
    // super();
    // String jsonConfig = globalManager.getConfigureInfomation(6000);
    // Assert.assertNotNull("jsonConfig can not be null", jsonConfig);
    // JSONTokener tokener = new JSONTokener(jsonConfig);
    // globalConfig = new JSONObject(tokener);
    // 
    // jsonConfig = localDiamond.getConfigureInfomation(6000);
    // tokener = new JSONTokener((jsonConfig == null) ? "{}" : jsonConfig);
    // localConfig = new JSONObject(tokener);
    // }
    // 
    // public String getString(String key) {
    // try {
    // if (localConfig.isNull(key)) {
    // return globalConfig.getString(key);
    // 
    // } else {
    // return localConfig.getString(key);
    // }
    // } catch (JSONException e) {
    // throw new RuntimeException(e);
    // }
    // }
    // }
    // 
    // private static JSONObject parseJson(DefaultDiamondManager globalManager)
    // throws JSONException {
    // String jsonConfig = globalManager.getConfigureInfomation(6000);
    // Assert.assertNotNull("jsonConfig can not be null", jsonConfig);
    // JSONTokener tokener = new JSONTokener(jsonConfig);
    // return new JSONObject(tokener);
    // }
    // 
    // private static JSONObject globalConfig;
    // 
    // public static boolean isRunInVersion2Environment() {
    // 
    // try {
    // if (globalConfig == null) {
    // synchronized (JstConfigFetcher.class) {
    // if (globalConfig == null) {
    // DefaultDiamondManager globalDiamond = getGlobalDiamond();
    // globalConfig = parseJson(globalDiamond);
    // globalDiamond.close();
    // }
    // }
    // }
    // 
    // return "2tao".equals(globalConfig.get(CONFIG_runenvironment));
    // } catch (JSONException e) {
    // throw new RuntimeException(e);
    // }
    // }
    // 
    // 
    // public String getJstAddress() {
    // return getInstance().jstAddress;
    // }
    // 
    // 
    // private JstConfigFetcher getInstance() {
    // return this;
    // }
    // 
    // private final static Map<String, JstConfigFetcher> indexsConfig = new HashMap<String, JstConfigFetcher>();
    // 
    // public static JstConfigFetcher get() {
    // return getInstance("search4");
    // }
    // 
    // public static JstConfigFetcher getInstance(String serviceName) {
    // 
    // Assert.assertTrue(StringUtils.startsWith(serviceName, "search4"));
    // 
    // JstConfigFetcher config = null;
    // config = indexsConfig.get(serviceName);
    // 
    // if (config == null) {
    // synchronized (indexsConfig) {
    // config = indexsConfig.get(serviceName);
    // if (config == null) {
    // config = new JstConfigFetcher(serviceName);
    // indexsConfig.put(serviceName, config);
    // }
    // }
    // }
    // 
    // return config;
    // }
    // 
    // /**
    // * @param args
    // */
    // public static void main(String[] args) {
    // JstConfigFetcher config = JstConfigFetcher
    // .getInstance("search4sucainew");
    // 
    // System.out.println(config.getJstAddress());
    // }
}
