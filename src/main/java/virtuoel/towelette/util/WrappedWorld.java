package virtuoel.towelette.util;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityContext;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.map.MapState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.tag.RegistryTagManager;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.Heightmap.Type;
import net.minecraft.world.LightType;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.RayTraceContext;
import net.minecraft.world.TickScheduler;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.Explosion.DestructionType;
import net.minecraft.world.level.LevelGeneratorType;
import net.minecraft.world.level.LevelProperties;

public class WrappedWorld extends World
{
	protected final World delegate;
	
	protected WrappedWorld(World delegate)
	{
		super(delegate.getLevelProperties(), delegate.getDimension().getType(), (w, d) -> null, null, delegate.isClient());
		this.delegate = delegate;
	}
	
	@Override
	public boolean isSkyVisible(BlockPos blockPos_1)
	{
		return delegate.isSkyVisible(blockPos_1);
	}
	
	@Override
	public boolean spawnEntity(Entity entity_1)
	{
		return delegate.spawnEntity(entity_1);
	}
	
	@Override
	public int getLightmapIndex(BlockPos blockPos_1, int int_1)
	{
		return delegate.getLightmapIndex(blockPos_1, int_1);
	}
	
	@Override
	public int getLuminance(BlockPos blockPos_1)
	{
		return delegate.getLuminance(blockPos_1);
	}
	
	@Override
	public int getMaxLightLevel()
	{
		return delegate.getMaxLightLevel();
	}
	
	@Override
	public float getMoonSize()
	{
		return delegate.getMoonSize();
	}
	
	@Override
	public int getHeight()
	{
		return delegate.getHeight();
	}
	
	@Override
	public BlockHitResult rayTrace(RayTraceContext rayTraceContext_1)
	{
		return delegate.rayTrace(rayTraceContext_1);
	}
	
	@Override
	public float getSkyAngle(float float_1)
	{
		return delegate.getSkyAngle(float_1);
	}
	
	@Override
	public boolean isAir(BlockPos blockPos_1)
	{
		return delegate.isAir(blockPos_1);
	}
	
	@Override
	public List<? extends PlayerEntity> getPlayers()
	{
		return delegate.getPlayers();
	}
	
	@Override
	public int getMoonPhase()
	{
		return delegate.getMoonPhase();
	}
	
	@Override
	public List<Entity> getEntities(Entity entity_1, Box box_1)
	{
		return delegate.getEntities(entity_1, box_1);
	}
	
	@Override
	public boolean method_8626(BlockPos blockPos_1)
	{
		return delegate.method_8626(blockPos_1);
	}
	
	@Override
	public TickScheduler<Block> getBlockTickScheduler()
	{
		return delegate.getBlockTickScheduler();
	}
	
	@Override
	public TickScheduler<Fluid> getFluidTickScheduler()
	{
		return delegate.getFluidTickScheduler();
	}
	
	@Override
	public Difficulty getDifficulty()
	{
		return delegate.getDifficulty();
	}
	
	@Override
	public boolean isChunkLoaded(int int_1, int int_2)
	{
		return delegate.isChunkLoaded(int_1, int_2);
	}
	
	@Override
	public <T extends Entity> List<T> getEntities(Class<? extends T> class_1, Box box_1)
	{
		return delegate.getEntities(class_1, box_1);
	}
	
	@Override
	public <T extends Entity> List<T> method_21728(Class<? extends T> class_1, Box box_1)
	{
		return delegate.method_21728(class_1, box_1);
	}
	
	@Override
	public void playLevelEvent(PlayerEntity var1, int var2, BlockPos var3, int var4)
	{
		delegate.playLevelEvent(var1, var2, var3, var4);
	}
	
	@Override
	public void playLevelEvent(int int_1, BlockPos blockPos_1, int int_2)
	{
		delegate.playLevelEvent(int_1, blockPos_1, int_2);
	}
	
	@Override
	public float getBrightness(BlockPos blockPos_1)
	{
		return delegate.getBrightness(blockPos_1);
	}
	
	@Override
	public int hashCode()
	{
		return delegate.hashCode();
	}
	
