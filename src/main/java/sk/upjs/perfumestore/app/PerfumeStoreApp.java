package sk.upjs.perfumestore.app;

import javafx.application.Application;
import javafx.stage.Stage;
import sk.upjs.perfumestore.config.Factory;
import sk.upjs.perfumestore.config.SceneManager;
import sk.upjs.perfumestore.product.ProductService;
import sk.upjs.perfumestore.user.DeliveryAddress;
import sk.upjs.perfumestore.user.DeliveryAddressService;
import sk.upjs.perfumestore.user.UserAccountService;

public class PerfumeStoreApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        UserAccountService userAccountService =
                new UserAccountService(Factory.INSTANCE.getUserAccountDao());

        ProductService productService =
                new ProductService(Factory.INSTANCE.getProductDao());

        DeliveryAddressService deliveryAddressService =
                new DeliveryAddressService(Factory.INSTANCE.getDeliveryAddressDao());

        SceneManager sceneManager = new SceneManager(
                userAccountService,
                productService,
                deliveryAddressService
        );

        sceneManager.showLogin(stage);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
