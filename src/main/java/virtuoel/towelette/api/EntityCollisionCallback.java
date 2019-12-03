package virtuoel.towelette.api;

import net.minecraft.entity.Entity;
import net.minecraft.state.State;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@FunctionalInterface
public interface EntityCollisionCallback<S extends State<S>>
{
	void onEntityCollision(S state, World world, BlockPos pos, Entity entity);
}
