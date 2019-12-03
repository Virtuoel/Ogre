package virtuoel.towelette.api;

import java.util.Optional;

import net.minecraft.state.State;
import net.minecraft.util.Identifier;
import net.minecraft.world.chunk.PalettedContainer;

public interface ChunkSectionStateLayer
{
	default <O, S extends State<S>> S setState(LayerData<O, S> layer, int x, int y, int z, S state)
	{
		return setState(layer, x, y, z, state, true);
	}
	
	<O, S extends State<S>> S setState(LayerData<O, S> layer, int x, int y, int z, S state, boolean synchronous);
	
	<O, S extends State<S>> S getState(LayerData<O, S> layer, int x, int y, int z);
	
	default <O, S extends State<S>> Optional<PalettedContainer<S>> getContainer(LayerData<O, S> layer)
	{
		final Identifier defaultId = LayerRegistrar.LAYERS.getDefaultId();
		final Identifier id;
		
		if (layer == LayerRegistrar.LAYERS.get(defaultId))
		{
			id = defaultId;
		}
		else
		{
			final Identifier layerId = LayerRegistrar.LAYERS.getId(layer);
			if (defaultId.equals(layerId))
			{
				return Optional.empty();
			}
			else
			{
				id = layerId;
			}
		}
		
		return getContainer(id);
	}
	
	<O, S extends State<S>> Optional<PalettedContainer<S>> getContainer(Identifier id);
}
