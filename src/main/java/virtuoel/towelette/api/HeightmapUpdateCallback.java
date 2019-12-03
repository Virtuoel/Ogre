package virtuoel.towelette.api;

import net.minecraft.state.State;
import net.minecraft.world.chunk.Chunk;

@FunctionalInterface
public interface HeightmapUpdateCallback<S extends State<S>>
{
	void trackHeightmapUpdate(Chunk chunk, int x, int y, int z, S state);
}
