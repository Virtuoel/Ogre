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
import virtuoel.towelette.api.PaletteData;
import virtuoel.towelette.api.PaletteRegistrar;

@Mixin(CachedBlockPosition.class)
public class CachedBlockPositionMixin implements CachedStatePosition
{
	@Shadow @Final ViewableWorld world;
	@Shadow @Final BlockPos pos;
	@Shadow @Final boolean forceLoad;
	
	@Unique final Map<Identifier, Object> states = new HashMap<>();
	
	@SuppressWarnings("unchecked")
	@Override
	public <O, S extends PropertyContainer<S>> S getState(PaletteData<O, S> layer)
	{
		return (S) states.computeIfAbsent(PaletteRegistrar.PALETTES.getId(layer), key ->
		{
			if(this.forceLoad || this.world.isBlockLoaded(this.pos))
			{
				return ((BlockViewStateLayer) this.world).getState(layer, this.pos);
			}
			
			return null;
		});
	}
}
