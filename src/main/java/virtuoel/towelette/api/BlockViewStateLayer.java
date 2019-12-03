package virtuoel.towelette.api;

import net.minecraft.state.State;
import net.minecraft.util.math.BlockPos;

public interface BlockViewStateLayer
{
	<O, S extends State<S>> S getState(LayerData<O, S> layer, BlockPos pos);
}
