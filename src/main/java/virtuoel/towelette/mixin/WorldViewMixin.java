package virtuoel.towelette.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldView;

@Mixin(WorldView.class)
public interface WorldViewMixin extends BlockView
{
	@Overwrite
	default boolean containsFluid(Box box)
	{
		final int minX = MathHelper.floor(box.x1);
		final int maxX = MathHelper.ceil(box.x2);
		final int minY = MathHelper.floor(box.y1);
		final int maxY = MathHelper.ceil(box.y2);
		final int minZ = MathHelper.floor(box.z1);
		final int maxZ = MathHelper.ceil(box.z2);
		
		try (BlockPos.PooledMutable pos = BlockPos.PooledMutable.get())
		{
			for (int x = minX; x < maxX; x++)
			{
				for (int y = minY; y < maxY; y++)
				{
					for (int z = minZ; z < maxZ; z++)
					{
						if (!this.getFluidState(pos.set(x, y, z)).isEmpty())
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
