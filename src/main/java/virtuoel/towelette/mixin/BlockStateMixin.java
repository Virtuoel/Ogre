package virtuoel.towelette.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import virtuoel.towelette.util.FluidUtils;

@Mixin(BlockState.class)
public abstract class BlockStateMixin
{
	@Inject(at = @At("RETURN"), method = "getStateForNeighborUpdate")
	private void onGetStateForNeighborUpdate(Direction direction, BlockState blockState, IWorld world, BlockPos pos, BlockPos otherPos, CallbackInfoReturnable<BlockState> info)
	{
		FluidUtils.scheduleFluidTick(world, pos);
	}
	
	@Inject(at = @At("RETURN"), method = "onBlockAdded")
	private void onOnBlockAdded(World world, BlockPos blockPos, BlockState blockState, boolean flag, CallbackInfo info)
	{
		FluidUtils.scheduleFluidTick(world, blockPos);
	}
	
	@Inject(at = @At("HEAD"), method = "getFluidState", cancellable = true)
	private void getFluidState(CallbackInfoReturnable<FluidState> info)
	{
		info.setReturnValue(Fluids.EMPTY.getDefaultState());
	}
}
