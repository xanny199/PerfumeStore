package sk.upjs.perfumestore.app;

import javafx.application.Application;
import javafx.stage.Stage;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import sk.upjs.perfumestore.cart.CartDAO;
import sk.upjs.perfumestore.cart.CartService;
import sk.upjs.perfumestore.config.Factory;
import sk.upjs.perfumestore.config.SceneManager;
import sk.upjs.perfumestore.config.TransactionConfig;
import sk.upjs.perfumestore.product.ProductService;
import sk.upjs.perfumestore.product.productImage.ProductImageService;
import sk.upjs.perfumestore.user.DeliveryAddressService;
import sk.upjs.perfumestore.transaction.SaleTransactionService;
import sk.upjs.perfumestore.user.UserAccountService;

public class PerfumeStoreApp extends Application {

    private AnnotationConfigApplicationContext springContext;

    @Override
    public void start(Stage stage) throws Exception {
        springContext = new AnnotationConfigApplicationContext(TransactionConfig.class);

        UserAccountService userAccountService =
                new UserAccountService(Factory.INSTANCE.getUserAccountDao());

        ProductService productService =
                new ProductService(Factory.INSTANCE.getProductDao());

        ProductImageService productImageService =
                new ProductImageService(Factory.INSTANCE.getProductImageDao());

        SaleTransactionService saleTransactionService =
                springContext.getBean(SaleTransactionService.class);

        DeliveryAddressService deliveryAddressService =
                new DeliveryAddressService(Factory.INSTANCE.getDeliveryAddressDao());

        CartService cartService =
                new CartService(Factory.INSTANCE.getCartDao());

        SceneManager sceneManager = new SceneManager(
                userAccountService,
                productService,
                productImageService,
                deliveryAddressService,
                saleTransactionService,
                cartService
        );

        sceneManager.showLogin(stage);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void stop() {
        try {
            if (springContext != null) {
                springContext.close();
            }
        } finally {
            Factory.INSTANCE.close();
        }
    }
}
