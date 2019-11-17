package virtuoel.towelette.mixin.layer;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.state.PropertyContainer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.EmptyChunk;
import virtuoel.towelette.api.ChunkStateLayer;
import virtuoel.towelette.api.PaletteRegistrar;

@Mixin(EmptyChunk.class)
public abstract class EmptyChunkMixin<O, S extends PropertyContainer<S>> implements ChunkStateLayer<O, S>
{
	@Override
	@Nullable
	public S setState(Identifier layer, BlockPos pos, S state, boolean pushed)
	{
		return null;
	}
	
	@Override
	public S getState(Identifier layer, int x, int y, int z)
	{
		return PaletteRegistrar.<O, S>getPaletteData(layer).getInvalidPositionState();
	}
}