	@Override
	public Stream<VoxelShape> method_20743(Entity entity_1, Box box_1, Set<Entity> set_1)
	{
		return delegate.method_20743(entity_1, box_1, set_1);
	}
	
	@Override
	public BlockHitResult rayTraceBlock(Vec3d vec3d_1, Vec3d vec3d_2, BlockPos blockPos_1, VoxelShape voxelShape_1, BlockState blockState_1)
	{
		return delegate.rayTraceBlock(vec3d_1, vec3d_2, blockPos_1, voxelShape_1, blockState_1);
	}
	
	@Override
	public int getEmittedStrongRedstonePower(BlockPos blockPos_1, Direction direction_1)
	{
		return delegate.getEmittedStrongRedstonePower(blockPos_1, direction_1);
	}
	
	@Override
	public boolean intersectsEntities(Entity entity_1, VoxelShape voxelShape_1)
	{
		return delegate.intersectsEntities(entity_1, voxelShape_1);
	}
	
	@Override
	public Chunk getChunk(BlockPos blockPos_1)
	{
		return delegate.getChunk(blockPos_1);
	}
	
	@Override
	public Chunk getChunk(int int_1, int int_2)
	{
		return delegate.getChunk(int_1, int_2);
	}
	
	@Override
	public PlayerEntity getClosestPlayer(double double_1, double double_2, double double_3, double double_4, Predicate<Entity> predicate_1)
	{
		return delegate.getClosestPlayer(double_1, double_2, double_3, double_4, predicate_1);
	}
	
	@Override
	public Chunk getChunk(int int_1, int int_2, ChunkStatus chunkStatus_1)
	{
		return delegate.getChunk(int_1, int_2, chunkStatus_1);
	}
	
	@Override
	public boolean canPlace(BlockState blockState_1, BlockPos blockPos_1, EntityContext entityContext_1)
	{
		return delegate.canPlace(blockState_1, blockPos_1, entityContext_1);
	}
	
	@Override
	public boolean intersectsEntities(Entity entity_1)
	{
		return delegate.intersectsEntities(entity_1);
	}
	
	@Override
	public boolean doesNotCollide(Box box_1)
	{
		return delegate.doesNotCollide(box_1);
	}
	
	@Override
	public boolean doesNotCollide(Entity entity_1)
	{
		return delegate.doesNotCollide(entity_1);
	}
	
	@Override
	public PlayerEntity getClosestPlayer(Entity entity_1, double double_1)
	{
		return delegate.getClosestPlayer(entity_1, double_1);
	}
	
	@Override
	public boolean doesNotCollide(Entity entity_1, Box box_1)
	{
		return delegate.doesNotCollide(entity_1, box_1);
	}
	
	@Override
	public PlayerEntity getClosestPlayer(double double_1, double double_2, double double_3, double double_4, boolean boolean_1)
	{
		return delegate.getClosestPlayer(double_1, double_2, double_3, double_4, boolean_1);
	}
	
	@Override
	public boolean doesNotCollide(Entity entity_1, Box box_1, Set<Entity> set_1)
	{
		return delegate.doesNotCollide(entity_1, box_1, set_1);
	}
	
	@Override
	public boolean equals(Object obj)
	{
		return delegate.equals(obj);
	}
	
	@Override
	public Biome getBiome(BlockPos blockPos_1)
	{
		return delegate.getBiome(blockPos_1);
	}
	
	@Override
	public PlayerEntity getClosestPlayer(double double_1, double double_2, double double_3)
	{
		return delegate.getClosestPlayer(double_1, double_2, double_3);
	}
	
	@Override
	public Stream<VoxelShape> getCollisionShapes(Entity entity_1, Box box_1, Set<Entity> set_1)
	{
		return delegate.getCollisionShapes(entity_1, box_1, set_1);
	}
	
	@Override
	public Stream<VoxelShape> method_20812(Entity entity_1, Box box_1)
	{
		return delegate.method_20812(entity_1, box_1);
	}
	
	@Override
	public boolean isClient()
	{
		return delegate.isClient();
	}
	
	@Override
	public MinecraftServer getServer()
	{
		return delegate.getServer();
	}
	
	@Environment(EnvType.CLIENT)
	@Override
	public void setDefaultSpawnClient()
	{
		delegate.setDefaultSpawnClient();
	}
	
