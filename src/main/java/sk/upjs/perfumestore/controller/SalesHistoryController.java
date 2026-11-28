package sk.upjs.perfumestore.controller;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import sk.upjs.perfumestore.config.SceneManager;
import sk.upjs.perfumestore.config.UIComponentConfigurator;
import sk.upjs.perfumestore.exception.NotFoundException;
import sk.upjs.perfumestore.transaction.SaleTransaction;
import sk.upjs.perfumestore.transaction.SaleTransactionService;
import sk.upjs.perfumestore.user.UserAccount;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Comparator;
import java.util.stream.Stream;

public class SalesHistoryController {

    @FXML
    private TextField saleSearchField;

    @FXML
    private ComboBox<String> dateFilterComboBox;

    @FXML
    private Button resetButton;

    @FXML
    private TableView<SaleTransaction> salesTable;

    @FXML
    private TableColumn<SaleTransaction, Long> saleIdColumn;

    @FXML
    private TableColumn<SaleTransaction, Timestamp> saleDateColumn;

    @FXML
    private TableColumn<SaleTransaction, Void> detailsColumn;

    @FXML
    private TableColumn<SaleTransaction, BigDecimal> saleTotalColumn;

    private UserAccount currentCashier;

    private final SaleTransactionService saleTransactionService;
    private final SceneManager sceneManager;
    private final MainLayoutController mainLayoutController;

    private final ObservableList<SaleTransaction> allOrders =
            FXCollections.observableArrayList();

    private final ObservableList<SaleTransaction> filteredOrders =
            FXCollections.observableArrayList();

    public SalesHistoryController(SaleTransactionService saleTransactionService, SceneManager sceneManager, MainLayoutController mainLayoutController) {
        this.saleTransactionService = saleTransactionService;
        this.sceneManager = sceneManager;
        this.mainLayoutController = mainLayoutController;
    }

    public void setCurrentCashier(UserAccount currentCashier) {
        this.currentCashier = currentCashier;
        loadOrders();
    }

    @FXML
    public void initialize() {
        salesTable.setItems(filteredOrders);

        initializeTextFieldAndCombox();

        salesTable.setFixedCellSize(40);
        salesTable.prefHeightProperty().bind(
                Bindings.createDoubleBinding(
                        () -> Math.min(
                                350.0,
                                Math.max(85.0, filteredOrders.size() * 40.0 + 55.0)
                        ),
                        filteredOrders
                )
        );
        initializeTable();
    }

    private void loadOrders() {
        allOrders.setAll(saleTransactionService.getAllCompletedOrdersForCashier(currentCashier.id()));
        applyFilters();
    }

    private void initializeTextFieldAndCombox() {
        dateFilterComboBox.setItems(FXCollections.observableArrayList(
                "Newest First",
                "Oldest First"
        ));

        UIComponentConfigurator.setupCombox(dateFilterComboBox, date -> date);
        setupFiltersListeners();
    }

    @FXML
    void resetButtonOnAction(ActionEvent event) {
        dateFilterComboBox.setValue(null);
        saleSearchField.clear();
    }

    private void initializeTable() {
        saleIdColumn.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(data.getValue().id())
        );

        saleDateColumn.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(data.getValue().createdAt())
        );
        UIComponentConfigurator.setupDateColumn(saleDateColumn);

        setupDetailsColumn();

        saleTotalColumn.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(data.getValue().totalPrice())
        );
        UIComponentConfigurator.setupMoneyColumn(saleTotalColumn);
    }

    private void applyFilters() {
        String searchID = saleSearchField.getText().trim().toLowerCase();

        String selectedOption = dateFilterComboBox.getValue();

        Stream<SaleTransaction> stream = allOrders.stream();

        if (!searchID.isEmpty()) {
            stream = stream.filter(
                    order -> order.id().toString().toLowerCase().contains(searchID)
            );
        }

        if (selectedOption != null) {
            switch (selectedOption) {
                case "Newest First":
                    stream = stream.sorted(
                            Comparator.comparing(SaleTransaction::createdAt).reversed()
                    );
                    break;
                case "Oldest First":
                    stream = stream.sorted(
                            Comparator.comparing(SaleTransaction::createdAt)
                    );
            }
        }

        filteredOrders.setAll(stream.toList());
    }

    private void setupFiltersListeners() {
        saleSearchField.textProperty().addListener(
                (observable, oldValue, newValue) -> applyFilters()
        );

        dateFilterComboBox.valueProperty().addListener(
                (observable, oldValue, newValue) -> applyFilters()
        );
    }

    private void setupDetailsColumn() {
        detailsColumn.setCellFactory(column -> new TableCell<>() {
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

                        if (selectedOrder != null) {
                            try {
                                Parent root = sceneManager.showOrderDetails(selectedOrder, mainLayoutController, OrdersDetailsContorller.PreviousPage.SALES_HISTORY);
                                mainLayoutController.showContent(root);
                            } catch (Exception e) {
                                throw new NotFoundException("Error" + e.getMessage());
                            }
                        }
                    });
                }
            }
        });
    }
}
