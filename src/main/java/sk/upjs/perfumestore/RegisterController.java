package sk.upjs.perfumestore;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import sk.upjs.perfumestore.user.UserAccount;
import sk.upjs.perfumestore.user.UserAccountService;




import java.io.IOException;

public class RegisterController {
    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField phoneField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    private final UserAccountService userAccountService;


    public RegisterController(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    private void showAlert(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("PerfumeStore");
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    void createAccountAction(ActionEvent event) {
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if(firstName.isBlank() || lastName.isBlank() || email.isBlank() || password.isBlank() ||  confirmPassword.isBlank()) {
            showAlert("Error","Please fill all required fields");
            return;
        }
        if(!password.equals(confirmPassword)) {
            showAlert("Error","Passwords do not match");
            return;
        }

        try{
            UserAccount user = userAccountService.register(firstName,lastName,email,phone,password);

            Stage stage = (Stage) emailField.getScene().getWindow();
            var loader = new FXMLLoader(getClass().getResource("/sk/upjs/perfumestore/fxml/common/main-layout.fxml"));
            Parent root = loader.load();

            stage.setScene(new Scene(root));
            stage.setTitle("PerfumeStore");
        }catch(BadRequestException e){
            showAlert("Registration failed",e.getMessage());
        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }

    @FXML
    void backToLoginAction(ActionEvent event) throws IOException {
        Stage stage = (Stage) emailField.getScene().getWindow();
        var loaderForLogin = new FXMLLoader(getClass().getResource("/sk/upjs/perfumestore/fxml/common/login.fxml"));

        loaderForLogin.setControllerFactory(controllerClass ->{
            if (controllerClass == LoginController.class){
                return new LoginController(userAccountService);
            }
            throw new IllegalArgumentException("unknown controller: " + controllerClass);
        });
        Parent root = loaderForLogin.load();
        stage.setScene(new Scene(root));
        stage.setTitle("PerfumeStore");
    }

}
