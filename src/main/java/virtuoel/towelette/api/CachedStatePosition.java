package virtuoel.towelette.api;

import net.minecraft.state.State;

public interface CachedStatePosition
{
	<O, S extends State<S>> S getState(LayerData<O, S> layer);
}
