package virtuoel.towelette.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin
{
	@Redirect(method = "travel(Lnet/minecraft/util/math/Vec3d;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;getFluidState()Lnet/minecraft/fluid/FluidState;"))
	private FluidState onTravelGetFluidStateProxy(BlockState obj)
	{
		final PlayerEntity self = ((PlayerEntity) (Object) (this));
		return self.world.getFluidState(new BlockPos(self.getX(), self.getY() + 1.0D - 0.1D, self.getZ()));
	}
}
