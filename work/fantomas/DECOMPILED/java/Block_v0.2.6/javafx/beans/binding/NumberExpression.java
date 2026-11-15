/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.binding;

import java.util.Locale;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.NumberBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.value.ObservableNumberValue;

public interface NumberExpression
extends ObservableNumberValue {
    public NumberBinding negate();

    public NumberBinding add(ObservableNumberValue var1);

    public NumberBinding add(double var1);

    public NumberBinding add(float var1);

    public NumberBinding add(long var1);

    public NumberBinding add(int var1);

    public NumberBinding subtract(ObservableNumberValue var1);

    public NumberBinding subtract(double var1);

    public NumberBinding subtract(float var1);

    public NumberBinding subtract(long var1);

    public NumberBinding subtract(int var1);

    public NumberBinding multiply(ObservableNumberValue var1);

    public NumberBinding multiply(double var1);

    public NumberBinding multiply(float var1);

    public NumberBinding multiply(long var1);

    public NumberBinding multiply(int var1);

    public NumberBinding divide(ObservableNumberValue var1);

    public NumberBinding divide(double var1);

    public NumberBinding divide(float var1);

    public NumberBinding divide(long var1);

    public NumberBinding divide(int var1);

    public BooleanBinding isEqualTo(ObservableNumberValue var1);

    public BooleanBinding isEqualTo(ObservableNumberValue var1, double var2);

    public BooleanBinding isEqualTo(double var1, double var3);

    public BooleanBinding isEqualTo(float var1, double var2);

    public BooleanBinding isEqualTo(long var1);

    public BooleanBinding isEqualTo(long var1, double var3);

    public BooleanBinding isEqualTo(int var1);

    public BooleanBinding isEqualTo(int var1, double var2);

    public BooleanBinding isNotEqualTo(ObservableNumberValue var1);

    public BooleanBinding isNotEqualTo(ObservableNumberValue var1, double var2);

    public BooleanBinding isNotEqualTo(double var1, double var3);

    public BooleanBinding isNotEqualTo(float var1, double var2);

    public BooleanBinding isNotEqualTo(long var1);

    public BooleanBinding isNotEqualTo(long var1, double var3);

    public BooleanBinding isNotEqualTo(int var1);

    public BooleanBinding isNotEqualTo(int var1, double var2);

    public BooleanBinding greaterThan(ObservableNumberValue var1);

    public BooleanBinding greaterThan(double var1);

    public BooleanBinding greaterThan(float var1);

    public BooleanBinding greaterThan(long var1);

    public BooleanBinding greaterThan(int var1);

    public BooleanBinding lessThan(ObservableNumberValue var1);

    public BooleanBinding lessThan(double var1);

    public BooleanBinding lessThan(float var1);

    public BooleanBinding lessThan(long var1);

    public BooleanBinding lessThan(int var1);

    public BooleanBinding greaterThanOrEqualTo(ObservableNumberValue var1);

    public BooleanBinding greaterThanOrEqualTo(double var1);

    public BooleanBinding greaterThanOrEqualTo(float var1);

    public BooleanBinding greaterThanOrEqualTo(long var1);

    public BooleanBinding greaterThanOrEqualTo(int var1);

    public BooleanBinding lessThanOrEqualTo(ObservableNumberValue var1);

    public BooleanBinding lessThanOrEqualTo(double var1);

    public BooleanBinding lessThanOrEqualTo(float var1);

    public BooleanBinding lessThanOrEqualTo(long var1);

    public BooleanBinding lessThanOrEqualTo(int var1);

    public StringBinding asString();

    public StringBinding asString(String var1);

    public StringBinding asString(Locale var1, String var2);
}

