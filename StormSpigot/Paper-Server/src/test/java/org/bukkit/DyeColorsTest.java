package org.bukkit;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.EnumColor;
import org.bukkit.support.AbstractTestingBase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class DyeColorsTest extends AbstractTestingBase {

    @Parameters(name = "{index}: {0}")
    public static List<Object[]> data() {
        List<Object[]> list = new ArrayList<Object[]>();
        for (DyeColor dye : DyeColor.values()) {
            list.add(new Object[] {dye});
        }
        return list;
    }

    @Parameter public DyeColor dye;

    @Test
    public void checkColor() {
        Color color = dye.getColor();
        float[] nmsColorArray = EnumColor.fromColorIndex(dye.getWoolData()).d();
        Color nmsColor = Color.fromRGB((int) (nmsColorArray[0] * 255), (int) (nmsColorArray[1] * 255), (int) (nmsColorArray[2] * 255));
        assertThat(color, is(nmsColor));
    }

    @Test
    public void checkFireworkColor() {
        Color color = dye.getFireworkColor();
        int nmsColor = EnumColor.fromColorIndex(dye.getWoolData()).f();
        assertThat(color, is(Color.fromRGB(nmsColor)));
    }
}
