package virtuoel.towelette.api;

import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface UpdateableFluid
{
	default void onFluidAdded(FluidState state, World world, BlockPos pos, FluidState oldState)
	{
		
	}
	
	default void neighborUpdate(FluidState state, World world, BlockPos pos, BlockPos otherPos)
	{
		
	}
}
