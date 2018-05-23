package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Main extends Application {
	
	@Override
	public void start(Stage primaryStage) {
		try {
				FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("./Window.fxml"));
				GridPane root = fxmlLoader.load();
				Scene scene = new Scene(root);
				primaryStage.setScene(scene);
				primaryStage.setTitle("TicTacToeGame");
				primaryStage.setOnHiding( e -> primaryStage_Hiding(e, fxmlLoader));
				primaryStage.show();
		} catch(Exception e) { 
			e.printStackTrace();
		}
		
	}
	
	public static void main(String[] args) {
		launch(args);
	}
	
	private void primaryStage_Hiding(WindowEvent e, FXMLLoader fxmlLoader) {
		System.out.println("");
		new PTPConsumer().receiveQueueMessages();
		System.exit(1);
	}

}
