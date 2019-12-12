package virtuoel.towelette.api;

import net.minecraft.state.State;
import net.minecraft.util.math.BlockPos;

public interface IWorldStateLayer extends ModifiableWorldStateLayer
{
	default <O, S extends State<S>> void updateNeighbors(LayerData<O, S> layer, BlockPos pos, S oldState)
	{
		
	}
}
