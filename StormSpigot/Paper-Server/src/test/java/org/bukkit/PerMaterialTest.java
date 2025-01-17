package org.bukkit;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import net.minecraft.server.Block;
import net.minecraft.server.BlockAccessAir;
import net.minecraft.server.BlockFalling;
import net.minecraft.server.BlockFire;
import net.minecraft.server.BlockPosition;
import net.minecraft.server.Blocks;
import net.minecraft.server.EntityHuman;
import net.minecraft.server.EnumHand;
import net.minecraft.server.IBlockData;
import net.minecraft.server.Item;
import net.minecraft.server.ItemRecord;
import net.minecraft.server.MovingObjectPositionBlock;
import net.minecraft.server.TileEntityFurnace;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;
import org.bukkit.support.AbstractTestingBase;
import org.bukkit.support.Util;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class PerMaterialTest extends AbstractTestingBase {
    private static Map<Block, Integer> fireValues;

    @BeforeClass
    public static void getFireValues() {
        fireValues = Util.getInternalState(BlockFire.class, Blocks.FIRE, "flameChances");
    }

    @Parameters(name = "{index}: {0}")
    public static List<Object[]> data() {
        List<Object[]> list = Lists.newArrayList();
        for (Material material : Material.values()) {
            if (!material.isLegacy()) {
                list.add(new Object[] {material});
            }
        }
        return list;
    }

    @Parameter public Material material;

    @Test
    public void isBlock() {
        if (material != Material.AIR && material != Material.CAVE_AIR && material != Material.VOID_AIR) {
            assertThat(material.isBlock(), is(not(CraftMagicNumbers.getBlock(material) == null)));
        }
    }

    @Test
    public void isSolid() {
        if (material == Material.AIR) {
            assertFalse(material.isSolid());
        } else if (material.isBlock()) {
            assertThat(material.isSolid(), is(CraftMagicNumbers.getBlock(material).getBlockData().getMaterial().isSolid()));
        } else {
            assertFalse(material.isSolid());
        }
    }

    @Test
    public void isEdible() {
        if (material.isBlock()) {
            assertFalse(material.isEdible());
        } else {
            assertThat(material.isEdible(), is(CraftMagicNumbers.getItem(material).isFood()));
        }
    }

    @Test
    public void isRecord() {
        assertThat(material.isRecord(), is(CraftMagicNumbers.getItem(material) instanceof ItemRecord));
    }

    @Test
    public void maxDurability() {
        if (INVALIDATED_MATERIALS.contains(material)) return;

        if (material == Material.AIR) {
            assertThat((int) material.getMaxDurability(), is(0));
        } else if (material.isBlock()) {
            Item item = CraftMagicNumbers.getItem(material);
            assertThat((int) material.getMaxDurability(), is(item.getMaxDurability()));
        }
    }

    @Test
    public void maxStackSize() {
        if (INVALIDATED_MATERIALS.contains(material)) return;

        final ItemStack bukkit = new ItemStack(material);
        final CraftItemStack craft = CraftItemStack.asCraftCopy(bukkit);
        if (material == Material.AIR) {
            final int MAX_AIR_STACK = 0 /* Why can't I hold all of these AIR? */;
            assertThat(material.getMaxStackSize(), is(MAX_AIR_STACK));
            assertThat(bukkit.getMaxStackSize(), is(MAX_AIR_STACK));
            assertThat(craft.getMaxStackSize(), is(MAX_AIR_STACK));
        } else {
            assertThat(material.getMaxStackSize(), is(CraftMagicNumbers.getItem(material).getMaxStackSize()));
            assertThat(bukkit.getMaxStackSize(), is(material.getMaxStackSize()));
            assertThat(craft.getMaxStackSize(), is(material.getMaxStackSize()));
        }
    }

    @Test
    public void isTransparent() {
        if (material == Material.AIR) {
            assertTrue(material.isTransparent());
        } else if (material.isBlock()) {
            // assertThat(material.isTransparent(), is(not(CraftMagicNumbers.getBlock(material).getBlockData().getMaterial().blocksLight()))); // PAIL: not unit testable anymore (17w50a)
        } else {
            assertFalse(material.isTransparent());
        }
    }

    @Test
    public void isFlammable() {
        if (material != Material.AIR && material.isBlock()) {
            assertThat(material.isFlammable(), is(CraftMagicNumbers.getBlock(material).getBlockData().getMaterial().isBurnable()));
        } else {
            assertFalse(material.isFlammable());
        }
    }

    @Test
    public void isBurnable() {
        if (material.isBlock()) {
            Block block = CraftMagicNumbers.getBlock(material);
            assertThat(material.isBurnable(), is(fireValues.containsKey(block) && fireValues.get(block) > 0));
        } else {
            assertFalse(material.isBurnable());
        }
    }

    @Test
    public void isFuel() {
        assertThat(material.isFuel(), is(TileEntityFurnace.isFuel(new net.minecraft.server.ItemStack(CraftMagicNumbers.getItem(material)))));
    }

    @Test
    public void isOccluding() {
        if (material.isBlock()) {
            assertThat(material.isOccluding(), is(CraftMagicNumbers.getBlock(material).isOccluding(CraftMagicNumbers.getBlock(material).getBlockData(), BlockAccessAir.INSTANCE, BlockPosition.ZERO)));
        } else {
            assertFalse(material.isOccluding());
        }
    }

    @Test
    public void hasGravity() {
        if (material.isBlock()) {
            assertThat(material.hasGravity(), is(CraftMagicNumbers.getBlock(material) instanceof BlockFalling));
        } else {
            assertFalse(material.hasGravity());
        }
    }

    @Test
    public void usesDurability() {
        if (!material.isBlock()) {
            assertThat(EnchantmentTarget.BREAKABLE.includes(material), is(CraftMagicNumbers.getItem(material).usesDurability()));
        } else {
            assertFalse(EnchantmentTarget.BREAKABLE.includes(material));
        }
    }

    @Test
    public void testDurability() {
        if (!material.isBlock()) {
            assertThat(material.getMaxDurability(), is((short) CraftMagicNumbers.getItem(material).getMaxDurability()));
        } else {
            assertThat(material.getMaxDurability(), is((short) 0));
        }
    }

    @Test
    public void testBlock() {
        if (material == Material.AIR) {
            assertTrue(material.isBlock());
        } else {
            assertThat(material.isBlock(), is(equalTo(CraftMagicNumbers.getBlock(material) != null)));
        }
    }

    @Test
    public void testAir() {
        if (material.isBlock()) {
            assertThat(material.isAir(), is(equalTo(CraftMagicNumbers.getBlock(material).getBlockData().isAir())));
        } else {
            assertThat(material.isAir(), is(equalTo(false)));
        }
    }

    @Test
    public void testItem() {
        if (material == Material.AIR) {
            assertTrue(material.isItem());
        } else {
            assertThat(material.isItem(), is(equalTo(CraftMagicNumbers.getItem(material) != null)));
        }
    }

    @Test
    public void testInteractable() throws ReflectiveOperationException {
        if (material.isBlock()) {
            assertThat(material.isInteractable(),
                    is(!CraftMagicNumbers.getBlock(material).getClass()
                            .getMethod("interact", IBlockData.class, net.minecraft.server.World.class, BlockPosition.class, EntityHuman.class, EnumHand.class, MovingObjectPositionBlock.class)
                            .getDeclaringClass().equals(Block.class)));
        } else {
            assertFalse(material.isInteractable());
        }
    }

    @Test
    public void testBlockHardness() {
        if (material.isBlock()) {
            assertThat(material.getHardness(), is(CraftMagicNumbers.getBlock(material).strength));
        }
    }

    @Test
    public void testBlastResistance() {
        if (material.isBlock()) {
            assertThat(material.getBlastResistance(), is(CraftMagicNumbers.getBlock(material).getDurability()));
        }
    }

    @Test
    public void testBlockDataCreation() {
        if (material.isBlock()) {
            assertNotNull(material.createBlockData());
        }
    }

    @Test
    public void testCraftingRemainingItem() {
        if (material.isItem()) {
            Item expectedItem = CraftMagicNumbers.getItem(material).p(); // PAIL rename p() -> getCraftingRemainingItem()
            Material expected = expectedItem == null ? null : CraftMagicNumbers.getMaterial(expectedItem);

            assertThat(material.getCraftingRemainingItem(), is(expected));
        }
    }
}
