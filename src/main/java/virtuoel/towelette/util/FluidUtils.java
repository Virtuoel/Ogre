package virtuoel.towelette.util;

import java.util.Optional;

import com.google.common.math.DoubleMath;
import com.google.gson.JsonElement;

import it.unimi.dsi.fastutil.objects.Object2ByteLinkedOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.SliceVoxelShape;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.IWorld;
import virtuoel.towelette.api.ToweletteConfig;
import virtuoel.towelette.mixin.VoxelShapeAccessor;

public class FluidUtils
{
	public static final ThreadLocal<Object2ByteLinkedOpenHashMap<StateNeighborGroup>> FLUID_FLOW_MAP = ThreadLocal.withInitial(() ->
	{
		@SuppressWarnings("serial")
		Object2ByteLinkedOpenHashMap<StateNeighborGroup> map = new Object2ByteLinkedOpenHashMap<StateNeighborGroup>(200)
		{
			@Override
			protected void rehash(int int_1)
			{
				
			}
		};
		map.defaultReturnValue((byte) 127);
		return map;
	});
	
	public static boolean isFluidFlowBlocked(Direction direction, BlockView world, VoxelShape shape, BlockState blockState, BlockPos blockPos, VoxelShape otherShape, BlockState otherState, BlockPos otherPos)
	{
		final boolean accurateFlowBlocking = Optional.ofNullable(ToweletteConfig.DATA.get("accurateFlowBlocking"))
			.filter(JsonElement::isJsonPrimitive)
			.map(JsonElement::getAsBoolean).orElse(true);
		
		if(accurateFlowBlocking)
		{
			if (shape != VoxelShapes.fullCube() && otherShape != VoxelShapes.fullCube())
			{
				final FluidState fluidState = world.getFluidState(otherPos);
				final VoxelShape inverseShape = fluidState.isEmpty() ? VoxelShapes.empty() : VoxelShapes.combine(VoxelShapes.fullCube(), fluidState.getShape(world, otherPos), BooleanBiFunction.ONLY_FIRST);
				final VoxelShape combinedOtherShape = VoxelShapes.combine(otherShape, inverseShape, BooleanBiFunction.OR);
				
				final Direction.Axis axis = direction.getAxis();
				final boolean positiveDirection = direction.getDirection() == Direction.AxisDirection.POSITIVE;
				VoxelShape fromShape = positiveDirection ? shape : combinedOtherShape;
				VoxelShape toShape = positiveDirection ? combinedOtherShape : shape;
				if (fromShape != VoxelShapes.empty() && !DoubleMath.fuzzyEquals(fromShape.getMaximum(axis), 1.0D, 1.0E-7D))
				{
					fromShape = VoxelShapes.empty();
				}
				
				if (toShape != VoxelShapes.empty() && !DoubleMath.fuzzyEquals(toShape.getMinimum(axis), 0.0D, 1.0E-7D))
				{
					toShape = VoxelShapes.empty();
				}
				
				return !VoxelShapes.matchesAnywhere(VoxelShapes.fullCube(), VoxelShapes.combine(new SliceVoxelShape(fromShape, axis, ((VoxelShapeAccessor) fromShape).getVoxels().getSize(axis) - 1), new SliceVoxelShape(toShape, axis, 0), BooleanBiFunction.OR), BooleanBiFunction.ONLY_FIRST);
			}
			else
			{
				return true;
			}
		}
		else
		{
			return VoxelShapes.method_1080(shape, otherShape, direction);
		}
	}
	
	public static boolean scheduleFluidTick(IWorld world, BlockPos pos)
	{
		return scheduleFluidTick(world.getFluidState(pos), world, pos);
	}
	
	public static boolean scheduleFluidTick(FluidState state, IWorld world, BlockPos pos)
	{
		if(!state.isEmpty())
		{
			scheduleFluidTickImpl(state.getFluid(), world, pos);
			return true;
		}
		return false;
	}
	
	public static boolean scheduleFluidTick(Fluid fluid, IWorld world, BlockPos pos)
	{
		if(!fluid.getDefaultState().isEmpty())
		{
			scheduleFluidTickImpl(fluid, world, pos);
			return true;
		}
		return false;
	}
	
	private static void scheduleFluidTickImpl(Fluid fluid, IWorld world, BlockPos pos)
	{
		world.getFluidTickScheduler().schedule(pos, fluid, fluid.getTickRate(world));
	}
}
