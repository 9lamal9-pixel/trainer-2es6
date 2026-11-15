/*
 * Decompiled with CFR 0.152.
 */
import base.communication.LanServer;
import base.data.BlockData;
import base.display.DisplayController;
import base.logic.LogicController;
import base.sound.SoundController;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main
extends Application {
    private static final Logger logger = LogManager.getLogger(Main.class);
    private double startWidth = 1024.0;
    private double startHeight = 768.0;
    private LogicController logicController;

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader;
        switch (BlockData.getInstance().getStartConfig().getBilModel()) {
            case BIL_M_ES2G: {
                loader = new FXMLLoader(this.getClass().getResource("block_es2g.fxml"));
                break;
            }
            case BIL_UT: {
                loader = new FXMLLoader(this.getClass().getResource("bil_ut.fxml"));
                break;
            }
            case BIL_V: {
                loader = new FXMLLoader(this.getClass().getResource("bil_v.fxml"));
                this.startWidth = 768.0;
                this.startHeight = 1024.0;
                break;
            }
            default: {
                loader = new FXMLLoader(this.getClass().getResource("block_other.fxml"));
            }
        }
        Pane root = (Pane)loader.load();
        DisplayController controller = (DisplayController)loader.getController();
        BlockData.getInstance().setDisplayController(controller);
        BlockData.getInstance().reloadRoadData(0);
        primaryStage.setTitle("BLOC");
        Scene scene = new Scene(root, this.startWidth, this.startHeight);
        primaryStage.setScene(scene);
        primaryStage.setX(BlockData.getInstance().getStartConfig().getWindowPositionResolutionData().getX());
        primaryStage.setY(BlockData.getInstance().getStartConfig().getWindowPositionResolutionData().getY());
        if (!BlockData.getInstance().getStartConfig().getDebug().getIsWindowed()) {
            primaryStage.initStyle(StageStyle.UNDECORATED);
        }
        primaryStage.show();
        this.setEvents(scene, root);
        primaryStage.setWidth((double)BlockData.getInstance().getStartConfig().getWindowPositionResolutionData().getWidth() + scene.getWindow().getWidth() - scene.getWidth());
        primaryStage.setHeight((double)BlockData.getInstance().getStartConfig().getWindowPositionResolutionData().getHeight() + scene.getWindow().getHeight() - scene.getHeight());
        LanServer lanServer = new LanServer();
        lanServer.start();
        logger.debug("Lan server start");
        SoundController.getInstance();
        ScheduledExecutorService logicScheduler = Executors.newSingleThreadScheduledExecutor();
        this.logicController = new LogicController();
        UpdateLogic updateLogic = new UpdateLogic();
        logicScheduler.scheduleAtFixedRate(updateLogic, 0L, 17L, TimeUnit.MILLISECONDS);
        primaryStage.setOnCloseRequest(event -> {
            lanServer.stop_close();
            SoundController.getInstance().destruct();
            Platform.exit();
            System.exit(0);
        });
    }

    private void setEvents(Scene scene, Pane contentPane) {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>(){
            final KeyCombination keyComb = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_ANY);

            @Override
            public void handle(KeyEvent ke) {
                if (this.keyComb.match(ke)) {
                    Platform.exit();
                    System.exit(0);
                }
            }
        });
        scene.widthProperty().addListener((observable, oldValue, newValue) -> {
            double newWidth = newValue.doubleValue();
            double curHeight = scene.getHeight();
            if (newWidth >= contentPane.getMinWidth() && curHeight >= contentPane.getMinHeight()) {
                Scale scale = new Scale(newWidth / this.startWidth, curHeight / this.startHeight, 0.0, 0.0);
                contentPane.getTransforms().setAll((Transform[])new Transform[]{scale});
                logger.debug("newWidth {}", (Object)newWidth);
            } else {
                scene.getRoot().prefWidth(contentPane.getMinWidth());
                scene.getRoot().prefHeight(contentPane.getMinHeight());
            }
        });
        scene.heightProperty().addListener((observable, oldValue, newValue) -> {
            double newHeight = newValue.doubleValue();
            double curWidth = scene.getWidth();
            if (newHeight >= contentPane.getMinHeight() && curWidth >= contentPane.getMinWidth()) {
                Scale scale = new Scale(curWidth / this.startWidth, newHeight / this.startHeight, 0.0, 0.0);
                contentPane.getTransforms().setAll((Transform[])new Transform[]{scale});
                logger.debug("newHeight {}", (Object)newHeight);
            } else {
                scene.getRoot().prefWidth(contentPane.getMinWidth());
                scene.getRoot().prefHeight(contentPane.getMinHeight());
            }
        });
    }

    public static void main(String[] args) {
        Main.launch(args);
    }

    class UpdateLogic
    implements Runnable {
        UpdateLogic() {
        }

        @Override
        public void run() {
            Main.this.logicController.update();
            if (BlockData.getInstance().getDisplayController() != null) {
                BlockData.getInstance().getDisplayController().update();
            }
        }
    }
}

