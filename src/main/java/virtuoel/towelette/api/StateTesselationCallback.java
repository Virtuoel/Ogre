package virtuoel.towelette.api;

import java.util.Random;

import net.minecraft.state.State;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

@FunctionalInterface
public interface StateTesselationCallback<S extends State<S>>
{
	boolean tesselateState(Object blockRenderManager, S state, BlockPos pos, BlockRenderView world, Object matrixStack, Object vertexConsumer, boolean checkSides, Random random);
}
