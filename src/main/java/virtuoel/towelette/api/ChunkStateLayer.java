package virtuoel.towelette.api;

import javax.annotation.Nullable;

import net.minecraft.util.math.BlockPos;

public interface ChunkStateLayer
{
	@Nullable
	default <S> S setState(BlockPos pos, S state)
	{
		return null;
	}
}
