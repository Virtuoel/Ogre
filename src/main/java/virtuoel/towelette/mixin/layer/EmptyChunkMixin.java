package virtuoel.towelette.mixin.layer;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.state.State;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.EmptyChunk;
import virtuoel.towelette.api.ChunkStateLayer;
import virtuoel.towelette.api.LayerData;

@Mixin(EmptyChunk.class)
public abstract class EmptyChunkMixin implements ChunkStateLayer
{
	@Override
	@Nullable
	public <O, S extends State<S>> S setState(LayerData<O, S> layer, BlockPos pos, S state, boolean pushed)
	{
		return null;
	}
	
	@Override
	public <O, S extends State<S>> S getState(LayerData<O, S> layer, int x, int y, int z)
	{
		return layer.getInvalidPositionState();
	}
}
