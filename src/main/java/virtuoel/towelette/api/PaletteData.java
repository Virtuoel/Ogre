package virtuoel.towelette.api;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.state.PropertyContainer;
import net.minecraft.state.StateFactory;
import net.minecraft.util.IdList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.BlockView;
import net.minecraft.world.chunk.Palette;
import net.minecraft.world.chunk.PalettedContainer;

public class PaletteData<O, S extends PropertyContainer<S>>
{
	final Palette<S> palette;
	final IdList<S> ids;
	final Function<CompoundTag, S> deserializer;
	final Function<S, CompoundTag> serializer;
	
	final Predicate<S> emptyPredicate;
	final Supplier<S> invalidPositionSupplier;
	final LightUpdatePredicate<S> lightUpdatePredicate;
	
	final Registry<O> entryRegistry;
	final Function<S, O> entryFunction;
	final Function<O, S> defaultStateFunction;
	final Function<O, StateFactory<O, S>> managerFunction;
	final Supplier<S> emptyStateSupplier;
	
	public PaletteData(
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
		this.palette = palette;
		this.ids = ids;
		this.deserializer = deserializer;
		this.serializer = serializer;
		
		this.emptyPredicate = emptyPredicate;
		this.invalidPositionSupplier = invalidPositionSupplier;
		this.lightUpdatePredicate = lightUpdatePredicate;
		
		this.entryRegistry = entryRegistry;
		this.entryFunction = entryFunction;
		this.defaultStateFunction = defaultStateFunction;
		this.managerFunction = managerFunction;
		this.emptyStateSupplier = emptyStateSupplier;
	}
	
	public Palette<S> getPalette()
	{
		return palette;
	}
	
	public IdList<S> getIds()
	{
		return ids;
	}
	
	public PalettedContainer<S> createContainer()
	{
		return new PalettedContainer<S>(palette, ids, deserializer, serializer, emptyStateSupplier.get());
	}
	
	public boolean isEmpty(final S state)
	{
		return emptyPredicate.test(state);
	}
	
	public S getInvalidPositionState()
	{
		return invalidPositionSupplier.get();
	}
	
	public boolean shouldEnqueueLightUpdate(BlockView world, BlockPos pos, S newState, S oldState)
	{
		return lightUpdatePredicate.test(world, pos, newState, oldState);
	}
	
	public Registry<O> getRegistry()
	{
		return entryRegistry;
	}
	
	public O getEntry(final S state)
	{
		return entryFunction.apply(state);
	}
	
	public S getDefaultState(final O entry)
	{
		return defaultStateFunction.apply(entry);
	}
	
	public StateFactory<O, S> getManager(final S state)
	{
		return getManager(getEntry(state));
	}
	
	public StateFactory<O, S> getManager(final O entry)
	{
		return managerFunction.apply(entry);
	}
	
	public S getEmptyState()
	{
		return emptyStateSupplier.get();
	}
}
