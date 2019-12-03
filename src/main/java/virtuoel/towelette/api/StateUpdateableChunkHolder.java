package virtuoel.towelette.api;

import net.minecraft.state.State;

public interface StateUpdateableChunkHolder
{
	<O, S extends State<S>> void markForStateUpdate(LayerData<O, S> layer, int x, int y, int z);
}
