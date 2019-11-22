package virtuoel.towelette.api;

import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public interface UpdateableFluid
{
	default void onFluidAdded(FluidState state, World world, BlockPos pos, FluidState oldState)
	{
		
	}
	
	default void neighborUpdate(FluidState state, World world, BlockPos pos, BlockPos otherPos)
	{
		
	}
	
	default FluidState getStateForNeighborUpdate(FluidState state, Direction direction, FluidState otherState, IWorld world, BlockPos blockPos, BlockPos otherPos)
	{
		return state;
	}
	
	default void updateNeighborStates(World world, BlockPos pos, FluidState state, int flags)
	{
		
	}
}
