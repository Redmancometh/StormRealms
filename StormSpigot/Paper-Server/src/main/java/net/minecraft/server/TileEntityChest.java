package net.minecraft.server;

import java.util.Iterator;
import java.util.List;
// CraftBukkit start
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
// CraftBukkit end

public class TileEntityChest extends TileEntityLootable { // Paper - Remove ITickable

    private NonNullList<ItemStack> items;
    protected float a;
    protected float b;
    protected int viewingCount;
    private int j;

    // CraftBukkit start - add fields and methods
    public List<HumanEntity> transaction = new java.util.ArrayList<HumanEntity>();
    private int maxStack = MAX_STACK;

    public List<ItemStack> getContents() {
        return this.items;
    }

    public void onOpen(CraftHumanEntity who) {
        transaction.add(who);
    }

    public void onClose(CraftHumanEntity who) {
        transaction.remove(who);
    }

    public List<HumanEntity> getViewers() {
        return transaction;
    }

    @Override
    public int getMaxStackSize() {
        return maxStack;
    }

    public void setMaxStackSize(int size) {
        maxStack = size;
    }
    // CraftBukkit end

    protected TileEntityChest(TileEntityTypes<?> tileentitytypes) {
        super(tileentitytypes);
        this.items = NonNullList.a(27, ItemStack.a);
    }

    public TileEntityChest() {
        this(TileEntityTypes.CHEST);
    }

    @Override
    public int getSize() {
        return 27;
    }

    @Override
    protected IChatBaseComponent getContainerName() {
        return new ChatMessage("container.chest", new Object[0]);
    }

    @Override
    public void load(NBTTagCompound nbttagcompound) {
        super.load(nbttagcompound);
        this.items = NonNullList.a(this.getSize(), ItemStack.a);
        if (!this.d(nbttagcompound)) {
            ContainerUtil.b(nbttagcompound, this.items);
        }

    }

    @Override
    public NBTTagCompound save(NBTTagCompound nbttagcompound) {
        super.save(nbttagcompound);
        if (!this.e(nbttagcompound)) {
            ContainerUtil.a(nbttagcompound, this.items);
        }

        return nbttagcompound;
    }

    public void tick() {
        int i = this.position.getX();
        int j = this.position.getY();
        int k = this.position.getZ();

        ++this.j;
    }

    public void doOpenLogic() {
        int i = this.position.getX();
        int j = this.position.getY();
        int k = this.position.getZ();

        //this.viewingCount = a(this.world, this, this.j, i, j, k, this.viewingCount); // Paper - check is faulty given our logic is called before active container set
        this.b = this.a;
        float f = 0.1F;

        if (this.viewingCount > 0 && this.a == 0.0F) {
            this.a(SoundEffects.BLOCK_CHEST_OPEN);
        }
    }

    public void doCloseLogic() {
        if (this.viewingCount == 0 /* && this.a > 0.0F || this.viewingCount > 0 && this.a < 1.0F */) { // Paper - disable all but player count check
            /* // Paper - disable animation stuff
            float f1 = this.a;

            if (this.viewingCount > 0) {
                this.a += 0.1F;
            } else {
                this.a -= 0.1F;
            }

            if (this.a > 1.0F) {
                this.a = 1.0F;
            }

            float f2 = 0.5F;

            if (this.a < 0.5F && f1 >= 0.5F) {
             */
            MCUtil.scheduleTask(10, () -> {
                    this.a(SoundEffects.BLOCK_CHEST_CLOSE);
                }, "Chest Sounds");
            //} // Paper end

            if (this.a < 0.0F) {
                this.a = 0.0F;
            }
        }

    }

    public static int a(World world, TileEntityContainer tileentitycontainer, int i, int j, int k, int l, int i1) {
        if (!world.isClientSide && i1 != 0 && (i + j + k + l) % 200 == 0) {
            i1 = a(world, tileentitycontainer, j, k, l);
        }

        return i1;
    }

    public static int a(World world, TileEntityContainer tileentitycontainer, int i, int j, int k) {
        int l = 0;
        float f = 5.0F;
        List<EntityHuman> list = world.a(EntityHuman.class, new AxisAlignedBB((double) ((float) i - 5.0F), (double) ((float) j - 5.0F), (double) ((float) k - 5.0F), (double) ((float) (i + 1) + 5.0F), (double) ((float) (j + 1) + 5.0F), (double) ((float) (k + 1) + 5.0F)));
        Iterator iterator = list.iterator();

        while (iterator.hasNext()) {
            EntityHuman entityhuman = (EntityHuman) iterator.next();

            if (entityhuman.activeContainer instanceof ContainerChest) {
                IInventory iinventory = ((ContainerChest) entityhuman.activeContainer).e();

                if (iinventory == tileentitycontainer || iinventory instanceof InventoryLargeChest && ((InventoryLargeChest) iinventory).a((IInventory) tileentitycontainer)) {
                    ++l;
                }
            }
        }

        return l;
    }

