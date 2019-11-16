package virtuoel.towelette.api;

import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableMap;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.state.PropertyContainer;
import net.minecraft.state.StateFactory;
import net.minecraft.state.property.Property;
import net.minecraft.util.IdList;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.DefaultedRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.BlockView;
import net.minecraft.world.chunk.IdListPalette;
import net.minecraft.world.chunk.Palette;
import virtuoel.towelette.Towelette;

public class PaletteRegistrar
{
	public static final DefaultedRegistry<PaletteData<?, ?>> PALETTES = Registry.REGISTRIES.add(Towelette.id("palettes"), new DefaultedRegistry<PaletteData<?, ?>>(ToweletteApi.MOD_ID + ":block_state"));
	
	public static final Identifier BLOCK_STATE = PALETTES.getDefaultId();
	public static final Identifier FLUID_STATE = new Identifier(ToweletteApi.MOD_ID, "fluid_state");
	
	public static <O, S extends PropertyContainer<S>> void registerPaletteData(
		final Identifier id,
		
		final IdList<S> ids,
		final Function<CompoundTag, S> deserializer,
		final Function<S, CompoundTag> serializer,
		
		final Predicate<S> emptyPredicate,
		final Supplier<S> invalidPositionSupplier,
		final LightUpdatePredicate<S> lightUpdatePredicate,
		
		final Registry<O> entryRegistry,
		final Function<S, O> entryFunction,
		final Function<O, S> defaultStateFunction,
		final Function<O, StateFactory<O, S>> managerFunction,
		final Supplier<S> emptyStateSupplier
	)
	{
		registerPaletteData(id, new IdListPalette<S>(ids, emptyStateSupplier.get()), ids, deserializer, serializer, emptyPredicate, invalidPositionSupplier, lightUpdatePredicate, entryRegistry, entryFunction, defaultStateFunction, managerFunction, emptyStateSupplier);
	}
	
	public static <O, S extends PropertyContainer<S>> void registerPaletteData(
		final Identifier id,
		
		final Palette<S> palette,
		
		final IdList<S> ids,
		final Function<CompoundTag, S> deserializer,
		final Function<S, CompoundTag> serializer,
		
		final Predicate<S> emptyPredicate,
		final Supplier<S> invalidPositionSupplier,
		final LightUpdatePredicate<S> lightUpdatePredicate,
		
		final Registry<O> entryRegistry,
		final Function<S, O> entryFunction,
		final Function<O, S> defaultStateFunction,
		final Function<O, StateFactory<O, S>> managerFunction,
		final Supplier<S> emptyStateSupplier
	)
	{
		final PaletteData<O, S> data = new PaletteData<>(palette, ids, deserializer, serializer, emptyPredicate, invalidPositionSupplier, lightUpdatePredicate, entryRegistry, entryFunction, defaultStateFunction, managerFunction, emptyStateSupplier);
		PALETTES.add(id, data);
	}
	
	@SuppressWarnings("unchecked")
	public static <O, S extends PropertyContainer<S>> PaletteData<O, S> getPaletteData(final Identifier id)
	{
		return (PaletteData<O, S>) (Object) PaletteRegistrar.PALETTES.get(id);
	}
	
	@SuppressWarnings("unchecked")
	public static <O, S extends PropertyContainer<S>> PaletteData<O, S> getPaletteData(final int id)
	{
		return (PaletteData<O, S>) (Object) PaletteRegistrar.PALETTES.get(id);
	}
	
	public static boolean shouldUpdateBlockStateLight(BlockView world, BlockPos pos, BlockState newState, BlockState oldState)
	{
		return newState != oldState && (newState.getLightSubtracted(world, pos) != oldState.getLightSubtracted(world, pos) || newState.getLuminance() != oldState.getLuminance() || newState.hasSidedTransparency() || oldState.hasSidedTransparency());
	}
	
	public static boolean shouldUpdateFluidStateLight(BlockView world, BlockPos pos, FluidState newState, FluidState oldState)
	{
		return newState != oldState && (newState.getBlockState().getLightSubtracted(world, pos) != oldState.getBlockState().getLightSubtracted(world, pos) || newState.getBlockState().getLuminance() != oldState.getBlockState().getLuminance() || newState.getBlockState().hasSidedTransparency() || oldState.getBlockState().hasSidedTransparency());
	}
	
	public static BlockState deserializeBlockState(CompoundTag compound)
	{
		return PaletteRegistrar.deserializeState(compound, Registry.BLOCK, Block::getDefaultState, Block::getStateFactory);
	}
	
	public static FluidState deserializeFluidState(CompoundTag compound)
	{
		return PaletteRegistrar.deserializeState(compound, Registry.FLUID, Fluid::getDefaultState, Fluid::getStateFactory);
	}
	
	public static <O, S extends PropertyContainer<S>> S deserializeState(CompoundTag compound, DefaultedRegistry<O> registry, Function<O, S> defaultStateFunc, Function<O, StateFactory<O, S>> stateManagerFunc)
	{
		final O entry = registry.get(compound.containsKey("Name", 8) ? new Identifier(compound.getString("Name")) : registry.getDefaultId());
		S container = defaultStateFunc.apply(entry);
		
		if(compound.containsKey("Properties", 10))
		{
			final CompoundTag properties = compound.getCompound("Properties");
			final StateFactory<O, S> stateFactory = stateManagerFunc.apply(entry);
			
			for(String key : properties.getKeys())
			{
				Property<?> property = stateFactory.getProperty(key);
				if(property != null)
				{
					container = withProperty(container, property, key, properties, compound);
				}
			}
		}
		
		return container;
	}
	
	private static <S extends PropertyContainer<S>, T extends Comparable<T>> S withProperty(S container, Property<T> property, String key, CompoundTag properties, CompoundTag compound)
	{
		return property.getValue(properties.getString(key)).map(v -> container.with(property, v)).orElseGet(() ->
		{
			Towelette.LOGGER.warn("Unable to read property: {} with value: {} for property container: {}", key, properties.getString(key), compound.toString());
			return container;
		});
	}
	
	public static CompoundTag serializeBlockState(BlockState state)
	{
		return PaletteRegistrar.serializeState(state, Registry.BLOCK, BlockState::getBlock);
	}
	
	public static CompoundTag serializeFluidState(FluidState state)
	{
		return PaletteRegistrar.serializeState(state, Registry.FLUID, FluidState::getFluid);
	}
	
	public static <O, S extends PropertyContainer<S>> CompoundTag serializeState(S state, Registry<O> registry, Function<S, O> entryFunc)
	{
		final CompoundTag stateCompound = new CompoundTag();
		stateCompound.putString("Name", registry.getId(entryFunc.apply(state)).toString());
		final ImmutableMap<Property<?>, Comparable<?>> entries = state.getEntries();
		if(!entries.isEmpty())
		{
			final CompoundTag propertyCompound = new CompoundTag();
			
			for(Entry<Property<?>, Comparable<?>> entry : entries.entrySet())
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
