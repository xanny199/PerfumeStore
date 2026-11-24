package sk.upjs.perfumestore.controller.ProductCatalogControllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import sk.upjs.perfumestore.product.Product;
import sk.upjs.perfumestore.product.productImage.ProductImageService;

import java.net.URL;

public class ProductCardController {

    @FXML
    private Button addToCartButton;

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

    public ProductCardController(ProductImageService productImageService) {
        this.productImageService = productImageService;
    }

    @FXML
    private void initialize() {
        detailsBox.managedProperty().bind(detailsBox.visibleProperty());
    }

    public void setProduct(Product product) {
        this.product = product;

        productNameLabel.setText(product.name());
        brandLabel.setText(product.brand());
        volumeLabel.setText(product.volumeML() + " ml");
        audienceLabel.setText(product.targetAudience().name());
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

    @FXML
    void detailsButtonOnAction(ActionEvent event) {
        boolean show = !detailsBox.isVisible();
        detailsBox.setVisible(show);
        detailsButton.setText(show ? "Hide Details" : "Show Details");
    }
}
