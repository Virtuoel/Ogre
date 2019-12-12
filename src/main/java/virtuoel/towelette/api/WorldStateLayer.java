package virtuoel.towelette.api;

import net.minecraft.state.State;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.level.LevelGeneratorType;

public interface WorldStateLayer extends IWorldStateLayer
{
	static final Direction[] UPDATE_ORDER = new Direction[] { Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH, Direction.DOWN, Direction.UP };
	
	@Override
	default <O, S extends State<S>> void updateNeighbors(LayerData<O, S> layer, BlockPos pos, S oldState)
	{
		if (((IWorld) this).getLevelProperties().getGeneratorType() != LevelGeneratorType.DEBUG_ALL_BLOCK_STATES)
		{
			updateNeighborsAlways(layer, pos, oldState);
		}
	}
	
	default <O, S extends State<S>> void updateNeighborsAlways(LayerData<O, S> layer, BlockPos pos, S oldState)
	{
		for(final Direction dir : UPDATE_ORDER)
		{
			updateNeighbor(layer, pos.offset(dir), pos, oldState);
		}
	}
	
	default <O, S extends State<S>> void updateNeighborsExcept(LayerData<O, S> layer, BlockPos pos, Direction direction, S oldState)
	{
		for(final Direction dir : UPDATE_ORDER)
		{
			if (dir != direction)
			{
				updateNeighbor(layer, pos.offset(dir), pos, oldState);
			}
		}
	}
	
	default <O, S extends State<S>> void updateNeighbor(LayerData<O, S> layer, BlockPos pos, BlockPos otherPos, S oldState)
	{
		if (!((WorldView) this).isClient())
		{
			layer.onNeighborUpdate(getState(layer, pos), (World) this, pos, oldState, otherPos, false);
			updateNeighborExceptLayer(layer, pos, otherPos);
		}
	}
	
	@SuppressWarnings("unchecked")
	default <O, S extends State<S>> void updateNeighborExceptLayer(LayerData<O, S> exceptLayer, BlockPos pos, BlockPos otherPos)
	{
		final Identifier exceptId = LayerRegistrar.LAYERS.getId(exceptLayer);
		for (final Identifier id : LayerRegistrar.LAYERS.getIds())
		{
			if (id.equals(exceptId))
			{
				continue;
			}
			
			@SuppressWarnings("rawtypes")
			final LayerData layer = LayerRegistrar.LAYERS.get(id);
			
			layer.onNeighborUpdate(getState(layer, pos), (World) this, pos, getState(layer, otherPos), otherPos, false);
		}
	}
}
