package virtuoel.towelette.api;

import net.minecraft.state.State;
import net.minecraft.util.math.BlockPos;

public interface StateUpdateableChunkManager
{
	<O, S extends State<S>> void onStateUpdate(LayerData<O, S> layer, BlockPos pos);
}
