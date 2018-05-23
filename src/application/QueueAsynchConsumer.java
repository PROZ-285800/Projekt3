package application;

import javax.jms.ConnectionFactory;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.TextMessage;

public class QueueAsynchConsumer implements MessageListener {

	@Override
	public void onMessage(Message message) {
		TextMessage textMessage = (TextMessage) message;
		try {
			System.out.printf("Odebrano wiadomość:'%s'\n", textMessage.getText());
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
	
	public void receiveQueueMessagesAsynch() {
		ConnectionFactory connectionFactory = new com.sun.messaging.ConnectionFactory();
		JMSContext jmsContext = connectionFactory.createContext();
		try {
			Queue queue = new com.sun.messaging.Queue("ATJQueue");
			JMSConsumer jmsConsumer = jmsContext.createConsumer(queue);
			jmsConsumer.setMessageListener(new QueueAsynchConsumer());
			while (true) {
				System.out.println("Czekam na wiadomość");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} catch (JMSException e) {
			e.printStackTrace();
		}
		jmsContext.close();
	}

}
