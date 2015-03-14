package client;

public class Gui extends javafx.application.Application
{
	private Client client; // Initialized from outside

	public void begin(Client client)
	{
		this.client = client;
		this.launch();
	}

	@Override
	public void start(javafx.stage.Stage stage) 
	{
		stage.setTitle("Calendar System");
		stage.getIcons().add(new javafx.scene.image.Image("file:icon.png"));
		javafx.scene.control.Button button = new javafx.scene.control.Button();
		button.setText("Say 'Hello World'");
		button.setOnAction
		(
			new javafx.event.EventHandler<javafx.event.ActionEvent>()
			{
				@Override
				public void handle(javafx.event.ActionEvent event)
				{
					System.out.println("Hello World!");
				}
			}
		);
		
		javafx.scene.layout.StackPane root = new javafx.scene.layout.StackPane();
		root.getChildren().add(button);
		stage.setScene(new javafx.scene.Scene(root, 300, 250));
		stage.show();
	}
}