	@Override
	public BlockState getTopNonAirState(BlockPos blockPos_1)
	{
		return delegate.getTopNonAirState(blockPos_1);
	}
	
	@Override
	public boolean isPlayerInRange(double double_1, double double_2, double double_3, double double_4)
	{
		return delegate.isPlayerInRange(double_1, double_2, double_3, double_4);
	}
	
	@Override
	public WorldChunk getWorldChunk(BlockPos blockPos_1)
	{
		return delegate.getWorldChunk(blockPos_1);
	}
	
	@Override
	public WorldChunk method_8497(int int_1, int int_2)
	{
		return delegate.method_8497(int_1, int_2);
	}
	
	@Override
	public Chunk getChunk(int int_1, int int_2, ChunkStatus chunkStatus_1, boolean boolean_1)
	{
		return delegate.getChunk(int_1, int_2, chunkStatus_1, boolean_1);
	}
	
	@Override
	public PlayerEntity getClosestPlayer(TargetPredicate targetPredicate_1, LivingEntity livingEntity_1)
	{
		return delegate.getClosestPlayer(targetPredicate_1, livingEntity_1);
	}
	
	@Override
	public PlayerEntity getClosestPlayer(TargetPredicate targetPredicate_1, LivingEntity livingEntity_1, double double_1, double double_2, double double_3)
	{
		return delegate.getClosestPlayer(targetPredicate_1, livingEntity_1, double_1, double_2, double_3);
	}
	
	@Override
	public boolean setBlockState(BlockPos blockPos_1, BlockState blockState_1, int int_1)
	{
		return delegate.setBlockState(blockPos_1, blockState_1, int_1);
	}
	
	@Override
	public PlayerEntity getClosestPlayer(TargetPredicate targetPredicate_1, double double_1, double double_2, double double_3)
	{
		return delegate.getClosestPlayer(targetPredicate_1, double_1, double_2, double_3);
	}
	
	@Override
	public <T extends LivingEntity> T method_21726(Class<? extends T> class_1, TargetPredicate targetPredicate_1, LivingEntity livingEntity_1, double double_1, double double_2, double double_3, Box box_1)
	{
		return delegate.method_21726(class_1, targetPredicate_1, livingEntity_1, double_1, double_2, double_3, box_1);
	}
	
	@Override
	public <T extends LivingEntity> T method_21727(Class<? extends T> class_1, TargetPredicate targetPredicate_1, LivingEntity livingEntity_1, double double_1, double double_2, double double_3, Box box_1)
	{
		return delegate.method_21727(class_1, targetPredicate_1, livingEntity_1, double_1, double_2, double_3, box_1);
	}
	
	@Override
	public <T extends LivingEntity> T getClosestEntity(List<? extends T> list_1, TargetPredicate targetPredicate_1, LivingEntity livingEntity_1, double double_1, double double_2, double double_3)
	{
		return delegate.getClosestEntity(list_1, targetPredicate_1, livingEntity_1, double_1, double_2, double_3);
	}
	
	@Override
	public boolean isWaterAt(BlockPos blockPos_1)
	{
		return delegate.isWaterAt(blockPos_1);
	}
	
	@Override
	public boolean intersectsFluid(Box box_1)
	{
		return delegate.intersectsFluid(box_1);
	}
	
	@Override
	public List<PlayerEntity> getPlayersInBox(TargetPredicate targetPredicate_1, LivingEntity livingEntity_1, Box box_1)
	{
		return delegate.getPlayersInBox(targetPredicate_1, livingEntity_1, box_1);
	}
	
	@Override
	public void onBlockChanged(BlockPos blockPos_1, BlockState blockState_1, BlockState blockState_2)
	{
		delegate.onBlockChanged(blockPos_1, blockState_1, blockState_2);
	}
	
	@Override
	public boolean clearBlockState(BlockPos blockPos_1, boolean boolean_1)
	{
		return delegate.clearBlockState(blockPos_1, boolean_1);
	}
	
