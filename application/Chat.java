package application;
	
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.TextAlignment;


public class Chat extends Application {
	
	private ListView<String> conversation;
	private ListView<String> roaster;
	private Label serverName;
	
	private ncClient client;
	
	public static final ObservableList<String> messages = FXCollections.observableArrayList();
	public static final ObservableList<String> users = FXCollections.observableArrayList();
	
	Thread messageListen;
	
	@Override
	public void start(Stage primaryStage){
		try {
			//Parent root = FXMLLoader.load(getClass().getResource("ncClient.fxml"));
			BorderPane root = new BorderPane();
			serverName = new Label("No Connection");
			conversation = new ListView<String>();
			roaster = new ListView<String>();
			TextField input = new TextField();
			
			serverName.setTextAlignment(TextAlignment.CENTER);
			
			root.setTop(serverName);
			root.setCenter(conversation);
			root.setRight(roaster);
			root.setBottom(input);
			
			input.setOnKeyPressed( key ->{
				if (key.getCode().equals(KeyCode.ENTER)){
		    		handleInput(input.getText());
		    		input.setText("");
		    	}
			});
			
			connect("sosial.tv");
			//listUsers();
			listenForMessages();
			
			
			
			
			roaster.setItems(users);
			conversation.setItems(messages);
			
			//initialize list and motd
			client.sendMessage("/list");
			client.sendMessage("/motd");
			
			Scene scene = new Scene(root, 600, 400);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			
			primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {	        
				@Override
	            public void handle(WindowEvent t) {
	            	System.exit(0);
	            }
	        });
			
			primaryStage.setTitle("NoCase Chat");
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
		
		
	}
	
	public void handleInput(String input){
		
		try{
			if(input.startsWith("/connect")){
				String host = "sosial.tv";
				client = new ncClient(host,1337, false);
				serverName.setText(host);
				client.sendMessage("/motd");
				
			}else{
				client.sendMessage(input);
			}
		
		}catch(Exception e){
			//Something went wrong
		}
	}
	
	public void connect(String host){
		try {
			client = new ncClient(host,1337, false);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		serverName.setText(host);
	}
	
	public void listenForMessages(){
		messageListen = new Thread(){
			@Override
			public void run(){
				while(true){
				
					try {
						String msg = client.readMessage();
						if(msg.startsWith("LIST")){
							users.clear();
							String[] user = msg.split(" ");
							for(int i = 1; i<user.length; i++)
								users.add(user[i]);
						}else if(msg.startsWith("<Motd> ->")){
							serverName.setText("sosial.tv" + " - " + msg);
						}else if(msg.startsWith("<New Motd> ->")){
							client.sendMessage("/motd");
						}else if(msg.startsWith("<Nick> ->") || msg.startsWith("<Connection> ->")){
							client.sendMessage("/list");
						}else{
							messages.add("[" + client.getTimeStamp() + "]"+ " " + msg);
							client.playNotificationSound();
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						//.printStackTrace();
					}
					
				}
			}
		};
		messageListen.start();
	}
	
	
	
}
