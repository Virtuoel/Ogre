package virtuoel.towelette.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.LavaFluid;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.IWorld;
import virtuoel.towelette.api.ModifiableWorldStateLayer;
import virtuoel.towelette.api.LayerRegistrar;

@Mixin(LavaFluid.class)
public class LavaFluidMixin
{
	@Redirect(method = "flow", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;getBlock()Lnet/minecraft/block/Block;"))
	private Block onFlowGetBlockProxy(BlockState obj, IWorld world, BlockPos pos, BlockState blockState, Direction direction, FluidState fluidState)
	{
		// TODO Make more flexible. Use tags?
		final boolean replaceable = obj.isAir() || obj.getBlock().getMaterial(obj).isReplaceable() || obj.getBlock() instanceof FluidBlock;
		if (replaceable)
		{
			final ModifiableWorldStateLayer w = ((ModifiableWorldStateLayer) world);
			w.setState(LayerRegistrar.FLUID, pos, Fluids.EMPTY.getDefaultState());
			return Blocks.WATER;
		}
		
		return Blocks.AIR;
	}
}
