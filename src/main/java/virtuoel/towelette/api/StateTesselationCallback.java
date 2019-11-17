package virtuoel.towelette.api;

import java.util.Random;

import net.minecraft.state.PropertyContainer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ExtendedBlockView;

@FunctionalInterface
public interface StateTesselationCallback<S extends PropertyContainer<S>>
{
	boolean tesselateState(Object blockRenderManager, S state, BlockPos pos, ExtendedBlockView world, Object bufferBuilder, Random random);
}
