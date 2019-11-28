package virtuoel.towelette.api;

import java.util.Optional;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import net.minecraft.block.BlockRenderLayer;
import net.minecraft.entity.Entity;
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

public class LayerData<O, S extends PropertyContainer<S>>
{
	private final Palette<S> palette;
	private final IdList<S> ids;
	
	private final Predicate<S> emptyPredicate;
	private final Supplier<S> invalidPositionSupplier;
	
	private final LightUpdatePredicate<S> lightUpdatePredicate;
	private final HeightmapUpdateCallback<S> heightmapCallback;
	
	private final StateAdditionCallback<S> stateAdditionCallback;
	private final StateNeighborUpdateCallback<S> stateNeighborUpdateCallback;
	private final UpdateStateNeighborsCallback<S> updateNeighborStatesCallback;
	private final UpdateAdjacentComparatorsCallback<S> updateAdjacentComparatorsCallback;
	
	private final Predicate<S> randomTickPredicate;
	private final RandomTickCallback<S> randomTickCallback;
	
	private final EntityCollisionCallback<S> entityCollisionCallback;
	
	private final Registry<O> entryRegistry;
	private final Function<S, O> ownerFunction;
	private final Function<O, S> defaultStateFunction;
	private final Function<O, StateFactory<O, S>> managerFunction;
	private final Supplier<S> emptyStateSupplier;
	private final Supplier<Identifier> defaultIdSupplier;
	
	private final BiFunction<World, LayerData<O, S>, World> worldFunction;
	
	private final OcclusionGraphCallback<S> occlusionGraphCallback;
	private final Predicate<S> renderPredicate;
	private final Function<S, BlockRenderLayer> renderLayerFunction;
	private final StateTesselationCallback<S> tesselationCallback;
	
	private LayerData(
		final Palette<S> palette,
		final IdList<S> ids,
		
		final Predicate<S> emptyPredicate,
		final Supplier<S> invalidPositionSupplier,
		
		final LightUpdatePredicate<S> lightUpdatePredicate,
		final HeightmapUpdateCallback<S> heightmapCallback,
		
		final StateAdditionCallback<S> stateAdditionCallback,
		final StateNeighborUpdateCallback<S> stateNeighborUpdateCallback,
		final UpdateStateNeighborsCallback<S> updateNeighborStatesCallback,
		final UpdateAdjacentComparatorsCallback<S> updateAdjacentComparatorsCallback,
		
		final Predicate<S> randomTickPredicate,
		final RandomTickCallback<S> randomTickCallback,
		
		final EntityCollisionCallback<S> entityCollisionCallback,
		
		final Registry<O> entryRegistry,
		final Function<S, O> ownerFunction,
		final Function<O, S> defaultStateFunction,
		final Function<O, StateFactory<O, S>> managerFunction,
		final Supplier<S> emptyStateSupplier,
		final Supplier<Identifier> defaultIdSupplier,
		
		final BiFunction<World, LayerData<O, S>, World> worldFunction,
		
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
		this.updateNeighborStatesCallback = updateNeighborStatesCallback;
		this.updateAdjacentComparatorsCallback = updateAdjacentComparatorsCallback;
		
		this.randomTickPredicate = randomTickPredicate;
		this.randomTickCallback = randomTickCallback;
		
		this.entityCollisionCallback = entityCollisionCallback;
		
		this.entryRegistry = entryRegistry;
		this.ownerFunction = ownerFunction;
		this.defaultStateFunction = defaultStateFunction;
		this.managerFunction = managerFunction;
		this.emptyStateSupplier = emptyStateSupplier;
		this.defaultIdSupplier = defaultIdSupplier;
		
		this.worldFunction = worldFunction;
		
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
		stateAdditionCallback.onStateAdded(state, worldFunction.apply(world, this), pos, oldState, pushed);
	}
	
	public void onNeighborUpdate(S state, World world, BlockPos pos, S otherState, BlockPos otherPos, boolean pushed)
	{
		stateNeighborUpdateCallback.onNeighborUpdate(state, worldFunction.apply(world, this), pos, otherState, otherPos, pushed);
	}
	
	public void updateNeighbors(World world, BlockPos pos, S state, S oldState, int flags)
	{
		updateNeighborStatesCallback.updateNeighbors(worldFunction.apply(world, this), pos, state, oldState, flags);
	}
	
	public void updateAdjacentComparators(World world, BlockPos pos, S state, S oldState)
	{
		updateAdjacentComparatorsCallback.updateAdjacentComparators(worldFunction.apply(world, this), pos, state, oldState);
	}
	
	public boolean hasRandomTicks(S state)
	{
		return randomTickPredicate.test(state);
	}
	
	public void onRandomTick(S state, World world, BlockPos pos, Random random)
	{
		randomTickCallback.onRandomTick(state, worldFunction.apply(world, this), pos, random);
	}
	
