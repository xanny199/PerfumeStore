package sk.upjs.perfumestore.controller.ProductCatalogControllers;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import sk.upjs.perfumestore.config.SceneManager;
import sk.upjs.perfumestore.product.Product;
import sk.upjs.perfumestore.product.ProductService;
import sk.upjs.perfumestore.product.productImage.ProductImageService;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Comparator;
import java.util.stream.Stream;

public class ProductCatalogController {

    @FXML
    private ComboBox<String> audienceComboBox;

    @FXML
    private ComboBox<String> brandComboBox;

    @FXML
    private ComboBox<String> categoryComboBox;

    @FXML
    private Label productCountLabel;

    @FXML
    private FlowPane productFlowPane;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> sortComboBox;

    @FXML
    private Button resetFiltersButton;

    private final ProductService productService;

    private final SceneManager sceneManager;

    public ProductCatalogController(ProductService productService, SceneManager sceneManager) {
        this.productService = productService;
        this.sceneManager = sceneManager;
    }

    private List<Product> availableProducts;

    @FXML
    private void initialize() {
        availableProducts = productService.getAllAvailableProducts();

        productCountLabel.setText(availableProducts.size() + " products");

        initializeComboBoxes();
        setupFilterListeners();

        displayProducts(availableProducts);
    }

    private void displayProducts(List<Product> products) {
        productFlowPane.getChildren().clear();

        String fxmlPath = "/sk/upjs/perfumestore/fxml/dialogs/product-card.fxml";
        for (Product p : products) {
            var loaderForProductCard = sceneManager.loadProductCatalogCard(fxmlPath);

            try {
                Parent productCard = loaderForProductCard.load();

                ProductCardController controller = loaderForProductCard.getController();
                controller.setProduct(p);

                productFlowPane.getChildren().add(productCard);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to load product card: " + p.id(), e);
            }
        }
    }

    @FXML
    void resetFiltersOnAction(ActionEvent event) {
        searchField.clear();

        categoryComboBox.setValue(null);
        brandComboBox.setValue(null);
        audienceComboBox.setValue(null);
        sortComboBox.setValue(null);
    }

    private void initializeComboBoxes() {
        for (Product.CategoryType category : Product.CategoryType.values()) {
            categoryComboBox.getItems().add(category.name());
        }

        for (Product.TargetAudience audience : Product.TargetAudience.values()) {
            audienceComboBox.getItems().add(audience.name());
        }

        brandComboBox.setItems(FXCollections.observableArrayList(
                availableProducts.stream()
                        .map(Product::brand)
                        .filter(Objects::nonNull)
                        .distinct()
                        .sorted()
                        .toList()
                )
        );

        sortComboBox.setItems(FXCollections.observableArrayList(
                "Name: A-Z",
                "Name: Z-A",
                "Price: Low to High",
                "Price: High to Low"
        ));

        setupPlaceholder(categoryComboBox);
        setupPlaceholder(brandComboBox);
        setupPlaceholder(audienceComboBox);
        setupPlaceholder(sortComboBox);
    }


    private void applyFilters() {
        String search = searchField.getText().trim().toLowerCase();

        String selectedCategory = categoryComboBox.getValue();

        String selectedBrand = brandComboBox.getValue();

        String selectedAudience = audienceComboBox.getValue();

        String selectedSort = sortComboBox.getValue();

        Stream<Product> stream = availableProducts.stream();

        if (!search.isEmpty()) {
            stream = stream.filter(product ->
                    product.name().toLowerCase().contains(search)
                            || product.brand().toLowerCase().contains(search)
            );
        }

        if (selectedCategory != null && !selectedCategory.equals("Category")) {
            stream = stream.filter(product ->
                    product.categoryType().name().equals(selectedCategory)
            );
        }

        if (selectedBrand != null && !selectedBrand.equals("Brand")) {
            stream = stream.filter(product ->
                    product.brand().equals(selectedBrand)
            );
        }

        if (selectedAudience != null && !selectedAudience.equals("Audience")) {
            stream = stream.filter(product ->
                    product.targetAudience().name().equals(selectedAudience)
            );
        }

        if (selectedSort != null && !selectedSort.equals("Sort")) {
            switch (selectedSort) {
                case "Name: A-Z" ->
                        stream = stream.sorted(
                                Comparator.comparing(Product::name)
                        );

                case "Name: Z-A" ->
                        stream = stream.sorted(
                                Comparator.comparing(Product::name).reversed()
                        );

                case "Price: Low to High" ->
                        stream = stream.sorted(
                                Comparator.comparing(Product::price)
                        );

                case "Price: High to Low" ->
                        stream = stream.sorted(
                                Comparator.comparing(Product::price).reversed()
                        );
            }
        }

        List<Product> filteredProducts = stream.toList();

        productCountLabel.setText(filteredProducts.size() + " products");
        displayProducts(filteredProducts);
    }


    private void setupFilterListeners() {
        searchField.textProperty().addListener(
                (observable, oldValue, newValue) -> applyFilters()
        );

        categoryComboBox.valueProperty().addListener(
                (observable, oldValue, newValue) -> applyFilters()
        );

        brandComboBox.valueProperty().addListener(
                (observable, oldValue, newValue) -> applyFilters()
        );

        audienceComboBox.valueProperty().addListener(
                (observable, oldValue, newValue) -> applyFilters()
        );

        sortComboBox.valueProperty().addListener(
                (observable, oldValue, newValue) -> applyFilters()
        );
    }

    private void setupPlaceholder(ComboBox<String> comboBox) {
        comboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(item == null ? comboBox.getPromptText() : item);
            }
        });
    }
}
