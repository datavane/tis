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
package com.qlangtech.tis.realtime.transfer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.core.io.Resource;
import com.qlangtech.tis.realtime.yarn.rpc.TopicInfo;
import com.qlangtech.tis.spring.LauncherResourceUtils;
import com.qlangtech.tis.spring.LauncherResourceUtils.AppLauncherResource;
import com.qlangtech.tis.spring.ResourceXmlApplicationContext;
import com.qlangtech.tis.common.utils.Assert;
import com.twodfire.async.message.client.consumer.ConsumerListener;
import com.twodfire.async.message.client.consumer.ConsumerListenerForRm;
import com.twodfire.async.message.client.consumer.IConsumerHandle;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public abstract class BasicTransferTool {

    private ResourceXmlApplicationContext appContext;

    // 本地保存监听的collection下的topic的状态
    protected final Map<String, TopicInfo> /* collection */
    collectionFocusTopicInfo = new HashMap<>();

    protected final AtomicBoolean initialized = new AtomicBoolean(false);

    private AppLauncherResource launcherResource;

    protected List<String> getIndexNames() {
        if (launcherResource == null) {
            throw new IllegalStateException("method startService has not been execute");
        }
        return launcherResource.getIndexNames();
    }

    public void startService(Set<String> includesCollectionNames) throws Exception {
        if (initialized.compareAndSet(false, true)) {
            AppLauncherResource launcherResource = getLauncherResource(includesCollectionNames);
            this.appContext = new ResourceXmlApplicationContext(launcherResource.getResource().toArray(new Resource[] {}));
            Assert.assertNotNull(this.appContext);
        }
    }

    private void addCollectionFocuseTag(String collection, String topic, Set<String> tags) {
        TopicInfo topicInfo = collectionFocusTopicInfo.get(collection);
        if (topicInfo == null) {
            synchronized (collectionFocusTopicInfo) {
                topicInfo = collectionFocusTopicInfo.get(collection);
                if (topicInfo == null) {
                    topicInfo = new TopicInfo();
                    collectionFocusTopicInfo.put(collection, topicInfo);
                }
            }
        }
        topicInfo.addTag(topic, tags);
    }

    protected List<IOnsListenerStatus> getAllTransferChannel() {
        if (this.appContext == null) {
            throw new IllegalStateException("appContext has not been initialize");
        }
        Map<String, ConsumerListener> consumeListeners = this.appContext.getBeansOfType(ConsumerListener.class);
        Map<String, ConsumerListenerForRm> consumeListenersRockMQ = this.appContext.getBeansOfType(ConsumerListenerForRm.class);
        IConsumerHandle handler = null;
        List<IOnsListenerStatus> incrChannels = new ArrayList<>();
        for (ConsumerListener l : consumeListeners.values()) {
            handler = l.getConsumerHandle();
            if (handler instanceof BasicONSListener) {
                add2Channels(incrChannels, l.getTopic(), (BasicONSListener) handler);
            }
        }
        for (ConsumerListenerForRm l : consumeListenersRockMQ.values()) {
            handler = l.getConsumerHandle();
            if (handler instanceof BasicONSListener) {
                add2Channels(incrChannels, l.getTopic(), (BasicONSListener) handler);
            } else if (handler instanceof IFocusTags) {
                IFocusTags focuseTags = (IFocusTags) handler;
                this.addCollectionFocuseTag(focuseTags.getCollectionName(), focuseTags.getTopic(), focuseTags.getFocusTags());
            }
        }
        if (incrChannels.size() < 1) {
            throw new IllegalStateException("incrChannels is empty");
        }
        return incrChannels;
    }

    private void add2Channels(List<IOnsListenerStatus> incrChannels, String topic, BasicONSListener listener) {
        incrChannels.add(listener);
        this.addCollectionFocuseTag(listener.getCollectionName(), topic, listener.getTableFocuse());
    }

    protected AppLauncherResource getLauncherResource(Set<String> includesCollectionNames) throws IOException {
        if (launcherResource == null) {
            launcherResource = LauncherResourceUtils.getAppResource(includesCollectionNames, "classpath*:com/dfire/tis/realtime/transfer/search4*/app-context*.xml");
        }
        return launcherResource;
    }

    private static final Set<String> includesCollectionNames = Collections.emptySet();

    protected AppLauncherResource getLauncherResource() throws IOException {
        return getLauncherResource(includesCollectionNames);
    }
}
