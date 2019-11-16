package virtuoel.towelette.mixin.layer;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.ModifiableWorld;
import virtuoel.towelette.api.ModifiableWorldStateLayer;

@Mixin(ModifiableWorld.class)
public interface ModifiableWorldMixin extends ModifiableWorldStateLayer
{
	
}
