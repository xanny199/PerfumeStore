package sk.upjs.perfumestore.controller;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import sk.upjs.perfumestore.config.SceneManager;
import sk.upjs.perfumestore.config.UIComponentConfigurator;
import sk.upjs.perfumestore.exception.NotFoundException;
import sk.upjs.perfumestore.transaction.SaleTransaction;
import sk.upjs.perfumestore.transaction.SaleTransactionService;
import sk.upjs.perfumestore.user.UserAccount;
import sk.upjs.perfumestore.user.UserAccountService;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.stream.Stream;

public class OrderManagementController {

    @FXML
    private TextField orderSearchField;

    @FXML
    private ComboBox<SaleTransaction.OrderStatus> statusFilterComboBox;

    @FXML
    private Button resetStatusesComboBox;

    @FXML
    private TableView<SaleTransaction> ordersTable;

    @FXML
    private TableColumn<SaleTransaction, Long> orderIdColumn;

    @FXML
    private TableColumn<SaleTransaction, String> customerColumn;

    @FXML
    private TableColumn<SaleTransaction, Timestamp> dateColumn;

    @FXML
    private TableColumn<SaleTransaction, SaleTransaction.OrderStatus> statusColumn;

    @FXML
    private TableColumn<SaleTransaction, Void> actionColumn;

    @FXML
    private TableColumn<SaleTransaction, BigDecimal> totalColumn;

    @FXML
    private Label selectedOrderLabel;

    @FXML
    private ComboBox<SaleTransaction.OrderStatus> newStatusComboBox;

    @FXML
    private Button updateStatusButton;

    private final ObservableList<SaleTransaction> allOrders =
            FXCollections.observableArrayList();

    private final ObservableList<SaleTransaction> filteredOrders =
            FXCollections.observableArrayList();

    private final SaleTransactionService saleTransactionService;
    private final UserAccountService userAccountService;
    private final SceneManager sceneManager;
    private final MainLayoutController mainLayoutController;
    private UserAccount currentCashier;

    private SaleTransaction selectedOrder;

    public OrderManagementController(SaleTransactionService saleTransactionService, UserAccountService userAccountService, SceneManager sceneManager, MainLayoutController mainLayoutController) {
        this.saleTransactionService = saleTransactionService;
        this.userAccountService = userAccountService;
        this.sceneManager = sceneManager;
        this.mainLayoutController = mainLayoutController;
    }

    public void setCurrentCashier(UserAccount currentCashier) {
        this.currentCashier = currentCashier;
    }

    @FXML
    void initialize() {
        loadOrders();
        ordersTable.setItems(filteredOrders);

        initializeStatusesComboxes();

        ordersTable.setFixedCellSize(40);

        ordersTable.prefHeightProperty().bind(
                Bindings.createDoubleBinding(
                        () -> Math.min(
                                350.0,
                                Math.max(85.0, filteredOrders.size() * 40.0 + 55.0)
                        ),
                        filteredOrders
                )
        );
        initializeTable();

        newStatusComboBox.setDisable(true);
        updateStatusButton.setDisable(true);
    }

    private void loadOrders() {
        allOrders.setAll(saleTransactionService.getAllOnlineOrders());
        applyFilters();
    }

    private void initializeStatusesComboxes() {
        for (SaleTransaction.OrderStatus orderStatus : SaleTransaction.OrderStatus.values()) {
            statusFilterComboBox.getItems().add(orderStatus);
        }

        UIComponentConfigurator.setupCombox(statusFilterComboBox, SaleTransaction.OrderStatus::name);
        setupFilterListeners();

        newStatusComboBox.getItems().addAll(
                SaleTransaction.OrderStatus.COMPLETED,
                SaleTransaction.OrderStatus.CANCELLED
        );
        UIComponentConfigurator.setupCombox(newStatusComboBox, SaleTransaction.OrderStatus::name);
    }

    @FXML
    void resetStatusesComboBoxOnAction(ActionEvent event) {
        statusFilterComboBox.setValue(null);
        resetSelectedOrder();
    }

    private void resetSelectedOrder() {
        selectedOrder = null;
        ordersTable.getSelectionModel().clearSelection();

        selectedOrderLabel.setText("Select an order to manage");
        newStatusComboBox.setValue(null);
        newStatusComboBox.setDisable(true);
        updateStatusButton.setDisable(true);
    }

