package src;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;

public class HospitalApp extends Application {

    ArrayList<Orthopedic> patients = new ArrayList<>();

    @Override
    public void start(Stage stage) {

        // ── Root Layout ───────────────────────────────────────
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f8f8f8;");

        // ── LEFT SIDEBAR ──────────────────────────────────────
        VBox sidebar = new VBox(20);
        sidebar.setPadding(new Insets(30, 20, 30, 20));
        sidebar.setPrefWidth(250);
        sidebar.setStyle("-fx-background-color: linear-gradient(to bottom, #74057a, #b10ac1);");
        sidebar.setAlignment(Pos.TOP_CENTER);

        Label sidebarTitle = new Label("Facility Info");
        sidebarTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        sidebarTitle.setStyle("-fx-text-fill: white;");

        TextField hospitalField = new TextField();
        hospitalField.setPromptText("Enter Hospital Name");
        setupStyledField(hospitalField);

        TextField doctorField = new TextField();
        doctorField.setPromptText("Enter Doctor Name");
        setupStyledField(doctorField);

        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #ffffff; -fx-opacity: 0.3;");

        Label hospitalLabel = new Label("Hospital:");
        Label doctorLabel   = new Label("Doctor:");
        hospitalLabel.setStyle("-fx-text-fill: #E0E0E0; -fx-font-weight: bold;");
        doctorLabel.setStyle("-fx-text-fill: #E0E0E0; -fx-font-weight: bold;");

        sidebar.getChildren().addAll(
            sidebarTitle, separator,
            hospitalLabel, hospitalField,
            doctorLabel, doctorField
        );

        // ── RIGHT MAIN CONTENT ────────────────────────────────
        VBox mainContent = new VBox(16);
        mainContent.setPadding(new Insets(25));
        mainContent.setAlignment(Pos.TOP_LEFT);

        Label mainTitle = new Label("Patient Registration System");
        mainTitle.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        mainTitle.setStyle("-fx-text-fill: #5e0065;");

        // ── Form Fields ───────────────────────────────────────
        GridPane formGrid = new GridPane();
        formGrid.setHgap(15);
        formGrid.setVgap(12);

        TextField idField = new TextField();
        idField.setPromptText("e.g. 101");

        TextField nameField = new TextField();
        nameField.setPromptText("e.g. Toph Beifong");

        TextField ageField = new TextField();
        ageField.setPromptText("e.g. 28");

        ComboBox<String> statusBox = new ComboBox<>();
        statusBox.getItems().addAll("Inpatient", "Outpatient", "Observation", "Ambulatory");
        statusBox.setValue("Outpatient");
        statusBox.setStyle("-fx-background-radius: 4; -fx-padding: 4;");
        statusBox.setPrefWidth(200);

        TextField conditionField = new TextField();
        conditionField.setPromptText("e.g. Fractured tibia");

        formGrid.add(createFormLabel("Patient ID:"), 0, 0); formGrid.add(idField,        1, 0);
        formGrid.add(createFormLabel("Full Name:"),  0, 1); formGrid.add(nameField,       1, 1);
        formGrid.add(createFormLabel("Age:"),        0, 2); formGrid.add(ageField,        1, 2);
        formGrid.add(createFormLabel("Status:"),     0, 3); formGrid.add(statusBox,       1, 3);
        formGrid.add(createFormLabel("Condition:"),  0, 4); formGrid.add(conditionField,  1, 4);

        // ── Output Area ───────────────────────────────────────
        TextArea displayArea = new TextArea();
        displayArea.setEditable(false);
        displayArea.setPromptText("Registered patient profiles will appear here...");
        displayArea.setPrefHeight(200);
        displayArea.setStyle(
            "-fx-control-inner-background: #FFFFFF;" +
            "-fx-font-family: 'Courier New';" +
            "-fx-border-color: #E0E0E0;" +
            "-fx-border-radius: 4;"
        );

        // ── Register Button ───────────────────────────────────
        Button addButton = new Button("Register Patient");
        addButton.setPrefWidth(200);
        String greenStyle =
            "-fx-background-color: #22de09; -fx-text-fill: white; -fx-font-weight: bold;" +
            "-fx-font-size: 14px; -fx-background-radius: 5; -fx-cursor: hand;";
        String greenHover =
            "-fx-background-color: #1ab800; -fx-text-fill: white; -fx-font-weight: bold;" +
            "-fx-font-size: 14px; -fx-background-radius: 5; -fx-cursor: hand;";
        addButton.setStyle(greenStyle);
        addButton.setOnMouseEntered(e -> addButton.setStyle(greenHover));
        addButton.setOnMouseExited(e  -> addButton.setStyle(greenStyle));

        addButton.setOnAction(e -> {
            try {
                if (idField.getText().isEmpty() || nameField.getText().isEmpty() || ageField.getText().isEmpty()) {
                    displayArea.appendText("⚠️ Error: Please fill out ID, Name, and Age.\n");
                    return;
                }

                int    id        = Integer.parseInt(idField.getText().trim());
                String name      = nameField.getText().trim();
                int    age       = Integer.parseInt(ageField.getText().trim());
                String doctor    = doctorField.getText().trim().isEmpty()   ? "Not Assigned"    : doctorField.getText().trim();
                String hospital  = hospitalField.getText().trim().isEmpty() ? "General Hospital" : hospitalField.getText().trim();
                String status    = statusBox.getValue();                          // ← fixed
                String condition = conditionField.getText().trim().isEmpty()
                                   ? "Not specified" : conditionField.getText().trim(); // ← fixed

                Orthopedic patient = new Orthopedic(id, name, age, doctor, hospital);
                patients.add(patient);

                try {
                    Connection con = DBConnection.connect();
                    String query = "INSERT INTO patients " +
                                   "(patient_id, patient_name, age, doctor_name, hospital_name, status, condition_desc) " +
                                   "VALUES (?, ?, ?, ?, ?, ?, ?)";
                    PreparedStatement pst = con.prepareStatement(query);
                    pst.setInt(1,    id);
                    pst.setString(2, name);
                    pst.setInt(3,    age);
                    pst.setString(4, doctor);
                    pst.setString(5, hospital);
                    pst.setString(6, status);
                    pst.setString(7, condition);
                    pst.executeUpdate();
                    con.close();
                    displayArea.appendText("✅ Patient saved to database.\n");
                } catch (Exception dbEx) {
                    displayArea.appendText("❌ DB Error: " + dbEx.getMessage() + "\n");
                    dbEx.printStackTrace();
                }

                displayArea.appendText(
                    "=========================================\n" +
                    "✅ NEW PATIENT RECORDED\n" +
                    "=========================================\n" +
                    "🏥 Facility  : " + hospital  + "\n" +
                    "👤 Physician : " + doctor    + "\n" +
                    "🆔 ID        : " + id        + "\n" +
                    "👤 Name      : " + name      + "\n" +
                    "🎂 Age       : " + age       + " years old\n" +
                    "📋 Status    : " + status    + "\n" +
                    "🩺 Condition : " + condition + "\n\n"
                );

                idField.clear();
                nameField.clear();
                ageField.clear();
                conditionField.clear();
                statusBox.setValue("Outpatient");

            } catch (NumberFormatException ex) {
                displayArea.appendText("⚠️ ID and Age must be valid whole numbers!\n");
            }
        });

        // ── View Patients Button ──────────────────────────────
        Button viewBtn = new Button("View All Patients");
        viewBtn.setPrefWidth(200);
        String purpleStyle =
            "-fx-background-color: #74057a; -fx-text-fill: white; -fx-font-weight: bold;" +
            "-fx-font-size: 14px; -fx-background-radius: 5; -fx-cursor: hand;";
        String purpleHover =
            "-fx-background-color: #5e0065; -fx-text-fill: white; -fx-font-weight: bold;" +
            "-fx-font-size: 14px; -fx-background-radius: 5; -fx-cursor: hand;";
        viewBtn.setStyle(purpleStyle);
        viewBtn.setOnMouseEntered(e -> viewBtn.setStyle(purpleHover));
        viewBtn.setOnMouseExited(e  -> viewBtn.setStyle(purpleStyle));
        viewBtn.setOnAction(e -> ViewPatientsWindow.show());

        // ── Button Row ────────────────────────────────────────
        HBox buttonRow = new HBox(12, addButton, viewBtn);
        buttonRow.setAlignment(Pos.CENTER_LEFT);

        mainContent.getChildren().addAll(mainTitle, formGrid, buttonRow, displayArea);

        root.setLeft(sidebar);
        root.setCenter(mainContent);

        Scene scene = new Scene(root, 780, 560);
        stage.setTitle("Hospital Information System");
        stage.setScene(scene);
        stage.show();
    }

    private Label createFormLabel(String text) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 14));
        lbl.setStyle("-fx-text-fill: #4A4A4A;");
        return lbl;
    }

    private void setupStyledField(TextField fld) {
        fld.setStyle("-fx-background-radius: 4; -fx-padding: 6; -fx-background-color: #FFFFFF;");
    }

    public static void main(String[] args) {
        DBConnection.connect();
        launch();
    }
}