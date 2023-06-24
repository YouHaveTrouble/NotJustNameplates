package me.youhavetrouble.notjustnameplates.displays;

import org.bukkit.Color;
import org.jetbrains.annotations.Nullable;

public record DisplayFrame(String text, Color backgroundColor) {

    public DisplayFrame(@Nullable String text, @Nullable Color backgroundColor) {
        this.text = text;
        this.backgroundColor = backgroundColor;
    }

}
