package com.hummer.data.sync.plugin.pipeline;

import com.alibaba.fastjson.JSONObject;
import com.hummer.common.utils.AppBusinessAssert;
import com.hummer.data.sync.plugin.model.OrderSyncMessage;
import com.panli.spaceship.mq.producer.client.MQSendCallBack;
import com.panli.spaceship.mq.producer.client.MQSendUtil;
import com.panli.spaceship.mq.producer.client.model.MQMessage;
import com.panli.spaceship.mq.producer.client.model.MQMessageExt;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Function;

/**
 * description     java类作用描述
 *
 * @author chen wei
 * @version 1.0
 * <p>Copyright: Copyright (c) 2020</p>
 * @date 2020/10/14 11:13
 */
@Component
@Slf4j
public class MqMessageProducer {

    public Boolean push(OrderSyncMessage data) {
        AppBusinessAssert.isTrue(data != null, 500, "order data  sync sending is null");
        return MQSendUtil.send(composeMQMessage(data));
    }

    public void asyncPush(OrderSyncMessage data, Function<Object, Boolean> function) {
        AppBusinessAssert.isTrue(data != null, 500, "order data async sending is null");
        MQSendUtil.asyncSend(composeMQMessage(data), new MQSendCallBack() {
            @Override
            public void complete(MQMessageExt message) {
                function.apply(null);
            }

            @Override
            public void fail(MQMessageExt message, Exception e) {
                function.apply(null);
            }
        });
    }

    private MQMessage composeMQMessage(OrderSyncMessage data) {
        MQMessage message = new MQMessage();
        message.setBusinessCode(data.getBusinessType());
        message.setOperationCode(data.getAction());
        message.setBodys(JSONObject.toJSONBytes(data));
        message.setSendMQTimeout(3000);
        message.setBusinessId(data.getBusinessId());
        return message;
    }
}
