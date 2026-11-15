/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.control.behavior;

import com.sun.javafx.scene.control.behavior.TextFieldBehavior;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.HitInfo;

public class PasswordFieldBehavior
extends TextFieldBehavior {
    public PasswordFieldBehavior(PasswordField passwordField) {
        super(passwordField);
    }

    @Override
    protected void deletePreviousWord() {
    }

    @Override
    protected void deleteNextWord() {
    }

    @Override
    protected void selectPreviousWord() {
    }

    @Override
    public void selectNextWord() {
    }

    @Override
    protected void previousWord() {
    }

    @Override
    protected void nextWord() {
    }

    @Override
    protected void selectWord() {
        this.selectAll();
    }

    @Override
    protected void mouseDoubleClick(HitInfo hitInfo) {
        ((TextField)this.getNode()).selectAll();
    }
}