	@Override
	public <T extends LivingEntity> List<T> getTargets(Class<? extends T> class_1, TargetPredicate targetPredicate_1, LivingEntity livingEntity_1, Box box_1)
	{
		return delegate.getTargets(class_1, targetPredicate_1, livingEntity_1, box_1);
	}
	
	@Override
	public boolean breakBlock(BlockPos blockPos_1, boolean boolean_1)
	{
		return delegate.breakBlock(blockPos_1, boolean_1);
	}
	
	@Override
	public int getLightLevel(BlockPos blockPos_1)
	{
		return delegate.getLightLevel(blockPos_1);
	}
	
	@Override
	public PlayerEntity getPlayerByUuid(UUID uUID_1)
	{
		return delegate.getPlayerByUuid(uUID_1);
	}
	
	@Override
	public String toString()
	{
		return delegate.toString();
	}
	
	@Override
	public int method_8603(BlockPos blockPos_1, int int_1)
	{
		return delegate.method_8603(blockPos_1, int_1);
	}
	
	@Override
	public boolean setBlockState(BlockPos blockPos_1, BlockState blockState_1)
	{
		return delegate.setBlockState(blockPos_1, blockState_1);
	}
	
	@Override
	public void updateListeners(BlockPos var1, BlockState var2, BlockState var3, int var4)
	{
		delegate.updateListeners(var1, var2, var3, var4);
	}
	
	@Override
	public boolean isBlockLoaded(BlockPos blockPos_1)
	{
		return delegate.isBlockLoaded(blockPos_1);
	}
	
	@Override
	public void updateNeighbors(BlockPos blockPos_1, Block block_1)
	{
		delegate.updateNeighbors(blockPos_1, block_1);
	}
	
	@Override
	public boolean isAreaLoaded(BlockPos blockPos_1, BlockPos blockPos_2)
	{
		return delegate.isAreaLoaded(blockPos_1, blockPos_2);
	}
	
	@Override
	public void scheduleBlockRender(BlockPos blockPos_1, BlockState blockState_1, BlockState blockState_2)
	{
		delegate.scheduleBlockRender(blockPos_1, blockState_1, blockState_2);
	}
	
	@Override
	public void updateNeighborsAlways(BlockPos blockPos_1, Block block_1)
	{
		delegate.updateNeighborsAlways(blockPos_1, block_1);
	}
	
	@Override
	public boolean isAreaLoaded(int int_1, int int_2, int int_3, int int_4, int int_5, int int_6)
	{
		return delegate.isAreaLoaded(int_1, int_2, int_3, int_4, int_5, int_6);
	}
	
	@Override
	public void updateNeighborsExcept(BlockPos blockPos_1, Block block_1, Direction direction_1)
	{
		delegate.updateNeighborsExcept(blockPos_1, block_1, direction_1);
	}
	
	@Override
	public void updateNeighbor(BlockPos blockPos_1, Block block_1, BlockPos blockPos_2)
	{
		delegate.updateNeighbor(blockPos_1, block_1, blockPos_2);
	}
	
	@Override
	public int getLightLevel(BlockPos blockPos_1, int int_1)
	{
		return delegate.getLightLevel(blockPos_1, int_1);
	}
	
	@Override
	public int getTop(Type heightmap$Type_1, int int_1, int int_2)
	{
		return delegate.getTop(heightmap$Type_1, int_1, int_2);
	}
	
	@Override
	public int getLightLevel(LightType lightType_1, BlockPos blockPos_1)
	{
		return delegate.getLightLevel(lightType_1, blockPos_1);
	}
	
	@Override
	public BlockState getBlockState(BlockPos blockPos_1)
	{
		return delegate.getBlockState(blockPos_1);
	}
	
	@Override
	public FluidState getFluidState(BlockPos blockPos_1)
	{
		return delegate.getFluidState(blockPos_1);
	}
	
	@Override
	public boolean isDaylight()
	{
		return delegate.isDaylight();
	}
	
	@Override
	public void playSound(PlayerEntity playerEntity_1, BlockPos blockPos_1, SoundEvent soundEvent_1, SoundCategory soundCategory_1, float float_1, float float_2)
	{
		delegate.playSound(playerEntity_1, blockPos_1, soundEvent_1, soundCategory_1, float_1, float_2);
	}
	
