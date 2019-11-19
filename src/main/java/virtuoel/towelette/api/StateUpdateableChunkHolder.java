package virtuoel.towelette.api;

import net.minecraft.state.PropertyContainer;

public interface StateUpdateableChunkHolder
{
	<O, S extends PropertyContainer<S>> void markForStateUpdate(PaletteData<O, S> layer, int x, int y, int z);
}
