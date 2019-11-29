package virtuoel.towelette.api;

import java.util.Random;

import net.minecraft.state.PropertyContainer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@FunctionalInterface
public interface RandomTickCallback<S extends PropertyContainer<S>>
{
	void onRandomTick(S state, World world, BlockPos pos, Random random);
}