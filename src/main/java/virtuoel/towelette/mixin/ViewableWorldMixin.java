package virtuoel.towelette.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;
import net.minecraft.world.ViewableWorld;

@Mixin(ViewableWorld.class)
public interface ViewableWorldMixin extends BlockView
{
	default boolean intersectsFluid(Box box)
	{
		final int minX = MathHelper.floor(box.minX);
		final int maxX = MathHelper.ceil(box.maxX);
		final int minY = MathHelper.floor(box.minY);
		final int maxY = MathHelper.ceil(box.maxY);
		final int minZ = MathHelper.floor(box.minZ);
		final int maxZ = MathHelper.ceil(box.maxZ);
		
		try (BlockPos.PooledMutable pos = BlockPos.PooledMutable.get())
		{
			for (int x = minX; x < maxX; x++)
			{
				for (int y = minY; y < maxY; y++)
				{
					for (int z = minZ; z < maxZ; z++)
					{
						if (!this.getFluidState(pos.method_10113(x, y, z)).isEmpty())
						{
							return true;
						}
					}
				}
			}
		}
		
		return false;
	}
}
