package virtuoel.towelette.mixin.layer;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.chunk.Chunk;
import virtuoel.towelette.api.ChunkFluidLayer;

@Mixin(Chunk.class)
public interface ChunkMixin extends ChunkFluidLayer
{
	
}
