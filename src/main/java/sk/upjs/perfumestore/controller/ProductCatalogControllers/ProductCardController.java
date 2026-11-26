package sk.upjs.perfumestore.controller.ProductCatalogControllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextFormatter;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import sk.upjs.perfumestore.cart.CartService;
import sk.upjs.perfumestore.product.Product;
import sk.upjs.perfumestore.product.productImage.ProductImageService;
import sk.upjs.perfumestore.user.UserAccount;

import java.net.URL;

public class ProductCardController {

    @FXML
    private Button addToCartButton;

    @FXML
    private Spinner<Integer> quantitySpinner;

    @FXML
    private Label audienceLabel;

    @FXML
    private Label priceLabel;

    @FXML
    private ImageView productImageView;

    @FXML
    private Label productNameLabel;

    @FXML
    private Label brandLabel;

    @FXML
    private Label volumeLabel;

    @FXML
    private Button detailsButton;

    @FXML
    private VBox detailsBox;

    @FXML
    private Label releaseYearLabel;

    @FXML
    private Label descriptionLabel;

    private Product product;

    private final ProductImageService productImageService;

    private final CartService cartService;

    private UserAccount currentUserAccount;

    public ProductCardController(ProductImageService productImageService, CartService cartService) {
        this.productImageService = productImageService;
        this.cartService = cartService;
    }

    @FXML
    private void initialize() {
        detailsBox.managedProperty().bind(detailsBox.visibleProperty());


        quantitySpinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);

        quantitySpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 99, 1));

        quantitySpinner.getEditor().setTextFormatter(new TextFormatter<>(change ->
                change.getControlNewText().matches("\\d{0,2}") ? change : null));


        quantitySpinner.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) {
                quantitySpinner.commitValue();
            }
        });
    }

    public void setProduct(Product product) {
        this.product = product;

        productNameLabel.setText(product.name());
        brandLabel.setText(product.brand());
        volumeLabel.setText(product.volumeML() + " ml");
        audienceLabel.setText(product.targetAudience() == null ? "" : product.targetAudience().name());
        priceLabel.setText(product.price() + " €");

        releaseYearLabel.setText("Release Year: "+ product.releaseYear());

        descriptionLabel.setText(
                product.description() == null || product.description().isBlank() ? "No description available." : product.description()
        );

        productImageService.getProductImageByProductId(product.id())
                .ifPresent(productImage -> {
                    String imagePath = productImage.imagePath();

                    URL imageURL = getClass().getResource(imagePath);

                    if(imageURL != null) {
                        Image image = new Image(String.valueOf(imageURL));
                        productImageView.setImage(image);
                    }
                });
    }

    public void setCurrentUserAccount(UserAccount currentUserAccount) {
        this.currentUserAccount = currentUserAccount;
    }

    @FXML
    void detailsButtonOnAction(ActionEvent event) {
        boolean show = !detailsBox.isVisible();
        detailsBox.setVisible(show);
        detailsButton.setText(show ? "Hide Details" : "Show Details");
    }

    @FXML
    void addToCartAction(ActionEvent event) {
        quantitySpinner.commitValue();
        Integer quantity = quantitySpinner.getValue();
        if (quantity == null) {
            quantity = 1;
        }
        cartService.addToCart(currentUserAccount.id(),product.id(),quantity);
        quantitySpinner.getValueFactory().setValue(1);
    }
}
