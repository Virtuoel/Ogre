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
import virtuoel.towelette.api.LayerData;
import virtuoel.towelette.api.LayerRegistrar;

@Mixin(ChunkRendererRegion.class)
public abstract class ChunkRendererRegionMixin implements BlockViewStateLayer
{
	@Shadow @Final int xSize;
	@Shadow @Final int ySize;
	@Shadow @Final int zSize;
	@Shadow @Final BlockState[] blockStates;
	@Shadow @Final FluidState[] fluidStates;
	
	@Shadow abstract int getIndex(BlockPos pos);
	
	@Unique private Object2ObjectLinkedOpenHashMap<Identifier, PropertyContainer<?>[]> states;
	
	@Inject(at = @At("RETURN"), method = "<init>(ISSS)V")
	private <O, S extends PropertyContainer<S>> void onConstruct(World world, int x, int z, WorldChunk[][] chunks, BlockPos from, BlockPos to, CallbackInfo info)
	{
		states = new Object2ObjectLinkedOpenHashMap<Identifier, PropertyContainer<?>[]>();
		
		boolean blockState = false;
		boolean fluidState = false;
		for (final Identifier id : LayerRegistrar.LAYERS.getIds())
		{
			final LayerData<O, S> layer = LayerRegistrar.getLayerData(id);
			
			final PropertyContainer<?>[] array;
			if (!blockState && layer == LayerRegistrar.BLOCK)
			{
				blockState = true;
				array = blockStates;
			}
			else if (!fluidState && layer == LayerRegistrar.FLUID)
			{
				fluidState = true;
				array = fluidStates;
			}
			else
			{
				array = new PropertyContainer[xSize * ySize * zSize];
				
				for (final BlockPos pos : BlockPos.iterate(from, to))
				{
					final int chunkX = (pos.getX() >> 4) - x;
					final int chunkZ = (pos.getZ() >> 4) - z;
					array[getIndex(pos)] = ((ChunkStateLayer) chunks[chunkX][chunkZ]).getState(layer, pos);
				}
			}
			
			states.put(id, array);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <O, S extends PropertyContainer<S>> S getState(LayerData<O, S> layer, BlockPos pos)
	{
		return (S) states.get(LayerRegistrar.LAYERS.getId(layer))[this.getIndex(pos)];
	}
}
