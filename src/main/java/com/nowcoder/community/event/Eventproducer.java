package com.nowcoder.community.event;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.community.entity.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class Eventproducer {

    @Autowired
    private KafkaTemplate kafkaTemplate;

    public void fireEvent(Event event){
        //将事件发布到指定的主题上
        kafkaTemplate.send(event.getTopic(), JSONObject.toJSONString(event));
    }

}
