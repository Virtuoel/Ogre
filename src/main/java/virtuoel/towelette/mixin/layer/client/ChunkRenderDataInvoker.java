package virtuoel.towelette.mixin.layer.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.block.BlockRenderLayer;
import net.minecraft.client.render.chunk.ChunkRenderData;

@Mixin(ChunkRenderData.class)
public interface ChunkRenderDataInvoker
{
	@Invoker
	void callSetNonEmpty(BlockRenderLayer blockRenderLayer);
}
