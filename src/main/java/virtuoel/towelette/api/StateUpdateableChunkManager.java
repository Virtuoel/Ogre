package virtuoel.towelette.api;

import net.minecraft.state.PropertyContainer;
import net.minecraft.util.math.BlockPos;

public interface StateUpdateableChunkManager
{
	<O, S extends PropertyContainer<S>> void onStateUpdate(PaletteData<O, S> layer, BlockPos pos);
}
