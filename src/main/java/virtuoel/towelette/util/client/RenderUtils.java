package virtuoel.towelette.util.client;

import net.minecraft.block.BlockRenderLayer;
import net.minecraft.client.render.chunk.ChunkRenderData;
import virtuoel.towelette.mixin.layer.client.ChunkRenderDataInvoker;

public class RenderUtils
{
	public static void setNonEmpty(ChunkRenderData data, BlockRenderLayer blockRenderLayer)
	{
		((ChunkRenderDataInvoker) data).callSetNonEmpty(blockRenderLayer);
	}
}
