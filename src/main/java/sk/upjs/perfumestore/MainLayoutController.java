package sk.upjs.perfumestore;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import sk.upjs.perfumestore.user.UserAccount;

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

    private UserAccount currentUserAccount;

    public void setCurrentUserAccount(UserAccount currentUserAccount) {
        this.currentUserAccount = Objects.requireNonNull(
                currentUserAccount,
                "Current user account cannot be null"
        );
        configureMenuForRole();
    }

    public void configureMenuForRole() {
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
    void cartButtonOnAction(ActionEvent event) {

    }

    @FXML
    void cashRegisterButtonOnAction(ActionEvent event) {

    }

    @FXML
    void manageProductsButtonOnAction(ActionEvent event) {

    }

    @FXML
    void myOrdersButtonOnAction(ActionEvent event) {

    }

    @FXML
    void ordersButtonOnAction(ActionEvent event) {

    }

    @FXML
    void productsButtonOnAction(ActionEvent event) {

    }

    @FXML
    void statisticsButtonOnAction(ActionEvent event) {

    }

    @FXML
    void stockButtonOnAction(ActionEvent event) {

    }

    private void setButtonVisibility(Button button, boolean visible) {
        button.setVisible(visible);
        button.setManaged(visible);
    }

}
