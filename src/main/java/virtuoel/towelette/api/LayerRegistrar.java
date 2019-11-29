package virtuoel.towelette.api;

import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.Reflection;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.chunk.ChunkOcclusionGraphBuilder;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.state.PropertyContainer;
import net.minecraft.state.StateFactory;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.DefaultedRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.IdListPalette;
import net.minecraft.world.chunk.Palette;
import virtuoel.towelette.mixin.layer.ChunkSectionAccessor;

public class LayerRegistrar
{
	public static final DefaultedRegistry<LayerData<?, ?>> LAYERS = Registry.REGISTRIES.add(
		new Identifier(ToweletteApi.MOD_ID, "layers"),
		new DefaultedRegistry<LayerData<?, ?>>(ToweletteApi.MOD_ID + ":block_state")
	);
	
	public static final Palette<BlockState> BLOCK_PALETTE;
	public static final Palette<FluidState> FLUID_PALETTE;
	
	public static final LayerData<Block, BlockState> BLOCK;
	public static final LayerData<Fluid, FluidState> FLUID;
	
	static
	{
		Reflection.initialize(ChunkSection.class);
		
		BLOCK_PALETTE = ChunkSectionAccessor.getBlockStatePalette();
		FLUID_PALETTE = new IdListPalette<>(Fluid.STATE_IDS, Fluids.EMPTY.getDefaultState());
		
		BLOCK = registerBlockLayer(LAYERS.getDefaultId());
		FLUID = registerFluidLayer(new Identifier(ToweletteApi.MOD_ID, "fluid_state"));
	}
	
	public static LayerData<Block, BlockState> registerBlockLayer(final Identifier id)
	{
		return registerBlockLayer(id, null);
	}
	
	public static LayerData<Block, BlockState> registerBlockLayer(final Identifier id, @Nullable UnaryOperator<LayerData.Builder<Block, BlockState>> builderOperator)
	{
		final LayerData.Builder<Block, BlockState> builder =
			LayerData.<Block, BlockState>builder()
			.palette(LayerRegistrar.BLOCK_PALETTE)
			.ids(Block.STATE_IDS)
			.emptyPredicate(BlockState::isAir)
			.invalidPositionSupplier(Blocks.VOID_AIR::getDefaultState)
			.lightUpdatePredicate((world, pos, newState, oldState) ->
			{
				return newState != oldState && (newState.getLightSubtracted(world, pos) != oldState.getLightSubtracted(world, pos) || newState.getLuminance() != oldState.getLuminance() || newState.hasSidedTransparency() || oldState.hasSidedTransparency());
			})
			.heightmapCallback((chunk, x, y, z, state) ->
			{
				chunk.getHeightmap(Heightmap.Type.MOTION_BLOCKING).trackUpdate(x, y, z, state);
				chunk.getHeightmap(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES).trackUpdate(x, y, z, state);
				chunk.getHeightmap(Heightmap.Type.OCEAN_FLOOR).trackUpdate(x, y, z, state);
				chunk.getHeightmap(Heightmap.Type.WORLD_SURFACE).trackUpdate(x, y, z, state);
			})
			.stateAdditionCallback(BlockState::onBlockAdded)
			.stateNeighborUpdateCallback((state, world, pos, otherState, otherPos, pushed) ->
			{
				state.neighborUpdate(world, pos, otherState.getBlock(), otherPos, pushed);
			})
			.updateNeighborStatesCallback((world, pos, state, oldState, flags) ->
			{
				oldState.method_11637(world, pos, flags);
				state.updateNeighborStates(world, pos, flags);
				state.method_11637(world, pos, flags);
			})
			.updateAdjacentComparatorsCallback((world, pos, state, oldState) ->
			{
				if (state.hasComparatorOutput())
				{
					world.updateHorizontalAdjacent(pos, oldState.getBlock());
				}
			})
			.randomTickPredicate(BlockState::hasRandomTicks)
			.randomTickCallback(BlockState::onRandomTick)
			.entityCollisionCallback(BlockState::onEntityCollision)
			.registry(Registry.BLOCK)
			.ownerFunction(BlockState::getBlock)
			.defaultStateFunction(Block::getDefaultState)
			.managerFunction(Block::getStateFactory)
			.emptyStateSupplier(Blocks.AIR::getDefaultState)
			.defaultIdFunction(Registry.BLOCK::getDefaultId);
		
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT)
		{
			builder.occlusionGraphCallback((chunkOcclusionGraphBuilder, state, world, pos) ->
				{
					if (state.isFullOpaque(world, pos))
					{
						((ChunkOcclusionGraphBuilder) chunkOcclusionGraphBuilder).markClosed(pos);
					}
				})
				.renderPredicate(state -> state.getRenderType() != BlockRenderType.INVISIBLE)
				.renderLayerFunction(state -> state.getBlock().getRenderLayer())
				.tesselationCallback((blockRenderManager, state, pos, world, bufferBuilder, random) ->
				{
					return ((BlockRenderManager) blockRenderManager).tesselateBlock(state, pos, world, (BufferBuilder) bufferBuilder, random);
				});
		}
		