    private void initializeTable() {
        orderIdColumn.setCellValueFactory(data ->
            new ReadOnlyObjectWrapper<>(data.getValue().id())
        );

        customerColumn.setCellValueFactory(data ->
            new ReadOnlyObjectWrapper<>(userAccountService.getUserAccount(data.getValue().customerId()).email())
        );

        dateColumn.setCellValueFactory(data ->
            new ReadOnlyObjectWrapper<>(data.getValue().createdAt())
        );
        UIComponentConfigurator.setupDateColumn(dateColumn);

        statusColumn.setCellValueFactory(data ->
            new ReadOnlyObjectWrapper<>(data.getValue().status())
        );

        setupActionColumn();

        totalColumn.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(data.getValue().totalPrice())
        );
        UIComponentConfigurator.setupMoneyColumn(totalColumn);
    }

    private void applyFilters() {
        String search = orderSearchField.getText().trim().toLowerCase();

        var selectedStatus = statusFilterComboBox.getSelectionModel().getSelectedItem();

        Stream<SaleTransaction> stream = allOrders.stream();

        if (!search.isEmpty()) {
            stream = stream.filter(saleTransaction ->
                    saleTransaction.id().toString().contains(search)
            );
        }

        if (selectedStatus != null) {
            stream = stream.filter(saleTransaction ->
                    saleTransaction.status().equals(selectedStatus)
            );
        }

        filteredOrders.setAll(stream.toList());
        resetSelectedOrder();
    }

    private void setupFilterListeners() {
        orderSearchField.textProperty().addListener(
                (observable, oldValue, newValue) -> applyFilters()
        );

        statusFilterComboBox.valueProperty().addListener(
                (observable, oldValue, newValue) -> applyFilters()
        );
    }

    @FXML
    void selectedOrderOnMouseClicked(MouseEvent event) {
        if (event.getClickCount() != 2) {return;}

        selectedOrder = ordersTable.getSelectionModel().getSelectedItem();

        if (selectedOrder == null) {return;}

        selectedOrderLabel.setText("Selected Order ID: " + selectedOrder.id());
        newStatusComboBox.setValue(null);

        if (selectedOrder.status().equals(SaleTransaction.OrderStatus.PENDING)) {
            newStatusComboBox.setDisable(false);
        } else newStatusComboBox.setDisable(true);

        updateStatusButton.setDisable(true);
    }

    @FXML
    void newStatusComboBoxOnAction(ActionEvent event) {
        updateStatusButton.setDisable(selectedOrder == null
                || selectedOrder.status() != SaleTransaction.OrderStatus.PENDING
                || newStatusComboBox.getValue() == null
        );
    }

    @FXML
    void updateStatusButtonOnAction(ActionEvent event) {
        if (selectedOrder == null
                || selectedOrder.status() != SaleTransaction.OrderStatus.PENDING
                || newStatusComboBox.getValue() == null
        ) {return;}

        var isChanged = saleTransactionService.changeStatusForOrder(selectedOrder, newStatusComboBox.getValue());
        if (isChanged) {
            resetSelectedOrder();
            loadOrders();
            showAlert("Success", "Order Status has been changed!");
        }  else {
            showAlert("Error", "Order Status was not changed");
        }
    }

    private void setupActionColumn() {
        actionColumn.setCellFactory(column -> new TableCell<>() {

            private final Button detailsButton = new Button("Details");

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(detailsButton);

                    detailsButton.getStyleClass().add("secondary-button");
                    detailsButton.setOnAction(event -> {
                        SaleTransaction selectedOrder = getTableRow().getItem();
                        try {
                            Parent root = sceneManager.showOrderDetails(selectedOrder, mainLayoutController, currentCashier);
                            mainLayoutController.showContent(root);
                        } catch (Exception e) {
                            throw new NotFoundException("Error" + e.getMessage());
                        }
                    });
                }
            }
        });
    }

    private void showAlert(String header, String message) {
        Alert alert = new Alert(header.equals("Error") ? Alert.AlertType.ERROR : Alert.AlertType.INFORMATION);
        alert.setTitle("PerfumeStore");
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
