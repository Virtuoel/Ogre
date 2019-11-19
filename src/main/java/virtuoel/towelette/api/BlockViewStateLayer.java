package virtuoel.towelette.api;

import net.minecraft.state.PropertyContainer;
import net.minecraft.util.math.BlockPos;

public interface BlockViewStateLayer
{
	<O, S extends PropertyContainer<S>> S getState(LayerData<O, S> layer, BlockPos pos);
}