	@Override
	public void playSound(PlayerEntity var1, double var2, double var4, double var6, SoundEvent var8, SoundCategory var9, float var10, float var11)
	{
		delegate.playSound(var1, var2, var4, var6, var8, var9, var10, var11);
	}
	
	@Override
	public void playSoundFromEntity(PlayerEntity var1, Entity var2, SoundEvent var3, SoundCategory var4, float var5, float var6)
	{
		delegate.playSoundFromEntity(var1, var2, var3, var4, var5, var6);
	}
	
	@Override
	public void playSound(double double_1, double double_2, double double_3, SoundEvent soundEvent_1, SoundCategory soundCategory_1, float float_1, float float_2, boolean boolean_1)
	{
		delegate.playSound(double_1, double_2, double_3, soundEvent_1, soundCategory_1, float_1, float_2, boolean_1);
	}
	
	@Environment(EnvType.CLIENT)
	@Override
	public void addParticle(ParticleEffect particleEffect_1, double double_1, double double_2, double double_3, double double_4, double double_5, double double_6)
	{
		delegate.addParticle(particleEffect_1, double_1, double_2, double_3, double_4, double_5, double_6);
	}
	
	@Override
	public void addParticle(ParticleEffect particleEffect_1, boolean boolean_1, double double_1, double double_2, double double_3, double double_4, double double_5, double double_6)
	{
		delegate.addParticle(particleEffect_1, boolean_1, double_1, double_2, double_3, double_4, double_5, double_6);
	}
	
	@Override
	public void addImportantParticle(ParticleEffect particleEffect_1, double double_1, double double_2, double double_3, double double_4, double double_5, double double_6)
	{
		delegate.addImportantParticle(particleEffect_1, double_1, double_2, double_3, double_4, double_5, double_6);
	}
	
	@Override
	public void addImportantParticle(ParticleEffect particleEffect_1, boolean boolean_1, double double_1, double double_2, double double_3, double double_4, double double_5, double double_6)
	{
		delegate.addImportantParticle(particleEffect_1, boolean_1, double_1, double_2, double_3, double_4, double_5, double_6);
	}
	
	@Environment(EnvType.CLIENT)
	@Override
	public float getAmbientLight(float float_1)
	{
		return delegate.getAmbientLight(float_1);
	}
	
	@Environment(EnvType.CLIENT)
	@Override
	public Vec3d getSkyColor(BlockPos blockPos_1, float float_1)
	{
		return delegate.getSkyColor(blockPos_1, float_1);
	}
	
	@Override
	public float getSkyAngleRadians(float float_1)
	{
		return delegate.getSkyAngleRadians(float_1);
	}
	
	@Environment(EnvType.CLIENT)
	@Override
	public Vec3d getCloudColor(float float_1)
	{
		return delegate.getCloudColor(float_1);
	}
	
	@Environment(EnvType.CLIENT)
	@Override
	public Vec3d getFogColor(float float_1)
	{
		return delegate.getFogColor(float_1);
	}
	
	@Environment(EnvType.CLIENT)
	@Override
	public float getStarsBrightness(float float_1)
	{
		return delegate.getStarsBrightness(float_1);
	}
	
	@Override
	public boolean addBlockEntity(BlockEntity blockEntity_1)
	{
		return delegate.addBlockEntity(blockEntity_1);
	}
	
	@Override
	public void addBlockEntities(Collection<BlockEntity> collection_1)
	{
		delegate.addBlockEntities(collection_1);
	}
	
	@Override
	public void tickBlockEntities()
	{
		delegate.tickBlockEntities();
	}
	
	@Override
	public void tickEntity(Consumer<Entity> consumer_1, Entity entity_1)
	{
		delegate.tickEntity(consumer_1, entity_1);
	}
	
	@Override
	public boolean isAreaNotEmpty(Box box_1)
	{
		return delegate.isAreaNotEmpty(box_1);
	}
	
	@Override
	public boolean doesAreaContainFireSource(Box box_1)
	{
		return delegate.doesAreaContainFireSource(box_1);
	}
	
	@Environment(EnvType.CLIENT)
	@Override
	public BlockState getBlockState(Box box_1, Block block_1)
	{
		return delegate.getBlockState(box_1, block_1);
	}
	
