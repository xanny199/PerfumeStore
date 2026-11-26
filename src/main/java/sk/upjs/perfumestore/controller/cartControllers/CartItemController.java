package sk.upjs.perfumestore.controller.cartControllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import sk.upjs.perfumestore.cart.CartItem;
import sk.upjs.perfumestore.cart.CartService;
import sk.upjs.perfumestore.product.Product;

public class CartItemController {

    @FXML
    private Label productNameLabel;

    @FXML
    private Label priceLabel;

    @FXML
    private Spinner<Integer> quantitySpinner;

    @FXML
    private Button removeButton;

    private final CartService cartService;

    private Product product;

    private Long userId;

    private Runnable onCartChanged;

    public CartItemController(CartService cartService) {
        this.cartService = cartService;
    }


    @FXML
    void initialize() {
        quantitySpinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);

        quantitySpinner.getEditor().setTextFormatter(new TextFormatter<>(change ->
                change.getControlNewText().matches("\\d{0,2}") ? change : null));


        quantitySpinner.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) {
                quantitySpinner.commitValue();
            }
        });
    }

    public void setCartItem(CartItem cartItem,Product product,Long userId,Runnable onCartChanged) {
        this.product=product;
        this.userId = userId;
        this.onCartChanged = onCartChanged;

        productNameLabel.setText(product.name());
        priceLabel.setText(product.price() + "€×" + cartItem.quantity());

        quantitySpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1,99,cartItem.quantity())
        );

        quantitySpinner.valueProperty().addListener((obs, oldValue, newValue) -> {
            if(newValue != null && !newValue.equals(oldValue)) {
                cartService.updateQuantity(userId, product.id(), newValue);
                onCartChanged.run();
            }
        });
    }

    @FXML
    void removeButtonOnAction(ActionEvent event) {
        cartService.deleteItem(userId, product.id());
        onCartChanged.run();
    }
}
