package virtuoel.towelette.api;

import net.minecraft.state.State;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

@FunctionalInterface
public interface LightUpdatePredicate<S extends State<S>>
{
	boolean test(BlockView world, BlockPos pos, S newState, S oldState);
}
