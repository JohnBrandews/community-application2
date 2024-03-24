package Graphical;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

public class EventsTabSwing extends BorderPane {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/boma"; // Replace with your MySQL server URL
    private static final String DB_USERNAME = "your_username"; // Replace with your MySQL username
    private static final String DB_PASSWORD = "your_password"; // Replace with your MySQL password

    private TextArea eventTextArea;
    private Button submitButton;
    private Button deleteButton;
    private Button editButton;
    private boolean isAdmin;
    private TextArea activitiesTextArea;
    private ObservableList<String> allActivities;

    public EventsTabSwing(String userType, boolean isAdmin, TextArea activitiesTextArea) {
        this.isAdmin = isAdmin;
        this.activitiesTextArea = activitiesTextArea;
        this.allActivities = FXCollections.observableArrayList();

        // Create a panel for the buttons
        HBox buttonPanel = new HBox();

        // Create buttons for event categories
        Button weddingsButton = new Button("Weddings");
        Button burialsButton = new Button("Burials");
        Button sportsButton = new Button("Sports Activities");
        Button voluntaryButton = new Button("Voluntary Activities");

        // Add action listeners to buttons
        weddingsButton.setOnAction(e -> showEventUI("Weddings"));
        burialsButton.setOnAction(e -> showEventUI("Burials"));
        sportsButton.setOnAction(e -> showEventUI("Sports Activities"));
        voluntaryButton.setOnAction(e -> showEventUI("Voluntary Activities"));

        // Add buttons to the button panel
        buttonPanel.getChildren().addAll(weddingsButton, burialsButton, sportsButton, voluntaryButton);

        // Add the button panel to the main panel
        setTop(buttonPanel);

        // Initialize the event panel with the "Weddings" category
        showEventUI("Weddings");
    }

    private void showEventUI(String category) {
        BorderPane eventPanel = new BorderPane();

        eventTextArea = new TextArea();
        eventTextArea.setPromptText("Enter event details...");
        eventTextArea.setWrapText(true);
        eventPanel.setCenter(eventTextArea);

        submitButton = new Button("Submit Event");
        submitButton.setOnAction(e -> showEventDialog(category));

        if (isAdmin) {
            // Add additional functionality for admins, e.g., delete event button
            deleteButton = new Button("Delete Event");
            deleteButton.setOnAction(e -> deleteEvent(category));
            editButton = new Button("Edit Event");
            editButton.setOnAction(e -> editEvent(category));
            eventPanel.setBottom(new HBox(submitButton, deleteButton, editButton));
        } else {
            eventPanel.setBottom(submitButton);
        }

        VBox eventsDisplay = new VBox();
        displayEvents(category, eventsDisplay);
        eventPanel.setTop(eventsDisplay);

        setCenter(eventPanel);
    }

    private void showEventDialog(String category) {
        Dialog<Event> eventDialog = new Dialog<>();
        eventDialog.setTitle("New Event");
        eventDialog.setHeaderText("Enter event details");

        DialogPane dialogPane = eventDialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField eventNameField = new TextField();
        eventNameField.setPromptText("Event Name");
        DatePicker eventDatePicker = new DatePicker();
        eventDatePicker.setPromptText("Event Date");

        grid.add(new Label("Event Name:"), 0, 0);
        grid.add(eventNameField, 1, 0);
        grid.add(new Label("Event Date:"), 0, 1);
        grid.add(eventDatePicker, 1, 1);

        dialogPane.setContent(grid);

        eventDialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                String eventName = eventNameField.getText();
                LocalDateTime eventDateTime = eventDatePicker.getValue().atStartOfDay();
                try (Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
                    String sql = "INSERT INTO events (name, category, event_date, created_by) VALUES (?, ?, ?, ?)";
                    PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
                    stmt.setString(1, eventName);
                    stmt.setString(2, category);
                    stmt.setDate(3, java.sql.Date.valueOf(eventDateTime.toLocalDate()));
                    
                    stmt.executeUpdate();

                    ResultSet rs = stmt.getGeneratedKeys();
                    if (rs.next()) {
                        int eventId = rs.getInt(1);
                        Event newEvent = new Event(eventId, eventName, eventDateTime);
                        allActivities.add(newEvent.toString());
                        updateActivitiesTextArea();
                        return newEvent;
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            return null;
        });

        Optional<Event> result = eventDialog.showAndWait();
        result.ifPresent(newEvent -> {
            displayEvents(category, null);
        });
    }

    private void deleteEvent(String category) {
        // Show a dialog to select the event to delete
        Dialog<Event> dialog = new Dialog<>();
        dialog.setTitle("Delete Event");
        dialog.setHeaderText("Select the event to delete");

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
            String sql = "SELECT id, name, event_date FROM events WHERE category = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, category);
            ResultSet rs = stmt.executeQuery();
            ObservableList<Event> events = FXCollections.observableArrayList();
            while (rs.next()) {
                int eventId = rs.getInt("id");
                String eventName = rs.getString("name");
                LocalDateTime eventDateTime = rs.getDate("event_date").toLocalDate().atStartOfDay();
                Event event = new Event(eventId, eventName, eventDateTime);
                events.add(event);
            }

            // Add event list to the dialog
            VBox eventsVBox = new VBox();
            for (Event event : events) {
                Label eventLabel = new Label(event.toString());
                eventsVBox.getChildren().add(eventLabel);
            }
            dialogPane.setContent(eventsVBox);

            // Handle event selection and deletion
            dialog.setResultConverter(buttonType -> {
                if (buttonType == ButtonType.OK) {
                    // TODO: Implement event deletion logic
                }
                return null;
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }

        dialog.showAndWait();
    }

    private void editEvent(String category) {
        // TODO: Implement edit event logic
    }

    private void displayEvents(String category, VBox eventsDisplay) {
        if (eventsDisplay != null) {
            eventsDisplay.getChildren().clear();
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
            String sql = "SELECT id, name, event_date FROM events WHERE category = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, category);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int eventId = rs.getInt("id");
                String eventName = rs.getString("name");
                LocalDateTime eventDateTime = rs.getDate("event_date").toLocalDate().atStartOfDay();
                Event event = new Event(eventId, eventName, eventDateTime);
                if (eventsDisplay != null) {
                    Label eventLabel = new Label(event.toString());
                    eventsDisplay.getChildren().add(eventLabel);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateActivitiesTextArea() {
        activitiesTextArea.clear();
        for (String activity : allActivities) {
            activitiesTextArea.appendText(activity + "\n");
        }
    }

    private static class Event {
        private final int id;
        private final String name;
        private final LocalDateTime dateTime;

        Event(int id, String name, LocalDateTime dateTime) {
            this.id = id;
            this.name = name;
            this.dateTime = dateTime;
        }

        int getId() {
            return id;
        }

        String getName() {
            return name;
        }

        LocalDateTime getDateTime() {
            return dateTime;
        }

        @Override
        public String toString() {
            return name + " - " + dateTime.toString();
        }
    }
}