package virtuoel.towelette.mixin.layer.fluid;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.state.State;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;
import virtuoel.towelette.api.ChunkSectionStateLayer;
import virtuoel.towelette.api.ChunkStateLayer;
import virtuoel.towelette.api.LayerData;
import virtuoel.towelette.api.LayerRegistrar;

@Mixin(WorldChunk.class)
public abstract class WorldChunkMixin implements ChunkStateLayer
{
	@Inject(at = @At("HEAD"), method = "setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Z)Lnet/minecraft/block/BlockState;", cancellable = true)
	public void setBlockState(BlockPos pos, BlockState state, boolean flag, CallbackInfoReturnable<BlockState> info)
	{
		final Block block = state.getBlock();
		if (block instanceof FluidBlock)
		{
			setState(LayerRegistrar.FLUID, pos, block.getFluidState(state), flag);
			info.setReturnValue(null);
		}
	}
}
