package sk.upjs.perfumestore.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import sk.upjs.perfumestore.config.SceneManager;
import sk.upjs.perfumestore.user.UserAccount;

import java.io.IOException;
import java.util.Objects;

public class MainLayoutController {

    @FXML
    private Button cartButton;

    @FXML
    private Button cashRegisterButton;

    @FXML
    private StackPane contentPane;

    @FXML
    private Button manageProductsButton;

    @FXML
    private Button myOrdersButton;

    @FXML
    private Button ordersButton;

    @FXML
    private Button productsButton;

    @FXML
    private Button statisticsButton;

    @FXML
    private Button stockButton;

    @FXML
    private Button profileButton;

    @FXML
    private Button logoutButton;

    private final SceneManager sceneManager;

    public MainLayoutController(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    private UserAccount currentUserAccount;

    public void setCurrentUserAccount(UserAccount currentUserAccount) {
        this.currentUserAccount = Objects.requireNonNull(
                currentUserAccount,
                "Current user account cannot be null"
        );
        configureMenuForRole();
        loadDefaultPage();
    }

    private void configureMenuForRole() {
        switch (currentUserAccount.role()) {
            case CLIENT:
                setButtonVisibility(cashRegisterButton, false);
                setButtonVisibility(ordersButton, false);
                setButtonVisibility(manageProductsButton, false);
                setButtonVisibility(statisticsButton, false);
                setButtonVisibility(stockButton, false);
                break;
            case CASHIER:
                setButtonVisibility(productsButton, false);
                setButtonVisibility(cartButton, false);
                setButtonVisibility(myOrdersButton, false);
                setButtonVisibility(manageProductsButton, false);
                setButtonVisibility(statisticsButton, false);
                setButtonVisibility(stockButton, false);
                break;
            case MANAGER:
                setButtonVisibility(productsButton, false);
                setButtonVisibility(cartButton, false);
                setButtonVisibility(myOrdersButton, false);
                setButtonVisibility(cashRegisterButton, false);
                setButtonVisibility(ordersButton, false);
        }
    }

    @FXML
    void productsButtonOnAction(ActionEvent event) {
        loadContentPage("/sk/upjs/perfumestore/fxml/client/product-catalog.fxml");
    }

    @FXML
    void cartButtonOnAction(ActionEvent event) {
        loadContentPage("/sk/upjs/perfumestore/fxml/client/cart-checkout.fxml");
    }

    @FXML
    void myOrdersButtonOnAction(ActionEvent event) {
        loadContentPage("/sk/upjs/perfumestore/fxml/client/my-orders.fxml");
    }

    @FXML
    void cashRegisterButtonOnAction(ActionEvent event) {
        loadContentPage("/sk/upjs/perfumestore/fxml/cashier/cash-register.fxml");
    }

    @FXML
    void ordersButtonOnAction(ActionEvent event) {
        loadContentPage("/sk/upjs/perfumestore/fxml/cashier/order-managment.fxml");
    }

    @FXML
    void manageProductsButtonOnAction(ActionEvent event) {
        loadContentPage("/sk/upjs/perfumestore/fxml/manager/product-managment.fxml");
    }

    @FXML
    void statisticsButtonOnAction(ActionEvent event) {
        loadContentPage("/sk/upjs/perfumestore/fxml/manager/statistics.fxml");
    }

    @FXML
    void stockButtonOnAction(ActionEvent event) {
        loadContentPage("/sk/upjs/perfumestore/fxml/manager/stock-managment.fxml");
    }

    @FXML
    void profileButtonOnAction(ActionEvent event) {
        try {
            Parent root = sceneManager.showMyAccountPage(
                    currentUserAccount,
                    updatedUser -> this.currentUserAccount = updatedUser
            );
            contentPane.getChildren().setAll(root);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load FXML: my-account.fxml", e);
        }
    }

    @FXML
    void logoutButtonOnAction(ActionEvent event) throws IOException {
        Stage currentStage = (Stage) logoutButton.getScene().getWindow();
        sceneManager.showLogin(currentStage);
    }

    private void setButtonVisibility(Button button, boolean visible) {
        button.setVisible(visible);
        button.setManaged(visible);
    }

    private void loadContentPage(String fxmlPath) {
        try {
            Parent root = sceneManager.showLoadContentPageForMainLayout(fxmlPath);
            contentPane.getChildren().setAll(root);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to load FXML: " + fxmlPath, e
            );
        }
    }

    private void loadDefaultPage() {
        switch (currentUserAccount.role()) {
            case CLIENT ->
                loadContentPage("/sk/upjs/perfumestore/fxml/client/product-catalog.fxml");
            case CASHIER ->
                loadContentPage("/sk/upjs/perfumestore/fxml/cashier/cash-register.fxml");
            case MANAGER ->
                loadContentPage("/sk/upjs/perfumestore/fxml/manager/product-managment.fxml");
        }
    }
}
