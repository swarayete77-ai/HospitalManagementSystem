package src;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;

import java.sql.*;
import java.util.Optional;

public class ViewPatientsWindow {

    public static void show() {

        Stage stage = new Stage();
        stage.setTitle("View All Patients");

        // ── Title ─────────────────────────────────────────────
        Label title = new Label("Registered Patients");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        title.setStyle("-fx-text-fill: #5e0065;");

        Label statusLabel = new Label("");
        statusLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 13px;");

        // ── Search bar ────────────────────────────────────────
        TextField searchField = new TextField();
        searchField.setPromptText("Search by name, doctor, hospital, status, condition...");
        searchField.setPrefWidth(380);
        searchField.setStyle("-fx-background-radius: 4; -fx-padding: 6;");

        // ── TableView ─────────────────────────────────────────
        TableView<PatientRow> table = new TableView<>();
        table.setEditable(true);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 13px;");

        TableColumn<PatientRow, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("patientId"));
        colId.setPrefWidth(60);

        TableColumn<PatientRow, String> colName = new TableColumn<>("Full Name");
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colName.setCellFactory(TextFieldTableCell.forTableColumn());
        colName.setOnEditCommit(e -> {
            e.getRowValue().setName(e.getNewValue());
            saveEdit(e.getRowValue(), statusLabel);
        });

        TableColumn<PatientRow, Integer> colAge = new TableColumn<>("Age");
        colAge.setCellValueFactory(new PropertyValueFactory<>("age"));
        colAge.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        colAge.setOnEditCommit(e -> {
            e.getRowValue().setAge(e.getNewValue());
            saveEdit(e.getRowValue(), statusLabel);
        });
        colAge.setPrefWidth(50);

        TableColumn<PatientRow, String> colDoctor = new TableColumn<>("Doctor");
        colDoctor.setCellValueFactory(new PropertyValueFactory<>("doctor"));
        colDoctor.setCellFactory(TextFieldTableCell.forTableColumn());
        colDoctor.setOnEditCommit(e -> {
            e.getRowValue().setDoctor(e.getNewValue());
            saveEdit(e.getRowValue(), statusLabel);
        });

        TableColumn<PatientRow, String> colHospital = new TableColumn<>("Hospital");
        colHospital.setCellValueFactory(new PropertyValueFactory<>("hospital"));
        colHospital.setCellFactory(TextFieldTableCell.forTableColumn());
        colHospital.setOnEditCommit(e -> {
            e.getRowValue().setHospital(e.getNewValue());
            saveEdit(e.getRowValue(), statusLabel);
        });

        TableColumn<PatientRow, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(ComboBoxTableCell.forTableColumn(
            "Inpatient", "Outpatient", "Observation", "Ambulatory"
        ));
        colStatus.setOnEditCommit(e -> {
            e.getRowValue().setStatus(e.getNewValue());
            saveEdit(e.getRowValue(), statusLabel);
        });
        colStatus.setPrefWidth(110);

        TableColumn<PatientRow, String> colCondition = new TableColumn<>("Condition");
        colCondition.setCellValueFactory(new PropertyValueFactory<>("conditionDesc"));
        colCondition.setCellFactory(TextFieldTableCell.forTableColumn());
        colCondition.setOnEditCommit(e -> {
            e.getRowValue().setConditionDesc(e.getNewValue());
            saveEdit(e.getRowValue(), statusLabel);
        });

