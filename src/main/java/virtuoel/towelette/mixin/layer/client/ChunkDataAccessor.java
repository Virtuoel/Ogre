package virtuoel.towelette.mixin.layer.client;

import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.chunk.ChunkBuilder;

@Mixin(ChunkBuilder.ChunkData.class)
public interface ChunkDataAccessor
{
	@Accessor
	Set<RenderLayer> getNonEmptyLayers();
	
	@Accessor
	Set<RenderLayer> getInitializedLayers();
	
	@Accessor
	void setEmpty(boolean empty);
}
