package virtuoel.towelette.api;

import net.minecraft.util.Identifier;

public interface ChunkSectionStateLayer
{
	default <S> S setState(Identifier layer, int x, int y, int z, S state)
	{
		return setState(layer, x, y, z, state, true);
	}
	
	<S> S setState(Identifier layer, int x, int y, int z, S state, boolean synchronous);
}
