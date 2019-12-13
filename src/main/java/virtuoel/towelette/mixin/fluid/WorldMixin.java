package virtuoel.towelette.mixin.fluid;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.FluidState;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

@Mixin(World.class)
public abstract class WorldMixin
{
	@Shadow abstract FluidState getFluidState(BlockPos pos);
	
	@Inject(method = "doesAreaContainFireSource", locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true, at = @At(value = "INVOKE", shift = Shift.AFTER, target = "Lnet/minecraft/world/World;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;"))
	private void doesAreaContainFireSourceGetFluidState(Box box, CallbackInfoReturnable<Boolean> info, int noop1, int noop2, int noop3, int noop4, int noop5, int noop6, BlockPos.PooledMutable pos, int noop7, int noop8, int noop9)
	{
		if (getFluidState(pos).matches(FluidTags.LAVA))
		{
			info.setReturnValue(true);
		}
	}
	
	@Shadow abstract boolean setBlockState(BlockPos pos, BlockState state, int flags);
	
	@Inject(at = @At(value = "HEAD"), method = "removeBlock", cancellable = true)
	private void onRemoveBlock(BlockPos pos, boolean flag, CallbackInfoReturnable<Boolean> info)
	{
		info.setReturnValue(setBlockState(pos, Blocks.AIR.getDefaultState(), 3 | (flag ? 64 : 0)));
	}
	
	@ModifyArg(method = "breakBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"))
	private BlockState breakBlockSetBlockState(BlockState blockState)
	{
		return Blocks.AIR.getDefaultState();
	}
}
