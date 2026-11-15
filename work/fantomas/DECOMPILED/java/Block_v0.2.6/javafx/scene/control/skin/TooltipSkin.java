/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control.skin;

import java.util.Collection;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.control.Tooltip;

public class TooltipSkin
implements Skin<Tooltip> {
    private Label tipLabel;
    private Tooltip tooltip;

    public TooltipSkin(Tooltip tooltip) {
        this.tooltip = tooltip;
        this.tipLabel = new Label();
        this.tipLabel.contentDisplayProperty().bind(tooltip.contentDisplayProperty());
        this.tipLabel.fontProperty().bind(tooltip.fontProperty());
        this.tipLabel.graphicProperty().bind(tooltip.graphicProperty());
        this.tipLabel.graphicTextGapProperty().bind(tooltip.graphicTextGapProperty());
        this.tipLabel.textAlignmentProperty().bind(tooltip.textAlignmentProperty());
        this.tipLabel.textOverrunProperty().bind(tooltip.textOverrunProperty());
        this.tipLabel.textProperty().bind(tooltip.textProperty());
        this.tipLabel.wrapTextProperty().bind(tooltip.wrapTextProperty());
        this.tipLabel.minWidthProperty().bind(tooltip.minWidthProperty());
        this.tipLabel.prefWidthProperty().bind(tooltip.prefWidthProperty());
        this.tipLabel.maxWidthProperty().bind(tooltip.maxWidthProperty());
        this.tipLabel.minHeightProperty().bind(tooltip.minHeightProperty());
        this.tipLabel.prefHeightProperty().bind(tooltip.prefHeightProperty());
        this.tipLabel.maxHeightProperty().bind(tooltip.maxHeightProperty());
        this.tipLabel.getStyleClass().setAll((Collection<String>)tooltip.getStyleClass());
        this.tipLabel.setStyle(tooltip.getStyle());
        this.tipLabel.setId(tooltip.getId());
    }

    @Override
    public Tooltip getSkinnable() {
        return this.tooltip;
    }

    @Override
    public Node getNode() {
        return this.tipLabel;
    }

    @Override
    public void dispose() {
        this.tooltip = null;
        this.tipLabel = null;
    }
}

