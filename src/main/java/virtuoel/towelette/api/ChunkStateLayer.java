package virtuoel.towelette.api;

import javax.annotation.Nullable;

import net.minecraft.state.State;
import net.minecraft.util.math.BlockPos;

public interface ChunkStateLayer
{
	@Nullable
	<O, S extends State<S>> S setState(LayerData<O, S> layer, BlockPos pos, S state, boolean pushed);
	
	default <O, S extends State<S>> S getState(LayerData<O, S> layer, BlockPos pos)
	{
		return getState(layer, pos.getX(), pos.getY(), pos.getZ());
	}
	
	<O, S extends State<S>> S getState(LayerData<O, S> layer, int x, int y, int z);
}
