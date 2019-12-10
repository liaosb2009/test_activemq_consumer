package com.active.common.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.TextMessage;

/**
 * @ClassName:TestConsumer
 * @Description
 * @Author liao
 * @Time 2019/7/4 16:48
 */
@Component
public class TestConsumer {

    @Autowired
    private KafkaTemplate kafkaTemplate;

    public static void main(String[] args) {


        System.out.println("111111111111");
    }


  /*  @JmsListener(destination = "${myqueue}")
    public void receie(TextMessage textMessage) {
        try {
            System.out.println("消费者受到消息:" + textMessage.getText());
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }*/

}
