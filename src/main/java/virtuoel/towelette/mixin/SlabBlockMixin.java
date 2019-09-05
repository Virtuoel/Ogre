package virtuoel.towelette.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.Waterloggable;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.IWorld;
import virtuoel.towelette.util.FluidUtils;

@Mixin(value = SlabBlock.class, priority = 999)
public abstract class SlabBlockMixin
{
	@Inject(at = @At("RETURN"), method = "getPlacementState", cancellable = true)
	public void onGetPlacementState(ItemPlacementContext context, CallbackInfoReturnable<BlockState> info)
	{
		if(context.getWorld().getBlockState(context.getBlockPos()).getBlock() == (SlabBlock) (Object) this)
		{
			final BlockState state = FluidUtils.getStateWithFluid(info.getReturnValue(), Fluids.EMPTY);
			if(state != null)
			{
				info.setReturnValue(state);
			}
		}
	}
	
	@Redirect(method = "canFillWithFluid", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Waterloggable;canFillWithFluid(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/fluid/Fluid;)Z"))
	public boolean canFillWithFluidProxy(Waterloggable obj, BlockView blockView_1, BlockPos blockPos_1, BlockState blockState_1, Fluid fluid_1)
	{
		return FluidUtils.canFillWithFluid(blockView_1, blockPos_1, blockState_1, fluid_1);
	}
	
	@Redirect(method = "tryFillWithFluid", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Waterloggable;tryFillWithFluid(Lnet/minecraft/world/IWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/fluid/FluidState;)Z"))
	public boolean tryFillWithFluidProxy(Waterloggable obj, IWorld iWorld_1, BlockPos blockPos_1, BlockState blockState_1, FluidState fluidState_1)
	{
		return FluidUtils.tryFillWithFluid(iWorld_1, blockPos_1, blockState_1, fluidState_1);
	}
}
