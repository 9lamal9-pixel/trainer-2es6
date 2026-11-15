/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.web;

import com.sun.javafx.application.PlatformImpl;
import com.sun.javafx.scene.ParentHelper;
import com.sun.javafx.scene.control.skin.FXVK;
import com.sun.javafx.scene.traversal.Algorithm;
import com.sun.javafx.scene.traversal.Direction;
import com.sun.javafx.scene.traversal.ParentTraversalEngine;
import com.sun.javafx.scene.traversal.TraversalContext;
import com.sun.javafx.scene.web.behavior.HTMLEditorBehavior;
import com.sun.javafx.webkit.Accessor;
import com.sun.webkit.WebPage;
import java.security.AccessController;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.css.StyleableProperty;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Orientation;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.SkinBase;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebView;
import javafx.util.Callback;
import org.w3c.dom.html.HTMLDocument;
import org.w3c.dom.html.HTMLElement;

public class HTMLEditorSkin
extends SkinBase<HTMLEditor> {
    private GridPane gridPane;
    private ToolBar toolbar1;
    private ToolBar toolbar2;
    private Button cutButton;
    private Button copyButton;
    private Button pasteButton;
    private Button insertHorizontalRuleButton;
    private ToggleGroup alignmentToggleGroup;
    private ToggleButton alignLeftButton;
    private ToggleButton alignCenterButton;
    private ToggleButton alignRightButton;
    private ToggleButton alignJustifyButton;
    private ToggleButton bulletsButton;
    private ToggleButton numbersButton;
    private Button indentButton;
    private Button outdentButton;
    private ComboBox<String> formatComboBox;
    private Map<String, String> formatStyleMap;
    private Map<String, String> styleFormatMap;
    private ComboBox<String> fontFamilyComboBox;
    private ComboBox<String> fontSizeComboBox;
    private Map<String, String> fontSizeMap;
    private Map<String, String> sizeFontMap;
    private ToggleButton boldButton;
    private ToggleButton italicButton;
    private ToggleButton underlineButton;
    private ToggleButton strikethroughButton;
    private ColorPicker fgColorButton;
    private ColorPicker bgColorButton;
    private WebView webView;
    private WebPage webPage;
    private ParentTraversalEngine engine;
    private boolean resetToolbarState = false;
    private String cachedHTMLText = "<html><head></head><body contenteditable=\"true\"></body></html>";
    private ResourceBundle resources;
    private boolean enableAtomicityCheck = false;
    private int atomicityCount = 0;
    private boolean isFirstRun = true;
    private static final int FONT_FAMILY_MENUBUTTON_WIDTH = 150;
    private static final int FONT_FAMILY_MENU_WIDTH = 100;
    private static final int FONT_SIZE_MENUBUTTON_WIDTH = 80;
    private static final Color DEFAULT_BG_COLOR = Color.WHITE;
    private static final Color DEFAULT_FG_COLOR = Color.BLACK;
    private static final String FORMAT_PARAGRAPH = "<p>";
    private static final String FORMAT_HEADING_1 = "<h1>";
    private static final String FORMAT_HEADING_2 = "<h2>";
    private static final String FORMAT_HEADING_3 = "<h3>";
    private static final String FORMAT_HEADING_4 = "<h4>";
    private static final String FORMAT_HEADING_5 = "<h5>";
    private static final String FORMAT_HEADING_6 = "<h6>";
    private static final String SIZE_XX_SMALL = "1";
    private static final String SIZE_X_SMALL = "2";
    private static final String SIZE_SMALL = "3";
    private static final String SIZE_MEDIUM = "4";
    private static final String SIZE_LARGE = "5";
    private static final String SIZE_X_LARGE = "6";
    private static final String SIZE_XX_LARGE = "7";
    private static final String[][] DEFAULT_FORMAT_MAPPINGS = new String[][]{{"<p>", "", "3"}, {"<h1>", Command.BOLD.getCommand(), "6"}, {"<h2>", Command.BOLD.getCommand(), "5"}, {"<h3>", Command.BOLD.getCommand(), "4"}, {"<h4>", Command.BOLD.getCommand(), "3"}, {"<h5>", Command.BOLD.getCommand(), "2"}, {"<h6>", Command.BOLD.getCommand(), "1"}};
    private static PseudoClass CONTAINS_FOCUS_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("contains-focus");
    private ListChangeListener<Node> itemsListener = change -> {
        while (change.next()) {
            if (change.getRemovedSize() <= 0) continue;
            for (Node node : change.getList()) {
                if (!(node instanceof WebView)) continue;
                this.webPage.dispose();
            }
        }
    };

    public HTMLEditorSkin(HTMLEditor hTMLEditor) {
        super(hTMLEditor);
        HTMLEditorBehavior hTMLEditorBehavior = new HTMLEditorBehavior(hTMLEditor);
        this.getChildren().clear();
        this.gridPane = new GridPane();
        this.gridPane.getStyleClass().add("grid");
        this.getChildren().addAll((Node[])new Node[]{this.gridPane});
        this.toolbar1 = new ToolBar();
        this.toolbar1.getStyleClass().add("top-toolbar");
        this.gridPane.add(this.toolbar1, 0, 0);
        this.toolbar2 = new ToolBar();
        this.toolbar2.getStyleClass().add("bottom-toolbar");
        this.gridPane.add(this.toolbar2, 0, 1);
        this.webView = new WebView();
        this.gridPane.add(this.webView, 0, 2);
        ColumnConstraints columnConstraints = new ColumnConstraints();
        columnConstraints.setHgrow(Priority.ALWAYS);
        this.gridPane.getColumnConstraints().add(columnConstraints);
        this.webPage = Accessor.getPageFor(this.webView.getEngine());
        this.webView.addEventHandler(MouseEvent.MOUSE_RELEASED, mouseEvent -> Platform.runLater(new Runnable(){

            @Override
            public void run() {
                HTMLEditorSkin.this.enableAtomicityCheck = true;
                HTMLEditorSkin.this.updateToolbarState(true);
                HTMLEditorSkin.this.enableAtomicityCheck = false;
            }
        }));
        this.webView.addEventHandler(KeyEvent.KEY_PRESSED, keyEvent -> {
            this.applyTextFormatting();
            if (keyEvent.getCode() == KeyCode.CONTROL || keyEvent.getCode() == KeyCode.META) {
                return;
            }
            if (keyEvent.getCode() == KeyCode.TAB && !keyEvent.isControlDown()) {
                if (!keyEvent.isShiftDown()) {
                    if (this.getCommandState(Command.BULLETS.getCommand()) || this.getCommandState(Command.NUMBERS.getCommand())) {
                        this.executeCommand(Command.INDENT.getCommand(), null);
                    } else {
                        this.executeCommand(Command.INSERT_TAB.getCommand(), null);
                    }
                } else if (this.getCommandState(Command.BULLETS.getCommand()) || this.getCommandState(Command.NUMBERS.getCommand())) {
                    this.executeCommand(Command.OUTDENT.getCommand(), null);
                }
                return;
            }
            if (this.fgColorButton != null && this.fgColorButton.isShowing() || this.bgColorButton != null && this.bgColorButton.isShowing()) {
                return;
            }
            Platform.runLater(() -> {
                if (this.webPage.getClientSelectedText().isEmpty()) {
                    if (keyEvent.getCode() == KeyCode.UP || keyEvent.getCode() == KeyCode.DOWN || keyEvent.getCode() == KeyCode.LEFT || keyEvent.getCode() == KeyCode.RIGHT || keyEvent.getCode() == KeyCode.HOME || keyEvent.getCode() == KeyCode.END) {
                        this.updateToolbarState(true);
                    } else if (keyEvent.isControlDown() || keyEvent.isMetaDown()) {
                        if (keyEvent.getCode() == KeyCode.B) {
                            this.performCommand(Command.BOLD);
                        } else if (keyEvent.getCode() == KeyCode.I) {
                            this.performCommand(Command.ITALIC);
                        } else if (keyEvent.getCode() == KeyCode.U) {
                            this.performCommand(Command.UNDERLINE);
                        }
                        this.updateToolbarState(true);
                    } else {
                        boolean bl = this.resetToolbarState = keyEvent.getCode() == KeyCode.ENTER;
                        if (this.resetToolbarState && this.getCommandState(Command.BOLD.getCommand()) != this.boldButton.selectedProperty().getValue().booleanValue()) {
                            this.executeCommand(Command.BOLD.getCommand(), this.boldButton.selectedProperty().getValue().toString());
                        }
                        this.updateToolbarState(false);
                    }
                    this.resetToolbarState = false;
                } else if (keyEvent.isShiftDown() && (keyEvent.getCode() == KeyCode.UP || keyEvent.getCode() == KeyCode.DOWN || keyEvent.getCode() == KeyCode.LEFT || keyEvent.getCode() == KeyCode.RIGHT || keyEvent.getCode() == KeyCode.HOME || keyEvent.getCode() == KeyCode.END)) {
                    this.updateToolbarState(true);
                } else if ((keyEvent.isControlDown() || keyEvent.isMetaDown()) && keyEvent.getCode() == KeyCode.A) {
                    this.updateToolbarState(true);
                }
            });
        });
        this.webView.addEventHandler(KeyEvent.KEY_RELEASED, keyEvent -> {
            if (keyEvent.getCode() == KeyCode.CONTROL || keyEvent.getCode() == KeyCode.META) {
                return;
            }
            if (this.fgColorButton != null && this.fgColorButton.isShowing() || this.bgColorButton != null && this.bgColorButton.isShowing()) {
                return;
            }
            Platform.runLater(() -> {
                if (this.webPage.getClientSelectedText().isEmpty()) {
                    if (keyEvent.getCode() == KeyCode.UP || keyEvent.getCode() == KeyCode.DOWN || keyEvent.getCode() == KeyCode.LEFT || keyEvent.getCode() == KeyCode.RIGHT || keyEvent.getCode() == KeyCode.HOME || keyEvent.getCode() == KeyCode.END) {
                        this.updateToolbarState(true);
                    } else if (keyEvent.isControlDown() || keyEvent.isMetaDown()) {
                        if (keyEvent.getCode() == KeyCode.B) {
                            this.performCommand(Command.BOLD);
                        } else if (keyEvent.getCode() == KeyCode.I) {
                            this.performCommand(Command.ITALIC);
                        } else if (keyEvent.getCode() == KeyCode.U) {
                            this.performCommand(Command.UNDERLINE);
                        }
                        this.updateToolbarState(true);
                    } else {
                        boolean bl = this.resetToolbarState = keyEvent.getCode() == KeyCode.ENTER;
                        if (!this.resetToolbarState) {
                            this.updateToolbarState(false);
                        }
                    }
                    this.resetToolbarState = false;
                }
            });
        });
        ((HTMLEditor)this.getSkinnable()).focusedProperty().addListener((observableValue, bl, bl2) -> Platform.runLater(new Runnable((Boolean)bl2){
            final /* synthetic */ Boolean val$newValue;
            {
                this.val$newValue = bl;
            }

            @Override
            public void run() {
                if (this.val$newValue.booleanValue()) {
                    HTMLEditorSkin.this.webView.requestFocus();
                }
            }
        }));
        this.webView.focusedProperty().addListener((observableValue, bl, bl2) -> {
            this.pseudoClassStateChanged(CONTAINS_FOCUS_PSEUDOCLASS_STATE, (boolean)bl2);
            Platform.runLater(new Runnable((Boolean)bl2){
                final /* synthetic */ Boolean val$newValue;
                {
                    this.val$newValue = bl;
                }

                @Override
                public void run() {
                    HTMLEditorSkin.this.updateToolbarState(true);
                    if (PlatformImpl.isSupported(ConditionalFeature.VIRTUAL_KEYBOARD)) {
                        Scene scene = ((HTMLEditor)HTMLEditorSkin.this.getSkinnable()).getScene();
                        if (this.val$newValue.booleanValue()) {
                            FXVK.attach(HTMLEditorSkin.this.webView);
                        } else if (scene == null || scene.getWindow() == null || !scene.getWindow().isFocused() || !(scene.getFocusOwner() instanceof TextInputControl)) {
                            FXVK.detach();
                        }
                    }
                }
            });
        });
        this.webView.getEngine().getLoadWorker().workDoneProperty().addListener((observableValue, number, number2) -> {
            Platform.runLater(() -> this.webView.requestLayout());
            double d = this.webView.getEngine().getLoadWorker().getTotalWork();
            if (number2.doubleValue() == d) {
                this.cachedHTMLText = null;
                Platform.runLater(() -> {
                    this.setContentEditable(true);
                    this.updateToolbarState(true);
                    this.updateNodeOrientation();
                    this.executeCommand(Command.STYLEWITHCSS.getCommand(), "true");
                });
            }
        });
        this.enableToolbar(true);
        this.setHTMLText(this.cachedHTMLText);
        this.engine = new ParentTraversalEngine((Parent)((Object)this.getSkinnable()), new Algorithm(){

            @Override
            public Node select(Node node, Direction direction, TraversalContext traversalContext) {
                return HTMLEditorSkin.this.cutButton;
            }

            @Override
            public Node selectFirst(TraversalContext traversalContext) {
                return HTMLEditorSkin.this.cutButton;
            }

            @Override
            public Node selectLast(TraversalContext traversalContext) {
                return HTMLEditorSkin.this.cutButton;
            }
        });
        ParentHelper.setTraversalEngine((Parent)((Object)this.getSkinnable()), this.engine);
        this.webView.setFocusTraversable(true);
        this.gridPane.getChildren().addListener(this.itemsListener);
    }

    public void performCommand(Command command) {
        switch (command) {
            case BOLD: {
                this.boldButton.fire();
                break;
            }
            case ITALIC: {
                this.italicButton.setSelected(!this.italicButton.isSelected());
                break;
            }
            case UNDERLINE: {
                this.underlineButton.setSelected(!this.underlineButton.isSelected());
            }
        }
    }

    @Override
    protected void layoutChildren(double d, double d2, double d3, double d4) {
        if (this.isFirstRun) {
            this.populateToolbars();
            this.isFirstRun = false;
        }
        super.layoutChildren(d, d2, d3, d4);
        double d5 = Math.max(this.toolbar1.prefWidth(-1.0), this.toolbar2.prefWidth(-1.0));
        this.toolbar1.setMinWidth(d5);
        this.toolbar1.setPrefWidth(d5);
        this.toolbar2.setMinWidth(d5);
        this.toolbar2.setPrefWidth(d5);
    }

    final String getHTMLText() {
        return this.cachedHTMLText != null ? this.cachedHTMLText : this.webPage.getHtml(this.webPage.getMainFrame());
    }

    final void setHTMLText(String string) {
        this.cachedHTMLText = string;
        this.webPage.load(this.webPage.getMainFrame(), string, "text/html");
        Platform.runLater(() -> this.updateToolbarState(true));
    }

    private void populateToolbars() {
        this.resources = ResourceBundle.getBundle(HTMLEditorSkin.class.getName());
        this.cutButton = this.addButton(this.toolbar1, this.resources.getString("cutIcon"), this.resources.getString("cut"), Command.CUT.getCommand(), "html-editor-cut");
        this.copyButton = this.addButton(this.toolbar1, this.resources.getString("copyIcon"), this.resources.getString("copy"), Command.COPY.getCommand(), "html-editor-copy");
        this.pasteButton = this.addButton(this.toolbar1, this.resources.getString("pasteIcon"), this.resources.getString("paste"), Command.PASTE.getCommand(), "html-editor-paste");
        this.toolbar1.getItems().add(new Separator(Orientation.VERTICAL));
        this.alignmentToggleGroup = new ToggleGroup();
        this.alignLeftButton = this.addToggleButton(this.toolbar1, this.alignmentToggleGroup, this.resources.getString("alignLeftIcon"), this.resources.getString("alignLeft"), Command.ALIGN_LEFT.getCommand(), "html-editor-align-left");
        this.alignCenterButton = this.addToggleButton(this.toolbar1, this.alignmentToggleGroup, this.resources.getString("alignCenterIcon"), this.resources.getString("alignCenter"), Command.ALIGN_CENTER.getCommand(), "html-editor-align-center");
        this.alignRightButton = this.addToggleButton(this.toolbar1, this.alignmentToggleGroup, this.resources.getString("alignRightIcon"), this.resources.getString("alignRight"), Command.ALIGN_RIGHT.getCommand(), "html-editor-align-right");
        this.alignJustifyButton = this.addToggleButton(this.toolbar1, this.alignmentToggleGroup, this.resources.getString("alignJustifyIcon"), this.resources.getString("alignJustify"), Command.ALIGN_JUSTIFY.getCommand(), "html-editor-align-justify");
        this.toolbar1.getItems().add(new Separator(Orientation.VERTICAL));
        this.outdentButton = this.addButton(this.toolbar1, this.resources.getString("outdentIcon"), this.resources.getString("outdent"), Command.OUTDENT.getCommand(), "html-editor-outdent");
        if (this.outdentButton.getGraphic() != null) {
            this.outdentButton.getGraphic().setNodeOrientation(NodeOrientation.INHERIT);
        }
        this.indentButton = this.addButton(this.toolbar1, this.resources.getString("indentIcon"), this.resources.getString("indent"), Command.INDENT.getCommand(), "html-editor-indent");
        if (this.indentButton.getGraphic() != null) {
            this.indentButton.getGraphic().setNodeOrientation(NodeOrientation.INHERIT);
        }
        this.toolbar1.getItems().add(new Separator(Orientation.VERTICAL));
        ToggleGroup toggleGroup = new ToggleGroup();
        this.bulletsButton = this.addToggleButton(this.toolbar1, toggleGroup, this.resources.getString("bulletsIcon"), this.resources.getString("bullets"), Command.BULLETS.getCommand(), "html-editor-bullets");
        if (this.bulletsButton.getGraphic() != null) {
            this.bulletsButton.getGraphic().setNodeOrientation(NodeOrientation.INHERIT);
        }
        this.numbersButton = this.addToggleButton(this.toolbar1, toggleGroup, this.resources.getString("numbersIcon"), this.resources.getString("numbers"), Command.NUMBERS.getCommand(), "html-editor-numbers");
        this.toolbar1.getItems().add(new Separator(Orientation.VERTICAL));
        this.formatComboBox = new ComboBox();
        this.formatComboBox.getStyleClass().add("font-menu-button");
        this.formatComboBox.setFocusTraversable(false);
        this.formatComboBox.setMinWidth(Double.NEGATIVE_INFINITY);
        this.toolbar2.getItems().add(this.formatComboBox);
        this.formatStyleMap = new HashMap<String, String>();
        this.styleFormatMap = new HashMap<String, String>();
        this.createFormatMenuItem(FORMAT_PARAGRAPH, this.resources.getString("paragraph"));
        Platform.runLater(() -> this.formatComboBox.setValue(this.resources.getString("paragraph")));
        this.createFormatMenuItem(FORMAT_HEADING_1, this.resources.getString("heading1"));
        this.createFormatMenuItem(FORMAT_HEADING_2, this.resources.getString("heading2"));
        this.createFormatMenuItem(FORMAT_HEADING_3, this.resources.getString("heading3"));
        this.createFormatMenuItem(FORMAT_HEADING_4, this.resources.getString("heading4"));
        this.createFormatMenuItem(FORMAT_HEADING_5, this.resources.getString("heading5"));
        this.createFormatMenuItem(FORMAT_HEADING_6, this.resources.getString("heading6"));
        this.formatComboBox.setTooltip(new Tooltip(this.resources.getString("format")));
        this.formatComboBox.valueProperty().addListener((observableValue, string, string2) -> {
            if (string2 == null) {
                this.formatComboBox.setValue(null);
            } else {
                String string3 = this.formatStyleMap.get(string2);
                this.executeCommand(Command.FORMAT.getCommand(), string3);
                this.updateToolbarState(false);
                for (int i = 0; i < DEFAULT_FORMAT_MAPPINGS.length; ++i) {
                    String[] stringArray = DEFAULT_FORMAT_MAPPINGS[i];
                    if (!stringArray[0].equalsIgnoreCase(string3)) continue;
                    this.executeCommand(Command.FONT_SIZE.getCommand(), stringArray[2]);
                    this.updateToolbarState(false);
                    break;
                }
            }
        });
        this.fontFamilyComboBox = new ComboBox();
        this.fontFamilyComboBox.getStyleClass().add("font-menu-button");
        this.fontFamilyComboBox.setMinWidth(150.0);
        this.fontFamilyComboBox.setPrefWidth(150.0);
        this.fontFamilyComboBox.setMaxWidth(150.0);
        this.fontFamilyComboBox.setFocusTraversable(false);
        this.fontFamilyComboBox.setTooltip(new Tooltip(this.resources.getString("fontFamily")));
        this.toolbar2.getItems().add(this.fontFamilyComboBox);
        this.fontFamilyComboBox.getProperties().put("comboBoxRowsToMeasureWidth", 0);
        this.fontFamilyComboBox.setCellFactory(new Callback<ListView<String>, ListCell<String>>(){

            @Override
            public ListCell<String> call(ListView<String> listView) {
                ListCell<String> listCell = new ListCell<String>(){

                    @Override
                    public void updateItem(String string, boolean bl) {
                        super.updateItem(string, bl);
                        if (string != null) {
                            this.setText(string);
                            this.setFont(new Font(string, 12.0));
                        }
                    }
                };
                listCell.setMinWidth(100.0);
                listCell.setPrefWidth(100.0);
                listCell.setMaxWidth(100.0);
                return listCell;
            }
        });
        Platform.runLater(() -> {
            ObservableList<String> observableList = FXCollections.observableArrayList(Font.getFamilies());
            observableList.add(0, "");
            for (String string : observableList) {
                this.fontFamilyComboBox.setValue("");
                this.fontFamilyComboBox.setItems(observableList);
            }
        });
        this.fontFamilyComboBox.valueProperty().addListener((observableValue, string, string2) -> this.executeCommand(Command.FONT_FAMILY.getCommand(), "".equals(string2) ? "''" : string2));
        this.fontSizeComboBox = new ComboBox();
        this.fontSizeComboBox.getStyleClass().add("font-menu-button");
        this.fontSizeComboBox.setFocusTraversable(false);
        this.toolbar2.getItems().add(this.fontSizeComboBox);
        this.fontSizeMap = new HashMap<String, String>();
        this.sizeFontMap = new HashMap<String, String>();
        this.createFontSizeMenuItem(SIZE_XX_SMALL, this.resources.getString("extraExtraSmall"));
        this.createFontSizeMenuItem(SIZE_X_SMALL, this.resources.getString("extraSmall"));
        this.createFontSizeMenuItem(SIZE_SMALL, this.resources.getString("small"));
        Platform.runLater(() -> this.fontSizeComboBox.setValue(this.resources.getString("small")));
        this.createFontSizeMenuItem(SIZE_MEDIUM, this.resources.getString("medium"));
        this.createFontSizeMenuItem(SIZE_LARGE, this.resources.getString("large"));
        this.createFontSizeMenuItem(SIZE_X_LARGE, this.resources.getString("extraLarge"));
        this.createFontSizeMenuItem(SIZE_XX_LARGE, this.resources.getString("extraExtraLarge"));
        this.fontSizeComboBox.setTooltip(new Tooltip(this.resources.getString("fontSize")));
        this.fontSizeComboBox.setCellFactory(new Callback<ListView<String>, ListCell<String>>(){

            @Override
            public ListCell<String> call(ListView<String> listView) {
                ListCell<String> listCell = new ListCell<String>(){

                    @Override
                    public void updateItem(String string, boolean bl) {
                        super.updateItem(string, bl);
                        if (string != null) {
                            this.setText(string);
                            String string2 = string.replaceFirst("[^0-9.].*$", "");
                            this.setFont(new Font((String)HTMLEditorSkin.this.fontFamilyComboBox.getValue(), Double.valueOf(string2)));
                        }
                    }
                };
                return listCell;
            }
        });
        this.fontSizeComboBox.valueProperty().addListener((observableValue, string, string2) -> {
            String string3 = this.getCommandValue(Command.FONT_SIZE.getCommand());
            if (!string2.equals(string3)) {
                this.executeCommand(Command.FONT_SIZE.getCommand(), this.fontSizeMap.get(string2));
            }
        });
        this.toolbar2.getItems().add(new Separator(Orientation.VERTICAL));
        this.boldButton = this.addToggleButton(this.toolbar2, null, this.resources.getString("boldIcon"), this.resources.getString("bold"), Command.BOLD.getCommand(), "html-editor-bold");
        this.boldButton.setOnAction(actionEvent -> {
            if (FORMAT_PARAGRAPH.equals(this.formatStyleMap.get(this.formatComboBox.getValue()))) {
                this.executeCommand(Command.BOLD.getCommand(), this.boldButton.selectedProperty().getValue().toString());
            }
        });
        this.italicButton = this.addToggleButton(this.toolbar2, null, this.resources.getString("italicIcon"), this.resources.getString("italic"), Command.ITALIC.getCommand(), "html-editor-italic");
        this.underlineButton = this.addToggleButton(this.toolbar2, null, this.resources.getString("underlineIcon"), this.resources.getString("underline"), Command.UNDERLINE.getCommand(), "html-editor-underline");
        this.strikethroughButton = this.addToggleButton(this.toolbar2, null, this.resources.getString("strikethroughIcon"), this.resources.getString("strikethrough"), Command.STRIKETHROUGH.getCommand(), "html-editor-strike");
        this.toolbar2.getItems().add(new Separator(Orientation.VERTICAL));
        this.insertHorizontalRuleButton = this.addButton(this.toolbar2, this.resources.getString("insertHorizontalRuleIcon"), this.resources.getString("insertHorizontalRule"), Command.INSERT_HORIZONTAL_RULE.getCommand(), "html-editor-hr");
        this.insertHorizontalRuleButton.setOnAction(actionEvent -> {
            this.executeCommand(Command.INSERT_NEW_LINE.getCommand(), null);
            this.executeCommand(Command.INSERT_HORIZONTAL_RULE.getCommand(), null);
            this.updateToolbarState(false);
        });
        this.fgColorButton = new ColorPicker();
        this.fgColorButton.getStyleClass().add("html-editor-foreground");
        this.fgColorButton.setFocusTraversable(false);
        this.toolbar1.getItems().add(this.fgColorButton);
        this.fgColorButton.setValue(DEFAULT_FG_COLOR);
        this.fgColorButton.setTooltip(new Tooltip(this.resources.getString("foregroundColor")));
        this.fgColorButton.setOnAction(actionEvent -> {
            Color color = (Color)this.fgColorButton.getValue();
            if (color != null) {
                this.executeCommand(Command.FOREGROUND_COLOR.getCommand(), this.colorValueToRGBA(color));
                this.fgColorButton.hide();
            }
        });
        this.bgColorButton = new ColorPicker();
        this.bgColorButton.getStyleClass().add("html-editor-background");
        this.bgColorButton.setFocusTraversable(false);
        this.toolbar1.getItems().add(this.bgColorButton);
        this.bgColorButton.setValue(DEFAULT_BG_COLOR);
        this.bgColorButton.setTooltip(new Tooltip(this.resources.getString("backgroundColor")));
        this.bgColorButton.setOnAction(actionEvent -> {
            Color color = (Color)this.bgColorButton.getValue();
            if (color != null) {
                this.executeCommand(Command.BACKGROUND_COLOR.getCommand(), this.colorValueToRGBA(color));
                this.bgColorButton.hide();
            }
        });
    }

    private String colorValueToRGBA(Color color) {
        return String.format((Locale)null, "rgba(%d, %d, %d, %.5f)", Math.round(color.getRed() * 255.0), Math.round(color.getGreen() * 255.0), Math.round(color.getBlue() * 255.0), color.getOpacity());
    }

    private Button addButton(ToolBar toolBar, String string, String string2, String string3, String string4) {
        Button button = new Button();
        button.setFocusTraversable(false);
        button.getStyleClass().add(string4);
        toolBar.getItems().add(button);
        Image image = AccessController.doPrivileged(() -> new Image(HTMLEditorSkin.class.getResource(string).toString()));
        ((StyleableProperty)((Object)button.graphicProperty())).applyStyle(null, new ImageView(image));
        button.setTooltip(new Tooltip(string2));
        button.setOnAction(actionEvent -> {
            this.executeCommand(string3, null);
            this.updateToolbarState(false);
        });
        return button;
    }

    private ToggleButton addToggleButton(ToolBar toolBar, ToggleGroup toggleGroup, String string, String string2, String string3, String string4) {
        ToggleButton toggleButton = new ToggleButton();
        toggleButton.setUserData(string3);
        toggleButton.setFocusTraversable(false);
        toggleButton.getStyleClass().add(string4);
        toolBar.getItems().add(toggleButton);
        if (toggleGroup != null) {
            toggleButton.setToggleGroup(toggleGroup);
        }
        Image image = AccessController.doPrivileged(() -> new Image(HTMLEditorSkin.class.getResource(string).toString()));
        ((StyleableProperty)((Object)toggleButton.graphicProperty())).applyStyle(null, new ImageView(image));
        toggleButton.setTooltip(new Tooltip(string2));
        if (!Command.BOLD.getCommand().equals(string3)) {
            toggleButton.selectedProperty().addListener((observableValue, bl, bl2) -> {
                if (this.getCommandState(string3) != bl2.booleanValue()) {
                    this.executeCommand(string3, null);
                }
            });
        }
        return toggleButton;
    }

    private void createFormatMenuItem(String string, String string2) {
        this.formatComboBox.getItems().add(string2);
        this.formatStyleMap.put(string2, string);
        this.styleFormatMap.put(string, string2);
    }

    private void createFontSizeMenuItem(String string, String string2) {
        this.fontSizeComboBox.getItems().add(string2);
        this.fontSizeMap.put(string2, string);
        this.sizeFontMap.put(string, string2);
    }

    private void updateNodeOrientation() {
        NodeOrientation nodeOrientation = ((HTMLEditor)this.getSkinnable()).getEffectiveNodeOrientation();
        HTMLDocument hTMLDocument = (HTMLDocument)this.webPage.getDocument(this.webPage.getMainFrame());
        HTMLElement hTMLElement = (HTMLElement)hTMLDocument.getDocumentElement();
        if (hTMLElement.getAttribute("dir") == null) {
            hTMLElement.setAttribute("dir", nodeOrientation == NodeOrientation.RIGHT_TO_LEFT ? "rtl" : "ltr");
        }
    }

    private void updateToolbarState(boolean bl) {
        Object object;
        String string;
        Object object2;
        Object object3;
        String string2;
        if (!this.webView.isFocused()) {
            return;
        }
        ++this.atomicityCount;
        this.copyButton.setDisable(!this.isCommandEnabled(Command.CUT.getCommand()));
        this.cutButton.setDisable(!this.isCommandEnabled(Command.COPY.getCommand()));
        this.pasteButton.setDisable(!this.isCommandEnabled(Command.PASTE.getCommand()));
        this.insertHorizontalRuleButton.setDisable(!this.isCommandEnabled(Command.INSERT_HORIZONTAL_RULE.getCommand()));
        if (bl) {
            this.alignLeftButton.setDisable(!this.isCommandEnabled(Command.ALIGN_LEFT.getCommand()));
            this.alignLeftButton.setSelected(this.getCommandState(Command.ALIGN_LEFT.getCommand()));
            this.alignCenterButton.setDisable(!this.isCommandEnabled(Command.ALIGN_CENTER.getCommand()));
            this.alignCenterButton.setSelected(this.getCommandState(Command.ALIGN_CENTER.getCommand()));
            this.alignRightButton.setDisable(!this.isCommandEnabled(Command.ALIGN_RIGHT.getCommand()));
            this.alignRightButton.setSelected(this.getCommandState(Command.ALIGN_RIGHT.getCommand()));
            this.alignJustifyButton.setDisable(!this.isCommandEnabled(Command.ALIGN_JUSTIFY.getCommand()));
            this.alignJustifyButton.setSelected(this.getCommandState(Command.ALIGN_JUSTIFY.getCommand()));
        } else if (this.alignmentToggleGroup.getSelectedToggle() != null && this.isCommandEnabled(string2 = this.alignmentToggleGroup.getSelectedToggle().getUserData().toString()) && !this.getCommandState(string2)) {
            this.executeCommand(string2, null);
        }
        if (this.alignmentToggleGroup.getSelectedToggle() == null && this.webPage.getClientSelectedText().isEmpty()) {
            this.alignmentToggleGroup.selectToggle(this.alignLeftButton);
        }
        this.bulletsButton.setDisable(!this.isCommandEnabled(Command.BULLETS.getCommand()));
        this.bulletsButton.setSelected(this.getCommandState(Command.BULLETS.getCommand()));
        this.numbersButton.setDisable(!this.isCommandEnabled(Command.NUMBERS.getCommand()));
        this.numbersButton.setSelected(this.getCommandState(Command.NUMBERS.getCommand()));
        this.indentButton.setDisable(!this.isCommandEnabled(Command.INDENT.getCommand()));
        this.outdentButton.setDisable(!this.isCommandEnabled(Command.OUTDENT.getCommand()));
        this.formatComboBox.setDisable(!this.isCommandEnabled(Command.FORMAT.getCommand()));
        string2 = this.getCommandValue(Command.FORMAT.getCommand());
        if (string2 != null) {
            object3 = "<" + string2 + ">";
            object2 = this.styleFormatMap.get(object3);
            string = (String)this.formatComboBox.getValue();
            if (this.resetToolbarState || ((String)object3).equals("<>") || ((String)object3).equalsIgnoreCase("<div>") || ((String)object3).equalsIgnoreCase("<blockquote>")) {
                this.formatComboBox.setValue(this.resources.getString("paragraph"));
            } else if (string != null && !string.equalsIgnoreCase((String)object2)) {
                this.formatComboBox.setValue((String)object2);
            }
        }
        this.fontFamilyComboBox.setDisable(!this.isCommandEnabled(Command.FONT_FAMILY.getCommand()));
        object3 = this.getCommandValue(Command.FONT_FAMILY.getCommand());
        if (object3 != null) {
            object2 = object3;
            if (((String)object2).startsWith("'")) {
                object2 = ((String)object2).substring(1);
            }
            if (((String)object2).endsWith("'")) {
                object2 = ((String)object2).substring(0, ((String)object2).length() - 1);
            }
            if ((string = this.fontFamilyComboBox.getValue()) instanceof String && !string.equals(object2)) {
                object = this.fontFamilyComboBox.getItems();
                String string3 = null;
                Iterator iterator = object.iterator();
                while (iterator.hasNext()) {
                    String string4 = (String)iterator.next();
                    if (string4.equals(object2)) {
                        string3 = string4;
                        break;
                    }
                    if (!string4.equals("") || !((String)object2).equals("Dialog")) continue;
                    string3 = string4;
                    break;
                }
                if (string3 != null) {
                    this.fontFamilyComboBox.setValue(string3);
                }
            }
        }
        this.fontSizeComboBox.setDisable(!this.isCommandEnabled(Command.FONT_SIZE.getCommand()));
        object2 = this.getCommandValue(Command.FONT_SIZE.getCommand());
        if (this.resetToolbarState && object2 == null) {
            this.fontSizeComboBox.setValue(this.sizeFontMap.get(SIZE_SMALL));
        } else if (object2 != null) {
            if (!((String)this.fontSizeComboBox.getValue()).equals(this.sizeFontMap.get(object2))) {
                this.fontSizeComboBox.setValue(this.sizeFontMap.get(object2));
            }
        } else if (this.fontSizeComboBox.getValue() == null || !((String)this.fontSizeComboBox.getValue()).equals(this.sizeFontMap.get(SIZE_SMALL))) {
            this.fontSizeComboBox.setValue(this.sizeFontMap.get(SIZE_SMALL));
        }
        this.boldButton.setDisable(!this.isCommandEnabled(Command.BOLD.getCommand()));
        this.boldButton.setSelected(this.getCommandState(Command.BOLD.getCommand()));
        this.italicButton.setDisable(!this.isCommandEnabled(Command.ITALIC.getCommand()));
        this.italicButton.setSelected(this.getCommandState(Command.ITALIC.getCommand()));
        this.underlineButton.setDisable(!this.isCommandEnabled(Command.UNDERLINE.getCommand()));
        this.underlineButton.setSelected(this.getCommandState(Command.UNDERLINE.getCommand()));
        this.strikethroughButton.setDisable(!this.isCommandEnabled(Command.STRIKETHROUGH.getCommand()));
        this.strikethroughButton.setSelected(this.getCommandState(Command.STRIKETHROUGH.getCommand()));
        this.fgColorButton.setDisable(!this.isCommandEnabled(Command.FOREGROUND_COLOR.getCommand()));
        string = this.getCommandValue(Command.FOREGROUND_COLOR.getCommand());
        if (string != null) {
            this.fgColorButton.setValue(this.getColor(string));
        }
        this.bgColorButton.setDisable(!this.isCommandEnabled(Command.BACKGROUND_COLOR.getCommand()));
        object = this.getCommandValue(Command.BACKGROUND_COLOR.getCommand());
        if (object != null) {
            this.bgColorButton.setValue(this.getColor((String)object));
        }
        this.atomicityCount = this.atomicityCount == 0 ? 0 : (this.atomicityCount = this.atomicityCount - 1);
    }

    private void enableToolbar(boolean bl) {
        Platform.runLater(() -> {
            if (this.copyButton == null) {
                return;
            }
            if (bl) {
                this.copyButton.setDisable(!this.isCommandEnabled(Command.COPY.getCommand()));
                this.cutButton.setDisable(!this.isCommandEnabled(Command.CUT.getCommand()));
                this.pasteButton.setDisable(!this.isCommandEnabled(Command.PASTE.getCommand()));
            } else {
                this.copyButton.setDisable(true);
                this.cutButton.setDisable(true);
                this.pasteButton.setDisable(true);
            }
            this.insertHorizontalRuleButton.setDisable(!bl);
            this.alignLeftButton.setDisable(!bl);
            this.alignCenterButton.setDisable(!bl);
            this.alignRightButton.setDisable(!bl);
            this.alignJustifyButton.setDisable(!bl);
            this.bulletsButton.setDisable(!bl);
            this.numbersButton.setDisable(!bl);
            this.indentButton.setDisable(!bl);
            this.outdentButton.setDisable(!bl);
            this.formatComboBox.setDisable(!bl);
            this.fontFamilyComboBox.setDisable(!bl);
            this.fontSizeComboBox.setDisable(!bl);
            this.boldButton.setDisable(!bl);
            this.italicButton.setDisable(!bl);
            this.underlineButton.setDisable(!bl);
            this.strikethroughButton.setDisable(!bl);
            this.fgColorButton.setDisable(!bl);
            this.bgColorButton.setDisable(!bl);
        });
    }

    private boolean executeCommand(String string, String string2) {
        if (!this.enableAtomicityCheck || this.enableAtomicityCheck && this.atomicityCount == 0) {
            return this.webPage.executeCommand(string, string2);
        }
        return false;
    }

    private boolean isCommandEnabled(String string) {
        return this.webPage.queryCommandEnabled(string);
    }

    private void setContentEditable(boolean bl) {
        HTMLDocument hTMLDocument = (HTMLDocument)this.webPage.getDocument(this.webPage.getMainFrame());
        HTMLElement hTMLElement = (HTMLElement)hTMLDocument.getDocumentElement();
        HTMLElement hTMLElement2 = (HTMLElement)hTMLElement.getElementsByTagName("body").item(0);
        hTMLElement2.setAttribute("contenteditable", Boolean.toString(bl));
    }

    private boolean getCommandState(String string) {
        return this.webPage.queryCommandState(string);
    }

    private String getCommandValue(String string) {
        return this.webPage.queryCommandValue(string);
    }

    private Color getColor(String string) {
        Color color = Color.web(string);
        if (color.equals(Color.TRANSPARENT)) {
            color = Color.WHITE;
        }
        return color;
    }

    private void applyTextFormatting() {
        if (this.getCommandState(Command.BULLETS.getCommand()) || this.getCommandState(Command.NUMBERS.getCommand())) {
            return;
        }
        if (this.webPage.getClientCommittedTextLength() == 0) {
            String string = this.formatStyleMap.get(this.formatComboBox.getValue());
            String string2 = ((String)this.fontFamilyComboBox.getValue()).toString();
            this.executeCommand(Command.FORMAT.getCommand(), string);
            this.executeCommand(Command.FONT_FAMILY.getCommand(), string2);
        }
    }

    void print(PrinterJob printerJob) {
        this.webView.getEngine().print(printerJob);
    }

    public static enum Command {
        CUT("cut"),
        COPY("copy"),
        PASTE("paste"),
        UNDO("undo"),
        REDO("redo"),
        INSERT_HORIZONTAL_RULE("inserthorizontalrule"),
        ALIGN_LEFT("justifyleft"),
        ALIGN_CENTER("justifycenter"),
        ALIGN_RIGHT("justifyright"),
        ALIGN_JUSTIFY("justifyfull"),
        BULLETS("insertUnorderedList"),
        NUMBERS("insertOrderedList"),
        INDENT("indent"),
        OUTDENT("outdent"),
        FORMAT("formatblock"),
        FONT_FAMILY("fontname"),
        FONT_SIZE("fontsize"),
        BOLD("bold"),
        ITALIC("italic"),
        UNDERLINE("underline"),
        STRIKETHROUGH("strikethrough"),
        FOREGROUND_COLOR("forecolor"),
        BACKGROUND_COLOR("backcolor"),
        STYLEWITHCSS("styleWithCSS"),
        INSERT_NEW_LINE("insertnewline"),
        INSERT_TAB("inserttab");

        private final String command;

        private Command(String string2) {
            this.command = string2;
        }

        public String getCommand() {
            return this.command;
        }
    }
}

