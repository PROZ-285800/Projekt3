package application;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import javax.jms.ConnectionFactory;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.TextMessage;

public class TicTacToeController {

	@FXML
	private TextField leftText;

	@FXML
	Button btn_reset;

	@FXML
	private TextField rightText;

	@FXML
	private Button btn0_0;

	@FXML
	private Button btn0_1;

	@FXML
	private Button btn0_2;

	@FXML
	private Button btn1_0;

	@FXML
	private Button btn1_1;

	@FXML
	private Button btn1_2;

	@FXML
	private Button btn2_0;

	@FXML
	private Button btn2_1;

	@FXML
	private Button btn2_2;

	private boolean myTurn;

	private String sign;

	List<Button> buttonList;

	private PTPProducer ptpProducer = new PTPProducer();

	private QueueAsynchConsumer ptpConsumer = new QueueAsynchConsumer();

	private String playerHash;

	///////////////////////////////////////////////////////////////////////////////////////////////
	@FXML
	private void initialize() {
		buttonList = new ArrayList<>();
		addButtonsToList();
		for (Button button : buttonList) {
			button.setText("-");
		}
		myTurn = true;
		playerHash = Integer.toString(Double.hashCode(Math.random()));
		System.out.println(playerHash);
		sign="X";		
		
		// START listening for messages
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				ptpConsumer.receiveQueueMessagesAsynch();
			}
		}).start();
	}

	

	private void func(Button button) {
		if (myTurn != true || button.getText().equals("X") || button.getText().equals("Y") ) {
			return;
		} else {
			button.setText(sign);
			ptpConsumer.clearBuffer();
			ptpProducer.sendMessage(buildMessage());
			myTurn = false;
		}
		
		if (checkWinning()) {
			buttonList.stream().forEach(btn -> {
				btn.setDisable(true);
			});

		} else if (checkDraw()) {
			leftText.setText("REMIS");
		}
		
	}

	private String buildMessage() {
		StringBuilder builder = new StringBuilder();
		builder.append(playerHash).append("|").append(buildActualPlatform()).append("|").append(sign);
		return builder.toString();
	}
	

	private boolean checkDraw() {
		for (Button button : buttonList) {
			if (button.getText().equals("-")) {
				return false;
			}

		}
		return true;
	}

	private String buildActualPlatform() {
		return new StringBuilder("").append(btn0_0.getText()).append(btn0_1.getText()).append(btn0_2.getText())
				.append(btn1_0.getText()).append(btn1_1.getText()).append(btn1_2.getText()).append(btn2_0.getText())
				.append(btn2_1.getText()).append(btn2_2.getText()).toString();
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////


	public class QueueAsynchConsumer implements MessageListener {

		@Override
		public void onMessage(Message message) {
			TextMessage textMessage = (TextMessage) message;
			
			try {
				System.out.println(textMessage.getText());
				String hash = getHashFromMessage(textMessage.getText());
			
				// bugFix
				if(hash == null ) {
					return;
				}
				// message sent from this player, resend
				if (hash.equals(playerHash)) {
					ptpProducer.sendMessage(buildMessage());
					
				// message came from another player	
				} else {
					String platform = getPlatformFromMessage(textMessage.getText());
					
			
					String sign_ = getSingFromMessage(textMessage.getText());
					
					Platform.runLater(new Runnable() {
						
						@Override
						public void run() {
							for(int i=0; i<9 ; i++) {
								buttonList.get(i).setText(platform.substring(i, i+1));
							}
							if (checkWinning() == true) {
								buttonList.stream().forEach(btn -> {
									btn.setDisable(true);
								});
							}
							
							if (checkDraw() == true) {
								
							}
						
							myTurn = true;
						}
					});
					
					if (sign_.equals("X")) {
						sign = "O";
					} else {
						sign = "X";
					}
				}
			} catch (JMSException e) {
				// TODO Auto-generated catch block
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
					System.out.println("Oczekuje na wiadomość");
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			} catch (JMSException e) {
				e.printStackTrace();
			}
			jmsContext.close();
		}
		
		public void clearBuffer() {
			ConnectionFactory connectionFactory = new com.sun.messaging.ConnectionFactory();
			JMSContext jmsContext = connectionFactory.createContext();
			try {
				((com.sun.messaging.ConnectionFactory) connectionFactory).setProperty(com.sun.messaging.ConnectionConfiguration.imqAddressList, "localhost:7676/jms"); // [hostName][:portNumber][/serviceName]
				Queue queue = new com.sun.messaging.Queue("ATJQueue");
				JMSConsumer jmsConsumer = jmsContext.createConsumer(queue);
				String msg;
				while ((msg = jmsConsumer.receiveBody(String.class, 10)) != null) {
					System.out.printf("Odebrano wiadomość:'%s'\n", msg);
				}
				jmsConsumer.close();
			} catch (JMSException e) {
				e.printStackTrace();
			}
			jmsContext.close();
		}
		
		private String getHashFromMessage(String message) {
			String mes = null;
			try {
				mes = message.substring(0, message.indexOf('|'));
			} catch (Exception e) {
				// TODO: handle exception
			}
			return mes;
		}
		
		private String getPlatformFromMessage(String message) {
			return message.substring(message.indexOf('|')+1, message.indexOf('|') + 10);
		}
		
		private String getSingFromMessage(String message) {
			return message.substring(message.length()-1, message.length());
		}
	}
	///////////////////////////////////////////////////////////////////////////////////////////////

	
	private boolean checkWinning() {

		String platform = buildActualPlatform();
		boolean win = false;
		if (platform.charAt(0) == 'X' && platform.charAt(1) == 'X' && platform.charAt(2) == 'X') {
			win = true;
		}

		if (platform.charAt(3) == 'X' && platform.charAt(4) == 'X' && platform.charAt(5) == 'X') {
			win = true;
		}

		if (platform.charAt(6) == 'X' && platform.charAt(7) == 'X' && platform.charAt(8) == 'X') {
			win = true;
		}

		if (platform.charAt(0) == 'X' && platform.charAt(3) == 'X' && platform.charAt(6) == 'X') {
			win = true;
		}

		if (platform.charAt(1) == 'X' && platform.charAt(4) == 'X' && platform.charAt(7) == 'X') {
			win = true;
		}

		if (platform.charAt(2) == 'X' && platform.charAt(5) == 'X' && platform.charAt(8) == 'X') {
			win = true;
		}

		if (platform.charAt(0) == 'X' && platform.charAt(4) == 'X' && platform.charAt(8) == 'X') {
			win = true;
		}

		if (platform.charAt(2) == 'X' && platform.charAt(4) == 'X' && platform.charAt(6) == 'X') {
			win = true;
		}

		if (win == true && sign.equals("X") ) {
			leftText.setText("WYGRANA");
			return true;
		}
		
		if( win == true && sign.equals("O")) {
			leftText.setText("PRZEGRANA");
		}

		if (platform.charAt(0) == 'O' && platform.charAt(1) == 'O' && platform.charAt(2) == 'O') {
			win = true;
		}

		if (platform.charAt(3) == 'O' && platform.charAt(4) == 'O' && platform.charAt(5) == 'O') {
			win = true;
		}

		if (platform.charAt(6) == 'O' && platform.charAt(7) == 'O' && platform.charAt(8) == 'O') {
			win = true;
		}

		if (platform.charAt(0) == 'O' && platform.charAt(3) == 'O' && platform.charAt(6) == 'O') {
			win = true;
		}

		if (platform.charAt(1) == 'O' && platform.charAt(4) == 'O' && platform.charAt(7) == 'O') {
			win = true;
		}

		if (platform.charAt(2) == 'O' && platform.charAt(5) == 'O' && platform.charAt(8) == 'O') {
			win = true;
		}

		if (platform.charAt(0) == 'O' && platform.charAt(4) == 'O' && platform.charAt(8) == 'O') {
			win = true;
		}

		if (platform.charAt(2) == 'O' && platform.charAt(4) == 'O' && platform.charAt(6) == 'O') {
			win = true;
		}

		if (win == true  && sign.equals("O")) {
			leftText.setText("WYGRANA");
			return true;
		}
		
		if( win == true && sign.equals("X")) {
			leftText.setText("PRZEGRANA");
		}

		return win;
	}
	
	private void addButtonsToList() {
		buttonList.add(btn0_0);
		buttonList.add(btn0_1);
		buttonList.add(btn0_2);
		buttonList.add(btn1_0);
		buttonList.add(btn1_1);
		buttonList.add(btn1_2);
		buttonList.add(btn2_0);
		buttonList.add(btn2_1);
		buttonList.add(btn2_2);
	}
	
	
	@FXML
	private void btn0_0Click() {
		func(btn0_0);
	}

	@FXML
	private void btn0_1Click() {
		func(btn0_1);
	}

	@FXML
	private void btn0_2Click() {
		func(btn0_2);
	}

	@FXML
	private void btn1_0Click() {
		func(btn1_0);
	}

	@FXML
	private void btn1_1Click() {
		func(btn1_1);
	}

	@FXML
	private void btn1_2Click() {
		func(btn1_2);
	}

	@FXML
	private void btn2_0Click() {
		func(btn2_0);
	}

	@FXML
	private void btn2_1Click() {
		func(btn2_1);
	}

	@FXML
	private void btn2_2Click() {
		func(btn2_2);
	}
	
	@FXML
	private void btn_resetClick() {
		for(Button btn : buttonList) {
			btn.setDisable(false);
			btn.setText("-");
			
		}
		leftText.setText(" ");
		sign = "X";
		myTurn = false; 
		ptpProducer.sendMessage(buildMessage());
	}
}
