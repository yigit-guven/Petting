package net.yigitguven.petting.client.gui;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.function.Consumer;

public class PetIntSliderButton extends AbstractSliderButton {
    private String translationKey;
    private final String unit;
    private final int minValue;
    private final int maxValue;
    private final Consumer<Integer> onApply;
    private int lastAppliedValue;

    public PetIntSliderButton(int x, int y, int width, int height, String translationKey, String unit, int minValue, int maxValue, int currentValue, Consumer<Integer> onApply) {
        super(x, y, width, height, Component.empty(), toRatio(currentValue, minValue, maxValue));
        this.translationKey = translationKey;
        this.unit = unit;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.onApply = onApply;
        this.lastAppliedValue = currentValue;
        this.updateMessage();
    }

    private static double toRatio(int val, int min, int max) {
        if (max <= min) return 0.0D;
        return Mth.clamp((double) (val - min) / (double) (max - min), 0.0D, 1.0D);
    }

    public int getIntValue() {
        return (int) Math.round(this.minValue + this.value * (this.maxValue - this.minValue));
    }

    public void setIntValue(int val) {
        this.value = toRatio(val, this.minValue, this.maxValue);
        this.lastAppliedValue = val;
        this.updateMessage();
    }

    public void setTranslationKey(String translationKey) {
        this.translationKey = translationKey;
        this.updateMessage();
    }

    @Override
    protected void updateMessage() {
        int current = getIntValue();
        this.setMessage(Component.translatable(this.translationKey).append(": " + current + " " + this.unit));
    }

    @Override
    protected void applyValue() {
        int current = getIntValue();
        if (current != this.lastAppliedValue) {
            this.lastAppliedValue = current;
            if (this.onApply != null) {
                this.onApply.accept(current);
            }
        }
    }
}
