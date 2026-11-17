package sk.upjs.perfumestore.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import sk.upjs.perfumestore.exception.BadRequestException;
import sk.upjs.perfumestore.config.SceneManager;
import sk.upjs.perfumestore.user.UserAccount;
import sk.upjs.perfumestore.user.UserAccountService;

import java.io.IOException;

public class LoginController {

    @FXML
    private Hyperlink createAccountHyperLink;

    @FXML
    private TextField loginEmailTextField;

    @FXML
    private PasswordField loginPasswordTextField;

    @FXML
    private Button signInButton;

    private final UserAccountService userAccountService;
    private final SceneManager sceneManager;

    public LoginController(UserAccountService userAccountService, SceneManager sceneManager) {
        this.userAccountService = userAccountService;
        this.sceneManager = sceneManager;
    }

    @FXML
    void createAccountHyperLinkOnAction(ActionEvent event) throws IOException {
        Stage stage = (Stage) createAccountHyperLink.getScene().getWindow();
        sceneManager.showRegister(stage);
    }

    @FXML
    void signInButtonOnAction(ActionEvent event) {
        var loginEmail = loginEmailTextField.getText();
        var loginPassword = loginPasswordTextField.getText();

        if (loginEmail.isBlank() ||  loginPassword.isBlank()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("PerfumeStore");
            alert.setHeaderText("Error");
            alert.setContentText("Please fill all the fields");
            alert.showAndWait();
            return;
        }

        try {
            UserAccount user = userAccountService.login(loginEmail, loginPassword);

            Stage stage = (Stage) signInButton.getScene().getWindow();
            sceneManager.showMainLayout(stage, user);

        } catch (BadRequestException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("PerfumeStore");
            alert.setHeaderText("Error");
            alert.setContentText("Invaild email or password");
            alert.showAndWait();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
