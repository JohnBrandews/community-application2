package Graphical;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class EventsTabSwing extends BorderPane {
    private final Map<String, ObservableList<Event>> eventCategories = new HashMap<>();
    private TextArea eventTextArea;
    private Button submitButton;
    private Button deleteButton;
    private Button editButton;
    private Button chatButton;
    private Button recentMessagesButton;
    private boolean isAdmin;
    private TextArea activitiesTextArea;
    private ObservableList<String> allActivities;
    private ObservableList<String> userMessages;
    private TextArea noticeTextArea;

    public EventsTabSwing(String userType, boolean isAdmin, TextArea activitiesTextArea) {
        this.isAdmin = isAdmin;
        this.activitiesTextArea = activitiesTextArea;
        this.allActivities = FXCollections.observableArrayList();
        this.userMessages = FXCollections.observableArrayList();

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

        // Add chat button and recent messages button if applicable
        if (!isAdmin) {
            chatButton = new Button("Chat with Admin");
            chatButton.setOnAction(e -> showChatDialog());
            setBottom(chatButton);
        } else {
            recentMessagesButton = new Button("Recent Messages");
            recentMessagesButton.setOnAction(e -> showRecentMessages());

            Button postNoticeButton = new Button("Post Notice");
            postNoticeButton.setOnAction(e -> showPostNoticeDialog());

            HBox adminButtons = new HBox(recentMessagesButton, postNoticeButton);
            setBottom(adminButtons);

            noticeTextArea = new TextArea();
            noticeTextArea.setEditable(false);
            noticeTextArea.setPromptText("No notice posted yet.");
            setRight(noticeTextArea);
        }
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
                return new Event(eventName, eventDateTime);
            }
            return null;
        });

        Optional<Event> result = eventDialog.showAndWait();
        result.ifPresent(newEvent -> {
            eventCategories.get(category).add(newEvent);
            displayEvents(category, null);
            allActivities.add(newEvent.toString());
            updateActivitiesTextArea();
            eventTextArea.clear(); // Clear the eventTextArea after submitting the event
        });
    }

    private void deleteEvent(String category) {
        // Show a dialog to select the event to delete
        ObservableList<Event> events = eventCategories.get(category);
        ChoiceDialog<Event> dialog = new ChoiceDialog<>(null, events);
        dialog.setTitle("Delete Event");
        dialog.setHeaderText("Select the event to delete");
        dialog.setContentText("Event:");

        Optional<Event> result = dialog.showAndWait();
        result.ifPresent(selectedEvent -> {
            events.remove(selectedEvent);
            displayEvents(category, null);
            allActivities.remove(selectedEvent.toString());
            updateActivitiesTextArea();
        });
    }

    private void editEvent(String category) {
        // Show a dialog to select the event to edit
        ObservableList<Event> events = eventCategories.get(category);
        ChoiceDialog<Event> dialog = new ChoiceDialog<>(null, events);
        dialog.setTitle("Edit Event");
        dialog.setHeaderText("Select the event to edit");
        dialog.setContentText("Event:");

        Optional<Event> result = dialog.showAndWait();
        result.ifPresent(selectedEvent -> {
            // Create a new dialog to edit the event details
            Dialog<Event> editDialog = new Dialog<>();
            editDialog.setTitle("Edit Event");
            editDialog.setHeaderText("Edit the event details");

            DialogPane editDialogPane = editDialog.getDialogPane();
            editDialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            GridPane editGrid = new GridPane();
            editGrid.setHgap(10);
            editGrid.setVgap(10);
            editGrid.setPadding(new Insets(20, 150, 10, 10));

            TextField editEventNameField = new TextField(selectedEvent.getName());
            DatePicker editEventDatePicker = new DatePicker(selectedEvent.getDateTime().toLocalDate());

            editGrid.add(new Label("Event Name:"), 0, 0);
            editGrid.add(editEventNameField, 1, 0);
            editGrid.add(new Label("Event Date:"), 0, 1);
            editGrid.add(editEventDatePicker, 1, 1);

            editDialogPane.setContent(editGrid);

            editDialog.setResultConverter(dialogButton -> {
                if (dialogButton == ButtonType.OK) {
                    String newEventName = editEventNameField.getText();
                    LocalDateTime newEventDateTime = editEventDatePicker.getValue().atStartOfDay();
                    return new Event(newEventName, newEventDateTime);
                }
                return null;
            });

            Optional<Event> editResult = editDialog.showAndWait();
            editResult.ifPresent(newEvent -> {
                int index = events.indexOf(selectedEvent);
                events.set(index, newEvent);
                displayEvents(category, null);
                int activityIndex = allActivities.indexOf(selectedEvent.toString());
                allActivities.set(activityIndex, newEvent.toString());
                updateActivitiesTextArea();
            });
        });
    }

    private void displayEvents(String category, VBox eventsDisplay) {
        if (eventsDisplay != null) {
            eventsDisplay.getChildren().clear();
            for (Event event : eventCategories.get(category)) {
                Label eventLabel = new Label(event.toString());
                eventsDisplay.getChildren().add(eventLabel);
            }
        }
    }

    private void updateActivitiesTextArea() {
        activitiesTextArea.clear();
        for (String activity : allActivities) {
            activitiesTextArea.appendText(activity + "\n");
        }
    }

    private void showChatDialog() {
        Dialog<String> chatDialog = new Dialog<>();
        chatDialog.setTitle("Chat with Admin");
        chatDialog.setHeaderText("Enter your message for the admin");

        DialogPane dialogPane = chatDialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextArea messageTextArea = new TextArea();
        messageTextArea.setPromptText("Write your message here...");
        dialogPane.setContent(messageTextArea);

        chatDialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return messageTextArea.getText();
            }
            return null;
        });

        Optional<String> result = chatDialog.showAndWait();
        result.ifPresent(message -> {
            userMessages.add(message); // Update the userMessages list with the new message
        });
    }

    private void showRecentMessages() {
        Dialog<Void> messagesDialog = new Dialog<>();
        messagesDialog.setTitle("Recent Messages");
        messagesDialog.setHeaderText("Messages from users");

        DialogPane dialogPane = messagesDialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.CLOSE);

        TextArea messagesTextArea = new TextArea();
        messagesTextArea.setEditable(false);
        for (String message : userMessages) {
            messagesTextArea.appendText(message + "\n");
        }
        dialogPane.setContent(messagesTextArea);

        messagesDialog.showAndWait();
    }

    private void showPostNoticeDialog() {
        Dialog<String> noticeDialog = new Dialog<>();
        noticeDialog.setTitle("Post Notice");
        noticeDialog.setHeaderText("Enter your notice");

        DialogPane dialogPane = noticeDialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextArea noticeTextArea = new TextArea();
        noticeTextArea.setPromptText("Write your notice here...");
        dialogPane.setContent(noticeTextArea);

        noticeDialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return noticeTextArea.getText();
            }
            return null;
        });

        Optional<String> result = noticeDialog.showAndWait();
        result.ifPresent(notice -> {
            this.noticeTextArea.setText(notice);
        });
    }

    private static class Event {
        private final String name;
        private final LocalDateTime dateTime;

        Event(String name, LocalDateTime dateTime) {
            this.name = name;
            this.dateTime = dateTime;
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