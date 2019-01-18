package virtuoel.towelette.mixin.layer;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ViewableWorld;
import virtuoel.towelette.api.CachedFluidPosition;

@Mixin(CachedBlockPosition.class)
public class CachedBlockPositionMixin implements CachedFluidPosition
{
	@Shadow @Final ViewableWorld world;
	@Shadow @Final BlockPos pos;
	@Shadow @Final boolean forceLoad;
	
	public FluidState fluidState;
	
	@Override
	public FluidState getFluidState()
	{
		if(this.fluidState == null && (this.forceLoad || this.world.isBlockLoaded(this.pos)))
		{
			this.fluidState = this.world.getFluidState(this.pos);
		}
		
		return this.fluidState;
	}
}
