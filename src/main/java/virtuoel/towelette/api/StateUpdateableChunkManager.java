package virtuoel.towelette.api;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public interface StateUpdateableChunkManager
{
	void onStateUpdate(Identifier layer, BlockPos pos);
}
