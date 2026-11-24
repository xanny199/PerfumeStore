package sk.upjs.perfumestore.controller.CashierControllers;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import sk.upjs.perfumestore.product.Product;
import sk.upjs.perfumestore.product.ProductService;
import sk.upjs.perfumestore.transaction.SaleTransactionService;
import sk.upjs.perfumestore.user.UserAccount;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.IllegalFormatException;
import java.util.Map;

public class CashRegisterController  {

    @FXML
    private Button addButton;

    @FXML
    private Button clearSaleButton;

    @FXML
    private Button completeSaleButton;

    @FXML
    private TableColumn<Product, BigDecimal> itemTotalColumn;

    @FXML
    private TableColumn<Product, BigDecimal> priceColumn;

    @FXML
    private TableColumn<Product, String> productColumn;

    @FXML
    private TableColumn<Product, Integer> quantityColumn;

    @FXML
    private TableView<Product> saleItemsTable;

    @FXML
    private ComboBox<Product> productComboBox;

    @FXML
    private Button resetComboxButton;

    @FXML
    private Label totalAmountLabel;

    private final ProductService productService;

    private final SaleTransactionService saleTransactionService;

    private UserAccount currentUserAccount;

    private final ObservableList<Product> availableProducts =
            FXCollections.observableArrayList();

    private final ObservableList<Product> saleProducts =
            FXCollections.observableArrayList();

    private final Map<Long, Integer> quantities = new HashMap<>();

    public CashRegisterController(ProductService productService, SaleTransactionService saleTransactionService) {
        this.productService = productService;
        this.saleTransactionService = saleTransactionService;
    }

    public void setCurrentUserAccount(UserAccount user) {
        this.currentUserAccount = user;
    }

    @FXML
    void initialize() {
        availableProducts.setAll(productService.getAllAvailableProducts());

        productComboBox.setItems(availableProducts);
        setupPlaceholder(productComboBox);
        setupProductComboBox();

        saleItemsTable.setItems(saleProducts);
        saleItemsTable.setFixedCellSize(55);

        saleItemsTable.prefHeightProperty().bind(
                Bindings.createDoubleBinding(
                        () -> Math.min(
                                450.0,
                                Math.max(120.0, saleProducts.size() * 55.0 + 65.0)
                        ),
                        saleProducts
                )
        );

        initializeTable();
        updateTotalAmount();
    }

    private void initializeTable() {
        productColumn.setCellValueFactory(data ->
            new ReadOnlyObjectWrapper<>(data.getValue().name())
        );

        quantityColumn.setCellValueFactory(data ->
            new ReadOnlyObjectWrapper<>(quantities.getOrDefault(data.getValue().id(), 0))
        );
        setupQuantityColumn(quantityColumn);

        priceColumn.setCellValueFactory(data ->
            new ReadOnlyObjectWrapper<>(data.getValue().price())
        );
        setupMoneyColumn(priceColumn);

        itemTotalColumn.setCellValueFactory(data -> {
            Product product = data.getValue();
            int count = quantities.getOrDefault(product.id(), 1);

            BigDecimal total = product.price().multiply(BigDecimal.valueOf(count));
            return new ReadOnlyObjectWrapper<>(total);
        });
        setupMoneyColumn(itemTotalColumn);
    }

    @FXML
    void addButtonOnAction(ActionEvent event) {
        Product selectedProduct = productComboBox.getValue();
        if (selectedProduct == null) {
            return;
        }

        int count = quantities.getOrDefault(selectedProduct.id(), 0);
        if (!productService.isAvaliableCountForCurrentStock(selectedProduct, count + 1)) {
            return;
        }

        quantities.put(selectedProduct.id(), count + 1);
        if (count == 0) saleProducts.add(selectedProduct);
        saleItemsTable.refresh();
        updateTotalAmount();
    }

    @FXML
    void clearSaleButtonOnAction(ActionEvent event) {
        saleProducts.clear();
        quantities.clear();
        productComboBox.setValue(null);
        updateTotalAmount();
    }

    @FXML
    void completeSaleButtonOnAction(ActionEvent event) {
        if (saleProducts.isEmpty()) {
            showAlert("Error", "No products selected");
            return;
        }

        saleTransactionService.completeInStoreSale(currentUserAccount.id(), saleProducts, quantities);
    }

    @FXML
    void resetComboxButtonOnAction(ActionEvent event) {productComboBox.setValue(null);}

    private void setupPlaceholder(ComboBox<Product> comboBox) {
        comboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Product item, boolean empty) {
                super.updateItem(item, empty);
                setText(item == null ? comboBox.getPromptText() : item.name());
            }
        });
    }

    private void setupProductComboBox() {
        productComboBox.setCellFactory(comboBox -> new ListCell<>() {
            @Override
            protected void updateItem(Product product, boolean empty) {
                super.updateItem(product, empty);

                if (empty || product == null) {
                    setText(null);
                } else {
                    setText(product.name());
                }
            }
        });
    }

    private void setupMoneyColumn(TableColumn<Product, BigDecimal> column) {
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal value, boolean empty) {
                super.updateItem(value, empty);

                if (empty || value == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f €", value));
                }
            }
        });
    }

    private void setupQuantityColumn(TableColumn<Product, Integer> column) {
        column.setCellFactory(col -> new TableCell<>() {

            private final TextField quantityField = new TextField();

            @Override
            protected void updateItem(Integer quantity, boolean empty) {
                super.updateItem(quantity, empty);

                if (empty || quantity == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                quantityField.setPrefWidth(65);
                quantityField.setText(String.valueOf(quantity));
                quantityField.setOnAction(event -> saveQuantity(this, quantityField));

                setText(null);
                setGraphic(quantityField);
            }
        });
    }

    private void saveQuantity(TableCell<Product, Integer> cell, TextField quantityField) {
        Product product = cell.getTableRow().getItem();
        if (product == null) return;

        try {
            int quantity = Integer.parseInt(quantityField.getText().trim());
            if (quantity <= 0) showAlert("Error", "Please enter a quantity greater than 0");
            if (!productService.isAvaliableCountForCurrentStock(product, quantity)) return;
            quantities.put(product.id(), quantity);

            saleItemsTable.refresh();
            updateTotalAmount();
        } catch (NumberFormatException | IllegalFormatException e) {
            showAlert("Error", "Please enter the product quantity");
        }
    }

    private void showAlert(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("PerfumeStore");
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void updateTotalAmount() {
        BigDecimal total = BigDecimal.ZERO;

        for (Product product : saleProducts) {
            int quantity = quantities.getOrDefault(product.id(), 1);

            total = total.add(product.price().multiply(BigDecimal.valueOf(quantity)));
        }

        totalAmountLabel.setText(String.format("%.2f €", total));
    }
}