	public void onEntityCollision(S state, World world, BlockPos pos, Entity entity)
	{
		entityCollisionCallback.onEntityCollision(state, worldFunction.apply(world, this), pos, entity);
	}
	
	public Registry<O> getRegistry()
	{
		return entryRegistry;
	}
	
	public O getOwner(final S state)
	{
		return ownerFunction.apply(state);
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
		return getManager(getOwner(state));
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
		return LayerRegistrar.deserializeState(compound, getRegistry(), defaultIdSupplier, defaultStateFunction, managerFunction);
	}
	
	public CompoundTag serializeState(S state)
	{
		return LayerRegistrar.serializeState(state, getRegistry(), ownerFunction);
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
		private HeightmapUpdateCallback<S> heightmapCallback = (c, x, y, z, s) -> {};
		
		private StateAdditionCallback<S> stateAdditionCallback = (s, w, p, o, b) -> {};
		private StateNeighborUpdateCallback<S> stateNeighborUpdateCallback = (s, w, p, e, o, u) -> {};
		private UpdateStateNeighborsCallback<S> updateNeighborStatesCallback = (w, p, s, o, f) -> {};
		private UpdateAdjacentComparatorsCallback<S> updateAdjacentComparatorsCallback = (w, p, s, o) -> {};
		
		private Predicate<S> randomTickPredicate = s -> false;
		private RandomTickCallback<S> randomTickCallback = (s, w, p, r) -> {};
		
		private EntityCollisionCallback<S> entityCollisionCallback;
		
		private Registry<O> entryRegistry;
		private Function<S, O> ownerFunction;
		private Function<O, S> defaultStateFunction;
		private Function<O, StateFactory<O, S>> managerFunction;
		private Supplier<S> emptyStateSupplier;
		private Supplier<Identifier> defaultIdSupplier = () -> entryRegistry.getId(ownerFunction.apply(emptyStateSupplier.get()));
		
		private BiFunction<World, LayerData<O, S>, World> worldFunction = (w, l) -> w;
		
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
		
		public Builder<O, S> heightmapCallback(HeightmapUpdateCallback<S> heightmapCallback)
		{
			this.heightmapCallback = heightmapCallback;
			return this;
		}
		
		public Builder<O, S> stateAdditionCallback(StateAdditionCallback<S> stateAdditionCallback)
		{
			this.stateAdditionCallback = stateAdditionCallback;
			return this;
		}
		
		public Builder<O, S> stateNeighborUpdateCallback(StateNeighborUpdateCallback<S> stateNeighborUpdateCallback)
		{
			this.stateNeighborUpdateCallback = stateNeighborUpdateCallback;
			return this;
		}
		
		public Builder<O, S> updateNeighborStatesCallback(UpdateStateNeighborsCallback<S> updateNeighborStatesCallback)
		{
			this.updateNeighborStatesCallback = updateNeighborStatesCallback;
			return this;
		}
		
		public Builder<O, S> updateAdjacentComparatorsCallback(UpdateAdjacentComparatorsCallback<S> updateAdjacentComparatorsCallback)
		{
			this.updateAdjacentComparatorsCallback = updateAdjacentComparatorsCallback;
			return this;
		}
		
		public Builder<O, S> randomTickPredicate(Predicate<S> randomTickPredicate)
		{
			this.randomTickPredicate = randomTickPredicate;
			return this;
		}
		
		public Builder<O, S> randomTickCallback(RandomTickCallback<S> randomTickCallback)
		{
			this.randomTickCallback = randomTickCallback;
			return this;
		}
		
		public Builder<O, S> entityCollisionCallback(EntityCollisionCallback<S> entityCollisionCallback)
		{
			this.entityCollisionCallback = entityCollisionCallback;
			return this;
		}
		
		public Builder<O, S> registry(Registry<O> entryRegistry)
		{
			this.entryRegistry = entryRegistry;
			return this;
		}
		
		public Builder<O, S> ownerFunction(Function<S, O> ownerFunction)
		{
			this.ownerFunction = ownerFunction;
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
		
		public Builder<O, S> worldFunction(BiFunction<World, LayerData<O, S>, World> worldFunction)
		{
			this.worldFunction = worldFunction;
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
		
		public LayerData<O, S> build()
		{
			return new LayerData<>(
				palette,
				ids,
				
				emptyPredicate,
				invalidPositionSupplier.orElse(emptyStateSupplier),
				
				lightUpdatePredicate,
				heightmapCallback,
				
				stateAdditionCallback,
				stateNeighborUpdateCallback,
				updateNeighborStatesCallback,
				updateAdjacentComparatorsCallback,
				
				randomTickPredicate,
				randomTickCallback,
				
				entityCollisionCallback,
				
				entryRegistry,
				ownerFunction,
				defaultStateFunction,
				managerFunction,
				emptyStateSupplier,
				defaultIdSupplier,
				
				worldFunction,
				
				occlusionGraphCallback,
				renderPredicate.orElse(emptyPredicate),
				renderLayerFunction,
				tesselationCallback
			);
		}
	}
}
