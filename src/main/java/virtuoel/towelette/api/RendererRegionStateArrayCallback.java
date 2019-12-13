package virtuoel.towelette.api;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.state.State;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

@FunctionalInterface
public interface RendererRegionStateArrayCallback<O, S extends State<S>>
{
	S[] createRendererRegionStateArray(LayerData<O, S> layer, World world, int chunkXOffset, int chunkZOffset, WorldChunk[][] chunks, BlockPos startPos, BlockPos endPos, int xSize, int ySize, int zSize, BlockState[] blockStates, FluidState[] fluidStates);
}
