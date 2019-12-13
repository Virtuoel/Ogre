package virtuoel.towelette.mixin.fluid;

import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

@Mixin(FluidBlock.class)
public abstract class FluidBlockMixin
{
	@Inject(at = @At("HEAD"), method = "randomTick", cancellable = true)
	private void onRandomTick(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo info)
	{
		info.cancel();
	}
}
