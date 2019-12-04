package virtuoel.towelette.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import virtuoel.towelette.api.BlockViewStateLayer;
import virtuoel.towelette.api.LayerData;
import virtuoel.towelette.api.LayerRegistrar;

@Mixin(Entity.class)
public abstract class EntityMixin
{
	@Shadow World world;
	
	@SuppressWarnings("unchecked")
	@Inject(method = "checkBlockCollision", locals = LocalCapture.CAPTURE_FAILSOFT, at = @At(value = "INVOKE", shift = Shift.AFTER, target = "Lnet/minecraft/block/BlockState;onEntityCollision(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/Entity;)V"))
	private void checkBlockCollisionGetFluidState(CallbackInfo info, Box noop1, BlockPos.PooledMutable noop2, BlockPos.PooledMutable noop3, BlockPos.PooledMutable pos, int noop4, int noop5, int noop6)
	{
		final BlockViewStateLayer w = ((BlockViewStateLayer) world);
		for (final Identifier id : LayerRegistrar.LAYERS.getIds())
		{
			if (id.equals(LayerRegistrar.LAYERS.getDefaultId()))
			{
				continue;
			}
			
			@SuppressWarnings("rawtypes")
			final LayerData layer = LayerRegistrar.LAYERS.get(id);
			
			layer.onEntityCollision(w.getState(layer, pos), world, pos, (Entity) (Object) this);
		}
	}
}
