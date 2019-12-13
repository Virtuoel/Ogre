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
import net.minecraft.state.State;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import virtuoel.towelette.api.BlockViewStateLayer;
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
	
	@Unique private Object2ObjectLinkedOpenHashMap<Identifier, State<?>[]> states;
	
	@Inject(at = @At("RETURN"), method = "<init>(Lnet/minecraft/world/World;II[[Lnet/minecraft/world/chunk/WorldChunk;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;)V")
	private <O, S extends State<S>> void onConstruct(World world, int chunkX, int chunkZ, WorldChunk[][] chunks, BlockPos startPos, BlockPos endPos, CallbackInfo info)
	{
		states = new Object2ObjectLinkedOpenHashMap<Identifier, State<?>[]>();
		
		for (final Identifier id : LayerRegistrar.LAYERS.getIds())
		{
			states.put(id, LayerRegistrar.getLayerData(id).createRendererRegionStateArray(world, chunkX, chunkZ, chunks, startPos, endPos, xSize, ySize, zSize, blockStates, fluidStates));
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <O, S extends State<S>> S getState(LayerData<O, S> layer, BlockPos pos)
	{
		return (S) states.get(LayerRegistrar.LAYERS.getId(layer))[this.getIndex(pos)];
	}
}
