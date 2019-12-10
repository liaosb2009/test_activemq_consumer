package com.active.common.consumer;

import com.active.common.dao.ActivemqHistoryMapper;
import com.active.common.dao.TestUserMapper;
import com.active.common.model.TestUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.apache.activemq.transport.TransportListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.jms.*;
import java.io.IOException;
import java.util.Date;

/*
 * @ClassName:TestConsumerMy
 * @Description TODO
 * @Author liao
 * @Time 2019/9/5 17:36
 */
@Component
@Slf4j
public class TestConsumerMy {

    // private final static String BROKERURL = "failover:(tcp://192.168.137.128:61616,tcp://192.168.137.128:61617,tcp://192.168.137.128:61618)?initialReconnectDelay=10&maxReconnectDelay=10000";
    private final static String BROKERURL = "tcp://192.168.137.128:61616";
    private final static String QUEUE_NAME = "failoverQueue01";
    @Autowired
    private TestUserMapper testUserMapper;

    @PostConstruct
    public void init() {
        try {
            this.conMessage();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public boolean conMessage() throws Exception {

        System.out.println("liao大神修改的地方11111=====================");
        ActiveMQConnectionFactory acMqConnection = new ActiveMQConnectionFactory(BROKERURL);
        acMqConnection.setSendTimeout(3000);
        //acMqConnection.setConnectResponseTimeout(1);
        //创建连接工厂
        Connection connection = acMqConnection.createConnection();
        //获得连接
        connection.start();
       //两个参数1：事务2：签收
        Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
        //自定义重复消费次数,允许进行重复消费，消费3次后再不行，默认进死信队列
        RedeliveryPolicy queuePolicy = new RedeliveryPolicy();
        //消费了3次后进死心队列
        queuePolicy.setMaximumRedeliveries(3);
        acMqConnection.setRedeliveryPolicy(queuePolicy);
        //队列或topic
        //队列
        Queue queue = session.createQueue(QUEUE_NAME);
        //创建消息消费者
        MessageConsumer consumer = session.createConsumer(queue);

        try {
            consumer.setMessageListener((message) -> {
                String jmsMessageID =null;
                try {
                    if (message != null && message instanceof TextMessage) {

                        TextMessage textMessage = (TextMessage) message;

                        jmsMessageID = textMessage.getJMSMessageID();
                        //String c01 = textMessage.getStringProperty("c01");
                        System.out.println("消息属性" + jmsMessageID);
                        System.out.println("消费消息:" + textMessage.getText());
                        this.insertSql("1");
                        //手动签收
                        textMessage.acknowledge();

                    } else if (message != null && message instanceof MapMessage) {
                        MapMessage mapMessage = (MapMessage) message;
                        String k1 = mapMessage.getString("k1");
                        System.out.println(k1);
                        mapMessage.acknowledge();
                    }

                } catch (Exception e) {
                    log.error("消费异常:"+e);
                    try {
                        log.warn("开始调用重新消费"+jmsMessageID);
                        session.recover();
                    } catch (JMSException ex) {
                        ex.printStackTrace();
                    }
                }
            });
        } catch (JMSException e) {
            session.recover();
            //如果报错了，手动异常
            e.printStackTrace();
            return false;
        }
//保证控制台不灭
        System.in.read();
// session.commit();
        consumer.close();
        session.close();
        connection.close();
        System.out.println("消费完成!");
//1.先生产后启动一号和二号消费者
//两个消费者等候消息，会一人一半，会存在轮休的味道
        return true;
    }


    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public boolean insertSql(String str) throws Exception {

        TestUser testUser = new TestUser();
        testUser.setUserAge("2");
        testUser.setUserName("aa");
        testUser.setCreateTime(new Date());

        testUserMapper.insertSelective(testUser);
        return true;

    }

}
