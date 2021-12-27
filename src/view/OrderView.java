package view;

import controller.OrderViewController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import view.panels.BestellingTablePane;
import view.panels.OrderButtonsPane;

public class OrderView {
	private Stage stage = new Stage();
	private final GridPane grid = new GridPane();
	private Button newOrderButton, closeOrderButton, payButton, toKitchenButton;
	private Label volgnrLabel;
	private ChoiceBox<String> kortingChoice;
	private HBox kortingHBox;
	private OrderButtonsPane orderButtons;
	private OrderViewController controller;
	private BestellingTablePane bestellingTablePane;

	public OrderView(OrderViewController controller){
		this.controller = controller;
		controller.setView(this);

		stage.setTitle("ORDER VIEW");
		stage.initStyle(StageStyle.UTILITY);
		stage.setX(20);
		stage.setY(20);
		Group root = new Group();
		Scene scene = new Scene(root, 650, 650);

		root.getChildren().add(grid);
		formatGrid();

		//---HEADER---
		//Nieuwe bestelling button
		HBox newOrderHBox = getHBox();
		newOrderButton = new Button("Nieuwe bestelling");
		newOrderButton.setCursor(Cursor.HAND);
		newOrderButton.setFont(new Font("Arial", 15));
		newOrderHBox.getChildren().add(newOrderButton);

		newOrderButton.setOnAction(event -> startNewOrder());

		//Bestelling volgnr
		volgnrLabel = new Label("Volgnr: 0");
		volgnrLabel.setFont(new Font("Arial", 15));
		newOrderHBox.getChildren().add(volgnrLabel);

		grid.add(newOrderHBox, 0, 0, 3, 1);

		//Korting choice
		kortingHBox = getHBox();
		kortingHBox.setDisable(true);
		kortingChoice = new ChoiceBox<>(FXCollections.observableArrayList(
				"Goedkoopste broodje gratis", "Test 1", "Test 2"));
		kortingHBox.getChildren().add(kortingChoice);
		grid.add(kortingHBox, 3,0,1,1);


		//---BROODJES EN BELEG BUTTONS---
		orderButtons = new OrderButtonsPane(controller);
		grid.add(orderButtons, 0, 1, 4, 2);


		//---BROODJES TABLE---
		bestellingTablePane = new BestellingTablePane(controller);
		grid.add(bestellingTablePane, 0, 3, 4, 2);
		orderButtons.setBestellingTablePane(bestellingTablePane);
		orderButtons.refreshButtons();
		orderButtons.disableButtons(true);


		//---FOOTER---
		closeOrderButton = new Button("Afsluiten bestelling");
		closeOrderButton.setDisable(true);
		closeOrderButton.setCursor(Cursor.HAND);
		grid.add(closeOrderButton, 0, 5, 1, 1);

		HBox betalenHBox = new HBox(5);
		betalenHBox.setAlignment(Pos.CENTER);
		betalenHBox.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, new CornerRadii(5), BorderStroke.THIN)));
		Label teBetalenLabel = new Label("Te betalen:");
		teBetalenLabel.setFont(new Font("Arial", 15));
		betalenHBox.getChildren().add(teBetalenLabel);

		Label prijsLabel = new Label("-");
		prijsLabel.setFont(new Font("Arial", 20));
		betalenHBox.getChildren().add(prijsLabel);
		grid.add(betalenHBox, 1, 5, 2, 1);

		HBox lastHBox = new HBox(5);
		lastHBox.setAlignment(Pos.CENTER);
		payButton = new Button("Betaal");
		payButton.setDisable(true);
		payButton.setCursor(Cursor.HAND);
		lastHBox.getChildren().add(payButton);

		toKitchenButton = new Button("Naar keuken");
		toKitchenButton.setDisable(true);
		toKitchenButton.setCursor(Cursor.HAND);
		lastHBox.getChildren().add(toKitchenButton);
		grid.add(lastHBox, 3, 5, 1, 1);


		//Stage
		stage.setScene(scene);
		stage.sizeToScene();			
		stage.show();
	}

	private HBox getHBox() {
		HBox hBox = new HBox(10);
		hBox.setPrefHeight(50);
		hBox.setPrefWidth(200);
		hBox.setAlignment(Pos.CENTER);
		return hBox;
	}

	public void formatGrid() {
		//Columns
		for (int i = 0; i < 4; ++i) {
			ColumnConstraints constraints = new ColumnConstraints();
			constraints.setPrefWidth(162);
			constraints.setHgrow(Priority.SOMETIMES);
			grid.getColumnConstraints().add(constraints);
		}
		//Rows
		for (int i = 0; i < 6; ++i) {
			RowConstraints constraints = new RowConstraints();
			constraints.setPrefHeight(30);
			constraints.setVgrow(Priority.SOMETIMES);
			grid.getRowConstraints().add(constraints);
		}

		grid.setVgap(20);
		grid.setHgap(20);
		grid.setPrefWidth(650);
		grid.setPrefHeight(650);
		grid.setPadding(new Insets(20, 20, 20, 20));
		GridPane.setHalignment(grid, HPos.CENTER);
	}

	public void startNewOrder() {
		//Disable start order button
		newOrderButton.setDisable(true);

		//Enable alle andere buttons
		closeOrderButton.setDisable(false);
		payButton.setDisable(false);
		toKitchenButton.setDisable(false);
		kortingHBox.setDisable(false);
		orderButtons.refreshButtons();
		bestellingTablePane.disableButtons(false);

		//Make new object bestelling
		controller.startNewOrder();

		//Set id of the order in the label
		int orderID = controller.getOrderID();
		volgnrLabel.setText("Volgnr: " + orderID);
	}

	public void refreshOrder() {
		orderButtons.refreshButtons();
		bestellingTablePane.refreshTable();
	}
}
