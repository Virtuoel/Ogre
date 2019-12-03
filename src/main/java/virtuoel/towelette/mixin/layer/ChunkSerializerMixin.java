package virtuoel.towelette.mixin.layer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.village.PointOfInterestStorage;
import net.minecraft.world.ChunkSerializer;
import net.minecraft.world.ChunkTickScheduler;
import net.minecraft.world.biome.source.BiomeArray;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.ChunkNibbleArray;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.UpgradeData;
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import virtuoel.towelette.api.ChunkSectionStateLayer;
import virtuoel.towelette.api.LayerData;
import virtuoel.towelette.api.LayerRegistrar;

@Mixin(ChunkSerializer.class)
public class ChunkSerializerMixin
{
	@Inject(method = "deserialize", locals = LocalCapture.CAPTURE_FAILSOFT, at = @At(value = "INVOKE", shift = Shift.AFTER, target = "Lnet/minecraft/world/chunk/PalettedContainer;read(Lnet/minecraft/nbt/ListTag;[J)V"))
	private static void onDeserialize(ServerWorld world, StructureManager structureManager, PointOfInterestStorage poiStorage, ChunkPos pos, CompoundTag tag, CallbackInfoReturnable<ProtoChunk> info, ChunkGenerator<?> chunkGenerator, BiomeSource biomeSource, CompoundTag levelTag, BiomeArray biomes, UpgradeData upgradeData, ChunkTickScheduler<Block> blockTickScheduler, ChunkTickScheduler<Fluid> fluidTickScheduler, boolean isLightOn, ListTag sectionList, int noop, ChunkSection sections[], boolean hasSkyLight, ChunkManager chunkManager, LightingProvider lightingProvider, int i, CompoundTag sectionTag, int y, ChunkSection chunkSection)
	{
		if (sectionTag.contains("StateLayerData"))
		{
			final CompoundTag layerTag = sectionTag.getCompound("StateLayerData");
			for (final LayerData<?, ?> layer : LayerRegistrar.LAYERS)
			{
				if (layer == LayerRegistrar.BLOCK)
				{
					continue;
				}
				
				final Identifier id = LayerRegistrar.LAYERS.getId(layer);
				if (layerTag.contains(id.toString()))
				{
					final CompoundTag layerDataTag = layerTag.getCompound(id.toString());
					
					((ChunkSectionStateLayer) chunkSection).getContainer(id).ifPresent(container ->
					{
						container.read(layerDataTag.getList("Palette", 10), layerDataTag.getLongArray("States"));
					});
				}
			}
		}
	}
	
	@Inject(method = "serialize", locals = LocalCapture.CAPTURE_FAILSOFT, at = @At(value = "INVOKE", shift = Shift.AFTER, target = "Lnet/minecraft/world/chunk/PalettedContainer;write(Lnet/minecraft/nbt/CompoundTag;Ljava/lang/String;Ljava/lang/String;)V"))
	private static void onSerialize(ServerWorld world, Chunk chunk, CallbackInfoReturnable<CompoundTag> info, ChunkPos pos, CompoundTag tag, CompoundTag levelTag, ChunkSection sections[], ListTag sectionList, LightingProvider lightingProvider, boolean isLightOn, int i, int j, ChunkSection chunkSection, ChunkNibbleArray blockLight, ChunkNibbleArray skyLight, CompoundTag sectionTag)
	{
		final CompoundTag layerTag = new CompoundTag();
		for (final LayerData<?, ?> layer : LayerRegistrar.LAYERS)
		{
			if (layer == LayerRegistrar.BLOCK)
			{
				continue;
			}
			
			final Identifier id = LayerRegistrar.LAYERS.getId(layer);
			final CompoundTag layerDataTag = new CompoundTag();
			
			((ChunkSectionStateLayer) chunkSection).getContainer(id).ifPresent(container ->
			{
				container.write(layerDataTag, "Palette", "States");
			});
			
			layerTag.put(id.toString(), layerDataTag);
		}
		
		sectionTag.put("StateLayerData", layerTag);
	}
}
