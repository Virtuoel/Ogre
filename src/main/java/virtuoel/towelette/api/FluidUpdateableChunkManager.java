package virtuoel.towelette.api;

import net.minecraft.util.math.BlockPos;

public interface FluidUpdateableChunkManager
{
	void onFluidUpdate(BlockPos pos);
}