	@Override
	public boolean containsBlockWithMaterial(Box box_1, Material material_1)
	{
		return delegate.containsBlockWithMaterial(box_1, material_1);
	}
	
	@Override
	public Explosion createExplosion(Entity entity_1, double double_1, double double_2, double double_3, float float_1, DestructionType explosion$DestructionType_1)
	{
		return delegate.createExplosion(entity_1, double_1, double_2, double_3, float_1, explosion$DestructionType_1);
	}
	
	@Override
	public Explosion createExplosion(Entity entity_1, double double_1, double double_2, double double_3, float float_1, boolean boolean_1, DestructionType explosion$DestructionType_1)
	{
		return delegate.createExplosion(entity_1, double_1, double_2, double_3, float_1, boolean_1, explosion$DestructionType_1);
	}
	
	@Override
	public Explosion createExplosion(Entity entity_1, DamageSource damageSource_1, double double_1, double double_2, double double_3, float float_1, boolean boolean_1, DestructionType explosion$DestructionType_1)
	{
		return delegate.createExplosion(entity_1, damageSource_1, double_1, double_2, double_3, float_1, boolean_1, explosion$DestructionType_1);
	}
	
	@Override
	public boolean method_8506(PlayerEntity playerEntity_1, BlockPos blockPos_1, Direction direction_1)
	{
		return delegate.method_8506(playerEntity_1, blockPos_1, direction_1);
	}
	
	@Environment(EnvType.CLIENT)
	@Override
	public String getChunkProviderStatus()
	{
		return delegate.getChunkProviderStatus();
	}
	
	@Override
	public BlockEntity getBlockEntity(BlockPos blockPos_1)
	{
		return delegate.getBlockEntity(blockPos_1);
	}
	
	@Override
	public void setBlockEntity(BlockPos blockPos_1, BlockEntity blockEntity_1)
	{
		delegate.setBlockEntity(blockPos_1, blockEntity_1);
	}
	
	@Override
	public void removeBlockEntity(BlockPos blockPos_1)
	{
		delegate.removeBlockEntity(blockPos_1);
	}
	
	@Override
	public boolean isHeightValidAndBlockLoaded(BlockPos blockPos_1)
	{
		return delegate.isHeightValidAndBlockLoaded(blockPos_1);
	}
	
	@Override
	public boolean doesBlockHaveSolidTopSurface(BlockPos blockPos_1, Entity entity_1)
	{
		return delegate.doesBlockHaveSolidTopSurface(blockPos_1, entity_1);
	}
	
	@Override
	public void calculateAmbientDarkness()
	{
		delegate.calculateAmbientDarkness();
	}
	
	@Override
	public void setMobSpawnOptions(boolean boolean_1, boolean boolean_2)
	{
		delegate.setMobSpawnOptions(boolean_1, boolean_2);
	}
	
	@Override
	public void close() throws IOException
	{
		delegate.close();
	}
	
	@Override
	public ChunkStatus getLeastChunkStatusForCollisionCalculation()
	{
		return delegate.getLeastChunkStatusForCollisionCalculation();
	}
	
	@Override
	public List<Entity> getEntities(Entity entity_1, Box box_1, Predicate<? super Entity> predicate_1)
	{
		return delegate.getEntities(entity_1, box_1, predicate_1);
	}
	
	@Override
	public List<Entity> getEntities(EntityType<?> entityType_1, Box box_1, Predicate<? super Entity> predicate_1)
	{
		return delegate.getEntities(entityType_1, box_1, predicate_1);
	}
	
	@Override
	public <T extends Entity> List<T> getEntities(Class<? extends T> class_1, Box box_1, Predicate<? super T> predicate_1)
	{
		return delegate.getEntities(class_1, box_1, predicate_1);
	}
	
	@Override
	public <T extends Entity> List<T> method_21729(Class<? extends T> class_1, Box box_1, Predicate<? super T> predicate_1)
	{
		return delegate.method_21729(class_1, box_1, predicate_1);
	}
	
	@Override
	public Entity getEntityById(int var1)
	{
		return delegate.getEntityById(var1);
	}
	
