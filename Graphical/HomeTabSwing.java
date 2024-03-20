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

import java.sql.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class HomeTabSwing extends Application {

    private final Map<String, String> userDataMap = new HashMap<>();
    private final Map<String, String> adminDataMap = new HashMap<>();

    // MySQL database connection details
    private static final String DB_URL = "jdbc:mysql://localhost:3306/community_platform";
    private static final String DB_USER = "your_username";
    private static final String DB_PASSWORD = "your_password";

    private Connection connection;

    @Override
    public void start(Stage primaryStage) {
        // Connect to the MySQL database
        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("Connected to the database successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

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
        // Store the user data in the MySQL database
        try {
            String query = "INSERT INTO users (name, password, user_type) VALUES (?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, name);
            statement.setString(2, password);
            statement.setString(3, userType);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
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
        // Validate user data against the MySQL database
        try {
            String query = "SELECT * FROM users WHERE name = ? AND password = ? AND user_type = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, name);
            statement.setString(2, password);
            statement.setString(3, userType);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void showEventsTab(Stage primaryStage, String userType, TextArea activitiesTextArea) {
        EventsTabSwing eventsTab;
        if (userType.equals("Admin")) {
            eventsTab = new EventsTabSwing(userType, true, activitiesTextArea, connection); // Pass true for admin access and the database connection
        } else {
            eventsTab = new EventsTabSwing(userType, false, activitiesTextArea, connection); // Pass false for normal user access and the database connection
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