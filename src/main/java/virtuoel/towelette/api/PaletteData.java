package virtuoel.towelette.api;

import java.util.Optional;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import net.minecraft.block.BlockRenderLayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.state.PropertyContainer;
import net.minecraft.state.StateFactory;
import net.minecraft.util.IdList;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.BlockView;
import net.minecraft.world.ExtendedBlockView;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.Palette;
import net.minecraft.world.chunk.PalettedContainer;

public class PaletteData<O, S extends PropertyContainer<S>>
{
	private final Palette<S> palette;
	private final IdList<S> ids;
	
	private final Predicate<S> emptyPredicate;
	private final Supplier<S> invalidPositionSupplier;
	private final LightUpdatePredicate<S> lightUpdatePredicate;
	private final HeightmapUpdateConsumer<S> heightmapCallback;
	private final StateAdditionConsumer<S> stateAdditionCallback;
	private final StateNeighborUpdateConsumer<O, S> stateNeighborUpdateCallback;
	
	private final Registry<O> entryRegistry;
	private final Function<S, O> entryFunction;
	private final Function<O, S> defaultStateFunction;
	private final Function<O, StateFactory<O, S>> managerFunction;
	private final Supplier<S> emptyStateSupplier;
	private final Supplier<Identifier> defaultIdSupplier;
	
	private final OcclusionGraphCallback<S> occlusionGraphCallback;
	private final Predicate<S> renderPredicate;
	private final Function<S, BlockRenderLayer> renderLayerFunction;
	private final StateTesselationCallback<S> tesselationCallback;
	