	@Override
	public void markDirty(BlockPos blockPos_1, BlockEntity blockEntity_1)
	{
		delegate.markDirty(blockPos_1, blockEntity_1);
	}
	
	@Override
	public int getSeaLevel()
	{
		return delegate.getSeaLevel();
	}
	
	@Override
	public World getWorld()
	{
		return delegate.getWorld();
	}
	
	@Override
	public LevelGeneratorType getGeneratorType()
	{
		return delegate.getGeneratorType();
	}
	
	@Override
	public int getReceivedStrongRedstonePower(BlockPos blockPos_1)
	{
		return delegate.getReceivedStrongRedstonePower(blockPos_1);
	}
	
	@Override
	public boolean isEmittingRedstonePower(BlockPos blockPos_1, Direction direction_1)
	{
		return delegate.isEmittingRedstonePower(blockPos_1, direction_1);
	}
	
	@Override
	public int getEmittedRedstonePower(BlockPos blockPos_1, Direction direction_1)
	{
		return delegate.getEmittedRedstonePower(blockPos_1, direction_1);
	}
	
	@Override
	public boolean isReceivingRedstonePower(BlockPos blockPos_1)
	{
		return delegate.isReceivingRedstonePower(blockPos_1);
	}
	
	@Override
	public int getReceivedRedstonePower(BlockPos blockPos_1)
	{
		return delegate.getReceivedRedstonePower(blockPos_1);
	}
	
	@Environment(EnvType.CLIENT)
	@Override
	public void disconnect()
	{
		delegate.disconnect();
	}
	
	@Override
	public void setTime(long long_1)
	{
		delegate.setTime(long_1);
	}
	
	@Override
	public long getSeed()
	{
		return delegate.getSeed();
	}
	
	@Override
	public long getTime()
	{
		return delegate.getTime();
	}
	
	@Override
	public long getTimeOfDay()
	{
		return delegate.getTimeOfDay();
	}
	
	@Override
	public void setTimeOfDay(long long_1)
	{
		delegate.setTimeOfDay(long_1);
	}
	
	@Override
	public BlockPos getSpawnPos()
	{
		return delegate.getSpawnPos();
	}
	
	@Override
	public void setSpawnPos(BlockPos blockPos_1)
	{
		delegate.setSpawnPos(blockPos_1);
	}
	
	@Override
	public boolean canPlayerModifyAt(PlayerEntity playerEntity_1, BlockPos blockPos_1)
	{
		return delegate.canPlayerModifyAt(playerEntity_1, blockPos_1);
	}
	
	@Override
	public void sendEntityStatus(Entity entity_1, byte byte_1)
	{
		delegate.sendEntityStatus(entity_1, byte_1);
	}
	
	@Override
	public ChunkManager getChunkManager()
	{
		return delegate.getChunkManager();
	}
	
	@Override
	public void addBlockAction(BlockPos blockPos_1, Block block_1, int int_1, int int_2)
	{
		delegate.addBlockAction(blockPos_1, block_1, int_1, int_2);
	}
	
	@Override
	public LevelProperties getLevelProperties()
	{
		return delegate.getLevelProperties();
	}
	
	@Override
	public GameRules getGameRules()
	{
		return delegate.getGameRules();
	}
	
	@Override
	public float getThunderGradient(float float_1)
	{
		return delegate.getThunderGradient(float_1);
	}
	
	@Environment(EnvType.CLIENT)
	@Override
	public void setThunderGradient(float float_1)
	{
		delegate.setThunderGradient(float_1);
	}
	
	@Override
	public float getRainGradient(float float_1)
	{
		return delegate.getRainGradient(float_1);
	}
	
	@Environment(EnvType.CLIENT)
	@Override
	public void setRainGradient(float float_1)
	{
		delegate.setRainGradient(float_1);
	}
	
	@Override
	public boolean isThundering()
	{
		return delegate.isThundering();
	}
	
	@Override
	public boolean isRaining()
	{
		return delegate.isRaining();
	}
	
	@Override
	public boolean hasRain(BlockPos blockPos_1)
	{
		return delegate.hasRain(blockPos_1);
	}
	
