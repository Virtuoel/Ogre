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
import net.minecraft.world.BlockView;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.Heightmap.Type;
import net.minecraft.world.LightType;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.RayTraceContext;
import net.minecraft.world.TickScheduler;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.Explosion.DestructionType;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.LevelGeneratorType;
import net.minecraft.world.level.LevelProperties;

public abstract class WrappedWorld extends World
{
	protected final World delegate;
	
	protected WrappedWorld(World delegate)
	{
		super(delegate.getLevelProperties(), delegate.getDimension().getType(), (w, d) -> null, null, delegate.isClient());
		this.delegate = delegate;
	}
	
	@Override
	public boolean breakBlock(BlockPos blockPos, boolean bl)
	{
		return delegate.breakBlock(blockPos, bl);
	}
	
	@Override
	public int getLightLevel(LightType lightType, BlockPos blockPos)
	{
		return delegate.getLightLevel(lightType, blockPos);
	}
	
	@Override
	public boolean spawnEntity(Entity entity)
	{
		return delegate.spawnEntity(entity);
	}
	
	@Override
	public int getBaseLightLevel(BlockPos blockPos, int i)
	{
		return delegate.getBaseLightLevel(blockPos, i);
	}
	
	@Override
	public int getLuminance(BlockPos blockPos)
	{
		return delegate.getLuminance(blockPos);
	}
	
