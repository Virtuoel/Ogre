package virtuoel.towelette.api;

import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;

public interface ModifiableWorldFluidLayer
{
	default boolean setFluidState(BlockPos pos, FluidState state)
	{
		return setFluidState(pos, state, 3);
	}
	
	default boolean setFluidState(BlockPos pos, FluidState state, int flags)
	{
		return false;
	}
	
	default boolean clearFluidState(BlockPos pos, boolean flag)
	{
		return setFluidState(pos, Fluids.EMPTY.getDefaultState(), 3 | (flag ? 64 : 0));
	}
}
