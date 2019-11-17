package virtuoel.towelette.api;

import net.minecraft.state.PropertyContainer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@FunctionalInterface
public interface StateNeighborUpdateConsumer<O, S extends PropertyContainer<S>>
{
	void onNeighborUpdate(S state, World world, BlockPos pos, O other, BlockPos otherPos, boolean pushed);
}
