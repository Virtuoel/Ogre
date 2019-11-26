package virtuoel.towelette.api;

import net.minecraft.state.PropertyContainer;
import net.minecraft.world.chunk.Chunk;

@FunctionalInterface
public interface HeightmapUpdateCallback<S extends PropertyContainer<S>>
{
	void trackHeightmapUpdate(Chunk chunk, int x, int y, int z, S state);
}
