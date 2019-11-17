package virtuoel.towelette.api;

import net.minecraft.state.PropertyContainer;
import net.minecraft.util.Identifier;

public interface ChunkSectionStateLayer<O, S extends PropertyContainer<S>>
{
	default S setState(Identifier layer, int x, int y, int z, S state)
	{
		return setState(layer, x, y, z, state, true);
	}
	
	S setState(Identifier layer, int x, int y, int z, S state, boolean synchronous);
	
	S getState(Identifier layer, int x, int y, int z);
}
