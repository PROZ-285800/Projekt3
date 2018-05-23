package application;

import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Queue;

import com.sun.messaging.ConnectionFactory;

public class PTPConsumer {
	public void receiveQueueMessages() {
		ConnectionFactory connectionFactory = new com.sun.messaging.ConnectionFactory();
		JMSContext jmsContext = connectionFactory.createContext();
		try {
			((com.sun.messaging.ConnectionFactory) connectionFactory).setProperty(com.sun.messaging.ConnectionConfiguration.imqAddressList, "localhost:7676/jms"); // [hostName][:portNumber][/serviceName]
			Queue queue = new com.sun.messaging.Queue("ATJQueue");
			JMSConsumer jmsConsumer = jmsContext.createConsumer(queue);
			String msg;
			while ((msg = jmsConsumer.receiveBody(String.class, 10)) != null) {
			}
			jmsConsumer.close();
		} catch (JMSException e) {
			e.printStackTrace();
		}
		jmsContext.close();
	}
}