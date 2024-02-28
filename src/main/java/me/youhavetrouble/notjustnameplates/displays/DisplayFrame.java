package me.youhavetrouble.notjustnameplates.displays;

import org.bukkit.Color;
import org.joml.Vector3f;

public record DisplayFrame(
        String text,
        Color backgroundColor,
        Vector3f scale,
        Vector3f offset,
        boolean shadowed,
        byte textOpacity
) {
}
