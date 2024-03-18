package Graphical;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.HashMap;

public class HomeTabSwing extends Application {

    private final HashMap<String, String> userDataMap = new HashMap<>();
    private final HashMap<String, String> adminDataMap = new HashMap<>();

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();

        // Create a TextArea to display all activities
        TextArea activitiesTextArea = new TextArea();
        activitiesTextArea.setEditable(false);
        root.setCenter(activitiesTextArea);

        // Create the "About" panel
        VBox aboutPanel = new VBox();
        Label welcomeLabel = new Label("Welcome to the community official platform");
        Button signUpButton = new Button("Sign Up");
        Button logInButton = new Button("Log In");
        aboutPanel.getChildren().addAll(welcomeLabel, signUpButton, logInButton);
        aboutPanel.setAlignment(Pos.CENTER);
        root.setTop(aboutPanel);

        // ActionListener for the "Sign Up" button
        signUpButton.setOnAction(e -> showSignUpDialog(primaryStage, activitiesTextArea));

        // ActionListener for the "Log In" button
        logInButton.setOnAction(e -> showLogInDialog(primaryStage, activitiesTextArea));

        Scene scene = new Scene(root, 400, 400);
        primaryStage.setTitle("Home Tab (JavaFX)");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void showSignUpDialog(Stage primaryStage, TextArea activitiesTextArea) {
        Dialog<UserData> signUpDialog = new Dialog<>();
        signUpDialog.setTitle("Sign Up");
        signUpDialog.setHeaderText("Enter your information");

        GridPane signUpPane = new GridPane();
        signUpPane.setPadding(new Insets(10));
        signUpPane.setHgap(10);
        signUpPane.setVgap(10);

        TextField nameField = new TextField();
        PasswordField passwordField = new PasswordField();
        ComboBox<String> userTypeComboBox = new ComboBox<>(FXCollections.observableArrayList("Normal User", "Admin"));

        signUpPane.add(new Label("Enter your name:"), 0, 0);
        signUpPane.add(nameField, 1, 0);
        signUpPane.add(new Label("Enter your password:"), 0, 1);
        signUpPane.add(passwordField, 1, 1);
        signUpPane.add(new Label("User Type:"), 0, 2);
        signUpPane.add(userTypeComboBox, 1, 2);

        signUpDialog.getDialogPane().setContent(signUpPane);

        ButtonType signUpButtonType = new ButtonType("Sign Up", ButtonBar.ButtonData.OK_DONE);
        signUpDialog.getDialogPane().getButtonTypes().addAll(signUpButtonType, ButtonType.CANCEL);

        signUpDialog.setResultConverter(param -> {
            if (param == signUpButtonType) {
                String name = nameField.getText();
                String password = passwordField.getText();
                String userType = userTypeComboBox.getValue();
                storeUserData(name, password, userType);
                return new UserData(name, password, userType);
            }
            return null;
        });

        signUpDialog.showAndWait().ifPresent(userData -> {
            showLogInDialog(primaryStage, activitiesTextArea);
        });
    }

    private void storeUserData(String name, String password, String userType) {
        // Store the user data in the appropriate HashMap
        if (userType.equals("Admin")) {
            adminDataMap.put(name, password);
        } else {
            userDataMap.put(name, password);
        }
    }

    private void showLogInDialog(Stage primaryStage, TextArea activitiesTextArea) {
        Dialog<UserData> logInDialog = new Dialog<>();
        logInDialog.setTitle("Log In");
        logInDialog.setHeaderText("Enter your credentials");

        GridPane logInPane = new GridPane();
        logInPane.setPadding(new Insets(10));
        logInPane.setHgap(10);
        logInPane.setVgap(10);

        TextField nameField = new TextField();
        PasswordField passwordField = new PasswordField();
        ComboBox<String> userTypeComboBox = new ComboBox<>(FXCollections.observableArrayList("Normal User", "Admin"));

        logInPane.add(new Label("Enter your name:"), 0, 0);
        logInPane.add(nameField, 1, 0);
        logInPane.add(new Label("Enter your password:"), 0, 1);
        logInPane.add(passwordField, 1, 1);
        logInPane.add(new Label("User Type:"), 0, 2);
        logInPane.add(userTypeComboBox, 1, 2);

        logInDialog.getDialogPane().setContent(logInPane);

        ButtonType logInButtonType = new ButtonType("Log In", ButtonBar.ButtonData.OK_DONE);
        logInDialog.getDialogPane().getButtonTypes().addAll(logInButtonType, ButtonType.CANCEL);

        logInDialog.setResultConverter(param -> {
            if (param == logInButtonType) {
                String name = nameField.getText();
                String password = passwordField.getText();
                String userType = userTypeComboBox.getValue();
                if (validateUserData(name, password, userType)) {
                    return new UserData(name, password, userType);
                }
            }
            return null;
        });

        logInDialog.showAndWait().ifPresent(userData -> {
            showEventsTab(primaryStage, userData.getUserType(), activitiesTextArea);
        });
    }

    private boolean validateUserData(String name, String password, String userType) {
        // Checking against the stored HashMaps
        HashMap<String, String> dataMap = userType.equals("Admin") ? adminDataMap : userDataMap;
        String storedPassword = dataMap.get(name);
        return storedPassword != null && storedPassword.equals(password);
    }

    private void showEventsTab(Stage primaryStage, String userType, TextArea activitiesTextArea) {
        EventsTabSwing eventsTab;
        if (userType.equals("Admin")) {
            eventsTab = new EventsTabSwing(userType, true, activitiesTextArea); // Pass true for admin access
        } else {
            eventsTab = new EventsTabSwing(userType, false, activitiesTextArea); // Pass false for normal user access
        }

        Scene scene = new Scene(eventsTab, 600, 400);
        Stage eventsStage = new Stage();
        eventsStage.setTitle("Events Tab");
        eventsStage.setScene(scene);
        eventsStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static class UserData {
        private final String name;
        private final String password;
        private final String userType;

        UserData(String name, String password, String userType) {
            this.name = name;
            this.password = password;
            this.userType = userType;
        }

        String getName() {
            return name;
        }

        String getPassword() {
            return password;
        }

        String getUserType() {
            return userType;
        }
    }
}