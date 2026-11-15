/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.control;

import com.sun.javafx.scene.control.IntegerField;
import com.sun.javafx.scene.control.Properties;
import com.sun.javafx.scene.control.WebColorField;
import com.sun.javafx.scene.control.skin.IntegerFieldSkin;
import com.sun.javafx.scene.control.skin.WebColorFieldSkin;
import com.sun.javafx.util.Utils;
import java.util.Collection;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableNumberValue;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Stop;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

public class CustomColorDialog
extends HBox {
    private final Stage dialog = new Stage();
    private ColorRectPane colorRectPane;
    private ControlsPane controlsPane;
    private ObjectProperty<Color> currentColorProperty = new SimpleObjectProperty<Color>(Color.WHITE);
    private ObjectProperty<Color> customColorProperty = new SimpleObjectProperty<Color>(Color.TRANSPARENT);
    private Runnable onSave;
    private Runnable onUse;
    private Runnable onCancel;
    private WebColorField webField = null;
    private Scene customScene;
    private String saveBtnText;
    private boolean showUseBtn = true;
    private boolean showOpacitySlider = true;
    private final EventHandler<KeyEvent> keyEventListener = keyEvent -> {
        switch (keyEvent.getCode()) {
            case ESCAPE: {
                this.dialog.setScene(null);
                this.dialog.close();
            }
        }
    };
    private InvalidationListener positionAdjuster = new InvalidationListener(){

        @Override
        public void invalidated(Observable observable) {
            if (Double.isNaN(CustomColorDialog.this.dialog.getWidth()) || Double.isNaN(CustomColorDialog.this.dialog.getHeight())) {
                return;
            }
            CustomColorDialog.this.dialog.widthProperty().removeListener(CustomColorDialog.this.positionAdjuster);
            CustomColorDialog.this.dialog.heightProperty().removeListener(CustomColorDialog.this.positionAdjuster);
            CustomColorDialog.this.fixPosition();
        }
    };

    public CustomColorDialog(Window window) {
        this.getStyleClass().add("custom-color-dialog");
        if (window != null) {
            this.dialog.initOwner(window);
        }
        this.dialog.setTitle(Properties.getColorPickerString("customColorDialogTitle"));
        this.dialog.initModality(Modality.APPLICATION_MODAL);
        this.dialog.initStyle(StageStyle.UTILITY);
        this.dialog.setResizable(false);
        this.dialog.addEventHandler(KeyEvent.ANY, this.keyEventListener);
        this.customScene = new Scene(this);
        Scene scene = window.getScene();
        if (scene != null) {
            if (scene.getUserAgentStylesheet() != null) {
                this.customScene.setUserAgentStylesheet(scene.getUserAgentStylesheet());
            }
            this.customScene.getStylesheets().addAll((Collection<String>)scene.getStylesheets());
        }
        this.buildUI();
        this.dialog.setScene(this.customScene);
    }

    private void buildUI() {
        this.colorRectPane = new ColorRectPane();
        this.controlsPane = new ControlsPane();
        CustomColorDialog.setHgrow(this.controlsPane, Priority.ALWAYS);
        this.getChildren().setAll((Node[])new Node[]{this.colorRectPane, this.controlsPane});
    }

    public void setCurrentColor(Color color) {
        this.currentColorProperty.set(color);
    }

    public final Color getCurrentColor() {
        return (Color)this.currentColorProperty.get();
    }

    public final ObjectProperty<Color> customColorProperty() {
        return this.customColorProperty;
    }

    public final void setCustomColor(Color color) {
        this.customColorProperty.set(color);
    }

    public final Color getCustomColor() {
        return (Color)this.customColorProperty.get();
    }

    public Runnable getOnSave() {
        return this.onSave;
    }

    public void setOnSave(Runnable runnable) {
        this.onSave = runnable;
    }

    public void setSaveBtnToOk() {
        this.saveBtnText = Properties.getColorPickerString("OK");
        this.buildUI();
    }

    public Runnable getOnUse() {
        return this.onUse;
    }

    public void setOnUse(Runnable runnable) {
        this.onUse = runnable;
    }

    public void setShowUseBtn(boolean bl) {
        this.showUseBtn = bl;
        this.buildUI();
    }

    public void setShowOpacitySlider(boolean bl) {
        this.showOpacitySlider = bl;
        this.buildUI();
    }

    public Runnable getOnCancel() {
        return this.onCancel;
    }

    public void setOnCancel(Runnable runnable) {
        this.onCancel = runnable;
    }

    public void setOnHidden(EventHandler<WindowEvent> eventHandler) {
        this.dialog.setOnHidden(eventHandler);
    }

    public Stage getDialog() {
        return this.dialog;
    }

    public void show() {
        if (this.dialog.getOwner() != null) {
            this.dialog.widthProperty().addListener(this.positionAdjuster);
            this.dialog.heightProperty().addListener(this.positionAdjuster);
            this.positionAdjuster.invalidated(null);
        }
        if (this.dialog.getScene() == null) {
            this.dialog.setScene(this.customScene);
        }
        this.colorRectPane.updateValues();
        this.dialog.show();
    }

    public void hide() {
        if (this.dialog.getOwner() != null) {
            this.dialog.hide();
        }
    }

    private void fixPosition() {
        Window window = this.dialog.getOwner();
        Screen screen = Utils.getScreen(window);
        Rectangle2D rectangle2D = screen.getBounds();
        double d = window.getX() + window.getWidth();
        double d2 = window.getX() - this.dialog.getWidth();
        double d3 = rectangle2D.getMaxX() >= d + this.dialog.getWidth() ? d : (rectangle2D.getMinX() <= d2 ? d2 : Math.max(rectangle2D.getMinX(), rectangle2D.getMaxX() - this.dialog.getWidth()));
        double d4 = Math.max(rectangle2D.getMinY(), Math.min(rectangle2D.getMaxY() - this.dialog.getHeight(), window.getY()));
        this.dialog.setX(d3);
        this.dialog.setY(d4);
    }

    @Override
    public void layoutChildren() {
        super.layoutChildren();
        if (this.dialog.getMinWidth() > 0.0 && this.dialog.getMinHeight() > 0.0) {
            return;
        }
        double d = Math.max(0.0, this.computeMinWidth(this.getHeight()) + (this.dialog.getWidth() - this.customScene.getWidth()));
        double d2 = Math.max(0.0, this.computeMinHeight(this.getWidth()) + (this.dialog.getHeight() - this.customScene.getHeight()));
        this.dialog.setMinWidth(d);
        this.dialog.setMinHeight(d2);
    }

    static double clamp(double d) {
        return d < 0.0 ? 0.0 : (d > 1.0 ? 1.0 : d);
    }

    private static LinearGradient createHueGradient() {
        Stop[] stopArray = new Stop[255];
        for (int i = 0; i < 255; ++i) {
            double d = 1.0 - 0.00392156862745098 * (double)i;
            int n = (int)((double)i / 255.0 * 360.0);
            stopArray[i] = new Stop(d, Color.hsb(n, 1.0, 1.0));
        }
        return new LinearGradient(0.0, 1.0, 0.0, 0.0, true, CycleMethod.NO_CYCLE, stopArray);
    }

    private static int doubleToInt(double d) {
        return (int)(d * 255.0 + 0.5);
    }

    private class ControlsPane
    extends VBox {
        private Label currentColorLabel;
        private Label newColorLabel;
        private Region currentColorRect;
        private Region newColorRect;
        private Region currentTransparent;
        private GridPane currentAndNewColor;
        private Region currentNewColorBorder;
        private ToggleButton hsbButton;
        private ToggleButton rgbButton;
        private ToggleButton webButton;
        private HBox hBox;
        private Label[] labels = new Label[4];
        private Slider[] sliders = new Slider[4];
        private IntegerField[] fields = new IntegerField[4];
        private Label[] units = new Label[4];
        private HBox buttonBox;
        private Region whiteBox;
        private GridPane settingsPane = new GridPane();
        private Property<Number>[] bindedProperties = new Property[4];

        public ControlsPane() {
            this.getStyleClass().add("controls-pane");
            this.currentNewColorBorder = new Region();
            this.currentNewColorBorder.setId("current-new-color-border");
            this.currentTransparent = new Region();
            this.currentTransparent.getStyleClass().addAll((String[])new String[]{"transparent-pattern"});
            this.currentColorRect = new Region();
            this.currentColorRect.getStyleClass().add("color-rect");
            this.currentColorRect.setId("current-color");
            this.currentColorRect.backgroundProperty().bind((ObservableValue<Background>)new ObjectBinding<Background>(){
                {
                    this.bind(CustomColorDialog.this.currentColorProperty);
                }

                @Override
                protected Background computeValue() {
                    return new Background(new BackgroundFill((Paint)CustomColorDialog.this.currentColorProperty.get(), CornerRadii.EMPTY, Insets.EMPTY));
                }
            });
            this.newColorRect = new Region();
            this.newColorRect.getStyleClass().add("color-rect");
            this.newColorRect.setId("new-color");
            this.newColorRect.backgroundProperty().bind((ObservableValue<Background>)new ObjectBinding<Background>(){
                {
                    this.bind(CustomColorDialog.this.customColorProperty);
                }

                @Override
                protected Background computeValue() {
                    return new Background(new BackgroundFill((Paint)CustomColorDialog.this.customColorProperty.get(), CornerRadii.EMPTY, Insets.EMPTY));
                }
            });
            this.currentColorLabel = new Label(Properties.getColorPickerString("currentColor"));
            this.newColorLabel = new Label(Properties.getColorPickerString("newColor"));
            this.whiteBox = new Region();
            this.whiteBox.getStyleClass().add("customcolor-controls-background");
            this.hsbButton = new ToggleButton(Properties.getColorPickerString("colorType.hsb"));
            this.hsbButton.getStyleClass().add("left-pill");
            this.rgbButton = new ToggleButton(Properties.getColorPickerString("colorType.rgb"));
            this.rgbButton.getStyleClass().add("center-pill");
            this.webButton = new ToggleButton(Properties.getColorPickerString("colorType.web"));
            this.webButton.getStyleClass().add("right-pill");
            ToggleGroup toggleGroup = new ToggleGroup();
            this.hBox = new HBox();
            this.hBox.setAlignment(Pos.CENTER);
            this.hBox.getChildren().addAll((Node[])new Node[]{this.hsbButton, this.rgbButton, this.webButton});
            Region region = new Region();
            region.setId("spacer1");
            Region region2 = new Region();
            region2.setId("spacer2");
            Region region3 = new Region();
            region3.setId("spacer-side");
            Region region4 = new Region();
            region4.setId("spacer-side");
            Region region5 = new Region();
            region5.setId("spacer-bottom");
            this.currentAndNewColor = new GridPane();
            this.currentAndNewColor.getColumnConstraints().addAll((ColumnConstraints[])new ColumnConstraints[]{new ColumnConstraints(), new ColumnConstraints()});
            ((ColumnConstraints)this.currentAndNewColor.getColumnConstraints().get(0)).setHgrow(Priority.ALWAYS);
            ((ColumnConstraints)this.currentAndNewColor.getColumnConstraints().get(1)).setHgrow(Priority.ALWAYS);
            this.currentAndNewColor.getRowConstraints().addAll((RowConstraints[])new RowConstraints[]{new RowConstraints(), new RowConstraints(), new RowConstraints()});
            ((RowConstraints)this.currentAndNewColor.getRowConstraints().get(2)).setVgrow(Priority.ALWAYS);
            VBox.setVgrow(this.currentAndNewColor, Priority.ALWAYS);
            this.currentAndNewColor.getStyleClass().add("current-new-color-grid");
            this.currentAndNewColor.add(this.currentColorLabel, 0, 0);
            this.currentAndNewColor.add(this.newColorLabel, 1, 0);
            this.currentAndNewColor.add(region, 0, 1, 2, 1);
            this.currentAndNewColor.add(this.currentTransparent, 0, 2, 2, 1);
            this.currentAndNewColor.add(this.currentColorRect, 0, 2);
            this.currentAndNewColor.add(this.newColorRect, 1, 2);
            this.currentAndNewColor.add(this.currentNewColorBorder, 0, 2, 2, 1);
            this.currentAndNewColor.add(region2, 0, 3, 2, 1);
            this.settingsPane = new GridPane();
            this.settingsPane.setId("settings-pane");
            this.settingsPane.getColumnConstraints().addAll((ColumnConstraints[])new ColumnConstraints[]{new ColumnConstraints(), new ColumnConstraints(), new ColumnConstraints(), new ColumnConstraints(), new ColumnConstraints(), new ColumnConstraints()});
            ((ColumnConstraints)this.settingsPane.getColumnConstraints().get(0)).setHgrow(Priority.NEVER);
            ((ColumnConstraints)this.settingsPane.getColumnConstraints().get(2)).setHgrow(Priority.ALWAYS);
            ((ColumnConstraints)this.settingsPane.getColumnConstraints().get(3)).setHgrow(Priority.NEVER);
            ((ColumnConstraints)this.settingsPane.getColumnConstraints().get(4)).setHgrow(Priority.NEVER);
            ((ColumnConstraints)this.settingsPane.getColumnConstraints().get(5)).setHgrow(Priority.NEVER);
            this.settingsPane.add(this.whiteBox, 0, 0, 6, 5);
            this.settingsPane.add(this.hBox, 0, 0, 6, 1);
            this.settingsPane.add(region3, 0, 0);
            this.settingsPane.add(region4, 5, 0);
            this.settingsPane.add(region5, 0, 4);
            CustomColorDialog.this.webField = new WebColorField();
            CustomColorDialog.this.webField.getStyleClass().add("web-field");
            CustomColorDialog.this.webField.setSkin(new WebColorFieldSkin(CustomColorDialog.this.webField));
            CustomColorDialog.this.webField.valueProperty().bindBidirectional(CustomColorDialog.this.customColorProperty);
            CustomColorDialog.this.webField.visibleProperty().bind(toggleGroup.selectedToggleProperty().isEqualTo(this.webButton));
            this.settingsPane.add(CustomColorDialog.this.webField, 2, 1);
            for (int i = 0; i < 4; ++i) {
                this.labels[i] = new Label();
                this.labels[i].getStyleClass().add("settings-label");
                this.sliders[i] = new Slider();
                this.fields[i] = new IntegerField();
                this.fields[i].getStyleClass().add("color-input-field");
                this.fields[i].setSkin(new IntegerFieldSkin(this.fields[i]));
                this.units[i] = new Label(i == 0 ? "\u00b0" : "%");
                this.units[i].getStyleClass().add("settings-unit");
                if (i > 0 && i < 3) {
                    this.labels[i].visibleProperty().bind(toggleGroup.selectedToggleProperty().isNotEqualTo(this.webButton));
                }
                if (i < 3) {
                    this.sliders[i].visibleProperty().bind(toggleGroup.selectedToggleProperty().isNotEqualTo(this.webButton));
                    this.fields[i].visibleProperty().bind(toggleGroup.selectedToggleProperty().isNotEqualTo(this.webButton));
                    this.units[i].visibleProperty().bind(toggleGroup.selectedToggleProperty().isEqualTo(this.hsbButton));
                }
                int n = 1 + i;
                if (i == 3) {
                    ++n;
                }
                if (i == 3 && !CustomColorDialog.this.showOpacitySlider) continue;
                this.settingsPane.add(this.labels[i], 1, n);
                this.settingsPane.add(this.sliders[i], 2, n);
                this.settingsPane.add(this.fields[i], 3, n);
                this.settingsPane.add(this.units[i], 4, n);
            }
            this.set(3, Properties.getColorPickerString("opacity_colon"), 100, CustomColorDialog.this.colorRectPane.alpha);
            this.hsbButton.setToggleGroup(toggleGroup);
            this.rgbButton.setToggleGroup(toggleGroup);
            this.webButton.setToggleGroup(toggleGroup);
            toggleGroup.selectedToggleProperty().addListener((observableValue, toggle, toggle2) -> {
                if (toggle2 == null) {
                    toggleGroup.selectToggle((Toggle)toggle);
                } else if (toggle2 == this.hsbButton) {
                    this.showHSBSettings();
                } else if (toggle2 == this.rgbButton) {
                    this.showRGBSettings();
                } else {
                    this.showWebSettings();
                }
            });
            toggleGroup.selectToggle(this.hsbButton);
            this.buttonBox = new HBox();
            this.buttonBox.setId("buttons-hbox");
            Button button = new Button(CustomColorDialog.this.saveBtnText != null && !CustomColorDialog.this.saveBtnText.isEmpty() ? CustomColorDialog.this.saveBtnText : Properties.getColorPickerString("Save"));
            button.setDefaultButton(true);
            button.setOnAction(actionEvent -> {
                if (CustomColorDialog.this.onSave != null) {
                    CustomColorDialog.this.onSave.run();
                }
                CustomColorDialog.this.dialog.hide();
            });
            Button button2 = new Button(Properties.getColorPickerString("Use"));
            button2.setOnAction(actionEvent -> {
                if (CustomColorDialog.this.onUse != null) {
                    CustomColorDialog.this.onUse.run();
                }
                CustomColorDialog.this.dialog.hide();
            });
            Button button3 = new Button(Properties.getColorPickerString("Cancel"));
            button3.setCancelButton(true);
            button3.setOnAction(actionEvent -> {
                CustomColorDialog.this.customColorProperty.set(CustomColorDialog.this.getCurrentColor());
                if (CustomColorDialog.this.onCancel != null) {
                    CustomColorDialog.this.onCancel.run();
                }
                CustomColorDialog.this.dialog.hide();
            });
            if (CustomColorDialog.this.showUseBtn) {
                this.buttonBox.getChildren().addAll((Node[])new Node[]{button, button2, button3});
            } else {
                this.buttonBox.getChildren().addAll((Node[])new Node[]{button, button3});
            }
            this.getChildren().addAll((Node[])new Node[]{this.currentAndNewColor, this.settingsPane, this.buttonBox});
        }

        private void showHSBSettings() {
            this.set(0, Properties.getColorPickerString("hue_colon"), 360, CustomColorDialog.this.colorRectPane.hue);
            this.set(1, Properties.getColorPickerString("saturation_colon"), 100, CustomColorDialog.this.colorRectPane.sat);
            this.set(2, Properties.getColorPickerString("brightness_colon"), 100, CustomColorDialog.this.colorRectPane.bright);
        }

        private void showRGBSettings() {
            this.set(0, Properties.getColorPickerString("red_colon"), 255, CustomColorDialog.this.colorRectPane.red);
            this.set(1, Properties.getColorPickerString("green_colon"), 255, CustomColorDialog.this.colorRectPane.green);
            this.set(2, Properties.getColorPickerString("blue_colon"), 255, CustomColorDialog.this.colorRectPane.blue);
        }

        private void showWebSettings() {
            this.labels[0].setText(Properties.getColorPickerString("web_colon"));
        }

        private void set(int n, String string, int n2, Property<Number> property) {
            this.labels[n].setText(string);
            if (this.bindedProperties[n] != null) {
                this.sliders[n].valueProperty().unbindBidirectional(this.bindedProperties[n]);
                this.fields[n].valueProperty().unbindBidirectional(this.bindedProperties[n]);
            }
            this.sliders[n].setMax(n2);
            this.sliders[n].valueProperty().bindBidirectional(property);
            this.labels[n].setLabelFor(this.sliders[n]);
            this.fields[n].setMaxValue(n2);
            this.fields[n].valueProperty().bindBidirectional(property);
            this.bindedProperties[n] = property;
        }
    }

    private class ColorRectPane
    extends HBox {
        private Pane colorRect;
        private Pane colorBar;
        private Pane colorRectOverlayOne;
        private Pane colorRectOverlayTwo;
        private Region colorRectIndicator;
        private Region colorBarIndicator;
        private boolean changeIsLocal = false;
        private DoubleProperty hue = new SimpleDoubleProperty(-1.0){

            @Override
            protected void invalidated() {
                if (!ColorRectPane.this.changeIsLocal) {
                    ColorRectPane.this.changeIsLocal = true;
                    ColorRectPane.this.updateHSBColor();
                    ColorRectPane.this.changeIsLocal = false;
                }
            }
        };
        private DoubleProperty sat = new SimpleDoubleProperty(-1.0){

            @Override
            protected void invalidated() {
                if (!ColorRectPane.this.changeIsLocal) {
                    ColorRectPane.this.changeIsLocal = true;
                    ColorRectPane.this.updateHSBColor();
                    ColorRectPane.this.changeIsLocal = false;
                }
            }
        };
        private DoubleProperty bright = new SimpleDoubleProperty(-1.0){

            @Override
            protected void invalidated() {
                if (!ColorRectPane.this.changeIsLocal) {
                    ColorRectPane.this.changeIsLocal = true;
                    ColorRectPane.this.updateHSBColor();
                    ColorRectPane.this.changeIsLocal = false;
                }
            }
        };
        private IntegerProperty red = new SimpleIntegerProperty(-1){

            @Override
            protected void invalidated() {
                if (!ColorRectPane.this.changeIsLocal) {
                    ColorRectPane.this.changeIsLocal = true;
                    ColorRectPane.this.updateRGBColor();
                    ColorRectPane.this.changeIsLocal = false;
                }
            }
        };
        private IntegerProperty green = new SimpleIntegerProperty(-1){

            @Override
            protected void invalidated() {
                if (!ColorRectPane.this.changeIsLocal) {
                    ColorRectPane.this.changeIsLocal = true;
                    ColorRectPane.this.updateRGBColor();
                    ColorRectPane.this.changeIsLocal = false;
                }
            }
        };
        private IntegerProperty blue = new SimpleIntegerProperty(-1){

            @Override
            protected void invalidated() {
                if (!ColorRectPane.this.changeIsLocal) {
                    ColorRectPane.this.changeIsLocal = true;
                    ColorRectPane.this.updateRGBColor();
                    ColorRectPane.this.changeIsLocal = false;
                }
            }
        };
        private DoubleProperty alpha = new SimpleDoubleProperty(100.0){

            @Override
            protected void invalidated() {
                if (!ColorRectPane.this.changeIsLocal) {
                    ColorRectPane.this.changeIsLocal = true;
                    CustomColorDialog.this.setCustomColor(new Color(CustomColorDialog.this.getCustomColor().getRed(), CustomColorDialog.this.getCustomColor().getGreen(), CustomColorDialog.this.getCustomColor().getBlue(), CustomColorDialog.clamp(ColorRectPane.this.alpha.get() / 100.0)));
                    ColorRectPane.this.changeIsLocal = false;
                }
            }
        };

        private void updateRGBColor() {
            Color color = Color.rgb(this.red.get(), this.green.get(), this.blue.get(), CustomColorDialog.clamp(this.alpha.get() / 100.0));
            this.hue.set(color.getHue());
            this.sat.set(color.getSaturation() * 100.0);
            this.bright.set(color.getBrightness() * 100.0);
            CustomColorDialog.this.setCustomColor(color);
        }

        private void updateHSBColor() {
            Color color = Color.hsb(this.hue.get(), CustomColorDialog.clamp(this.sat.get() / 100.0), CustomColorDialog.clamp(this.bright.get() / 100.0), CustomColorDialog.clamp(this.alpha.get() / 100.0));
            this.red.set(CustomColorDialog.doubleToInt(color.getRed()));
            this.green.set(CustomColorDialog.doubleToInt(color.getGreen()));
            this.blue.set(CustomColorDialog.doubleToInt(color.getBlue()));
            CustomColorDialog.this.setCustomColor(color);
        }

        private void colorChanged() {
            if (!this.changeIsLocal) {
                this.changeIsLocal = true;
                this.hue.set(CustomColorDialog.this.getCustomColor().getHue());
                this.sat.set(CustomColorDialog.this.getCustomColor().getSaturation() * 100.0);
                this.bright.set(CustomColorDialog.this.getCustomColor().getBrightness() * 100.0);
                this.red.set(CustomColorDialog.doubleToInt(CustomColorDialog.this.getCustomColor().getRed()));
                this.green.set(CustomColorDialog.doubleToInt(CustomColorDialog.this.getCustomColor().getGreen()));
                this.blue.set(CustomColorDialog.doubleToInt(CustomColorDialog.this.getCustomColor().getBlue()));
                this.changeIsLocal = false;
            }
        }

        public ColorRectPane() {
            this.getStyleClass().add("color-rect-pane");
            CustomColorDialog.this.customColorProperty().addListener((observableValue, color, color2) -> this.colorChanged());
            this.colorRectIndicator = new Region();
            this.colorRectIndicator.setId("color-rect-indicator");
            this.colorRectIndicator.setManaged(false);
            this.colorRectIndicator.setMouseTransparent(true);
            this.colorRectIndicator.setCache(true);
            StackPane stackPane = new StackPane();
            this.colorRect = new StackPane(){

                @Override
                public Orientation getContentBias() {
                    return Orientation.VERTICAL;
                }

                @Override
                protected double computePrefWidth(double d) {
                    return d;
                }

                @Override
                protected double computeMaxWidth(double d) {
                    return d;
                }
            };
            this.colorRect.getStyleClass().addAll((String[])new String[]{"color-rect", "transparent-pattern"});
            Pane pane = new Pane();
            pane.backgroundProperty().bind((ObservableValue<Background>)new ObjectBinding<Background>(){
                {
                    this.bind(ColorRectPane.this.hue);
                }

                @Override
                protected Background computeValue() {
                    return new Background(new BackgroundFill(Color.hsb(ColorRectPane.this.hue.getValue(), 1.0, 1.0), CornerRadii.EMPTY, Insets.EMPTY));
                }
            });
            this.colorRectOverlayOne = new Pane();
            this.colorRectOverlayOne.getStyleClass().add("color-rect");
            this.colorRectOverlayOne.setBackground(new Background(new BackgroundFill(new LinearGradient(0.0, 0.0, 1.0, 0.0, true, CycleMethod.NO_CYCLE, new Stop(0.0, Color.rgb(255, 255, 255, 1.0)), new Stop(1.0, Color.rgb(255, 255, 255, 0.0))), CornerRadii.EMPTY, Insets.EMPTY)));
            EventHandler<MouseEvent> eventHandler = mouseEvent -> {
                double d = mouseEvent.getX();
                double d2 = mouseEvent.getY();
                this.sat.set(CustomColorDialog.clamp(d / this.colorRect.getWidth()) * 100.0);
                this.bright.set(100.0 - CustomColorDialog.clamp(d2 / this.colorRect.getHeight()) * 100.0);
            };
            this.colorRectOverlayTwo = new Pane();
            this.colorRectOverlayTwo.getStyleClass().addAll((String[])new String[]{"color-rect"});
            this.colorRectOverlayTwo.setBackground(new Background(new BackgroundFill(new LinearGradient(0.0, 0.0, 0.0, 1.0, true, CycleMethod.NO_CYCLE, new Stop(0.0, Color.rgb(0, 0, 0, 0.0)), new Stop(1.0, Color.rgb(0, 0, 0, 1.0))), CornerRadii.EMPTY, Insets.EMPTY)));
            this.colorRectOverlayTwo.setOnMouseDragged(eventHandler);
            this.colorRectOverlayTwo.setOnMousePressed(eventHandler);
            Pane pane2 = new Pane();
            pane2.setMouseTransparent(true);
            pane2.getStyleClass().addAll((String[])new String[]{"color-rect", "color-rect-border"});
            this.colorBar = new Pane();
            this.colorBar.getStyleClass().add("color-bar");
            this.colorBar.setBackground(new Background(new BackgroundFill(CustomColorDialog.createHueGradient(), CornerRadii.EMPTY, Insets.EMPTY)));
            this.colorBarIndicator = new Region();
            this.colorBarIndicator.setId("color-bar-indicator");
            this.colorBarIndicator.setMouseTransparent(true);
            this.colorBarIndicator.setCache(true);
            this.colorRectIndicator.layoutXProperty().bind(this.sat.divide(100).multiply(this.colorRect.widthProperty()));
            this.colorRectIndicator.layoutYProperty().bind(Bindings.subtract(1, (ObservableNumberValue)this.bright.divide(100)).multiply(this.colorRect.heightProperty()));
            this.colorBarIndicator.layoutYProperty().bind(this.hue.divide(360).multiply(this.colorBar.heightProperty()));
            stackPane.opacityProperty().bind(this.alpha.divide(100));
            EventHandler<MouseEvent> eventHandler2 = mouseEvent -> {
                double d = mouseEvent.getY();
                this.hue.set(CustomColorDialog.clamp(d / this.colorRect.getHeight()) * 360.0);
            };
            this.colorBar.setOnMouseDragged(eventHandler2);
            this.colorBar.setOnMousePressed(eventHandler2);
            this.colorBar.getChildren().setAll((Node[])new Node[]{this.colorBarIndicator});
            stackPane.getChildren().setAll((Node[])new Node[]{pane, this.colorRectOverlayOne, this.colorRectOverlayTwo});
            this.colorRect.getChildren().setAll((Node[])new Node[]{stackPane, pane2, this.colorRectIndicator});
            HBox.setHgrow(this.colorRect, Priority.SOMETIMES);
            this.getChildren().addAll((Node[])new Node[]{this.colorRect, this.colorBar});
        }

        private void updateValues() {
            if (CustomColorDialog.this.getCurrentColor() == null) {
                CustomColorDialog.this.setCurrentColor(Color.TRANSPARENT);
            }
            this.changeIsLocal = true;
            this.hue.set(CustomColorDialog.this.getCurrentColor().getHue());
            this.sat.set(CustomColorDialog.this.getCurrentColor().getSaturation() * 100.0);
            this.bright.set(CustomColorDialog.this.getCurrentColor().getBrightness() * 100.0);
            this.alpha.set(CustomColorDialog.this.getCurrentColor().getOpacity() * 100.0);
            CustomColorDialog.this.setCustomColor(Color.hsb(this.hue.get(), CustomColorDialog.clamp(this.sat.get() / 100.0), CustomColorDialog.clamp(this.bright.get() / 100.0), CustomColorDialog.clamp(this.alpha.get() / 100.0)));
            this.red.set(CustomColorDialog.doubleToInt(CustomColorDialog.this.getCustomColor().getRed()));
            this.green.set(CustomColorDialog.doubleToInt(CustomColorDialog.this.getCustomColor().getGreen()));
            this.blue.set(CustomColorDialog.doubleToInt(CustomColorDialog.this.getCustomColor().getBlue()));
            this.changeIsLocal = false;
        }

        @Override
        protected void layoutChildren() {
            super.layoutChildren();
            this.colorRectIndicator.autosize();
            double d = Math.min(this.colorRect.getWidth(), this.colorRect.getHeight());
            this.colorRect.resize(d, d);
            this.colorBar.resize(this.colorBar.getWidth(), d);
        }
    }
}