    private void a(SoundEffect soundeffect) {
        if (!this.getBlock().hasProperty(BlockChest.c)) { return; } // Paper - this can be delayed, double check exists - Fixes GH-2074
        BlockPropertyChestType blockpropertychesttype = (BlockPropertyChestType) this.getBlock().get(BlockChest.c);

        if (blockpropertychesttype != BlockPropertyChestType.LEFT) {
            double d0 = (double) this.position.getX() + 0.5D;
            double d1 = (double) this.position.getY() + 0.5D;
            double d2 = (double) this.position.getZ() + 0.5D;

            if (blockpropertychesttype == BlockPropertyChestType.RIGHT) {
                EnumDirection enumdirection = BlockChest.i(this.getBlock());

                d0 += (double) enumdirection.getAdjacentX() * 0.5D;
                d2 += (double) enumdirection.getAdjacentZ() * 0.5D;
            }

            this.world.playSound((EntityHuman) null, d0, d1, d2, soundeffect, SoundCategory.BLOCKS, 0.5F, this.world.random.nextFloat() * 0.1F + 0.9F);
        }
    }

    @Override
    public boolean setProperty(int i, int j) {
        if (i == 1) {
            this.viewingCount = j;
            return true;
        } else {
            return super.setProperty(i, j);
        }
    }

    @Override
    public void startOpen(EntityHuman entityhuman) {
        if (!entityhuman.isSpectator()) {
            if (this.viewingCount < 0) {
                this.viewingCount = 0;
            }
            int oldPower = Math.max(0, Math.min(15, this.viewingCount)); // CraftBukkit - Get power before new viewer is added

            ++this.viewingCount;
            if (this.world == null) return; // CraftBukkit
            doOpenLogic(); // Paper

            // CraftBukkit start - Call redstone event
            if (this.getBlock().getBlock() == Blocks.TRAPPED_CHEST) {
                int newPower = Math.max(0, Math.min(15, this.viewingCount));

                if (oldPower != newPower) {
                    org.bukkit.craftbukkit.event.CraftEventFactory.callRedstoneChange(world, position, oldPower, newPower);
                }
            }
            // CraftBukkit end
            this.onOpen();
        }

    }

    @Override
    public void closeContainer(EntityHuman entityhuman) {
        if (!entityhuman.isSpectator()) {
            int oldPower = Math.max(0, Math.min(15, this.viewingCount)); // CraftBukkit - Get power before new viewer is added
            --this.viewingCount;

            // CraftBukkit start - Call redstone event
            doCloseLogic(); // Paper
            if (this.getBlock().getBlock() == Blocks.TRAPPED_CHEST) {
                int newPower = Math.max(0, Math.min(15, this.viewingCount));

                if (oldPower != newPower) {
                    org.bukkit.craftbukkit.event.CraftEventFactory.callRedstoneChange(world, position, oldPower, newPower);
                }
            }
            // CraftBukkit end
            this.onOpen();
        }

    }

    protected void onOpen() {
        Block block = this.getBlock().getBlock();

        if (block instanceof BlockChest) {
            this.world.playBlockAction(this.position, block, 1, this.viewingCount);
            this.world.applyPhysics(this.position, block);
        }

    }

    @Override
    protected NonNullList<ItemStack> f() {
        return this.items;
    }

    @Override
    protected void a(NonNullList<ItemStack> nonnulllist) {
        this.items = nonnulllist;
    }

    public static int a(IBlockAccess iblockaccess, BlockPosition blockposition) {
        IBlockData iblockdata = iblockaccess.getType(blockposition);

        if (iblockdata.getBlock().isTileEntity()) {
            TileEntity tileentity = iblockaccess.getTileEntity(blockposition);

            if (tileentity instanceof TileEntityChest) {
                return ((TileEntityChest) tileentity).viewingCount;
            }
        }

        return 0;
    }

    public static void a(TileEntityChest tileentitychest, TileEntityChest tileentitychest1) {
        NonNullList<ItemStack> nonnulllist = tileentitychest.f();

        tileentitychest.a(tileentitychest1.f());
        tileentitychest1.a(nonnulllist);
    }

    @Override
    protected Container createContainer(int i, PlayerInventory playerinventory) {
        return ContainerChest.a(i, playerinventory, this);
    }

    // CraftBukkit start
    @Override
    public boolean isFilteredNBT() {
        return false; // Paper
    }
    // CraftBukkit end
}