	private PaletteData(
		final Palette<S> palette,
		final IdList<S> ids,
		
		final Predicate<S> emptyPredicate,
		final Supplier<S> invalidPositionSupplier,
		final LightUpdatePredicate<S> lightUpdatePredicate,
		final HeightmapUpdateConsumer<S> heightmapCallback,
		final StateAdditionConsumer<S> stateAdditionCallback,
		final StateNeighborUpdateConsumer<O, S> stateNeighborUpdateCallback,
		
		final Registry<O> entryRegistry,
		final Function<S, O> entryFunction,
		final Function<O, S> defaultStateFunction,
		final Function<O, StateFactory<O, S>> managerFunction,
		final Supplier<S> emptyStateSupplier,
		final Supplier<Identifier> defaultIdSupplier,
		
		final OcclusionGraphCallback<S> occlusionGraphCallback,
		final Predicate<S> renderPredicate,
		final Function<S, BlockRenderLayer> renderLayerFunction,
		final StateTesselationCallback<S> tesselationCallback
	)
	{
		this.palette = palette;
		this.ids = ids;
		
		this.emptyPredicate = emptyPredicate;
		this.invalidPositionSupplier = invalidPositionSupplier;
		this.lightUpdatePredicate = lightUpdatePredicate;
		this.heightmapCallback = heightmapCallback;
		this.stateAdditionCallback = stateAdditionCallback;
		this.stateNeighborUpdateCallback = stateNeighborUpdateCallback;
		
		this.entryRegistry = entryRegistry;
		this.entryFunction = entryFunction;
		this.defaultStateFunction = defaultStateFunction;
		this.managerFunction = managerFunction;
		this.emptyStateSupplier = emptyStateSupplier;
		this.defaultIdSupplier = defaultIdSupplier;
		
		this.occlusionGraphCallback = occlusionGraphCallback;
		this.renderPredicate = renderPredicate;
		this.renderLayerFunction = renderLayerFunction;
		this.tesselationCallback = tesselationCallback;
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
		return new PalettedContainer<S>(palette, ids, this::deserializeState, this::serializeState, getEmptyState());
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
	
	public void trackHeightmapUpdate(Chunk chunk, int x, int y, int z, S state)
	{
		heightmapCallback.trackHeightmapUpdate(chunk, x, y, z, state);
	}
	
	public void onStateAdded(S state, World world, BlockPos pos, S oldState, boolean pushed)
	{
		stateAdditionCallback.onStateAdded(state, world, pos, oldState, pushed);
	}
	
	public void onNeighborUpdate(S state, World world, BlockPos pos, O other, BlockPos otherPos, boolean pushed)
	{
		stateNeighborUpdateCallback.onNeighborUpdate(state, world, pos, other, otherPos, pushed);
	}
	
	public Registry<O> getRegistry()
	{
		return entryRegistry;
	}
	
	public O getEntry(final S state)
	{
		return entryFunction.apply(state);
	}
	
	public Optional<O> getEntryOrEmpty(final Identifier id)
	{
		return getRegistry().getOrEmpty(id);
	}
	
	public S getDefaultState(final O entry)
	{
		return defaultStateFunction.apply(entry);
	}
	
	public S getDefaultState(final Identifier id)
	{
		return getEntryOrEmpty(id).map(this::getDefaultState).orElseGet(emptyStateSupplier);
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
	
	public void handleOcclusionGraph(Object chunkOcclusionGraphBuilder, S state, BlockView world, BlockPos pos)
	{
		occlusionGraphCallback.handleOcclusionGraph(chunkOcclusionGraphBuilder, state, world, pos);
	}
	
	public boolean shouldRender(final S state)
	{
		return renderPredicate.test(state);
	}
	
	public BlockRenderLayer getRenderLayer(final S state)
	{
		return renderLayerFunction.apply(state);
	}
	
	public boolean tesselate(Object blockRenderManager, S state, BlockPos pos, ExtendedBlockView world, Object bufferBuilder, Random random)
	{
		return tesselationCallback.tesselateState(blockRenderManager, state, pos, world, bufferBuilder, random);
	}
	
	public S deserializeState(CompoundTag compound)
	{
		return PaletteRegistrar.deserializeState(compound, getRegistry(), defaultIdSupplier, defaultStateFunction, managerFunction);
	}
	
	public CompoundTag serializeState(S state)
	{
		return PaletteRegistrar.serializeState(state, getRegistry(), entryFunction);
	}
	
	public static <O, S extends PropertyContainer<S>> Builder<O, S>builder()
	{
		return new Builder<>();
	}
	
	public static class Builder<O, S extends PropertyContainer<S>>
	{
		private Palette<S> palette;
		private IdList<S> ids;
		
		private Predicate<S> emptyPredicate;
		private Optional<Supplier<S>> invalidPositionSupplier = Optional.empty();
		private LightUpdatePredicate<S> lightUpdatePredicate;
		private HeightmapUpdateConsumer<S> heightmapCallback = (c, x, y, z, s) -> {};
		private StateAdditionConsumer<S> stateAdditionCallback = (s, w, p, o, b) -> {};
		private StateNeighborUpdateConsumer<O, S> stateNeighborUpdateCallback = (s, w, p, e, o, u) -> {};
		
		private Registry<O> entryRegistry;
		private Function<S, O> entryFunction;
		private Function<O, S> defaultStateFunction;
		private Function<O, StateFactory<O, S>> managerFunction;
		private Supplier<S> emptyStateSupplier;
		private Supplier<Identifier> defaultIdSupplier = () -> entryRegistry.getId(entryFunction.apply(emptyStateSupplier.get()));
		
		private OcclusionGraphCallback<S> occlusionGraphCallback = (b, s, w, p) -> {};
		private Optional<Predicate<S>> renderPredicate = Optional.empty();
		private Function<S, BlockRenderLayer> renderLayerFunction;
		private StateTesselationCallback<S> tesselationCallback;
		
		private Builder()
		{
			
		}
		
		public Builder<O, S> palette(Palette<S> palette)
		{
			this.palette = palette;
			return this;
		}
		
		public Builder<O, S> ids(IdList<S> ids)
		{
			this.ids = ids;
			return this;
		}
		
		public Builder<O, S> emptyPredicate(Predicate<S> emptyPredicate)
		{
			this.emptyPredicate = emptyPredicate;
			return this;
		}
		
		public Builder<O, S> invalidPositionSupplier(Supplier<S> invalidPositionSupplier)
		{
			this.invalidPositionSupplier = Optional.ofNullable(invalidPositionSupplier);
			return this;
		}
		
		public Builder<O, S> lightUpdatePredicate(LightUpdatePredicate<S> lightUpdatePredicate)
		{
			this.lightUpdatePredicate = lightUpdatePredicate;
			return this;
		}
		
		public Builder<O, S> heightmapCallback(HeightmapUpdateConsumer<S> heightmapCallback)
		{
			this.heightmapCallback = heightmapCallback;
			return this;
		}
		
		public Builder<O, S> stateAdditionCallback(StateAdditionConsumer<S> stateAdditionCallback)
		{
			this.stateAdditionCallback = stateAdditionCallback;
			return this;
		}
		
		public Builder<O, S> stateNeighborUpdateCallback(StateNeighborUpdateConsumer<O, S> stateNeighborUpdateCallback)
		{
			this.stateNeighborUpdateCallback = stateNeighborUpdateCallback;
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
		
		public Builder<O, S> defaultIdFunction(Supplier<Identifier> defaultIdSupplier)
		{
			this.defaultIdSupplier = defaultIdSupplier;
			return this;
		}
		
		public Builder<O, S> occlusionGraphCallback(OcclusionGraphCallback<S> occlusionGraphCallback)
		{
			this.occlusionGraphCallback = occlusionGraphCallback;
			return this;
		}
		
		public Builder<O, S> renderPredicate(Predicate<S> renderPredicate)
		{
			this.renderPredicate = Optional.ofNullable(renderPredicate);
			return this;
		}
		
		public Builder<O, S> renderLayerFunction(Function<S, BlockRenderLayer> renderLayerFunction)
		{
			this.renderLayerFunction = renderLayerFunction;
			return this;
		}
		
		public Builder<O, S> tesselationCallback(StateTesselationCallback<S> tesselationCallback)
		{
			this.tesselationCallback = tesselationCallback;
			return this;
		}
		
		public PaletteData<O, S> build()
		{
			return new PaletteData<>(
				palette,
				ids,
				emptyPredicate,
				invalidPositionSupplier.orElse(emptyStateSupplier),
				lightUpdatePredicate,
				heightmapCallback,
				stateAdditionCallback,
				stateNeighborUpdateCallback,
				entryRegistry,
				entryFunction,
				defaultStateFunction,
				managerFunction,
				emptyStateSupplier,
				defaultIdSupplier,
				occlusionGraphCallback,
				renderPredicate.orElse(emptyPredicate),
				renderLayerFunction,
				tesselationCallback
			);
		}
	}
}
