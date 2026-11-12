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

    public LoginController(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    @FXML
    void createAccountHyperLinkOnAction(ActionEvent event) throws IOException {
        Stage stage = (Stage) createAccountHyperLink.getScene().getWindow();
        var loaderForRegistration = new FXMLLoader(getClass().getResource("/sk/upjs/perfumestore/fxml/common/register.fxml"));
        Parent root = loaderForRegistration.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Register Account");
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
            var loaderFroMainLayout = new FXMLLoader(getClass().getResource("/sk/upjs/perfumestore/fxml/common/main-layout.fxml"));
            Parent root = loaderFroMainLayout.load();

            MainLayoutController controller = loaderFroMainLayout.getController();
            controller.setCurrentUserAccount(user);

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("PerfumeStore");

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
