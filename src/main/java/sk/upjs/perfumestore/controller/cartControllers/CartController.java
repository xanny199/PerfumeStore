package sk.upjs.perfumestore.controller.cartControllers;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import sk.upjs.perfumestore.cart.Cart;
import sk.upjs.perfumestore.cart.CartItem;
import sk.upjs.perfumestore.cart.CartService;
import sk.upjs.perfumestore.config.SceneManager;
import sk.upjs.perfumestore.product.Product;
import sk.upjs.perfumestore.product.ProductService;
import sk.upjs.perfumestore.user.DeliveryAddress;
import sk.upjs.perfumestore.user.DeliveryAddressService;
import sk.upjs.perfumestore.user.UserAccount;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

public class CartController {

    @FXML
    private VBox cartItemsContainer;

    @FXML
    private ComboBox<DeliveryAddress> deliveryAddressComboBox;

    @FXML
    private Label totalLabel;

    @FXML
    private Button placeOrderButton;

    @FXML
    private Button clearCartButton;

    private final CartService cartService;

    private final ProductService productService;

    private final DeliveryAddressService deliveryAddressService;

    private final SceneManager sceneManager;

    private final UserAccount  currentUser;

    public CartController(CartService cartService, ProductService productService, DeliveryAddressService deliveryAddressService, SceneManager sceneManager, UserAccount userAccount) {
        this.cartService = cartService;
        this.productService = productService;
        this.deliveryAddressService = deliveryAddressService;
        this.sceneManager = sceneManager;
        this.currentUser = userAccount;
    }

    @FXML
    private void initialize() {
        loadCartItems();

        loadAddresses();

        deliveryAddressComboBox.setConverter(new StringConverter<DeliveryAddress>() {
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
    }

    @FXML
    void addAddressButtonOnAction(){
        try {
            sceneManager.showAddAddressDialog(currentUser.id());
            loadAddresses();
        }catch (IOException e){
            showAlert(Alert.AlertType.ERROR,"Error","Failed to open Add Address dialog.");
        }
    }

    @FXML
    void placeOrderButtonOnAction(){

    }

    @FXML
    void clearCartButtonOnAction(){
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("PerfumeStore");
        confirm.setHeaderText("Clear cart");
        confirm.setContentText("Remove all products from your cart?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK){
            cartService.clearCart(currentUser.id());
            loadCartItems();
        }
    }

    private void loadCartItems() {
        cartItemsContainer.getChildren().clear();

        List<CartItem> items = cartService.findByUserId(currentUser.id()).map(Cart::items).orElse(List.of());

        if(items.isEmpty()){
            cartItemsContainer.getChildren().add(new Label("Your cart is empty"));
            totalLabel.setText("0.00 €");
            placeOrderButton.setDisable(true);
            clearCartButton.setDisable(true);
            return;
        }

        BigDecimal total = BigDecimal.ZERO;
        String path = "/sk/upjs/perfumestore/fxml/dialogs/cart-item.fxml";

        for(CartItem item : items){
            Product product = productService.getProductById(item.productId());

            var loader = sceneManager.loadCartItem(path);
            try{
                Parent row = loader.load();
                CartItemController controller = loader.getController();

                controller.setCartItem(item,product, currentUser.id(), this::loadCartItems);
                cartItemsContainer.getChildren().add(row);
            }catch (IOException e){
                throw new IllegalStateException("Failed to load cart item: " + item.productId(), e);
            }
            total = total.add(product.price().multiply(BigDecimal.valueOf(item.quantity())));
        }
        totalLabel.setText(total + "€");
        placeOrderButton.setDisable(false);
        clearCartButton.setDisable(false);
    }

    private void loadAddresses() {
        List<DeliveryAddress> addresses = deliveryAddressService.findByUserId(currentUser.id());
        deliveryAddressComboBox.setItems(FXCollections.observableArrayList(addresses));
        if (!addresses.isEmpty()) {
            deliveryAddressComboBox.getSelectionModel().selectFirst();
        }
    }

    private void showAlert(Alert.AlertType type, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle("PerfumeStore");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

}
