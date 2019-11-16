package virtuoel.towelette.api;

import net.minecraft.state.PropertyContainer;
import net.minecraft.util.Identifier;

public interface ChunkSectionStateLayer
{
	default <O, S extends PropertyContainer<S>> S setState(Identifier layer, int x, int y, int z, S state)
	{
		return setState(layer, x, y, z, state, true);
	}
	
	<O, S extends PropertyContainer<S>> S setState(Identifier layer, int x, int y, int z, S state, boolean synchronous);
	
	<O, S extends PropertyContainer<S>> S getState(Identifier layer, int x, int y, int z);
}
