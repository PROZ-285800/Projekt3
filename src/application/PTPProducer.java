package application;

import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.Queue;

import com.sun.messaging.ConnectionFactory;

public class PTPProducer {
	
	public void sendMessage(String message) {
		ConnectionFactory connectionFactory = new com.sun.messaging.ConnectionFactory();
		try {
			((com.sun.messaging.ConnectionFactory) connectionFactory)
					.setProperty(com.sun.messaging.ConnectionConfiguration.imqAddressList, "localhost:7676/jms");
			JMSContext jmsContext = connectionFactory.createContext();
			JMSProducer jmsProducer = jmsContext.createProducer();
			Queue queue = new com.sun.messaging.Queue("ATJQueue");
			jmsProducer.send(queue, message);
			jmsContext.close();
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
}
