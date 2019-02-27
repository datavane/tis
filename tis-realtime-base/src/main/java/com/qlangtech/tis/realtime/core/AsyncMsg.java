package com.qlangtech.tis.realtime.core;

import java.io.Serializable;

public interface AsyncMsg extends Serializable {
  
    String getTopic();

    String getTag();

    <T> T getContent();

    /**
     * Message实体
     *
     * @return
     */
    <T> T getMessage();

    String getMsgID();

    String getKey();

    /**
     * 重试次数
     *
     * @return
     */
    int getReconsumeTimes();

    /**
     * 开始投递的时间
     *
     * @return
     */
    long getStartDeliverTime();


    /**
     * 最初的MessageID。在消息重试时msgID会变
     *
     * @return
     */
    String getOriginMsgID();
}
