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

public class HomeTabSwing extends Application {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/boma"; // Replace with your MySQL server URL
    private static final String DB_USERNAME = "root"; // Replace with your MySQL username
    private static final String DB_PASSWORD = "password"; // Replace with your MySQL password

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();

        // TextArea to display all activities
        TextArea activitiesTextArea = new TextArea();
        activitiesTextArea.setEditable(false);
        root.setCenter(activitiesTextArea);

        // "About" panel
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
        ComboBox<String> userTypeComboBox = new ComboBox<>(FXCollections.observableArrayList("normal", "admin"));

        signUpPane.add(new Label("Enter your name:"), 0, 0);
        signUpPane.add(nameField, 1, 0);
        signUpPane.add(new Label("Enter your password:"), 0, 1);
        signUpPane.add(passwordField, 1, 1);
        signUpPane.add(new Label("user type:"), 0, 2);
        signUpPane.add(userTypeComboBox, 1, 2);

        signUpDialog.getDialogPane().setContent(signUpPane);

        ButtonType signUpButtonType = new ButtonType("Sign Up", ButtonBar.ButtonData.OK_DONE);
        signUpDialog.getDialogPane().getButtonTypes().addAll(signUpButtonType, ButtonType.CANCEL);

        signUpDialog.setResultConverter(param -> {
            if (param == signUpButtonType) {
                String name = nameField.getText();
                String password = passwordField.getText();
                String usertype = userTypeComboBox.getValue();
                if (storeUserData(name, password, usertype)) {
                    return new UserData(name, password, usertype, 0);
                }
            }
            return null;
        });

        signUpDialog.showAndWait().ifPresent(userData -> {
            showLogInDialog(primaryStage, activitiesTextArea);
        });
    }
    private boolean storeUserData(String name, String password, String userType) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
            String checkUsernameQuery = "SELECT COUNT(*) FROM users WHERE username = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkUsernameQuery);
            checkStmt.setString(1, name);
            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            int count = rs.getInt(1);
    
            if (count > 0) {
                // Username already exists, handle the error condition
                System.out.println("Username already exists. Please choose a different username.");
                return false;
            }
    
            String sql = "INSERT INTO users (username, password_hash, user_type) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, name);
            if (userType.equalsIgnoreCase("admin")) {
                stmt.setString(2, "admin123");
            } else {
                stmt.setString(2, password);
            }
            stmt.setString(3, userType.equalsIgnoreCase("admin") ? "admin" : "normal");
            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
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
                String usertype = userTypeComboBox.getValue();
                int userId = validateUserData(name, password, usertype); // Modified to return userId
                if (userId != -1) {
                    // Store the userId in a static variable or other data structure
                    // Example: currentUserId = userId;
                    return new UserData(name, password, usertype, userId);
                }
            }
            return null;
        });
    
        logInDialog.showAndWait().ifPresent(userData -> {
            showEventsTab(primaryStage, userData.getusertype(), activitiesTextArea);
        });
    }
    private int validateUserData(String name, String password, String userType) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
            String sql = "SELECT id, password_hash FROM users WHERE username = ? AND user_type = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, name);
            stmt.setString(2, userType.equalsIgnoreCase("admin") ? "admin" : "normal");
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int userId = rs.getInt("id");
                String storedPasswordHash = rs.getString("password_hash");
                if (userType.equalsIgnoreCase("admin")) {
                    // For admin users, check if the entered password matches the hardcoded password
                    if (password.equals("admin123")) { // Replace "admin123" with your desired admin password
                        return userId;
                    }
                } else if (password.equals(storedPasswordHash)) {
                    return userId; // Normal user with correct password, return userId
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Invalid credentials
    }
    private void showEventsTab(Stage primaryStage, String userType, TextArea activitiesTextArea) {
        EventsTabSwing eventsTab;
        if (userType.equalsIgnoreCase("admin")) { // Use equalsIgnoreCase to ignore case
            eventsTab = new EventsTabSwing(userType, true, activitiesTextArea); // Pass true for admin access
        } else {
            eventsTab = new EventsTabSwing(userType, false, activitiesTextArea); // Pass false for normal user access
        }
    
        // ... rest of the method ...
    
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
        private final String usertype;
        private final int userId;
    
        UserData(String name, String password, String usertype, int userId) {
            this.name = name;
            this.password = password;
            this.usertype = usertype;
            this.userId = userId;
        }
    
        String getName() {
            return name;
        }
    
        String getPassword() {
            return password;
        }
        String getusertype() {
            return usertype;
        }
        int getUserId() {
            return userId;
        }
    }
}