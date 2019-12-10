package com.active.common.consumer;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQMessageProducer;
import org.apache.activemq.AsyncCallback;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.*;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

/*
 * @ClassName:ActiveMqTran
 * @Description  解决分布式事物
 * @Author liao
 * @Time 2019/9/5 15:52
 */
public class ActiveMqTran {


    private final static String BROKERURL = "failover:(tcp://192.168.137.128:61616,tcp://192.168.137.128:61617,tcp://192.168.137.128:61618)";
    private final static String QUEUE_NAME = "failoverQueue01";
    private volatile Integer counts = 0;


    public static void main(String[] args) throws Exception {

        ActiveMqTran activeMqTran = new ActiveMqTran();
        activeMqTran.testactivemq();

        /**1、只要存库，就要保证消息一定进入了消息队列生产者，为了保证消息发送成功，
         * 首先存个消息确认表，确认表中存在数据发送的生产消息的消息id和对应是否成功的状态，可以显示0未成功，1成功
         * (兜底)然后用定时任务去扫表，当有0时再次去发送,当确认接收后使用回调方法，将状态改为1
         * 当再次重发还没发送成功,立刻发短信通知运维或者出现告警，是不是他喵的消息队列集群挂了
         * 确认接收时，转变成手动确认处理，防止重复消费将messageId放在redis,如果存在id则不再消费，redis中的数据可以定时清除
         * key就是id,value就是时间戳,定期清除2天以前的
         * 2、如果接收消息异常:那么就要在回调方法里再次重发，如果再次发送还不成功,告警或者发短信然后放入异常数据表，记录这条消息
         * 到mysql
         *
         */
        //


    }




    @Transactional(rollbackFor = Exception.class)
    public void testactivemq() throws Exception {

        ActiveMQConnectionFactory acMqConnection = new ActiveMQConnectionFactory(BROKERURL);
        //开启异步投递
        acMqConnection.setUseAsyncSend(true);
        //设置超时时间
        acMqConnection.setSendTimeout(5000);
        //创建连接工厂
        Connection connection = acMqConnection.createConnection();
        //获得连接
        connection.start();
        //两个参数1：事务2：签收
        /**
         * 当第一个参数为true时，表示使用了事务，现在使用send方法，并不能发送还需要
         * commit提交
         * 解决分布式事务使用手动签收
         */
        Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
        //队列或topic
        //队列
        Queue queue = session.createQueue(QUEUE_NAME);
        //创建消息生产者
        ActiveMQMessageProducer acProducer = (ActiveMQMessageProducer) session.createProducer(queue);
        //持久化
        acProducer.setDeliveryMode(DeliveryMode.PERSISTENT);
        //producer.setTimeToLive(30000L);
        try {
            CountDownLatch countDownLatch = new CountDownLatch(2);
            TextMessage textMessage = session.createTextMessage("text--msg");
            //创建消息
            //  textMessage.setStringProperty("c01", "vip");
            //使用mysql,必须开启持久化
            //消息头id,用来处理异步发送确实发送成功
            textMessage.setJMSMessageID(UUID.randomUUID().toString() + "_liao");
            String jmsMessageID = textMessage.getJMSMessageID();

            //异步成功后返回的信息
            acProducer.send(textMessage, new AsyncCallback() {
                @Override
                public void onSuccess() {
                    System.out.println(jmsMessageID + "===success");
                    //成功以后修改状态
                    countDownLatch.countDown();
                }

                @Override
                public void onException(JMSException e) {
                    try {
                        acProducer.send(textMessage, new AsyncCallback() {
                            @Override
                            public void onSuccess() {
                                countDownLatch.countDown();
                                System.out.println(jmsMessageID + "===success");
                            }

                            @Override
                            public void onException(JMSException e) {
                                countDownLatch.countDown();
                                //再次异常,告警或者给运维发短信
                                System.out.println("s");
                                //需要抛出异常
                                test2(1);
                            }
                        });
                    } catch (JMSException ex) {
                        test2(1);
                        // ex.printStackTrace();
                    }
                }
            });
            //阻塞主线程
            countDownLatch.await();
            if (counts != 1) {
                counts = 0;
                throw new Exception("回滚");
            }
            acProducer.close();
            //在生产者关闭前添加提交
            // session.commit();
            System.out.println("生产完成!");
        } catch (JMSException e) {
            //消息队列回滚
            //  session.rollback();
            e.printStackTrace();
        } finally {
            if (session != null) {
                session.close();
            }
            if (connection != null) {
                connection.close();
            }
        }
    }

    public Integer test2(Integer count) {
        counts = count;
        return counts;
    }
}
