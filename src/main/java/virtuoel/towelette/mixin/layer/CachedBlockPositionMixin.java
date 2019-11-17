package virtuoel.towelette.mixin.layer;

import java.util.HashMap;
import java.util.Map;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.state.PropertyContainer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ViewableWorld;
import virtuoel.towelette.api.BlockViewStateLayer;
import virtuoel.towelette.api.CachedStatePosition;

@Mixin(CachedBlockPosition.class)
public class CachedBlockPositionMixin<S extends PropertyContainer<S>> implements CachedStatePosition<S>
{
	@Shadow @Final ViewableWorld world;
	@Shadow @Final BlockPos pos;
	@Shadow @Final boolean forceLoad;
	
	@Unique final Map<Identifier, Object> states = new HashMap<>();
	
	@SuppressWarnings("unchecked")
	@Override
	public S getState(Identifier layer)
	{
		return (S) states.computeIfAbsent(layer, key ->
		{
			if(this.forceLoad || this.world.isBlockLoaded(this.pos))
			{
				return ((BlockViewStateLayer<S>) this.world).getState(key, this.pos);
			}
			
			return null;
		});
	}
}