	@Override
	public boolean hasHighHumidity(BlockPos blockPos_1)
	{
		return delegate.hasHighHumidity(blockPos_1);
	}
	
	@Override
	public MapState getMapState(String var1)
	{
		return delegate.getMapState(var1);
	}
	
	@Override
	public void putMapState(MapState var1)
	{
		delegate.putMapState(var1);
	}
	
	@Override
	public int getNextMapId()
	{
		return delegate.getNextMapId();
	}
	
	@Override
	public void playGlobalEvent(int int_1, BlockPos blockPos_1, int int_2)
	{
		delegate.playGlobalEvent(int_1, blockPos_1, int_2);
	}
	
	@Override
	public int getEffectiveHeight()
	{
		return delegate.getEffectiveHeight();
	}
	
	@Environment(EnvType.CLIENT)
	@Override
	public double getHorizonHeight()
	{
		return delegate.getHorizonHeight();
	}
	
	@Override
	public CrashReportSection addDetailsToCrashReport(CrashReport crashReport_1)
	{
		return delegate.addDetailsToCrashReport(crashReport_1);
	}
	
	@Override
	public void setBlockBreakingProgress(int var1, BlockPos var2, int var3)
	{
		delegate.setBlockBreakingProgress(var1, var2, var3);
	}
	
	@Environment(EnvType.CLIENT)
	@Override
	public void addFireworkParticle(double double_1, double double_2, double double_3, double double_4, double double_5, double double_6, CompoundTag compoundTag_1)
	{
		delegate.addFireworkParticle(double_1, double_2, double_3, double_4, double_5, double_6, compoundTag_1);
	}
	
	@Override
	public Scoreboard getScoreboard()
	{
		return delegate.getScoreboard();
	}
	
	@Override
	public void updateHorizontalAdjacent(BlockPos blockPos_1, Block block_1)
	{
		delegate.updateHorizontalAdjacent(blockPos_1, block_1);
	}
	
	@Override
	public LocalDifficulty getLocalDifficulty(BlockPos blockPos_1)
	{
		return delegate.getLocalDifficulty(blockPos_1);
	}
	
	@Override
	public int getAmbientDarkness()
	{
		return delegate.getAmbientDarkness();
	}
	
	@Environment(EnvType.CLIENT)
	@Override
	public int getTicksSinceLightning()
	{
		return delegate.getTicksSinceLightning();
	}
	
	@Override
	public void setTicksSinceLightning(int int_1)
	{
		delegate.setTicksSinceLightning(int_1);
	}
	
	@Override
	public WorldBorder getWorldBorder()
	{
		return delegate.getWorldBorder();
	}
	
	@Override
	public void sendPacket(Packet<?> packet_1)
	{
		delegate.sendPacket(packet_1);
	}
	
	@Override
	public BlockPos locateStructure(String string_1, BlockPos blockPos_1, int int_1, boolean boolean_1)
	{
		return delegate.locateStructure(string_1, blockPos_1, int_1, boolean_1);
	}
	
	@Override
	public Dimension getDimension()
	{
		return delegate.getDimension();
	}
	
	@Override
	public Random getRandom()
	{
		return delegate.getRandom();
	}
	
	@Override
	public boolean testBlockState(BlockPos blockPos_1, Predicate<BlockState> predicate_1)
	{
		return delegate.testBlockState(blockPos_1, predicate_1);
	}
	
	@Override
	public RecipeManager getRecipeManager()
	{
		return delegate.getRecipeManager();
	}
	
	@Override
	public RegistryTagManager getTagManager()
	{
		return delegate.getTagManager();
	}
	
	@Override
	public BlockPos getRandomPosInChunk(int int_1, int int_2, int int_3, int int_4)
	{
		return delegate.getRandomPosInChunk(int_1, int_2, int_3, int_4);
	}
	
	@Override
	public boolean isSavingDisabled()
	{
		return delegate.isSavingDisabled();
	}
	
	@Override
	public Profiler getProfiler()
	{
		return delegate.getProfiler();
	}
	
	@Override
	public BlockPos getTopPosition(Type heightmap$Type_1, BlockPos blockPos_1)
	{
		return delegate.getTopPosition(heightmap$Type_1, blockPos_1);
	}
	
}
