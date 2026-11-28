package sk.upjs.perfumestore.controller;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import sk.upjs.perfumestore.config.UIComponentConfigurator;
import sk.upjs.perfumestore.product.ProductService;
import sk.upjs.perfumestore.transaction.SaleTransaction;
import sk.upjs.perfumestore.transaction.TransactionItem;
import sk.upjs.perfumestore.user.UserAccount;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class OrdersDetailsContorller {

    @FXML
    private Button backButton;

    @FXML
    private Label orderDateLabel;

    @FXML
    private Label orderIdLabel;

    @FXML
    private TableView<TransactionItem> orderItemsTable;

    @FXML
    private Label orderStatusLabel;

    @FXML
    private Label orderTypeLabel;

    @FXML
    private TableColumn<TransactionItem, String> productColumn;

    @FXML
    private TableColumn<TransactionItem, Integer> quantityColumn;

    @FXML
    private TableColumn<TransactionItem, BigDecimal> subtotalColumn;

    @FXML
    private Label totalPriceLabel;

    @FXML
    private TableColumn<TransactionItem, BigDecimal> unitPriceColumn;

    private MainLayoutController mainLayoutController;

    private final ProductService productService;

    private final ObservableList<TransactionItem> orderItems = FXCollections.observableArrayList();

    private final Map<Long, String> productNames = new HashMap<>();

    public enum PreviousPage {
        MY_ORDERS,
        ORDER_MANAGEMENT,
        SALES_HISTORY
    }

    private PreviousPage previousPage;

    public OrdersDetailsContorller(ProductService productService) {
        this.productService = productService;
    }

    public void setCurrentSaleTransaction(SaleTransaction saleTransaction) {
        Objects.requireNonNull(saleTransaction);

        orderIdLabel.setText("Order ID: " + saleTransaction.id());
        orderDateLabel.setText("Date: " + saleTransaction.createdAt().toLocalDateTime().format(
                DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
        ));
        orderStatusLabel.setText("Status: " + saleTransaction.status());
        orderTypeLabel.setText("Type: " + saleTransaction.transactionType());

        totalPriceLabel.setText("Total Price: " + saleTransaction.totalPrice() + " €");

        orderItems.setAll((saleTransaction.items() == null) ? List.of() : saleTransaction.items());

        productNames.clear();

        if (saleTransaction.items() != null) {
            for (TransactionItem item : saleTransaction.items()) {
                if (!productNames.containsKey(item.productId())) {
                    String name = productService.getProductName(item.productId());
                    productNames.put(item.productId(), name);
                }
            }
        }
    }

    public void setMainLayoutController(MainLayoutController mainLayoutController) {
        this.mainLayoutController = mainLayoutController;
    }

    public void setPreviousPage(PreviousPage previousPage) {
        this.previousPage = previousPage;
    }

    @FXML
    public void initialize() {
        orderItemsTable.setItems(orderItems);

        orderItemsTable.setFixedCellSize(40);
        orderItemsTable.prefHeightProperty().bind(
                Bindings.createDoubleBinding(
                        () -> Math.min(
                                350.0,
                                Math.max(85.0, orderItems.size() * 40.0 + 55.0)
                        ),
                        orderItems
                )
        );

        initializeTableItems();

        if (previousPage == PreviousPage.MY_ORDERS) {
            backButton.setText("Back to orders");
        } else if (previousPage == PreviousPage.ORDER_MANAGEMENT) {
            backButton.setText("Back to order management");
        } else if (previousPage == PreviousPage.SALES_HISTORY) {
            backButton.setText("Back to sale history");
        }
    }

    @FXML
    void backButtonOnAction(ActionEvent event) {
        if (previousPage == PreviousPage.MY_ORDERS) {
            mainLayoutController.showMyOrders();
        } else if (previousPage == PreviousPage.ORDER_MANAGEMENT) {
            mainLayoutController.showOrderManagement();
        } else if (previousPage == PreviousPage.SALES_HISTORY) {
            mainLayoutController.showSalesHistory();
        }
    }

    private void initializeTableItems() {
        productColumn.setCellValueFactory(data ->
            new ReadOnlyObjectWrapper<>(productNames.get(data.getValue().productId()))
        );

        quantityColumn.setCellValueFactory(data ->
            new ReadOnlyObjectWrapper<>(data.getValue().quantity())
        );

        unitPriceColumn.setCellValueFactory(data ->
            new  ReadOnlyObjectWrapper<>(data.getValue().unitPrice())
        );
        UIComponentConfigurator.setupMoneyColumn(unitPriceColumn);

        subtotalColumn.setCellValueFactory(data ->
            new ReadOnlyObjectWrapper<>(data.getValue().unitPrice().multiply(BigDecimal.valueOf(data.getValue().quantity())))
        );
        UIComponentConfigurator.setupMoneyColumn(subtotalColumn);
    }
}
