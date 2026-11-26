package sk.upjs.perfumestore.controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import javafx.fxml.FXML;
import sk.upjs.perfumestore.config.SceneManager;
import sk.upjs.perfumestore.exception.BadRequestException;
import sk.upjs.perfumestore.user.DeliveryAddress;
import sk.upjs.perfumestore.user.DeliveryAddressService;
import sk.upjs.perfumestore.user.UserAccount;
import sk.upjs.perfumestore.user.UserAccountService;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

public class MyAccountController {
    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneField;

    @FXML
    private ComboBox<DeliveryAddress> addressComboBox;
    @FXML
    private TextField streetField;
    @FXML
    private TextField cityField;
    @FXML
    private TextField postalCodeField;
    @FXML
    private TextField countryField;

    @FXML
    private PasswordField currentPasswordField;
    @FXML
    private PasswordField newPasswordField;
    @FXML
    private PasswordField confirmNewPasswordField;
    @FXML
    private TextField currentPasswordVisibleField;
    @FXML
    private TextField newPasswordVisibleField;
    @FXML
    private TextField confirmNewPasswordVisibleField;
    @FXML
    private Button togglePasswordsButton;
    @FXML
    private VBox deliveryAddressesSection;

    private final UserAccountService userAccountService;
    private final DeliveryAddressService deliveryAddressService;
    private final SceneManager sceneManager;

    private UserAccount currentUser;
    private Consumer<UserAccount> onUserUpdated;
    private boolean passwordsVisible = false;

    public MyAccountController(UserAccountService userAccountService, DeliveryAddressService deliveryAddressService, SceneManager sceneManager) {
        this.userAccountService = userAccountService;
        this.deliveryAddressService = deliveryAddressService;
        this.sceneManager = sceneManager;
    }

    @FXML
    public void initialize() {
        currentPasswordVisibleField.textProperty().bindBidirectional(currentPasswordField.textProperty());
        newPasswordVisibleField.textProperty().bindBidirectional(newPasswordField.textProperty());
        confirmNewPasswordVisibleField.textProperty().bindBidirectional(confirmNewPasswordField.textProperty());

        addressComboBox.setConverter(new StringConverter<DeliveryAddress>() {
            @Override
            public String toString(DeliveryAddress address) {
                if (address == null) return "";
                return address.street() + ", " + address.city() + " (" + address.country() + ")";
            }

            @Override
            public DeliveryAddress fromString(String string) {
                return null;
            }
        });

        addressComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                streetField.setText(newVal.street());
                cityField.setText(newVal.city());
                postalCodeField.setText(newVal.postalCode());
                countryField.setText(newVal.country());
            } else {
                clearAddressFields();
            }
        });

    }

    public void setCurrentUserAccount(UserAccount userAccount) {
        this.currentUser = userAccount;
        firstNameField.setText(userAccount.firstName());
        lastNameField.setText(userAccount.lastName());
        emailField.setText(userAccount.email());
        phoneField.setText(userAccount.phone());

        boolean isClient = userAccount.role() == UserAccount.Role.CLIENT;

        deliveryAddressesSection.setVisible(isClient);
        deliveryAddressesSection.setManaged(isClient);

        if(isClient) {
            loadAddresses();
        }
    }

    public void setOnUserUpdated(Consumer<UserAccount> onUserUpdated) {
        this.onUserUpdated = onUserUpdated;
    }

    private void loadAddresses() {
        List<DeliveryAddress> addresses = deliveryAddressService.findByUserId(currentUser.id());
        addressComboBox.setItems(FXCollections.observableArrayList(addresses));
        if (!addresses.isEmpty()) {
            addressComboBox.getSelectionModel().selectFirst();
        } else {
            clearAddressFields();
        }
    }

    @FXML
    void addAddressAction(ActionEvent event) {
        try {
            sceneManager.showAddAddressDialog(currentUser.id());
            loadAddresses();
        }catch (IOException e){
            showAlert(Alert.AlertType.ERROR,"Error","Failed to open Add Address dialog.");
        }
    }


    @FXML
    void togglePasswordsAction(ActionEvent event) {
        passwordsVisible = !passwordsVisible;
        setPasswordFieldVisibility(currentPasswordField, currentPasswordVisibleField, passwordsVisible);
        setPasswordFieldVisibility(newPasswordField, newPasswordVisibleField, passwordsVisible);
        setPasswordFieldVisibility(confirmNewPasswordField, confirmNewPasswordVisibleField, passwordsVisible);
        togglePasswordsButton.setText(passwordsVisible ? "Hide pass" : "Show pass");
    }

    @FXML
    void saveAllChangesAction(ActionEvent event) {
        try {
            UserAccount updatedUser = userAccountService.updateAccount(
                    currentUser,
                    firstNameField.getText(),
                    lastNameField.getText(),
                    phoneField.getText(),
                    currentPasswordField.getText(),
                    newPasswordField.getText(),
                    confirmNewPasswordField.getText()
            );

            if (updatedUser == currentUser) {
                showAlert(Alert.AlertType.INFORMATION, "No Changes", "Nothing was changed.");
                return;
            }

            currentUser = updatedUser;
            if (onUserUpdated != null) {
                onUserUpdated.accept(currentUser);
            }

            currentPasswordField.clear();
            newPasswordField.clear();
            confirmNewPasswordField.clear();

            showAlert(Alert.AlertType.INFORMATION, "Success", "Changes saved successfully.");
        } catch (BadRequestException | IllegalArgumentException e) {
            showAlert(Alert.AlertType.ERROR, "Save Failed", e.getMessage());
        }
    }

    @FXML
    void deleteAddressAction(ActionEvent event) {
        DeliveryAddress selectedAddress = addressComboBox.getValue();
        if (selectedAddress == null) {
            showAlert(Alert.AlertType.WARNING, "No Address Selected", "Please select an address to delete");
            return;
        }
        try{
            deliveryAddressService.deleteAddress(selectedAddress);
            loadAddresses();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Delete Failed", e.getMessage());
        }
    }

    private void setPasswordFieldVisibility(PasswordField hidden, TextField visible, boolean show) {
        visible.setVisible(show);
        visible.setManaged(show);
        hidden.setVisible(!show);
        hidden.setManaged(!show);
    }

    private void clearAddressFields() {
        streetField.clear();
        cityField.clear();
        postalCodeField.clear();
        countryField.clear();
    }

    private void showAlert(Alert.AlertType type, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle("PerfumeStore");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
