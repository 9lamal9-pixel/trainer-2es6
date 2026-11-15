/*
 * Decompiled with CFR 0.152.
 */
package javafx.embed.swing;

import com.sun.javafx.collections.ObservableListWrapper;
import com.sun.javafx.scene.input.ExtendedInputMethodRequests;
import java.awt.Rectangle;
import java.awt.event.InputMethodEvent;
import java.awt.font.TextHitInfo;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.ArrayList;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.input.InputMethodHighlight;
import javafx.scene.input.InputMethodRequests;
import javafx.scene.input.InputMethodTextRun;

class InputMethodSupport {
    InputMethodSupport() {
    }

    public static ObservableList<InputMethodTextRun> inputMethodEventComposed(String string, int n) {
        ArrayList<InputMethodTextRun> arrayList = new ArrayList<InputMethodTextRun>();
        if (n < string.length()) {
            arrayList.add(new InputMethodTextRun(string.substring(n), InputMethodHighlight.UNSELECTED_RAW));
        }
        return new ObservableListWrapper<InputMethodTextRun>(arrayList);
    }

    public static String getTextForEvent(InputMethodEvent inputMethodEvent) {
        AttributedCharacterIterator attributedCharacterIterator = inputMethodEvent.getText();
        if (inputMethodEvent.getText() != null) {
            char c = attributedCharacterIterator.first();
            StringBuilder stringBuilder = new StringBuilder();
            while (c != '\uffff') {
                stringBuilder.append(c);
                c = attributedCharacterIterator.next();
            }
            return stringBuilder.toString();
        }
        return "";
    }

    public static class InputMethodRequestsAdapter
    implements java.awt.im.InputMethodRequests {
        private final InputMethodRequests fxRequests;

        public InputMethodRequestsAdapter(InputMethodRequests inputMethodRequests) {
            this.fxRequests = inputMethodRequests;
        }

        @Override
        public Rectangle getTextLocation(TextHitInfo textHitInfo) {
            Point2D point2D = this.fxRequests.getTextLocation(textHitInfo.getInsertionIndex());
            return new Rectangle((int)point2D.getX(), (int)point2D.getY(), 0, 0);
        }

        @Override
        public TextHitInfo getLocationOffset(int n, int n2) {
            int n3 = this.fxRequests.getLocationOffset(n, n2);
            return TextHitInfo.afterOffset(n3);
        }

        @Override
        public int getInsertPositionOffset() {
            if (this.fxRequests instanceof ExtendedInputMethodRequests) {
                return ((ExtendedInputMethodRequests)this.fxRequests).getInsertPositionOffset();
            }
            return 0;
        }

        @Override
        public AttributedCharacterIterator getCommittedText(int n, int n2, AttributedCharacterIterator.Attribute[] attributeArray) {
            String string = null;
            if (this.fxRequests instanceof ExtendedInputMethodRequests) {
                string = ((ExtendedInputMethodRequests)this.fxRequests).getCommittedText(n, n2);
            }
            if (string == null) {
                string = "";
            }
            return new AttributedString(string).getIterator();
        }

        @Override
        public int getCommittedTextLength() {
            if (this.fxRequests instanceof ExtendedInputMethodRequests) {
                return ((ExtendedInputMethodRequests)this.fxRequests).getCommittedTextLength();
            }
            return 0;
        }

        @Override
        public AttributedCharacterIterator cancelLatestCommittedText(AttributedCharacterIterator.Attribute[] attributeArray) {
            return null;
        }

        @Override
        public AttributedCharacterIterator getSelectedText(AttributedCharacterIterator.Attribute[] attributeArray) {
            String string = this.fxRequests.getSelectedText();
            if (string == null) {
                string = "";
            }
            return new AttributedString(string).getIterator();
        }
    }
}

