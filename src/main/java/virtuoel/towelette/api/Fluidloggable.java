package virtuoel.towelette.api;

import java.util.Optional;

import com.google.gson.JsonElement;

import net.minecraft.block.BlockState;
import net.minecraft.block.Waterloggable;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.IWorld;

public interface Fluidloggable extends Waterloggable
{
	public static final FluidProperty FLUID = FluidProperty.FLUID;
	
	@Override
	default boolean canFillWithFluid(BlockView blockView_1, BlockPos blockPos_1, BlockState blockState_1, Fluid fluid_1)
	{
		return canFillImpl(blockView_1, blockPos_1, blockState_1, fluid_1);
	}
	
	@Override
	default boolean tryFillWithFluid(IWorld iWorld_1, BlockPos blockPos_1, BlockState blockState_1, FluidState fluidState_1)
	{
		return tryFillImpl(iWorld_1, blockPos_1, blockState_1, fluidState_1);
	}
	
	@Override
	default Fluid tryDrainFluid(IWorld iWorld_1, BlockPos blockPos_1, BlockState blockState_1)
	{
		final FluidState fluidState = FLUID.getFluidState(blockState_1);
		if(!fluidState.isEmpty())
		{
			blockState_1 = blockState_1.with(FLUID, FLUID.of(Fluids.EMPTY));
			if(blockState_1.contains(Properties.WATERLOGGED))
			{
				blockState_1 = blockState_1.with(Properties.WATERLOGGED, false);
			}
			iWorld_1.setBlockState(blockPos_1, blockState_1, 3);
		}
		return fluidState.getFluid();
	}
	
	public static boolean canFillImpl(BlockView blockView_1, BlockPos blockPos_1, BlockState blockState_1, Fluid fluid_1)
	{
		return FLUID.isValid(fluid_1) && (FLUID.getFluidState(blockState_1).isEmpty() ||
			Optional.ofNullable(ToweletteConfig.DATA.get("replaceableFluids"))
			.filter(JsonElement::isJsonPrimitive)
			.map(JsonElement::getAsBoolean).orElse(false));
	}
	
	public static boolean tryFillImpl(IWorld iWorld_1, BlockPos blockPos_1, BlockState blockState_1, FluidState fluidState_1)
	{
		if(canFillImpl(iWorld_1, blockPos_1, blockState_1, fluidState_1.getFluid()))
		{
			if(!iWorld_1.isClient())
			{
				final Fluid fluid = fluidState_1.getFluid();
				if(fluid == Fluids.WATER && blockState_1.contains(Properties.WATERLOGGED))
				{
					blockState_1 = blockState_1.with(Properties.WATERLOGGED, true);
				}
				iWorld_1.setBlockState(blockPos_1, blockState_1.with(FLUID, FLUID.of(fluidState_1)), 3);
				iWorld_1.getFluidTickScheduler().schedule(blockPos_1, fluid, fluid.getTickRate(iWorld_1));
			}
			
			return true;
		}
		else
		{
			return false;
		}
	}
	
	@Deprecated
	public static boolean fillImpl(IWorld iWorld_1, BlockPos blockPos_1, BlockState blockState_1, FluidState fluidState_1)
	{
		return tryFillImpl(iWorld_1, blockPos_1, blockState_1, fluidState_1);
	}
}
