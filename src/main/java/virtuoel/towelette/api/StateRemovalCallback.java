package virtuoel.towelette.api;

import net.minecraft.state.State;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@FunctionalInterface
public interface StateRemovalCallback<S extends State<S>>
{
	void onStateRemoved(S oldState, World world, BlockPos pos, S state, boolean pushed);
}
