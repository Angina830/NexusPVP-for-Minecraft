package com.nexuspvp.modules;

import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.BooleanSetting;
import com.nexuspvp.setting.NumberSetting;
import com.nexuspvp.util.Compat;
import net.minecraft.util.Hand;

public class ViewModel extends Module {

    private final NumberSetting translateX = new NumberSetting("TranslateX", 0.0, -2.0, 2.0, 0.05);
    private final NumberSetting translateY = new NumberSetting("TranslateY", 0.0, -2.0, 2.0, 0.05);
    private final NumberSetting translateZ = new NumberSetting("TranslateZ", 0.0, -2.0, 2.0, 0.05);
    
    private final NumberSetting rotateX = new NumberSetting("RotateX", 0.0, -180.0, 180.0, 1.0);
    private final NumberSetting rotateY = new NumberSetting("RotateY", 0.0, -180.0, 180.0, 1.0);
    private final NumberSetting rotateZ = new NumberSetting("RotateZ", 0.0, -180.0, 180.0, 1.0);
    
    private final NumberSetting scaleX = new NumberSetting("ScaleX", 1.0, 0.1, 2.0, 0.05);
    private final NumberSetting scaleY = new NumberSetting("ScaleY", 1.0, 0.1, 2.0, 0.05);
    private final NumberSetting scaleZ = new NumberSetting("ScaleZ", 1.0, 0.1, 2.0, 0.05);
    
    private final BooleanSetting onlyMainHand = new BooleanSetting("OnlyMainHand", false);

    public ViewModel() {
        super("ViewModel", "Customize first-person hand and item display", Category.PLAYER);
        addSetting(translateX);
        addSetting(translateY);
        addSetting(translateZ);
        addSetting(rotateX);
        addSetting(rotateY);
        addSetting(rotateZ);
        addSetting(scaleX);
        addSetting(scaleY);
        addSetting(scaleZ);
        addSetting(onlyMainHand);
    }

    public float getTranslateX() { return translateX.getFloatValue(); }
    public float getTranslateY() { return translateY.getFloatValue(); }
    public float getTranslateZ() { return translateZ.getFloatValue(); }

    public float getRotateX() { return rotateX.getFloatValue(); }
    public float getRotateY() { return rotateY.getFloatValue(); }
    public float getRotateZ() { return rotateZ.getFloatValue(); }

    public float getScaleX() { return scaleX.getFloatValue(); }
    public float getScaleY() { return scaleY.getFloatValue(); }
    public float getScaleZ() { return scaleZ.getFloatValue(); }

    public boolean isOnlyMainHand() { return onlyMainHand.isEnabled(); }

    public void setTranslateX(double val) { translateX.setValue(Math.max(-2.0, Math.min(2.0, val))); }
    public void setTranslateY(double val) { translateY.setValue(Math.max(-2.0, Math.min(2.0, val))); }
    public void setTranslateZ(double val) { translateZ.setValue(Math.max(-2.0, Math.min(2.0, val))); }

    public void setRotateX(double val) { rotateX.setValue(Math.max(-180.0, Math.min(180.0, val))); }
    public void setRotateY(double val) { rotateY.setValue(Math.max(-180.0, Math.min(180.0, val))); }
    public void setRotateZ(double val) { rotateZ.setValue(Math.max(-180.0, Math.min(180.0, val))); }

    public void setScaleX(double val) { scaleX.setValue(Math.max(0.1, Math.min(2.0, val))); }
    public void setScaleY(double val) { scaleY.setValue(Math.max(0.1, Math.min(2.0, val))); }
    public void setScaleZ(double val) { scaleZ.setValue(Math.max(0.1, Math.min(2.0, val))); }

    public void resetAll() {
        setTranslateX(0.0);
        setTranslateY(0.0);
        setTranslateZ(0.0);
        setRotateX(0.0);
        setRotateY(0.0);
        setRotateZ(0.0);
        setScaleX(1.0);
        setScaleY(1.0);
        setScaleZ(1.0);
    }
}
