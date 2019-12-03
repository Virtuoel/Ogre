package virtuoel.towelette.api;

import java.util.Random;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.State;
import net.minecraft.util.math.BlockPos;

@FunctionalInterface
public interface RandomTickCallback<S extends State<S>>
{
	void randomTick(S state, ServerWorld world, BlockPos pos, Random random);
}
