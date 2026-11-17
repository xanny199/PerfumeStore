package sk.upjs.perfumestore.app;

import javafx.application.Application;
import javafx.stage.Stage;
import sk.upjs.perfumestore.config.Factory;
import sk.upjs.perfumestore.config.SceneManager;
import sk.upjs.perfumestore.user.UserAccountService;

public class PerfumeStoreApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        UserAccountService service =
                new UserAccountService(Factory.INSTANCE.getUserAccountDao());
        SceneManager sceneManager = new SceneManager(service);

        sceneManager.showLogin(stage);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
