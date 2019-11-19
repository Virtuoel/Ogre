package virtuoel.towelette.api;

import javax.annotation.Nullable;

import net.minecraft.state.PropertyContainer;
import net.minecraft.util.math.BlockPos;

public interface ChunkStateLayer
{
	@Nullable
	<O, S extends PropertyContainer<S>> S setState(LayerData<O, S> layer, BlockPos pos, S state, boolean pushed);
	
	default <O, S extends PropertyContainer<S>> S getState(LayerData<O, S> layer, BlockPos pos)
	{
		return getState(layer, pos.getX(), pos.getY(), pos.getZ());
	}
	
	<O, S extends PropertyContainer<S>> S getState(LayerData<O, S> layer, int x, int y, int z);
}
