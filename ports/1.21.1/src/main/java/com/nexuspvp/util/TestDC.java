package com.nexuspvp.util;
import net.minecraft.client.gui.DrawContext;
public class TestDC {
    public static void test(DrawContext ctx) {
        // compiler error will list methods if we make invalid call or reflection
        Class<?> c = DrawContext.class;
        for (java.lang.reflect.Method m : c.getMethods()) {
            if (m.getName().toLowerCase().contains("item") || m.getName().toLowerCase().contains("stack") || m.getName().toLowerCase().contains("overlay")) {
                System.out.println("DC Method: " + m.getName() + " -> " + java.util.Arrays.toString(m.getParameterTypes()));
            }
        }
    }
}
