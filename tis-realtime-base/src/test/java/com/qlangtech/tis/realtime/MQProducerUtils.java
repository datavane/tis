package com.qlangtech.tis.realtime;

import com.qlangtech.tis.manage.common.TisUTF8;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;

/**
 * @author: baisui 百岁
 * @create: 2020-11-02 10:13
 **/
public class MQProducerUtils {
    public static final String nameAddress = "192.168.28.201:9876";
    public static final String topic = "baisui-test";

    public static Message createMsg(String line, String tag) {
        return new Message(topic /* Topic */, tag, line.getBytes(TisUTF8.get()) /* Message body */);
    }

    public static DefaultMQProducer createProducter() throws Exception {
        DefaultMQProducer producer = new DefaultMQProducer("produce_baisui_test");
        // https://github.com/apache/rocketmq/issues/568
        producer.setVipChannelEnabled(false);
        producer.setSendMsgTimeout(30000);
        // Specify name server addresses.
        producer.setNamesrvAddr(nameAddress);
        //Launch the instance.
        producer.start();
        return producer;
    }

}
