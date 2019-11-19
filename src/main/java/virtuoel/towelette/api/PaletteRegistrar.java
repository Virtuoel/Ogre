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
import virtuoel.towelette.Towelette;
import virtuoel.towelette.mixin.layer.ChunkSectionAccessor;
import virtuoel.towelette.util.PaletteUtils;

public class PaletteRegistrar
{
	public static final DefaultedRegistry<PaletteData<?, ?>> PALETTES = Registry.REGISTRIES.add(Towelette.id("palettes"), new DefaultedRegistry<PaletteData<?, ?>>(ToweletteApi.MOD_ID + ":block_state"));
	
	public static final Palette<BlockState> BLOCK_PALETTE;
	public static final Palette<FluidState> FLUID_PALETTE;
	
	public static final PaletteData<Block, BlockState> BLOCKS;
	public static final PaletteData<Fluid, FluidState> FLUIDS;
	
	static
	{
		Reflection.initialize(ChunkSection.class);
		
		BLOCK_PALETTE = ChunkSectionAccessor.getBlockStatePalette();
		FLUID_PALETTE = new IdListPalette<>(Fluid.STATE_IDS, Fluids.EMPTY.getDefaultState());
		
		BLOCKS = registerBlockLayer(PALETTES.getDefaultId());
		FLUIDS = registerFluidLayer(new Identifier(ToweletteApi.MOD_ID, "fluid_state"));
	}
	
	public static PaletteData<Block, BlockState> registerBlockLayer(final Identifier id)
	{
		return PaletteRegistrar.PALETTES.add(id,
			PaletteData.<Block, BlockState>builder()
			.palette(PaletteRegistrar.BLOCK_PALETTE)
			.ids(Block.STATE_IDS)
			.emptyPredicate(BlockState::isAir)
			.invalidPositionSupplier(Blocks.VOID_AIR::getDefaultState)
			.lightUpdatePredicate(PaletteUtils::shouldUpdateBlockStateLight)
			.heightmapCallback(PaletteUtils::blockStateHeightmapUpdate)
			.stateAdditionCallback(PaletteUtils::onBlockStateAdded)
			.stateNeighborUpdateCallback(PaletteUtils::onBlockStateNeighborUpdate)
			.registry(Registry.BLOCK)
			.entryFunction(BlockState::getBlock)
			.defaultStateFunction(Block::getDefaultState)
			.managerFunction(Block::getStateFactory)
			.emptyStateSupplier(Blocks.AIR::getDefaultState)
			.defaultIdFunction(Registry.BLOCK::getDefaultId)
			.occlusionGraphCallback(PaletteUtils.Client::handleBlockStateOcclusionGraph)
			.renderPredicate(PaletteUtils.Client::shouldRenderBlockState)
			.renderLayerFunction(PaletteUtils.Client::getBlockStateRenderLayer)
			.tesselationCallback(PaletteUtils.Client::tesselateBlockState)
			.build()
		);
	}
	
	public static PaletteData<Fluid, FluidState> registerFluidLayer(final Identifier id)
	{
		return PaletteRegistrar.PALETTES.add(id,
			PaletteData.<Fluid, FluidState>builder()
			.palette(PaletteRegistrar.FLUID_PALETTE)
			.ids(Fluid.STATE_IDS)
			.emptyPredicate(FluidState::isEmpty)
			.lightUpdatePredicate(PaletteUtils::shouldUpdateFluidStateLight)
			.stateAdditionCallback(PaletteUtils::onFluidStateAdded)
			.stateNeighborUpdateCallback(PaletteUtils::onFluidStateNeighborUpdate)
			.registry(Registry.FLUID)
			.entryFunction(FluidState::getFluid)
			.defaultStateFunction(Fluid::getDefaultState)
			.managerFunction(Fluid::getStateFactory)
			.emptyStateSupplier(Fluids.EMPTY::getDefaultState)
			.defaultIdFunction(Registry.FLUID::getDefaultId)
			.renderPredicate(PaletteUtils.Client::shouldRenderFluidState)
			.renderLayerFunction(PaletteUtils.Client::getFluidStateRenderLayer)
			.tesselationCallback(PaletteUtils.Client::tesselateFluidState)
			.build()
		);
	}
	
	@SuppressWarnings("unchecked")
	public static <O, S extends PropertyContainer<S>> PaletteData<O, S> getPaletteData(final Identifier id)
	{
		return (PaletteData<O, S>) PaletteRegistrar.PALETTES.get(id);
	}
	
	@SuppressWarnings("unchecked")
	public static <O, S extends PropertyContainer<S>> PaletteData<O, S> getPaletteData(final int id)
	{
		return (PaletteData<O, S>) PaletteRegistrar.PALETTES.get(id);
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
