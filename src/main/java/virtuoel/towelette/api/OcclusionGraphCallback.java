package virtuoel.towelette.api;

import net.minecraft.state.PropertyContainer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

@FunctionalInterface
public interface OcclusionGraphCallback<S extends PropertyContainer<S>>
{
	void handleOcclusionGraph(Object chunkOcclusionGraphBuilder, S state, BlockView world, BlockPos pos);
}
