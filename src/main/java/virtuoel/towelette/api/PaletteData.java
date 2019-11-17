package virtuoel.towelette.api;

import java.util.Optional;
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
import net.minecraft.world.chunk.IdListPalette;
import net.minecraft.world.chunk.Palette;
import net.minecraft.world.chunk.PalettedContainer;

public class PaletteData<O, S extends PropertyContainer<S>>
{
	private final Palette<S> palette;
	private final IdList<S> ids;
	private final Function<CompoundTag, S> deserializer;
	private final Function<S, CompoundTag> serializer;
	
	private final Predicate<S> emptyPredicate;
	private final Supplier<S> invalidPositionSupplier;
	private final LightUpdatePredicate<S> lightUpdatePredicate;
	
	private final Registry<O> entryRegistry;
	private final Function<S, O> entryFunction;
	private final Function<O, S> defaultStateFunction;
	private final Function<O, StateFactory<O, S>> managerFunction;
	private final Supplier<S> emptyStateSupplier;
	
	private PaletteData(
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
		return new PalettedContainer<S>(palette, ids, deserializer, serializer, getEmptyState());
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
	
	public static <O, S extends PropertyContainer<S>> Builder<O, S>builder()
	{
		return new Builder<>();
	}
	
	public static class Builder<O, S extends PropertyContainer<S>>
	{
		private Optional<Palette<S>> palette = Optional.empty();
		private IdList<S> ids;
		private Function<CompoundTag, S> deserializer;
		private Function<S, CompoundTag> serializer;
		
		private Predicate<S> emptyPredicate;
		private Optional<Supplier<S>> invalidPositionSupplier = Optional.empty();
		private LightUpdatePredicate<S> lightUpdatePredicate;
		
		private Registry<O> entryRegistry;
		private Function<S, O> entryFunction;
		private Function<O, S> defaultStateFunction;
		private Function<O, StateFactory<O, S>> managerFunction;
		private Supplier<S> emptyStateSupplier;
		
		private Builder()
		{
			
		}
		
		public Builder<O, S> palette(Palette<S> palette)
		{
			this.palette = Optional.of(palette);
			return this;
		}
		
		public Builder<O, S> ids(IdList<S> ids)
		{
			this.ids = ids;
			return this;
		}
		
		public Builder<O, S> deserializer(Function<CompoundTag, S> deserializer)
		{
			this.deserializer = deserializer;
			return this;
		}
		
		public Builder<O, S> serializer(Function<S, CompoundTag> serializer)
		{
			this.serializer = serializer;
			return this;
		}
		
		public Builder<O, S> emptyPredicate(Predicate<S> emptyPredicate)
		{
			this.emptyPredicate = emptyPredicate;
			return this;
		}
		
		public Builder<O, S> invalidPositionSupplier(Supplier<S> invalidPositionSupplier)
		{
			this.invalidPositionSupplier = Optional.of(invalidPositionSupplier);
			return this;
		}
		
		public Builder<O, S> lightUpdatePredicate(LightUpdatePredicate<S> lightUpdatePredicate)
		{
			this.lightUpdatePredicate = lightUpdatePredicate;
			return this;
		}
		
		public Builder<O, S> registry(Registry<O> entryRegistry)
		{
			this.entryRegistry = entryRegistry;
			return this;
		}
		
		public Builder<O, S> entryFunction(Function<S, O> entryFunction)
		{
			this.entryFunction = entryFunction;
			return this;
		}
		
		public Builder<O, S> defaultStateFunction(Function<O, S> defaultStateFunction)
		{
			this.defaultStateFunction = defaultStateFunction;
			return this;
		}
		
		public Builder<O, S> managerFunction(Function<O, StateFactory<O, S>> managerFunction)
		{
			this.managerFunction = managerFunction;
			return this;
		}
		
		public Builder<O, S> emptyStateSupplier(Supplier<S> emptyStateSupplier)
		{
			this.emptyStateSupplier = emptyStateSupplier;
			return this;
		}
		
		public PaletteData<O, S> build()
		{
			return new PaletteData<>(
				palette.orElseGet(() -> new IdListPalette<S>(ids, emptyStateSupplier.get())),
				ids,
				deserializer,
				serializer,
				emptyPredicate,
				invalidPositionSupplier.orElse(emptyStateSupplier),
				lightUpdatePredicate,
				entryRegistry,
				entryFunction,
				defaultStateFunction,
				managerFunction,
				emptyStateSupplier
			);
		}
	}
}
