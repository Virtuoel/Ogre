package virtuoel.towelette.mixin.layer;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.state.PropertyContainer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.EmptyChunk;
import virtuoel.towelette.api.ChunkStateLayer;
import virtuoel.towelette.api.PaletteData;

@Mixin(EmptyChunk.class)
public abstract class EmptyChunkMixin implements ChunkStateLayer
{
	@Override
	@Nullable
	public <O, S extends PropertyContainer<S>> S setState(PaletteData<O, S> layer, BlockPos pos, S state, boolean pushed)
	{
		return null;
	}
	
	@Override
	public <O, S extends PropertyContainer<S>> S getState(PaletteData<O, S> layer, int x, int y, int z)
	{
		return layer.getInvalidPositionState();
	}
}
