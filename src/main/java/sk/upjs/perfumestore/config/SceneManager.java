package sk.upjs.perfumestore.config;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sk.upjs.perfumestore.controller.LoginController;
import sk.upjs.perfumestore.controller.MainLayoutController;
import sk.upjs.perfumestore.controller.RegisterController;
import sk.upjs.perfumestore.user.UserAccount;
import sk.upjs.perfumestore.user.UserAccountService;

import java.io.IOException;

public class SceneManager {

    private final UserAccountService userAccountService;

    public SceneManager(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    public void showLogin(Stage stage) throws IOException {
        var loaderForLogin = new FXMLLoader(getClass().getResource("/sk/upjs/perfumestore/fxml/common/login.fxml"));

        loaderForLogin.setControllerFactory(controllerClass -> {
            if(controllerClass.equals(LoginController.class)) {
                return new LoginController(userAccountService, this);
            } throw new IllegalArgumentException(
                    "Unknown controller: " + controllerClass.getName()
            );
        });

        Parent root = loaderForLogin.load();
        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.setTitle("Perfume Store");
        stage.centerOnScreen();
    }

    public void showMainLayout(Stage stage, UserAccount user) throws IOException {
        var loaderForMainLayout = new FXMLLoader(getClass().getResource("/sk/upjs/perfumestore/fxml/common/main-layout.fxml"));

        loaderForMainLayout.setControllerFactory(controllerClass -> {
            if(controllerClass.equals(MainLayoutController.class)) {
                return new MainLayoutController(this);
            } throw new IllegalArgumentException(
                    "Unknown controller: " + controllerClass.getName()
            );
        });

        Parent root = loaderForMainLayout.load();

        MainLayoutController controller = loaderForMainLayout.getController();
        controller.setCurrentUserAccount(user);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Perfume Store");
        stage.centerOnScreen();
    }

    public void showRegister(Stage stage) throws IOException {
        var loaderForRegister = new FXMLLoader(getClass().getResource("/sk/upjs/perfumestore/fxml/common/register.fxml"));

        loaderForRegister.setControllerFactory(controllerClass -> {
            if(controllerClass.equals(RegisterController.class)) {
                return new RegisterController(userAccountService, this);
            } throw new IllegalArgumentException(
                    "Unknown controller: " + controllerClass.getName()
            );
        });

        Parent root = loaderForRegister.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Perfume Store");
        stage.centerOnScreen();
    }
}
