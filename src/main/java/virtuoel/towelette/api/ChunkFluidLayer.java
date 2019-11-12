package virtuoel.towelette.api;

import javax.annotation.Nullable;

import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;

public interface ChunkFluidLayer
{
	@Nullable
	default FluidState setFluidState(BlockPos pos, FluidState state)
	{
		return null;
	}
}
