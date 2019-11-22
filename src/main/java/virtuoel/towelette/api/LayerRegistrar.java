package virtuoel.towelette.api;

import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.Reflection;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
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
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.IdListPalette;
import net.minecraft.world.chunk.Palette;
import virtuoel.towelette.mixin.layer.ChunkSectionAccessor;
import virtuoel.towelette.util.LayerUtils;

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
		return LayerRegistrar.LAYERS.add(id,
			LayerData.<Block, BlockState>builder()
			.palette(LayerRegistrar.BLOCK_PALETTE)
			.ids(Block.STATE_IDS)
			.emptyPredicate(BlockState::isAir)
			.invalidPositionSupplier(Blocks.VOID_AIR::getDefaultState)
			.lightUpdatePredicate(LayerUtils::shouldUpdateBlockStateLight)
			.heightmapCallback(LayerUtils::blockStateHeightmapUpdate)
			.stateAdditionCallback(LayerUtils::onBlockStateAdded)
			.stateNeighborUpdateCallback(LayerUtils::onBlockStateNeighborUpdate)
			.updateNeighborStatesCallback(LayerUtils::updateNeighborBlockStates)
			
			.randomTickPredicate(BlockState::hasRandomTicks)
			.registry(Registry.BLOCK)
			.entryFunction(BlockState::getBlock)
			.defaultStateFunction(Block::getDefaultState)
			.managerFunction(Block::getStateFactory)
			.emptyStateSupplier(Blocks.AIR::getDefaultState)
			.defaultIdFunction(Registry.BLOCK::getDefaultId)
			.occlusionGraphCallback(LayerUtils.Client::handleBlockStateOcclusionGraph)
			.renderPredicate(LayerUtils.Client::shouldRenderBlockState)
			.renderLayerFunction(LayerUtils.Client::getBlockStateRenderLayer)
			.tesselationCallback(LayerUtils.Client::tesselateBlockState)
			.build()
		);
	}
	
	public static LayerData<Fluid, FluidState> registerFluidLayer(final Identifier id)
	{
		return LayerRegistrar.LAYERS.add(id,
			LayerData.<Fluid, FluidState>builder()
			.palette(LayerRegistrar.FLUID_PALETTE)
			.ids(Fluid.STATE_IDS)
			.emptyPredicate(FluidState::isEmpty)
			.lightUpdatePredicate(LayerUtils::shouldUpdateFluidStateLight)
			.stateAdditionCallback(LayerUtils::onFluidStateAdded)
			.stateNeighborUpdateCallback(LayerUtils::onFluidStateNeighborUpdate)
			.updateNeighborStatesCallback(LayerUtils::updateNeighborFluidStates)
			
			.randomTickPredicate(FluidState::hasRandomTicks)
			.registry(Registry.FLUID)
			.entryFunction(FluidState::getFluid)
			.defaultStateFunction(Fluid::getDefaultState)
			.managerFunction(Fluid::getStateFactory)
			.emptyStateSupplier(Fluids.EMPTY::getDefaultState)
			.defaultIdFunction(Registry.FLUID::getDefaultId)
			.renderPredicate(LayerUtils.Client::shouldRenderFluidState)
			.renderLayerFunction(LayerUtils.Client::getFluidStateRenderLayer)
			.tesselationCallback(LayerUtils.Client::tesselateFluidState)
			.build()
		);
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
		
		if(compound.containsKey("Properties", 10))
		{
			final CompoundTag properties = compound.getCompound("Properties");
			final StateFactory<O, S> stateFactory = stateManagerFunc.apply(entry);
			
			for(final String key : properties.getKeys())
			{
				final Property<?> property = stateFactory.getProperty(key);
				if(property != null)
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
		if(!entries.isEmpty())
		{
			final CompoundTag propertyCompound = new CompoundTag();
			
			for(final Entry<Property<?>, Comparable<?>> entry : entries.entrySet())
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
