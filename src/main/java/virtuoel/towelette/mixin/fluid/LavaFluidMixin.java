package virtuoel.towelette.mixin.fluid;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.LavaFluid;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.IWorld;
import virtuoel.towelette.api.LayerRegistrar;
import virtuoel.towelette.api.ModifiableWorldStateLayer;
import virtuoel.towelette.util.FluidUtils;

@Mixin(LavaFluid.class)
public class LavaFluidMixin
{
	@Redirect(method = "flow", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;getBlock()Lnet/minecraft/block/Block;"))
	private Block onFlowGetBlockProxy(BlockState obj, IWorld world, BlockPos pos, BlockState blockState, Direction direction, FluidState fluidState)
	{
		if (FluidUtils.canFluidInteractionReplace(world, pos))
		{
			final ModifiableWorldStateLayer w = ((ModifiableWorldStateLayer) world);
			w.setState(LayerRegistrar.FLUID_LAYER, pos, Fluids.EMPTY.getDefaultState());
			return Blocks.WATER;
		}
		
		return Blocks.AIR;
	}
}
