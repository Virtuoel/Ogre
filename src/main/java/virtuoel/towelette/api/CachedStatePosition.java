package virtuoel.towelette.api;

import net.minecraft.state.PropertyContainer;

public interface CachedStatePosition
{
	<O, S extends PropertyContainer<S>> S getState(PaletteData<O, S> layer);
}
