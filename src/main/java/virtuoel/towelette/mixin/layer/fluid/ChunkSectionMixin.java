package virtuoel.towelette.mixin.layer.fluid;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.fluid.FluidState;
import net.minecraft.world.chunk.ChunkSection;
import virtuoel.towelette.api.ChunkSectionStateLayer;
import virtuoel.towelette.api.LayerRegistrar;
import virtuoel.towelette.util.LayeredPalettedContainerHolder;

@Mixin(ChunkSection.class)
public abstract class ChunkSectionMixin implements ChunkSectionStateLayer, LayeredPalettedContainerHolder
{
	@Inject(at = @At("HEAD"), method = "hasRandomFluidTicks()Z", cancellable = true)
	public void onHasRandomFluidTicks(CallbackInfoReturnable<Boolean> info)
	{
		info.setReturnValue(getPalettedContainerDataMap().get(LayerRegistrar.LAYERS.getId(LayerRegistrar.FLUID)).getRight() > 0);
	}
	
	@Inject(at = @At("HEAD"), method = "setBlockState(IIILnet/minecraft/block/BlockState;Z)Lnet/minecraft/block/BlockState;", cancellable = true)
	public void setBlockState(int x, int y, int z, BlockState state, boolean flag, CallbackInfoReturnable<BlockState> info)
	{
		final Block block = state.getBlock();
		if (block instanceof FluidBlock)
		{
			setState(LayerRegistrar.FLUID, x, y, z, block.getFluidState(state), flag);
			info.setReturnValue(state);
		}
		else
		{
			info.setReturnValue(setState(LayerRegistrar.BLOCK, x, y, z, state, flag));
		}
	}
	
	@Inject(at = @At("HEAD"), method = "getFluidState(III)Lnet/minecraft/fluid/FluidState;", cancellable = true)
	public void onGetFluidState(int x, int y, int z, CallbackInfoReturnable<FluidState> info)
	{
		info.setReturnValue(getState(LayerRegistrar.FLUID, x, y, z));
	}
}
