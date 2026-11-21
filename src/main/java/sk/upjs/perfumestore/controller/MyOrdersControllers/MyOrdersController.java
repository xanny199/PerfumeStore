package sk.upjs.perfumestore.controller.MyOrdersControllers;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import sk.upjs.perfumestore.config.SceneManager;
import sk.upjs.perfumestore.controller.MainLayoutController;
import sk.upjs.perfumestore.exception.NotFoundException;
import sk.upjs.perfumestore.transaction.SaleTransaction;
import sk.upjs.perfumestore.transaction.SaleTransactionService;
import sk.upjs.perfumestore.user.UserAccount;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MyOrdersController {

    @FXML
    private TableColumn<SaleTransaction, Void> actionColumn;

    @FXML
    private ComboBox<String> allStatusesCombox;

    @FXML
    private TableColumn<SaleTransaction, OffsetDateTime> dateColumn;

    @FXML
    private TableColumn<SaleTransaction, Long> orderIdColumn;

    @FXML
    private TableView<SaleTransaction> ordersTable;

    @FXML
    private TableColumn<SaleTransaction, SaleTransaction.OrderStatus> statusColumn;

    @FXML
    private TableColumn<SaleTransaction, BigDecimal> totalColumn;

    @FXML
    private Button resetStatusCombox;

    private UserAccount currentUserAccount;

    private List<SaleTransaction> allOrders = List.of();

    private final ObservableList<SaleTransaction> userOrders =
            FXCollections.observableArrayList();

    private final SaleTransactionService saleTransactionService;
    private final SceneManager sceneManager;
    private final MainLayoutController mainLayoutController;

    public MyOrdersController(SaleTransactionService saleTransactionService, SceneManager sceneManager, MainLayoutController mainLayoutController) {
        this.saleTransactionService = saleTransactionService;
        this.sceneManager = sceneManager;
        this.mainLayoutController = mainLayoutController;
    }

    public void setCurrentUserAccount(UserAccount currentUserAccount) {
        this.currentUserAccount = currentUserAccount;

        allOrders = saleTransactionService.getUserOrders(currentUserAccount.id());

        applyFilter();
    }

    @FXML
    private void initialize() {
        ordersTable.setItems(userOrders);
        ordersTable.setPlaceholder(new Label("No orders found"));

        ordersTable.setFixedCellSize(55);

        ordersTable.prefHeightProperty().bind(
                Bindings.createDoubleBinding(
                        () -> Math.min(
                                450.0,
                                Math.max(120.0, userOrders.size() * 55.0 + 65.0)
                        ),
                        userOrders
                )
        );

        initializeStatusCombox();
        setupFilterListeners();

        initializeTable();
    }

    private void initializeTable() {
        orderIdColumn.setCellValueFactory(data ->
            new ReadOnlyObjectWrapper<>(data.getValue().id())
        );

        dateColumn.setCellValueFactory(data ->
            new ReadOnlyObjectWrapper<>(data.getValue().createdAt())
        );
        setupDateColumn();

        statusColumn.setCellValueFactory(data ->
            new ReadOnlyObjectWrapper<>(data.getValue().status())
        );

        totalColumn.setCellValueFactory(data ->
            new ReadOnlyObjectWrapper<>(data.getValue().totalPrice())
        );
        setupTotalColumn();

        setupActionColumn();
    }

    private void initializeStatusCombox() {
        for (SaleTransaction.OrderStatus orderStatus : SaleTransaction.OrderStatus.values()) {
            allStatusesCombox.getItems().add(orderStatus.name());
        }

        setupPlaceholder(allStatusesCombox);
    }

    private void applyFilter() {
        String selectedStatus = allStatusesCombox.getValue();

        List<SaleTransaction> filteredOrders = allOrders.stream()
                        .filter(order ->
                                selectedStatus == null || order.status().name().equals(selectedStatus)
                        )
                        .toList();

        userOrders.setAll(FXCollections.observableArrayList(filteredOrders));
    }

    @FXML
    void resetStatusComboxOnAction(ActionEvent event) {
        allStatusesCombox.setValue(null);
    }

    private void setupFilterListeners() {
        allStatusesCombox.valueProperty().addListener(
                (observable, oldValue, newValue) -> applyFilter()
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

    private void setupDateColumn() {
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

        dateColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(OffsetDateTime date, boolean empty) {
                super.updateItem(date, empty);

                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.format(formatter));
                }
            }
        });
    }

    private void setupTotalColumn() {
        totalColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal total, boolean empty) {
                super.updateItem(total, empty);

                if (empty || total == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f €", total));
                }
            }
        });
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
                        SaleTransaction order = getTableRow().getItem();
                        try {
                            Parent root = sceneManager.showOrderDetails(order, mainLayoutController);
                            mainLayoutController.showContent(root);
                        } catch (IOException e) {
                            throw new NotFoundException("");
                        }
                    });
                }
            }
        });
    }
}
