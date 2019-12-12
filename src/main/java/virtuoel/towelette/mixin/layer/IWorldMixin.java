package virtuoel.towelette.mixin.layer;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.IWorld;
import virtuoel.towelette.api.IWorldStateLayer;

@Mixin(IWorld.class)
public interface IWorldMixin extends IWorldStateLayer
{
	
}
