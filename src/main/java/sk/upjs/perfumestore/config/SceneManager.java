package sk.upjs.perfumestore.config;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import sk.upjs.perfumestore.controller.*;
import sk.upjs.perfumestore.controller.CashierControllers.CashRegisterController;
import sk.upjs.perfumestore.controller.ProductCatalogControllers.ProductCardController;
import sk.upjs.perfumestore.controller.ProductCatalogControllers.ProductCatalogController;
import sk.upjs.perfumestore.controller.MyOrdersControllers.MyOrdersController;
import sk.upjs.perfumestore.controller.MyOrdersControllers.OrdersDetailsContorller;
import sk.upjs.perfumestore.product.ProductService;
import sk.upjs.perfumestore.product.productImage.ProductImageService;
import sk.upjs.perfumestore.transaction.SaleTransaction;
import sk.upjs.perfumestore.transaction.SaleTransactionService;
import sk.upjs.perfumestore.user.DeliveryAddressService;
import sk.upjs.perfumestore.user.UserAccount;
import sk.upjs.perfumestore.user.UserAccountService;

import java.io.IOException;
import java.util.function.Consumer;

public class SceneManager {

    private final UserAccountService userAccountService;

    private final ProductService productService;

    private final ProductImageService productImageService;

    private final SaleTransactionService saleTransactionService;

    private final DeliveryAddressService deliveryAddressService;

    public SceneManager(UserAccountService userAccountService, ProductService productService, ProductImageService productImageService, DeliveryAddressService deliveryAddressService, SaleTransactionService saleTransactionService) {
        this.userAccountService = userAccountService;
        this.productService = productService;
        this.productImageService = productImageService;
        this.deliveryAddressService = deliveryAddressService;
        this.saleTransactionService = saleTransactionService;
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

    public Parent showLoadContentPageForMainLayout(String path, UserAccount currentUserAccount, MainLayoutController mainLayoutController) throws IOException {
        var loader = new FXMLLoader(getClass().getResource(path));

        loader.setControllerFactory(controllerClass -> {
            if(controllerClass.equals(ProductCatalogController.class)) {
                return new ProductCatalogController(productService, this);
            }
            if(controllerClass.equals(MyOrdersController.class)) {
                return new MyOrdersController(saleTransactionService, this, mainLayoutController);
            }
            if (controllerClass.equals(CashRegisterController.class)) {
                return new CashRegisterController(productService, saleTransactionService);
            }
            if (controllerClass.equals(OrderManagementController.class)) {
                return new OrderManagementController(saleTransactionService, userAccountService);
            } throw new IllegalArgumentException(
                    "Unknown controller: " + controllerClass.getName()
            );
        });

        Parent root = loader.load();

        if (loader.getController() instanceof MyOrdersController controller) {
            controller.setCurrentUserAccount(currentUserAccount);
        }
        if (loader.getController() instanceof CashRegisterController controller) {
            controller.setCurrentUserAccount(currentUserAccount);
        }

        return root;
    }
    public Parent showMyAccountPage(UserAccount user, Consumer<UserAccount> onUserUpdated) throws IOException {
        var loaderForMyAccount = new FXMLLoader(getClass().getResource("/sk/upjs/perfumestore/fxml/client/my-account.fxml"));

        loaderForMyAccount.setControllerFactory(controllerClass -> {
            if (controllerClass.equals(MyAccountController.class)) {
                return new MyAccountController(userAccountService, deliveryAddressService,this);
            } throw new IllegalArgumentException(
                    "Unknown controller: " + controllerClass.getName()
            );
        });

        Parent root = loaderForMyAccount.load();

        MyAccountController controller = loaderForMyAccount.getController();
        controller.setCurrentUserAccount(user);
        controller.setOnUserUpdated(onUserUpdated);

        return root;
    }

    public void showAddAddressDialog(Long userId) throws IOException{
        var loaderForDeliveryAddressAdd = new FXMLLoader(getClass().getResource("/sk/upjs/perfumestore/fxml/dialogs/address-edit.fxml"));

        loaderForDeliveryAddressAdd.setControllerFactory(controllerClass -> {
            if(controllerClass.equals(AddressEditController.class)){
                return new AddressEditController(deliveryAddressService,userId);
            }throw new IllegalArgumentException("Unknown controller: " + controllerClass.getName());
        });

        Parent root = loaderForDeliveryAddressAdd.load();

        Stage dialogStage = new Stage();
        dialogStage.setTitle("Add Delivery Address");
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setScene(new Scene(root));
        dialogStage.centerOnScreen();
        dialogStage.showAndWait();
    }

    public FXMLLoader loadProductCatalogCard(String path) {
        var loader = new FXMLLoader(getClass().getResource(path));

        loader.setControllerFactory(controllerClass -> {
            if(controllerClass.equals(ProductCardController.class)) {
                return new ProductCardController(productImageService);
            } throw new IllegalArgumentException(
                    "Unknown controller: " + controllerClass.getName()
            );
        });

        return loader;
    }

    public Parent showOrderDetails(SaleTransaction saleTransaction, MainLayoutController mainLayoutController) throws IOException {
        var loaderForOrderDetails = new FXMLLoader(getClass().getResource("/sk/upjs/perfumestore/fxml/dialogs/order-details.fxml"));

        loaderForOrderDetails.setControllerFactory(controllerClass -> {
            if(controllerClass.equals(OrdersDetailsContorller.class)) {
                return new OrdersDetailsContorller(productService);
            } throw new IllegalArgumentException(
                    "Unknown controller: " + controllerClass.getName()
            );
        });

        Parent root = loaderForOrderDetails.load();

        OrdersDetailsContorller controller = loaderForOrderDetails.getController();
        controller.setCurrentSaleTransaction(saleTransaction);
        controller.setMainLayoutController(mainLayoutController);

        return root;
    }
}
