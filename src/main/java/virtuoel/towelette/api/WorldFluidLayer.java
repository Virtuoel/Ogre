package virtuoel.towelette.api;

import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;

public interface WorldFluidLayer
{
	default FluidState setFluidState(BlockPos pos, FluidState state)
	{
		return setFluidState(pos, state, 3);
	}
	
	FluidState setFluidState(BlockPos pos, FluidState state, int flags);
}
