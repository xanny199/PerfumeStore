package sk.upjs.perfumestore.config;

import javafx.scene.control.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

public class UIComponentConfigurator {
    public static <T> void setupCombox(ComboBox<T> comboBox, Function<T, String> nameProvider) {
        comboBox.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);

                setText(empty || item == null ? null : nameProvider.apply(item));
            }
        });

        comboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);

                setText(item == null ? comboBox.getPromptText() : nameProvider.apply(item));
            }
        });
    }

    public static <T> void setupMoneyColumn(TableColumn<T, BigDecimal> column) {
        column.setCellFactory(cl -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal value, boolean empty) {
                super.updateItem(value, empty);

                setText(empty || value == null ? null : String.format("%.2f €", value));
            }
        });
    }

    public static <T> void setupDateColumn(TableColumn<T, Timestamp> column) {
        column.setCellFactory(cl -> new TableCell<>() {

            DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

            @Override
            protected void updateItem(Timestamp date, boolean empty) {
                super.updateItem(date, empty);

                setText(empty || date == null ? null : date.toLocalDateTime().format(formatter));
            }
        });
    }
}
