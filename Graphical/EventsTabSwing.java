package Graphical;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventsTabSwing extends BorderPane {
    private final Map<String, ObservableList<String>> eventCategories = new HashMap<>();
    private TextArea eventTextArea;
    private Button submitButton;
    private Button deleteButton;
    private boolean isAdmin;

    public EventsTabSwing(String userType, List<String> allActivities, boolean isAdmin) {
        this.isAdmin = isAdmin;

        // Create a panel for the buttons
        HBox buttonPanel = new HBox();

        // Create buttons for event categories
        Button weddingsButton = new Button("Weddings");
        Button burialsButton = new Button("Burials");
        Button sportsButton = new Button("Sports Activities");
        Button voluntaryButton = new Button("Voluntary Activities");

        // Initialize event lists for each category
        eventCategories.put("Weddings", FXCollections.observableArrayList());
        eventCategories.put("Burials", FXCollections.observableArrayList());
        eventCategories.put("Sports Activities", FXCollections.observableArrayList());
        eventCategories.put("Voluntary Activities", FXCollections.observableArrayList());

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
        submitButton.setOnAction(e -> {
            String eventText = eventTextArea.getText();
            if (!eventText.isEmpty()) {
                eventCategories.get(category).add(eventText);
                eventTextArea.clear();
                displayEvents(category, null);
            }
        });

        if (isAdmin) {
            // Add additional functionality for admins, e.g., delete event button
            deleteButton = new Button("Delete Event");
            deleteButton.setOnAction(e -> deleteEvent(category));
            eventPanel.setBottom(new HBox(submitButton, deleteButton));
        } else {
            eventPanel.setBottom(submitButton);
        }

        VBox eventsDisplay = new VBox();
        displayEvents(category, eventsDisplay);
        eventPanel.setTop(eventsDisplay);

        setCenter(eventPanel);
    }

    private void deleteEvent(String category) {
        // Implement delete event functionality for admins
        // e.g., show a dialog to select the event to delete and remove it from the list
    }

    private void displayEvents(String category, VBox eventsDisplay) {
        eventsDisplay.getChildren().clear();
        for (String event : eventCategories.get(category)) {
            Label eventLabel = new Label(event);
            eventsDisplay.getChildren().add(eventLabel);
        }
    }
}