	@Override
	public boolean isSkyVisible(BlockPos blockPos)
	{
		return delegate.isSkyVisible(blockPos);
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
	public BlockHitResult rayTrace(RayTraceContext rayTraceContext)
	{
		return delegate.rayTrace(rayTraceContext);
	}
	
	@Override
	public Biome getBiome(BlockPos blockPos)
	{
		return delegate.getBiome(blockPos);
	}
	
	@Override
	public float getSkyAngle(float f)
	{
		return delegate.getSkyAngle(f);
	}
	
	@Override
	public boolean canPlace(BlockState blockState, BlockPos blockPos, EntityContext entityContext)
	{
		return delegate.canPlace(blockState, blockPos, entityContext);
	}
	
	@Override
	public int getColor(BlockPos blockPos, ColorResolver colorResolver)
	{
		return delegate.getColor(blockPos, colorResolver);
	}
	
	@Override
	public int getMoonPhase()
	{
		return delegate.getMoonPhase();
	}
	
	@Override
	public List<? extends PlayerEntity> getPlayers()
	{
		return delegate.getPlayers();
	}
	
	@Override
	public List<Entity> getEntities(Entity entity, Box box)
	{
		return delegate.getEntities(entity, box);
	}
	
	@Override
	public TickScheduler<Block> getBlockTickScheduler()
	{
		return delegate.getBlockTickScheduler();
	}
	
	@Override
	public Biome getBiomeForNoiseGen(int i, int j, int k)
	{
		return delegate.getBiomeForNoiseGen(i, j, k);
	}
	
	@Override
	public TickScheduler<Fluid> getFluidTickScheduler()
	{
		return delegate.getFluidTickScheduler();
	}
	
	@Override
	public boolean intersectsEntities(Entity entity)
	{
		return delegate.intersectsEntities(entity);
	}
	
	@Override
	public Difficulty getDifficulty()
	{
		return delegate.getDifficulty();
	}
	
	@Override
	public boolean doesNotCollide(Box box)
	{
		return delegate.doesNotCollide(box);
	}
	
	@Override
	public Biome getGeneratorStoredBiome(int i, int j, int k)
	{
		return delegate.getGeneratorStoredBiome(i, j, k);
	}
	
	@Override
	public boolean isChunkLoaded(int i, int j)
	{
		return delegate.isChunkLoaded(i, j);
	}
	
	@Override
	public boolean doesNotCollide(Entity entity)
	{
		return delegate.doesNotCollide(entity);
	}
	
	@Override
	public boolean doesNotCollide(Entity entity, Box box)
	{
		return delegate.doesNotCollide(entity, box);
	}
	
	@Override
	public <T extends Entity> List<T> getNonSpectatingEntities(Class<? extends T> var1, Box box)
	{
		return delegate.getNonSpectatingEntities(var1, box);
	}
	
	@Override
	public boolean isAir(BlockPos blockPos)
	{
		return delegate.isAir(blockPos);
	}
	
	@Override
	public boolean doesNotCollide(Entity entity, Box box, Set<Entity> set)
	{
		return delegate.doesNotCollide(entity, box, set);
	}
	
	@Override
	public boolean isSkyVisibleAllowingSea(BlockPos blockPos)
	{
		return delegate.isSkyVisibleAllowingSea(blockPos);
	}
	
	@Override
	public <T extends Entity> List<T> getEntitiesIncludingUngeneratedChunks(Class<? extends T> var1, Box box)
	{
		return delegate.getEntitiesIncludingUngeneratedChunks(var1, box);
	}
	
	@Override
	public void playLevelEvent(PlayerEntity playerEntity, int i, BlockPos blockPos, int j)
	{
		delegate.playLevelEvent(playerEntity, i, blockPos, j);
	}
	
	@Override
	public void playLevelEvent(int i, BlockPos blockPos, int j)
	{
		delegate.playLevelEvent(i, blockPos, j);
	}
	
	@Override
	public Stream<VoxelShape> getCollisions(Entity entity, Box box, Set<Entity> set)
	{
		return delegate.getCollisions(entity, box, set);
	}
	
	@Override
	public BlockHitResult rayTraceBlock(Vec3d vec3d, Vec3d vec3d2, BlockPos blockPos, VoxelShape voxelShape, BlockState blockState)
	{
		return delegate.rayTraceBlock(vec3d, vec3d2, blockPos, voxelShape, blockState);
	}
	
	@Override
	public Stream<VoxelShape> getEntityCollisions(Entity entity, Box box, Set<Entity> set)
	{
		return delegate.getEntityCollisions(entity, box, set);
	}
	
	@Override
	public int hashCode()
	{
		return delegate.hashCode();
	}
	
	@Override
	public Stream<VoxelShape> getBlockCollisions(Entity entity, Box box)
	{
		return delegate.getBlockCollisions(entity, box);
	}
	
	@Override
	public boolean intersectsEntities(Entity entity, VoxelShape voxelShape)
	{
		return delegate.intersectsEntities(entity, voxelShape);
	}
	
	@Override
	public BlockPos getTopPosition(Type type, BlockPos blockPos)
	{
		return delegate.getTopPosition(type, blockPos);
	}
	
	@Override
	public float getBrightness(BlockPos blockPos)
	{
		return delegate.getBrightness(blockPos);
	}
	
	@Override
	public int getStrongRedstonePower(BlockPos blockPos, Direction direction)
	{
		return delegate.getStrongRedstonePower(blockPos, direction);
	}
	
	@Override
	public PlayerEntity getClosestPlayer(double d, double e, double f, double g, Predicate<Entity> predicate)
	{
		return delegate.getClosestPlayer(d, e, f, g, predicate);
	}
	
	@Override
	public Chunk getChunk(BlockPos blockPos)
	{
		return delegate.getChunk(blockPos);
	}
	
	@Override
	public Chunk getChunk(int i, int j, ChunkStatus chunkStatus)
	{
		return delegate.getChunk(i, j, chunkStatus);
	}
	
	@Override
	public boolean isWater(BlockPos blockPos)
	{
		return delegate.isWater(blockPos);
	}
	
	@Override
	public boolean containsFluid(Box box)
	{
		return delegate.containsFluid(box);
	}
	
	@Override
	public PlayerEntity getClosestPlayer(Entity entity, double d)
	{
		return delegate.getClosestPlayer(entity, d);
	}
	
	@Override
	public PlayerEntity getClosestPlayer(double d, double e, double f, double g, boolean bl)
	{
		return delegate.getClosestPlayer(d, e, f, g, bl);
	}
	
	@Override
	public PlayerEntity getClosestPlayer(double d, double e, double f)
	{
		return delegate.getClosestPlayer(d, e, f);
	}
	
	@Override
	public boolean equals(Object obj)
	{
		return delegate.equals(obj);
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
	public BlockState getTopNonAirState(BlockPos blockPos)
	{
		return delegate.getTopNonAirState(blockPos);
	}
	
	@Override
	public int getLightLevel(BlockPos blockPos)
	{
		return delegate.getLightLevel(blockPos);
	}
	
	@Override
	public int getLightLevel(BlockPos blockPos, int i)
	{
		return delegate.getLightLevel(blockPos, i);
	}
	
	@Override
	public boolean isPlayerInRange(double d, double e, double f, double g)
	{
		return delegate.isPlayerInRange(d, e, f, g);
	}
	
	@Override
	public boolean isChunkLoaded(BlockPos blockPos)
	{
		return delegate.isChunkLoaded(blockPos);
	}
	
	@Override
	public WorldChunk getWorldChunk(BlockPos blockPos)
	{
		return delegate.getWorldChunk(blockPos);
	}
	
	@Override
	public boolean isRegionLoaded(BlockPos blockPos, BlockPos blockPos2)
	{
		return delegate.isRegionLoaded(blockPos, blockPos2);
	}
	
	@Override
	public Chunk getChunk(int i, int j, ChunkStatus chunkStatus, boolean bl)
	{
		return delegate.getChunk(i, j, chunkStatus, bl);
	}
	
	@Override
	public boolean isRegionLoaded(int i, int j, int k, int l, int m, int n)
	{
		return delegate.isRegionLoaded(i, j, k, l, m, n);
	}
	
	@Override
	public PlayerEntity getClosestPlayer(TargetPredicate targetPredicate, LivingEntity livingEntity)
	{
		return delegate.getClosestPlayer(targetPredicate, livingEntity);
	}
	
	@Override
	public boolean setBlockState(BlockPos blockPos, BlockState blockState, int i)
	{
		return delegate.setBlockState(blockPos, blockState, i);
	}
	
	@Override
	public PlayerEntity getClosestPlayer(TargetPredicate targetPredicate, LivingEntity livingEntity, double d, double e, double f)
	{
		return delegate.getClosestPlayer(targetPredicate, livingEntity, d, e, f);
	}
	
	@Override
	public PlayerEntity getClosestPlayer(TargetPredicate targetPredicate, double d, double e, double f)
	{
		return delegate.getClosestPlayer(targetPredicate, d, e, f);
	}
	
	@Override
	public <T extends LivingEntity> T getClosestEntity(Class<? extends T> var1, TargetPredicate targetPredicate, LivingEntity livingEntity, double d, double e, double f, Box box)
	{
		return delegate.getClosestEntity(var1, targetPredicate, livingEntity, d, e, f, box);
	}
	
	@Override
	public <T extends LivingEntity> T getClosestEntityIncludingUngeneratedChunks(Class<? extends T> var1, TargetPredicate targetPredicate, LivingEntity livingEntity, double d, double e, double f, Box box)
	{
		return delegate.getClosestEntityIncludingUngeneratedChunks(var1, targetPredicate, livingEntity, d, e, f, box);
	}
	
	@Override
	public <T extends LivingEntity> T getClosestEntity(List<? extends T> list, TargetPredicate targetPredicate, LivingEntity livingEntity, double d, double e, double f)
	{
		return delegate.getClosestEntity(list, targetPredicate, livingEntity, d, e, f);
	}
	
	@Override
	public List<PlayerEntity> getPlayers(TargetPredicate targetPredicate, LivingEntity livingEntity, Box box)
	{
		return delegate.getPlayers(targetPredicate, livingEntity, box);
	}
	
	@Override
	public void onBlockChanged(BlockPos blockPos, BlockState blockState, BlockState blockState2)
	{
		delegate.onBlockChanged(blockPos, blockState, blockState2);
	}
	
	@Override
	public boolean removeBlock(BlockPos blockPos, boolean bl)
	{
		return delegate.removeBlock(blockPos, bl);
	}
	
	@Override
	public <T extends LivingEntity> List<T> getTargets(Class<? extends T> var1, TargetPredicate targetPredicate, LivingEntity livingEntity, Box box)
	{
		return delegate.getTargets(var1, targetPredicate, livingEntity, box);
	}
	
	@Override
	public boolean breakBlock(BlockPos blockPos, boolean bl, Entity entity)
	{
		return delegate.breakBlock(blockPos, bl, entity);
	}
	
	@Override
	public PlayerEntity getPlayerByUuid(UUID uUID)
	{
		return delegate.getPlayerByUuid(uUID);
	}
	
	@Override
	public boolean setBlockState(BlockPos blockPos, BlockState blockState)
	{
		return delegate.setBlockState(blockPos, blockState);
	}
	
	@Override
	public void updateListeners(BlockPos blockPos, BlockState blockState, BlockState blockState2, int i)
	{
		delegate.updateListeners(blockPos, blockState, blockState2, i);
	}
	
	@Override
	public void updateNeighbors(BlockPos blockPos, Block block)
	{
		delegate.updateNeighbors(blockPos, block);
	}
	
	@Override
	public void checkBlockRerender(BlockPos blockPos, BlockState blockState, BlockState blockState2)
	{
		delegate.checkBlockRerender(blockPos, blockState, blockState2);
	}
	
	@Override
	public void updateNeighborsAlways(BlockPos blockPos, Block block)
	{
		delegate.updateNeighborsAlways(blockPos, block);
	}
	
	@Override
	public String toString()
	{
		return delegate.toString();
	}
	
	@Override
	public void updateNeighborsExcept(BlockPos blockPos, Block block, Direction direction)
	{
		delegate.updateNeighborsExcept(blockPos, block, direction);
	}
	
	@Override
	public void updateNeighbor(BlockPos blockPos, Block block, BlockPos blockPos2)
	{
		delegate.updateNeighbor(blockPos, block, blockPos2);
	}
	
	@Override
	public int getTopY(Type type, int i, int j)
	{
		return delegate.getTopY(type, i, j);
	}
	
	@Override
	public LightingProvider getLightingProvider()
	{
		return delegate.getLightingProvider();
	}
	
	@Override
	public BlockState getBlockState(BlockPos blockPos)
	{
		return delegate.getBlockState(blockPos);
	}
	
	@Override
	public FluidState getFluidState(BlockPos blockPos)
	{
		return delegate.getFluidState(blockPos);
	}
	
	@Override
	public boolean isDay()
	{
		return delegate.isDay();
	}
	
	@Override
	public boolean isNight()
	{
		return delegate.isNight();
	}
	
	@Override
	public void playSound(PlayerEntity playerEntity, BlockPos blockPos, SoundEvent soundEvent, SoundCategory soundCategory, float f, float g)
	{
		delegate.playSound(playerEntity, blockPos, soundEvent, soundCategory, f, g);
	}
	
	@Override
	public void playSound(PlayerEntity playerEntity, double d, double e, double f, SoundEvent soundEvent, SoundCategory soundCategory, float g, float h)
	{
		delegate.playSound(playerEntity, d, e, f, soundEvent, soundCategory, g, h);
	}
	
	@Override
	public void playSoundFromEntity(PlayerEntity playerEntity, Entity entity, SoundEvent soundEvent, SoundCategory soundCategory, float f, float g)
	{
		delegate.playSoundFromEntity(playerEntity, entity, soundEvent, soundCategory, f, g);
	}
	
	@Override
	public void playSound(double d, double e, double f, SoundEvent soundEvent, SoundCategory soundCategory, float g, float h, boolean bl)
	{
		delegate.playSound(d, e, f, soundEvent, soundCategory, g, h, bl);
	}
	
	@Override
	public void addParticle(ParticleEffect particleEffect, double d, double e, double f, double g, double h, double i)
	{
		delegate.addParticle(particleEffect, d, e, f, g, h, i);
	}
	
	@Environment(EnvType.CLIENT)
	@Override
	public void addParticle(ParticleEffect particleEffect, boolean bl, double d, double e, double f, double g, double h, double i)
	{
		delegate.addParticle(particleEffect, bl, d, e, f, g, h, i);
	}
	
	@Override
	public void addImportantParticle(ParticleEffect particleEffect, double d, double e, double f, double g, double h, double i)
	{
		delegate.addImportantParticle(particleEffect, d, e, f, g, h, i);
	}
	
	@Override
	public void addImportantParticle(ParticleEffect particleEffect, boolean bl, double d, double e, double f, double g, double h, double i)
	{
		delegate.addImportantParticle(particleEffect, bl, d, e, f, g, h, i);
	}
	
	@Override
	public float getSkyAngleRadians(float f)
	{
		return delegate.getSkyAngleRadians(f);
	}
	
	@Override
	public boolean addBlockEntity(BlockEntity blockEntity)
	{
		return delegate.addBlockEntity(blockEntity);
	}
	
	@Override
	public void addBlockEntities(Collection<BlockEntity> collection)
	{
		delegate.addBlockEntities(collection);
	}
	
	@Override
	public void tickBlockEntities()
	{
		delegate.tickBlockEntities();
	}
	
	@Override
	public void tickEntity(Consumer<Entity> consumer, Entity entity)
	{
		delegate.tickEntity(consumer, entity);
	}
	
	@Override
	public boolean isAreaNotEmpty(Box box)
	{
		return delegate.isAreaNotEmpty(box);
	}
	
	@Override
	public boolean doesAreaContainFireSource(Box box)
	{
		return delegate.doesAreaContainFireSource(box);
	}
	
	@Environment(EnvType.CLIENT)
	@Override
	public BlockState getBlockState(Box box, Block block)
	{
		return delegate.getBlockState(box, block);
	}
	
	@Override
	public boolean containsBlockWithMaterial(Box box, Material material)
	{
		return delegate.containsBlockWithMaterial(box, material);
	}
	
	@Override
	public Explosion createExplosion(Entity entity, double d, double e, double f, float g, DestructionType destructionType)
	{
		return delegate.createExplosion(entity, d, e, f, g, destructionType);
	}
	
	@Override
	public Explosion createExplosion(Entity entity, double d, double e, double f, float g, boolean bl, DestructionType destructionType)
	{
		return delegate.createExplosion(entity, d, e, f, g, bl, destructionType);
	}
	
	@Override
	public Explosion createExplosion(Entity entity, DamageSource damageSource, double d, double e, double f, float g, boolean bl, DestructionType destructionType)
	{
		return delegate.createExplosion(entity, damageSource, d, e, f, g, bl, destructionType);
	}
	
	@Override
	public boolean extinguishFire(PlayerEntity playerEntity, BlockPos blockPos, Direction direction)
	{
		return delegate.extinguishFire(playerEntity, blockPos, direction);
	}
	
	@Environment(EnvType.CLIENT)
	@Override
	public String getDebugString()
	{
		return delegate.getDebugString();
	}
	
	@Override
	public BlockEntity getBlockEntity(BlockPos blockPos)
	{
		return delegate.getBlockEntity(blockPos);
	}
	
	@Override
	public void setBlockEntity(BlockPos blockPos, BlockEntity blockEntity)
	{
		delegate.setBlockEntity(blockPos, blockEntity);
	}
	
	@Override
	public void removeBlockEntity(BlockPos blockPos)
	{
		delegate.removeBlockEntity(blockPos);
	}
	
	@Override
	public boolean canSetBlock(BlockPos blockPos)
	{
		return delegate.canSetBlock(blockPos);
	}
	
	@Override
	public boolean isTopSolid(BlockPos blockPos, Entity entity)
	{
		return delegate.isTopSolid(blockPos, entity);
	}
	
	@Override
	public void calculateAmbientDarkness()
	{
		delegate.calculateAmbientDarkness();
	}
	
	@Override
	public void setMobSpawnOptions(boolean bl, boolean bl2)
	{
		delegate.setMobSpawnOptions(bl, bl2);
	}
	
	@Override
	public void close() throws IOException
	{
		delegate.close();
	}
	
	@Override
	public BlockView getExistingChunk(int i, int j)
	{
		return delegate.getExistingChunk(i, j);
	}
	
	@Override
	public List<Entity> getEntities(Entity entity, Box box, Predicate<? super Entity> predicate)
	{
		return delegate.getEntities(entity, box, predicate);
	}
	
	@Override
	public <T extends Entity> List<T> getEntities(EntityType<T> entityType, Box box, Predicate<? super T> predicate)
	{
		return delegate.getEntities(entityType, box, predicate);
	}
	
	@Override
	public <T extends Entity> List<T> getEntities(Class<? extends T> var1, Box box, Predicate<? super T> predicate)
	{
		return delegate.getEntities(var1, box, predicate);
	}
	
	@Override
	public <T extends Entity> List<T> getEntitiesIncludingUngeneratedChunks(Class<? extends T> var1, Box box, Predicate<? super T> predicate)
	{
		return delegate.getEntitiesIncludingUngeneratedChunks(var1, box, predicate);
	}
	
	@Override
	public Entity getEntityById(int i)
	{
		return delegate.getEntityById(i);
	}
	
	@Override
	public void markDirty(BlockPos blockPos, BlockEntity blockEntity)
	{
		delegate.markDirty(blockPos, blockEntity);
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
	public int getReceivedStrongRedstonePower(BlockPos blockPos)
	{
		return delegate.getReceivedStrongRedstonePower(blockPos);
	}
	
	@Override
	public boolean isEmittingRedstonePower(BlockPos blockPos, Direction direction)
	{
		return delegate.isEmittingRedstonePower(blockPos, direction);
	}
	
	@Override
	public int getEmittedRedstonePower(BlockPos blockPos, Direction direction)
	{
		return delegate.getEmittedRedstonePower(blockPos, direction);
	}
	
	@Override
	public boolean isReceivingRedstonePower(BlockPos blockPos)
	{
		return delegate.isReceivingRedstonePower(blockPos);
	}
	
	@Override
	public int getReceivedRedstonePower(BlockPos blockPos)
	{
		return delegate.getReceivedRedstonePower(blockPos);
	}
	
	@Environment(EnvType.CLIENT)
	@Override
	public void disconnect()
	{
		delegate.disconnect();
	}
	
	@Override
	public void setTime(long l)
	{
		delegate.setTime(l);
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
	public void setTimeOfDay(long l)
	{
		delegate.setTimeOfDay(l);
	}
	
	@Override
	public BlockPos getSpawnPos()
	{
		return delegate.getSpawnPos();
	}
	
	@Override
	public void setSpawnPos(BlockPos blockPos)
	{
		delegate.setSpawnPos(blockPos);
	}
	
	@Override
	public boolean canPlayerModifyAt(PlayerEntity playerEntity, BlockPos blockPos)
	{
		return delegate.canPlayerModifyAt(playerEntity, blockPos);
	}
	
	@Override
	public void sendEntityStatus(Entity entity, byte b)
	{
		delegate.sendEntityStatus(entity, b);
	}
	
	@Override
	public ChunkManager getChunkManager()
	{
		return delegate.getChunkManager();
	}
	
	@Override
	public void addBlockAction(BlockPos blockPos, Block block, int i, int j)
	{
		delegate.addBlockAction(blockPos, block, i, j);
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
	public float getThunderGradient(float f)
	{
		return delegate.getThunderGradient(f);
	}
	
	@Environment(EnvType.CLIENT)
	@Override
	public void setThunderGradient(float f)
	{
		delegate.setThunderGradient(f);
	}
	
	@Override
	public float getRainGradient(float f)
	{
		return delegate.getRainGradient(f);
	}
	
	@Environment(EnvType.CLIENT)
	@Override
	public void setRainGradient(float f)
	{
		delegate.setRainGradient(f);
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
	public boolean hasRain(BlockPos blockPos)
	{
		return delegate.hasRain(blockPos);
	}
	
	@Override
	public boolean hasHighHumidity(BlockPos blockPos)
	{
		return delegate.hasHighHumidity(blockPos);
	}
	
	@Override
	public MapState getMapState(String string)
	{
		return delegate.getMapState(string);
	}
	
	@Override
	public void putMapState(MapState mapState)
	{
		delegate.putMapState(mapState);
	}
	
	@Override
	public int getNextMapId()
	{
		return delegate.getNextMapId();
	}
	
	@Override
	public void playGlobalEvent(int i, BlockPos blockPos, int j)
	{
		delegate.playGlobalEvent(i, blockPos, j);
	}
	
	@Override
	public int getEffectiveHeight()
	{
		return delegate.getEffectiveHeight();
	}
	
	@Override
	public CrashReportSection addDetailsToCrashReport(CrashReport crashReport)
	{
		return delegate.addDetailsToCrashReport(crashReport);
	}
	
	@Override
	public void setBlockBreakingInfo(int i, BlockPos blockPos, int j)
	{
		delegate.setBlockBreakingInfo(i, blockPos, j);
	}
	
	@Environment(EnvType.CLIENT)
	@Override
	public void addFireworkParticle(double d, double e, double f, double g, double h, double i, CompoundTag compoundTag)
	{
		delegate.addFireworkParticle(d, e, f, g, h, i, compoundTag);
	}
	
	@Override
	public Scoreboard getScoreboard()
	{
		return delegate.getScoreboard();
	}
	
	@Override
	public void updateHorizontalAdjacent(BlockPos blockPos, Block block)
	{
		delegate.updateHorizontalAdjacent(blockPos, block);
	}
	
	@Override
	public LocalDifficulty getLocalDifficulty(BlockPos blockPos)
	{
		return delegate.getLocalDifficulty(blockPos);
	}
	
	@Override
	public int getAmbientDarkness()
	{
		return delegate.getAmbientDarkness();
	}
	
	@Override
	public void setLightningTicksLeft(int i)
	{
		delegate.setLightningTicksLeft(i);
	}
	
	@Override
	public WorldBorder getWorldBorder()
	{
		return delegate.getWorldBorder();
	}
	
	@Override
	public void sendPacket(Packet<?> packet)
	{
		delegate.sendPacket(packet);
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
	public boolean testBlockState(BlockPos blockPos, Predicate<BlockState> predicate)
	{
		return delegate.testBlockState(blockPos, predicate);
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
	public BlockPos getRandomPosInChunk(int i, int j, int k, int l)
	{
		return delegate.getRandomPosInChunk(i, j, k, l);
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
	public BiomeAccess getBiomeAccess()
	{
		return delegate.getBiomeAccess();
	}
	
	@Override
	public WorldChunk getChunk(int i, int j)
	{
		return delegate.getChunk(i, j);
	}
}
