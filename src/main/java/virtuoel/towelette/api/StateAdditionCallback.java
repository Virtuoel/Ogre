package virtuoel.towelette.api;

import net.minecraft.state.PropertyContainer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@FunctionalInterface
public interface StateAdditionCallback<S extends PropertyContainer<S>>
{
	void onStateAdded(S state, World world, BlockPos pos, S oldState, boolean pushed);
}
