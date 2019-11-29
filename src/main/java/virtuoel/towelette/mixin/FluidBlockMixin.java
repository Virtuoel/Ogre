package virtuoel.towelette.mixin;

import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(FluidBlock.class)
public abstract class FluidBlockMixin
{
	@Inject(at = @At("HEAD"), method = "onRandomTick", cancellable = true)
	private void onOnRandomTick(BlockState state, World world, BlockPos pos, Random random, CallbackInfo info)
	{
		info.cancel();
	}
}
