package virtuoel.towelette.mixin.layer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.PropertyContainer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;
import virtuoel.towelette.api.ChunkSectionStateLayer;
import virtuoel.towelette.api.LayerData;
import virtuoel.towelette.api.LayerRegistrar;

@Mixin(ServerWorld.class)
public class ServerWorldMixin
{
	@SuppressWarnings("unchecked")
	@Inject(method = "tickChunk", cancellable = true, at = @At(value = "INVOKE", shift = Shift.BEFORE, target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", ordinal = 1))
	private void tickChunkTickBlocks(WorldChunk chunk, int randomTickSpeed, CallbackInfo info)
	{
		final ServerWorld self = (ServerWorld) (Object) this;
		final ChunkPos chunkPos = chunk.getPos();
		final int x = chunkPos.getStartX();
		final int z = chunkPos.getStartZ();
		final Profiler profiler = self.getProfiler();
		
		if (randomTickSpeed > 0)
		{
			final ChunkSection[] sections = chunk.getSectionArray();
			final int length = sections.length;
			
			for (int i = 0; i < length; i++)
			{
				final ChunkSection section = sections[i];
				final ChunkSectionStateLayer sectionLayer = (ChunkSectionStateLayer) section;
				
				if (section != WorldChunk.EMPTY_SECTION && section.hasRandomTicks())
				{
					final int y = section.getYOffset();
					
					for (int j = 0; j < randomTickSpeed; ++j)
					{
						final BlockPos pos = self.getRandomPosInChunk(x, y, z, 15);
						profiler.push("randomTick");
						
						for(@SuppressWarnings("rawtypes") final LayerData layer : LayerRegistrar.LAYERS)
						{
							final PropertyContainer<?> state = sectionLayer.getState(layer, pos.getX() - x, pos.getY() - y, pos.getZ() - z);
							if(layer.hasRandomTicks(state))
							{
								layer.onRandomTick(state, self, pos, self.random);
							}
						}
						
						profiler.pop();
					}
				}
			}
		}
		
		info.cancel();
	}
}
