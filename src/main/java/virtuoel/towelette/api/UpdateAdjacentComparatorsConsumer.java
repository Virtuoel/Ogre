package virtuoel.towelette.api;

import net.minecraft.state.PropertyContainer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@FunctionalInterface
public interface UpdateAdjacentComparatorsConsumer<S extends PropertyContainer<S>>
{
	void updateAdjacentComparators(World world, BlockPos pos, S state, S oldState);
}
