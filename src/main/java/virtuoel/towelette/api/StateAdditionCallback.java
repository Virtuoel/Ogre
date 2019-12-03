package virtuoel.towelette.api;

import net.minecraft.state.State;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@FunctionalInterface
public interface StateAdditionCallback<S extends State<S>>
{
	void onStateAdded(S state, World world, BlockPos pos, S oldState, boolean pushed);
}
