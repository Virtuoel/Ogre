package virtuoel.towelette.api;

import java.util.Map.Entry;
import java.util.function.Function;
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
import net.minecraft.util.Lazy;
import net.minecraft.util.registry.DefaultedRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.chunk.IdListPalette;
import net.minecraft.world.chunk.Palette;
import net.minecraft.world.chunk.PalettedContainer;
import virtuoel.towelette.Towelette;

public class PaletteRegistrar
{
	public static final DefaultedRegistry<Palette<?>> PALETTES = Registry.REGISTRIES.add(new Identifier(ToweletteApi.MOD_ID, "palettes"), new DefaultedRegistry<Palette<?>>(ToweletteApi.MOD_ID + ":block_states"));
	public static final DefaultedRegistry<Supplier<PalettedContainer<?>>> PALETTED_CONTAINER_BUILDERS = Registry.REGISTRIES.add(new Identifier(ToweletteApi.MOD_ID, "paletted_container_builders"), new DefaultedRegistry<Supplier<PalettedContainer<?>>>(ToweletteApi.MOD_ID + ":block_states"));
	
	@SuppressWarnings("unchecked")
	public static final Lazy<Palette<BlockState>> BLOCK_STATES = new Lazy<>(() -> (Palette<BlockState>) PALETTES.get(new Identifier(ToweletteApi.MOD_ID, "block_states")));
	@SuppressWarnings("unchecked")
	public static final Lazy<Palette<FluidState>> FLUID_STATES = new Lazy<>(() -> (Palette<FluidState>) PALETTES.get(new Identifier(ToweletteApi.MOD_ID, "fluid_states")));
	
	public static <S> void registerPaletteBuilder(final Identifier id, final IdList<S> ids, final Function<CompoundTag, S> deserializer, final Function<S, CompoundTag> serializer, final S defaultEntry)
	{
		registerPaletteBuilder(PALETTES.add(id, new IdListPalette<S>(ids, defaultEntry)), ids, deserializer, serializer, defaultEntry);
	}
	
	public static <S> void registerPaletteBuilder(final Palette<S> palette, final IdList<S> ids, final Function<CompoundTag, S> deserializer, final Function<S, CompoundTag> serializer, final S defaultEntry)
	{
		PALETTED_CONTAINER_BUILDERS.add(PALETTES.getId(palette), () -> new PalettedContainer<S>(palette, ids, deserializer, serializer, defaultEntry));
	}
	
	@SuppressWarnings("unchecked")
	public static <S> Supplier<PalettedContainer<S>> getBuilder(final Lazy<Palette<S>> palette)
	{
		return (Supplier<PalettedContainer<S>>) (Object) PaletteRegistrar.PALETTED_CONTAINER_BUILDERS.get(PaletteRegistrar.PALETTES.getId(palette.get()));
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
	
	public static final PaletteRegistrar INSTANCE = new PaletteRegistrar();
	
	private PaletteRegistrar()
	{
		
	}
}
