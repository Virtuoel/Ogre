package virtuoel.towelette.api;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Palette;

public interface ModifiableWorldStateLayer
{
	default <S> boolean setState(BlockPos pos, S state)
	{
		return setState(pos, state, 3);
	}
	
	default <S> boolean setState(BlockPos pos, S state, int flags)
	{
		return false;
	}
	
	default <S> boolean clearState(Palette<S> palette, BlockPos pos, boolean flag)
	{
		return setState(pos, palette.getByIndex(-1), 3 | (flag ? 64 : 0));
	}
}
