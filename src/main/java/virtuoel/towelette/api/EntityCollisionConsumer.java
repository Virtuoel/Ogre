package virtuoel.towelette.api;

import net.minecraft.entity.Entity;
import net.minecraft.state.PropertyContainer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@FunctionalInterface
public interface EntityCollisionConsumer<S extends PropertyContainer<S>>
{
	void onEntityCollision(S state, World world, BlockPos pos, Entity entity);
}
