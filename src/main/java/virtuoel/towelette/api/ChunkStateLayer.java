package virtuoel.towelette.api;

import javax.annotation.Nullable;

import net.minecraft.state.PropertyContainer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public interface ChunkStateLayer<O, S extends PropertyContainer<S>>
{
	@Nullable
	S setState(Identifier layer, BlockPos pos, S state, boolean pushed);
	
	default S getState(Identifier layer, BlockPos pos)
	{
		return getState(layer, pos.getX(), pos.getY(), pos.getZ());
	}
	
	S getState(Identifier layer, int x, int y, int z);
}
