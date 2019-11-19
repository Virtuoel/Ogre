package virtuoel.towelette.api;

import net.minecraft.state.PropertyContainer;

public interface ChunkSectionStateLayer
{
	default <O, S extends PropertyContainer<S>> S setState(PaletteData<O, S> layer, int x, int y, int z, S state)
	{
		return setState(layer, x, y, z, state, true);
	}
	
	<O, S extends PropertyContainer<S>> S setState(PaletteData<O, S> layer, int x, int y, int z, S state, boolean synchronous);
	
	<O, S extends PropertyContainer<S>> S getState(PaletteData<O, S> layer, int x, int y, int z);
}
