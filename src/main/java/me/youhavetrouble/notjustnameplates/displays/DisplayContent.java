package me.youhavetrouble.notjustnameplates.displays;

import org.bukkit.entity.Display;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DisplayContent {

    private final List<DisplayFrame> frames = new ArrayList<>();
    private int refreshRate, interpolationDuration, interpolationDelay, currentFrame, viewRange;
    private Display.Billboard billboard = Display.Billboard.HORIZONTAL;
    private boolean seeThrough = false;

    public DisplayContent() {
    }

    /**
     * Set the refresh rate of the display in ticks. 0 means no refresh rate.
     */
    public void setRefreshRate(int refreshRate) {
        this.refreshRate = refreshRate;
    }

    public int getRefreshRate() {
        return refreshRate;
    }

    public void setBillboard(@NotNull Display.Billboard billboard) {
        this.billboard = billboard;
    }

    public Display.Billboard getBillboard() {
        return billboard;
    }

    public void setSeeThrough(boolean seeThrough) {
        this.seeThrough = seeThrough;
    }

    public boolean getSeeThrough() {
        return seeThrough;
    }

    public void setInterpolationDelay(int interpolationDelay) {
        this.interpolationDelay = interpolationDelay;
    }

    public void setInterpolationDuration(int interpolationDuration) {
        this.interpolationDuration = interpolationDuration;
    }

    public int getInterpolationDelay() {
        return interpolationDelay;
    }

    public int getInterpolationDuration() {
        return interpolationDuration;
    }

    public void setViewRange(int viewRange) {
        this.viewRange = viewRange;
    }

    public int getViewRange() {
        return viewRange;
    }

    public void addFrame(DisplayFrame frame) {
        frames.add(frame);
    }

    public List<DisplayFrame> getFrames() {
        return Collections.unmodifiableList(frames);
    }

    public DisplayFrame getCurrentFrame() {
        if (frames.isEmpty()) return null;
        return frames.get(currentFrame);
    }

    public void advanceFrame() {
        if (frames.isEmpty()) return;
        currentFrame = currentFrame + 1 >= frames.size() ? 0 : currentFrame + 1;
    }
}
