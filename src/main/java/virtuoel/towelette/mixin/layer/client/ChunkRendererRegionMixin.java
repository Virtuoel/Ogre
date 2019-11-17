package virtuoel.towelette.mixin.layer.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.chunk.ChunkRendererRegion;
import net.minecraft.fluid.FluidState;
import net.minecraft.state.PropertyContainer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import virtuoel.towelette.api.BlockViewStateLayer;
import virtuoel.towelette.api.ChunkStateLayer;
import virtuoel.towelette.api.PaletteRegistrar;

@Mixin(ChunkRendererRegion.class)
public abstract class ChunkRendererRegionMixin<O, S extends PropertyContainer<S>> implements BlockViewStateLayer<S>
{
	@Shadow @Final int xSize;
	@Shadow @Final int ySize;
	@Shadow @Final int zSize;
	@Shadow @Final BlockState[] blockStates;
	@Shadow @Final FluidState[] fluidStates;
	
	@Shadow abstract int getIndex(BlockPos pos);
	
	@Unique private Object2ObjectLinkedOpenHashMap<Identifier, S[]> states;
	
	@Inject(at = @At("RETURN"), method = "<init>(ISSS)V")
	private void onConstruct(World world, int x, int z, WorldChunk[][] chunks, BlockPos from, BlockPos to, CallbackInfo info)
	{
		states = new Object2ObjectLinkedOpenHashMap<Identifier, S[]>();
		
		boolean blockState = false;
		boolean fluidState = false;
		for(final Identifier layer : PaletteRegistrar.PALETTES.getIds())
		{
			Object array;
			if(!blockState && layer.equals(PaletteRegistrar.BLOCK_STATE))
			{
				blockState = true;
				array = blockStates;
			}
			else if(!fluidState && layer.equals(PaletteRegistrar.FLUID_STATE))
			{
				fluidState = true;
				array = fluidStates;
			}
			else
			{
				array = new PropertyContainer[xSize * ySize * zSize];
				@SuppressWarnings("unchecked")
				final S[] stateArray = (S[]) array;
				
				int index;
				for(final BlockPos pos : BlockPos.iterate(from, to))
				{
					int chunkX = (pos.getX() >> 4) - x;
					int chunkZ = (pos.getZ() >> 4) - z;
					@SuppressWarnings("unchecked")
					ChunkStateLayer<O, S> chunk = ((ChunkStateLayer<O, S>) chunks[chunkX][chunkZ]);
					index = this.getIndex(pos);
					stateArray[index] = chunk.getState(layer, pos);
				}
			}
			
			@SuppressWarnings("unchecked")
			final S[] stateArray = (S[]) array;
			states.put(layer, stateArray);
		}
	}
	
	@Override
	public S getState(Identifier layer, BlockPos pos)
	{
		return states.get(layer)[this.getIndex(pos)];
	}
}
