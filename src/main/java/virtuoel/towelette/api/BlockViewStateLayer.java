package virtuoel.towelette.api;

import net.minecraft.state.PropertyContainer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public interface BlockViewStateLayer<S extends PropertyContainer<S>>
{
	S getState(Identifier layer, BlockPos pos);
}
