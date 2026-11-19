package sk.upjs.perfumestore.config;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sk.upjs.perfumestore.controller.LoginController;
import sk.upjs.perfumestore.controller.MainLayoutController;
import sk.upjs.perfumestore.controller.ProductCatalogControllers.ProductCardController;
import sk.upjs.perfumestore.controller.ProductCatalogControllers.ProductCatalogController;
import sk.upjs.perfumestore.controller.RegisterController;
import sk.upjs.perfumestore.product.ProductService;
import sk.upjs.perfumestore.user.UserAccount;
import sk.upjs.perfumestore.user.UserAccountService;

import java.io.IOException;

public class SceneManager {

    private final UserAccountService userAccountService;

    private final ProductService productService;

    public SceneManager(UserAccountService userAccountService, ProductService productService) {
        this.userAccountService = userAccountService;
        this.productService = productService;
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

    public Parent showLoadContentPageForMainLayout(String path) throws IOException {
        var loader = new FXMLLoader(getClass().getResource(path));

        loader.setControllerFactory(controllerClass -> {
            if(controllerClass.equals(ProductCatalogController.class)) {
                return new ProductCatalogController(productService, this);
            } throw new IllegalArgumentException(
                    "Unknown controller: " + controllerClass.getName()
            );
        });

        return loader.load();
    }

    public FXMLLoader loadProductCatalogCard(String path) {
        var loaderForProductCatalogCard = new FXMLLoader(getClass().getResource(path));

        return loaderForProductCatalogCard;
    }
}
