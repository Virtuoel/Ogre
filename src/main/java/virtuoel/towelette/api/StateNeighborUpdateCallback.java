package virtuoel.towelette.api;

import net.minecraft.state.State;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@FunctionalInterface
public interface StateNeighborUpdateCallback<S extends State<S>>
{
	void onNeighborUpdate(S state, World world, BlockPos pos, S otherState, BlockPos otherPos, boolean pushed);
}