		return LayerRegistrar.LAYERS.add(id, (builderOperator == null ? builder : builderOperator.apply(builder)).build());
	}
	
	public static LayerData<Fluid, FluidState> registerFluidLayer(final Identifier id)
	{
		return registerFluidLayer(id, null);
	}
	
	public static LayerData<Fluid, FluidState> registerFluidLayer(final Identifier id, @Nullable UnaryOperator<LayerData.Builder<Fluid, FluidState>> builderOperator)
	{
		final LayerData.Builder<Fluid, FluidState> builder =
			LayerData.<Fluid, FluidState>builder()
			.palette(LayerRegistrar.FLUID_PALETTE)
			.ids(Fluid.STATE_IDS)
			.emptyPredicate(FluidState::isEmpty)
			.lightUpdatePredicate((world, pos, newState, oldState) ->
			{
				return newState != oldState && (newState.getBlockState().getLightSubtracted(world, pos) != oldState.getBlockState().getLightSubtracted(world, pos) || newState.getBlockState().getLuminance() != oldState.getBlockState().getLuminance() || newState.getBlockState().hasSidedTransparency() || oldState.getBlockState().hasSidedTransparency());
			})
			.stateAdditionCallback((state, world, pos, oldState, pushed) ->
			{
				((UpdateableFluid) state.getFluid()).onFluidAdded(state, world, pos, oldState);
			})
			.stateNeighborUpdateCallback((state, world, pos, otherState, otherPos, pushed) ->
			{
				((UpdateableFluid) state.getFluid()).neighborUpdate(state, world, pos, otherPos);
			})
			.updateNeighborStatesCallback((world, pos, state, oldState, flags) ->
			{
				((UpdateableFluid) state.getFluid()).updateNeighborStates(world, pos, state, flags);
			})
			.randomTickPredicate(FluidState::hasRandomTicks)
			.randomTickCallback(FluidState::onRandomTick)
			.entityCollisionCallback((state, world, pos, entity) ->
			{
				((CollidableFluid) state.getFluid()).onEntityCollision(state, world, pos, entity);
			})
			.registry(Registry.FLUID)
			.ownerFunction(FluidState::getFluid)
			.defaultStateFunction(Fluid::getDefaultState)
			.managerFunction(Fluid::getStateFactory)
			.emptyStateSupplier(Fluids.EMPTY::getDefaultState)
			.defaultIdFunction(Registry.FLUID::getDefaultId);
		
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT)
		{
			builder.renderPredicate(state -> !state.isEmpty())
				.renderLayerFunction(FluidState::getRenderLayer)
				.tesselationCallback((blockRenderManager, state, pos, world, bufferBuilder, random) ->
				{
					return ((BlockRenderManager) blockRenderManager).tesselateFluid(pos, world, (BufferBuilder) bufferBuilder, state);
				});
		}
		
		return LayerRegistrar.LAYERS.add(id, (builderOperator == null ? builder : builderOperator.apply(builder)).build());
	}
	
	@SuppressWarnings("unchecked")
	public static <O, S extends PropertyContainer<S>> LayerData<O, S> getLayerData(final Identifier id)
	{
		return (LayerData<O, S>) LayerRegistrar.LAYERS.get(id);
	}
	
	@SuppressWarnings("unchecked")
	public static <O, S extends PropertyContainer<S>> LayerData<O, S> getLayerData(final int id)
	{
		return (LayerData<O, S>) LayerRegistrar.LAYERS.get(id);
	}
	
	public static <O, S extends PropertyContainer<S>> S deserializeState(CompoundTag compound, Registry<O> registry, Supplier<Identifier> defaultIdSupplier, Function<O, S> defaultStateFunc, Function<O, StateFactory<O, S>> stateManagerFunc)
	{
		final O entry = registry.get(compound.containsKey("Name", 8) ? new Identifier(compound.getString("Name")) : defaultIdSupplier.get());
		S container = defaultStateFunc.apply(entry);
		
		if (compound.containsKey("Properties", 10))
		{
			final CompoundTag properties = compound.getCompound("Properties");
			final StateFactory<O, S> stateFactory = stateManagerFunc.apply(entry);
			
			for (final String key : properties.getKeys())
			{
				final Property<?> property = stateFactory.getProperty(key);
				if (property != null)
				{
					container = withProperty(container, property, key, properties, compound);
				}
			}
		}
		
		return container;
	}
	
	private static final Logger LOGGER = LogManager.getLogger(ToweletteApi.MOD_ID);
	
	private static <S extends PropertyContainer<S>, T extends Comparable<T>> S withProperty(S container, Property<T> property, String key, CompoundTag properties, CompoundTag compound)
	{
		return property.getValue(properties.getString(key)).map(v -> container.with(property, v)).orElseGet(() ->
		{
			LOGGER.warn("Unable to read property: {} with value: {} for property container: {}", key, properties.getString(key), compound.toString());
			return container;
		});
	}
	
	public static <O, S extends PropertyContainer<S>> CompoundTag serializeState(S state, Registry<O> registry, Function<S, O> entryFunc)
	{
		final CompoundTag stateCompound = new CompoundTag();
		stateCompound.putString("Name", registry.getId(entryFunc.apply(state)).toString());
		final ImmutableMap<Property<?>, Comparable<?>> entries = state.getEntries();
		if (!entries.isEmpty())
		{
			final CompoundTag propertyCompound = new CompoundTag();
			
			for (final Entry<Property<?>, Comparable<?>> entry : entries.entrySet())
			{
				@SuppressWarnings("rawtypes")
				final Property property = entry.getKey();
				@SuppressWarnings("unchecked")
				final String name = property.getName(entry.getValue());
				propertyCompound.putString(property.getName(), name);
			}
			
			stateCompound.put("Properties", propertyCompound);
		}
		
		return stateCompound;
	}
}