        // ── Delete column ─────────────────────────────────────
        TableColumn<PatientRow, Void> colDelete = new TableColumn<>("Action");
        colDelete.setPrefWidth(80);
        colDelete.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Delete");
            {
                btn.setStyle(
                    "-fx-background-color: #c0392b; -fx-text-fill: white;" +
                    "-fx-font-size: 11px; -fx-background-radius: 4; -fx-cursor: hand;"
                );
                btn.setOnAction(e -> {
                    PatientRow row = getTableView().getItems().get(getIndex());
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Delete Patient");
                    confirm.setHeaderText("Delete " + row.getName() + "?");
                    confirm.setContentText("This cannot be undone.");
                    Optional<ButtonType> result = confirm.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        deletePatient(row.getPatientId(), statusLabel);
                        getTableView().getItems().remove(row);
                    }
                });
            }
            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : btn);
            }
        });

        table.getColumns().addAll(
            colId, colName, colAge, colDoctor,
            colHospital, colStatus, colCondition, colDelete
        );

        // ── Master list + search logic ────────────────────────
        ObservableList<PatientRow> masterList = FXCollections.observableArrayList();

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isBlank()) {
                table.setItems(masterList);
                statusLabel.setText(masterList.size() + " patient(s) loaded");
            } else {
                String q = newVal.toLowerCase();
                ObservableList<PatientRow> filtered = FXCollections.observableArrayList();
                for (PatientRow r : masterList) {
                    if (matches(r, q)) filtered.add(r);
                }
                table.setItems(filtered);
                statusLabel.setText(filtered.size() + " result(s) for \"" + newVal + "\"");
            }
        });

        // ── Load from DB ──────────────────────────────────────
        Runnable loadData = () -> {
            masterList.clear();
            try {
                Connection con = DBConnection.connect();
                if (con == null) { statusLabel.setText("❌ Cannot connect."); return; }
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM patients");
                while (rs.next()) {
                    masterList.add(new PatientRow(
                        rs.getInt("patient_id"),
                        rs.getString("patient_name"),
                        rs.getInt("age"),
                        rs.getString("doctor_name"),
                        rs.getString("hospital_name"),
                        rs.getString("status"),
                        rs.getString("condition_desc")
                    ));
                }
                table.setItems(masterList);
                statusLabel.setText(masterList.size() + " patient(s) loaded");
                con.close();
            } catch (Exception e) {
                statusLabel.setText("❌ " + e.getMessage());
                e.printStackTrace();
            }
        };

        loadData.run();

        // ── Buttons ───────────────────────────────────────────
        Button refreshBtn = new Button("⟳  Refresh");
        refreshBtn.setStyle(
            "-fx-background-color: #74057a; -fx-text-fill: white;" +
            "-fx-font-weight: bold; -fx-background-radius: 5; -fx-cursor: hand; -fx-padding: 7 18 7 18;"
        );
        refreshBtn.setOnAction(e -> { searchField.clear(); loadData.run(); });

        Label editHint = new Label("💡 Double-click any cell to edit. Changes save automatically.");
        editHint.setStyle("-fx-font-size: 12px; -fx-text-fill: #888888;");

        // ── Layout ────────────────────────────────────────────
        HBox topBar  = new HBox(15, title, statusLabel);
        topBar.setAlignment(Pos.CENTER_LEFT);

        HBox toolbar = new HBox(10, searchField, refreshBtn);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        VBox root = new VBox(12, topBar, toolbar, editHint, table);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f8f8f8;");

        Scene scene = new Scene(root, 980, 500);
        stage.setScene(scene);
        stage.show();
    }

    // ── Save edited row back to MySQL ─────────────────────────
    private static void saveEdit(PatientRow row, Label statusLabel) {
        try {
            Connection con = DBConnection.connect();
            String sql = "UPDATE patients SET patient_name=?, age=?, doctor_name=?, " +
                         "hospital_name=?, status=?, condition_desc=? WHERE patient_id=?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, row.getName());
            pst.setInt(2,    row.getAge());
            pst.setString(3, row.getDoctor());
            pst.setString(4, row.getHospital());
            pst.setString(5, row.getStatus());
            pst.setString(6, row.getConditionDesc());
            pst.setInt(7,    row.getPatientId());
            pst.executeUpdate();
            con.close();
            statusLabel.setText("✅ Patient " + row.getPatientId() + " updated.");
        } catch (Exception e) {
            statusLabel.setText("❌ Save failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ── Delete patient from MySQL ─────────────────────────────
    private static void deletePatient(int id, Label statusLabel) {
        try {
            Connection con = DBConnection.connect();
            PreparedStatement pst = con.prepareStatement(
                "DELETE FROM patients WHERE patient_id=?"
            );
            pst.setInt(1, id);
            pst.executeUpdate();
            con.close();
            statusLabel.setText("🗑️ Patient " + id + " deleted.");
        } catch (Exception e) {
            statusLabel.setText("❌ Delete failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ── Search matcher ────────────────────────────────────────
    private static boolean matches(PatientRow r, String q) {
        return r.getName().toLowerCase().contains(q)
            || r.getDoctor().toLowerCase().contains(q)
            || r.getHospital().toLowerCase().contains(q)
            || String.valueOf(r.getAge()).contains(q)
            || String.valueOf(r.getPatientId()).contains(q)
            || (r.getStatus() != null && r.getStatus().toLowerCase().contains(q))
            || (r.getConditionDesc() != null && r.getConditionDesc().toLowerCase().contains(q));
    }
}