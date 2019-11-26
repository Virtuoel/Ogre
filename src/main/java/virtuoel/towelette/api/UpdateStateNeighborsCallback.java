package virtuoel.towelette.api;

import net.minecraft.state.PropertyContainer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@FunctionalInterface
public interface UpdateStateNeighborsCallback<S extends PropertyContainer<S>>
{
	void updateNeighbors(World world, BlockPos pos, S state, S oldState, int flags);
}
