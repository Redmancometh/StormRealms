package net.minecraft.server;

import javax.annotation.Nullable;

public class EntityCustom extends EntityMonster {

	private static final DataWatcherObject<Byte> b = DataWatcher.a(EntityCustom.class, DataWatcherRegistry.a);

	public EntityCustom(EntityTypes<? extends EntityCustom> entitytypes, World world) {
		super(entitytypes, world);
	}

	@Override
	protected void initPathfinder() {

	}

	@Override
	public double aS() {
		return (double) (this.getHeight() * 0.5F);
	}

	@Override
	protected NavigationAbstract b(World world) {
		return new NavigationSpider(this, world);
	}

	@Override
	protected void initDatawatcher() {
		super.initDatawatcher();
		this.datawatcher.register(EntityCustom.b, (byte) 0);
	}

	@Override
	public int getId() {
		return 52;
	}

	@Override
	public void tick() {
		super.tick();
		if (!this.world.isClientSide) {
			this.r(this.positionChanged);
		}

	}

	@Override
	protected void initAttributes() {
		super.initAttributes();
		this.getAttributeInstance(GenericAttributes.MAX_HEALTH).setValue(16.0D);
		this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.30000001192092896D);
	}

	@Override
	protected SoundEffect getSoundAmbient() {
		return SoundEffects.ENTITY_SPIDER_AMBIENT;
	}

	@Override
	protected SoundEffect getSoundHurt(DamageSource damagesource) {
		return SoundEffects.ENTITY_SPIDER_HURT;
	}

	@Override
	protected SoundEffect getSoundDeath() {
		return SoundEffects.ENTITY_SPIDER_DEATH;
	}

	@Override
	protected void a(BlockPosition blockposition, IBlockData iblockdata) {
		this.a(SoundEffects.ENTITY_SPIDER_STEP, 0.15F, 1.0F);
	}

	@Override
	public boolean isClimbing() {
		return this.l();
	}

	@Override
	public void a(IBlockData iblockdata, Vec3D vec3d) {
		if (iblockdata.getBlock() != Blocks.COBWEB) {
			super.a(iblockdata, vec3d);
		}

	}

	@Override
	public EnumMonsterType getMonsterType() {
		return EnumMonsterType.ARTHROPOD;
	}

	@Override
	public boolean d(MobEffect mobeffect) {
		return mobeffect.getMobEffect() == MobEffects.POISON ? false : super.d(mobeffect);
	}

	public boolean l() {
		return ((Byte) this.datawatcher.get(EntityCustom.b) & 1) != 0;
	}

	public void r(boolean flag) {
		byte b0 = (Byte) this.datawatcher.get(EntityCustom.b);

		if (flag) {
			b0 = (byte) (b0 | 1);
		} else {
			b0 &= -2;
		}

		this.datawatcher.set(EntityCustom.b, b0);
	}

	@Nullable
	@Override
	public GroupDataEntity prepare(GeneratorAccess generatoraccess, DifficultyDamageScaler difficultydamagescaler,
			EnumMobSpawn enummobspawn, @Nullable GroupDataEntity groupdataentity,
			@Nullable NBTTagCompound nbttagcompound) {
		Object object = super.prepare(generatoraccess, difficultydamagescaler, enummobspawn, groupdataentity,
				nbttagcompound);

		if (generatoraccess.getRandom().nextInt(100) == 0) {
			EntitySkeleton entityskeleton = (EntitySkeleton) EntityTypes.SKELETON.a(this.world);

			entityskeleton.setPositionRotation(this.locX(), this.locY(), this.locZ(), this.yaw, 0.0F);
			entityskeleton.prepare(generatoraccess, difficultydamagescaler, enummobspawn, (GroupDataEntity) null,
					(NBTTagCompound) null);
			generatoraccess.addEntity(entityskeleton, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.JOCKEY);
			entityskeleton.startRiding(this);
		}
		return (GroupDataEntity) object;
	}

	@Override
	protected float b(EntityPose entitypose, EntitySize entitysize) {
		return 0.65F;
	}

	/*
	 * this.goalSelector.a(1, new PathfinderGoalFloat(this)); this.goalSelector.a(3,
	 * new PathfinderGoalLeapAtTarget(this, 0.4F)); this.goalSelector.a(5, new
	 * PathfinderGoalRandomStrollLand(this, 0.8D)); this.goalSelector.a(6, new
	 * PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
	 * this.goalSelector.a(6, new PathfinderGoalRandomLookaround(this));
	 * this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this, new Class[0]));
	 * this.targetSelector.a(2, new
	 * EntityCustom.PathfinderGoalSpiderNearestAttackableTarget<>(this,
	 * EntityHuman.class)); this.targetSelector.a(3, new
	 * EntityCustom.PathfinderGoalSpiderNearestAttackableTarget<>(this,
	 * EntityIronGolem.class));
	 */

}
