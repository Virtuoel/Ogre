package virtuoel.towelette.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import virtuoel.towelette.api.CollidableFluid;
import virtuoel.towelette.api.LayerRegistrar;
import virtuoel.towelette.api.ModifiableWorldStateLayer;
import virtuoel.towelette.api.UpdateableFluid;

@Mixin(Fluid.class)
public class FluidMixin implements CollidableFluid, UpdateableFluid
{
	@Override
	public void onFluidAdded(FluidState state, World world, BlockPos pos, FluidState oldState)
	{
		if (!lavaTouchedWater(state, world, pos))
		{
			world.getFluidTickScheduler().schedule(pos, state.getFluid(), state.getFluid().getTickRate(world));
		}
	}
	
	@Override
	public void neighborUpdate(FluidState state, World world, BlockPos pos, BlockPos otherPos)
	{
		if (!lavaTouchedWater(state, world, pos))
		{
			world.getFluidTickScheduler().schedule(pos, state.getFluid(), state.getFluid().getTickRate(world));
		}
	}
	
	@Override
	public FluidState getStateForNeighborUpdate(FluidState state, Direction direction, FluidState otherState, IWorld world, BlockPos blockPos, BlockPos otherPos)
	{
		if (state.isStill() || otherState.isStill())
		{
			world.getFluidTickScheduler().schedule(blockPos, state.getFluid(), state.getFluid().getTickRate(world));
		}
		
		return UpdateableFluid.super.getStateForNeighborUpdate(state, direction, otherState, world, blockPos, otherPos);
	}
	
	@Unique private static final Direction[] UPDATE_ORDER = new Direction[] { Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH, Direction.DOWN, Direction.UP };
	
	@Override
	public void updateNeighborStates(World world, BlockPos pos, FluidState state, int flags)
	{
		try (final BlockPos.PooledMutable mutablePos = BlockPos.PooledMutable.get())
		{
			for (final Direction direction : UPDATE_ORDER)
			{
				mutablePos.set(pos).setOffset(direction);
				final FluidState fluidState = world.getFluidState(mutablePos);
				final FluidState newState = getStateForNeighborUpdate(fluidState, direction.getOpposite(), state, world, mutablePos, pos);
				if (newState != fluidState)
				{
					final ModifiableWorldStateLayer w = ((ModifiableWorldStateLayer) world);
					w.setState(LayerRegistrar.FLUID, mutablePos, newState, flags & -33);
				}
			}
		}
	}
	
	@Unique
	private static boolean lavaTouchedWater(FluidState state, World world, BlockPos pos)
	{
		if (state.matches(FluidTags.LAVA))
		{
			boolean touchingWater = false;
			for (final Direction dir : Direction.values())
			{
				if (dir != Direction.DOWN && world.getFluidState(pos.offset(dir)).matches(FluidTags.WATER))
				{
					touchingWater = true;
					break;
				}
			}
			
			if (touchingWater)
			{
				final ModifiableWorldStateLayer w = ((ModifiableWorldStateLayer) world);
				if (state.isStill())
				{
					w.setState(LayerRegistrar.FLUID, pos, Fluids.EMPTY.getDefaultState());
					world.setBlockState(pos, Blocks.OBSIDIAN.getDefaultState());
					world.playLevelEvent(1501, pos, 0);
					return true;
				}
				else if (state.getHeight(world, pos) >= 4.0F/9.0F)
				{
					w.setState(LayerRegistrar.FLUID, pos, Fluids.EMPTY.getDefaultState());
					world.setBlockState(pos, Blocks.COBBLESTONE.getDefaultState());
					world.playLevelEvent(1501, pos, 0);
					return true;
				}
			}
		}
		
		return false;
	}
}
