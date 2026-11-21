package sk.upjs.perfumestore.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import sk.upjs.perfumestore.exception.BadRequestException;
import sk.upjs.perfumestore.user.DeliveryAddressService;



public class AddressEditController {

    @FXML
    private TextField streetField;
    @FXML
    private TextField cityField;
    @FXML
    private TextField postalCodeField;
    @FXML
    private TextField countryField;

    private final DeliveryAddressService deliveryAddressService;
    private final Long userId;

    public AddressEditController(DeliveryAddressService deliveryAddressService, Long userId) {
        this.deliveryAddressService = deliveryAddressService;
        this.userId = userId;
    }
    
    @FXML
    void saveOnAction(ActionEvent event) {
        String street = streetField.getText();
        String city = cityField.getText();
        String postalCode = postalCodeField.getText();
        String country = countryField.getText();
        try {
            deliveryAddressService.create(userId, street, city, postalCode, country);
            cancelOnAction(event);
        }catch (BadRequestException | IllegalArgumentException e){
            showAlert(Alert.AlertType.ERROR, "Save Failed", e.getMessage());
        }
    }
    
    @FXML
    void cancelOnAction(ActionEvent event) {
        Stage stage = (Stage) streetField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle("PerfumeStore");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
