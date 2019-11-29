package virtuoel.towelette.mixin.layer;

import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.fluid.FluidState;
import net.minecraft.state.PropertyContainer;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.PalettedContainer;
import virtuoel.towelette.api.ChunkSectionStateLayer;
import virtuoel.towelette.api.LayerData;
import virtuoel.towelette.api.LayerRegistrar;
import virtuoel.towelette.util.LayeredPalettedContainerHolder;

@Mixin(ChunkSection.class)
public class ChunkSectionMixin implements ChunkSectionStateLayer, LayeredPalettedContainerHolder
{
	@Shadow @Final PalettedContainer<BlockState> container;
	
	@Unique private Object2ObjectLinkedOpenHashMap<Identifier, MutableTriple<PalettedContainer<?>, Short, Short>> palettedContainers;
	
	@Override
	public Map<Identifier, MutableTriple<PalettedContainer<?>, Short, Short>> getPalettedContainerDataMap()
	{
		return palettedContainers;
	}
	
	@Inject(at = @At("RETURN"), method = "<init>(ISSS)V")
	private void onConstruct(int yOffset, short nonEmptyBlockCount, short randomTickableBlockCount, short nonEmptyFluidCount, CallbackInfo info)
	{
		palettedContainers = new Object2ObjectLinkedOpenHashMap<>();
		
		for (final Identifier id : LayerRegistrar.LAYERS.getIds())
		{
			final LayerData<?, ?> data = LayerRegistrar.LAYERS.get(id);
			palettedContainers.put(id, new MutableTriple<>(data == LayerRegistrar.BLOCK ? this.container : data.createContainer(), (short) 0, (short) 0));
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <O, S extends PropertyContainer<S>> Optional<PalettedContainer<S>> getContainer(Identifier id)
	{
		if (palettedContainers.containsKey(id))
		{
			return Optional.of((PalettedContainer<S>) palettedContainers.get(id).getLeft());
		}
		
		return Optional.empty();
	}
	
	@Inject(at = @At("HEAD"), method = "calculateCounts()V", cancellable = true)
	public <O, S extends PropertyContainer<S>> void onCalculateCounts(CallbackInfo info)
	{
		palettedContainers.entrySet().forEach(entry ->
		{
			final LayerData<O, S> layer = LayerRegistrar.getLayerData(entry.getKey());
			final MutableTriple<PalettedContainer<?>, Short, Short> data = entry.getValue();
			
			data.setMiddle((short) 0);
			data.setRight((short) 0);
			
			@SuppressWarnings("unchecked")
			final PalettedContainer<S> container = ((PalettedContainer<S>) data.getLeft());
			
			container.method_21732((state, value) ->
			{
				if (!layer.isEmpty(state))
				{
					data.setMiddle((short) (data.getMiddle() + value));
					if (layer.hasRandomTicks(state))
					{
						data.setRight((short) (data.getRight() + value));
					}
				}
			});
		});
		
		info.cancel();
	}
	
	@Inject(at = @At("HEAD"), method = "lock()V", cancellable = true)
	public void onLock(CallbackInfo info)
	{
		palettedContainers.values().forEach(data ->
		{
			data.getLeft().lock();
		});
		
		info.cancel();
	}
	
	@Inject(at = @At("HEAD"), method = "unlock()V", cancellable = true)
	public void onUnlock(CallbackInfo info)
	{
		palettedContainers.values().forEach(data ->
		{
			data.getLeft().unlock();
		});
		
		info.cancel();
	}
	
	@Inject(at = @At("RETURN"), method = "isEmpty()Z", cancellable = true)
	public void onIsEmpty(CallbackInfoReturnable<Boolean> info)
	{
		palettedContainers.values().forEach(data ->
		{
			if (data.getMiddle() != 0)
			{
				info.setReturnValue(false);
				return;
			}
		});
	}
	
	@Inject(at = @At("HEAD"), method = "hasRandomTicks()Z", cancellable = true)
	public void onHasRandomTicks(CallbackInfoReturnable<Boolean> info)
	{
		info.setReturnValue(palettedContainers.values().stream().anyMatch(data -> data.getRight() > 0));
	}
	
	@Inject(at = @At("HEAD"), method = "hasRandomBlockTicks()Z", cancellable = true)
	public void onHasRandomBlockTicks(CallbackInfoReturnable<Boolean> info)
	{
		info.setReturnValue(palettedContainers.get(LayerRegistrar.LAYERS.getId(LayerRegistrar.BLOCK)).getRight() > 0);
	}
	
	@Inject(at = @At("HEAD"), method = "hasRandomFluidTicks()Z", cancellable = true)
	public void onHasRandomFluidTicks(CallbackInfoReturnable<Boolean> info)
	{
		info.setReturnValue(palettedContainers.get(LayerRegistrar.LAYERS.getId(LayerRegistrar.FLUID)).getRight() > 0);
	}
	
	@Inject(at = @At("HEAD"), method = "setBlockState(IIILnet/minecraft/block/BlockState;Z)Lnet/minecraft/block/BlockState;", cancellable = true)
	public void setBlockState(int x, int y, int z, BlockState state, boolean flag, CallbackInfoReturnable<BlockState> info)
	{
		final Block block = state.getBlock();
		if (block instanceof FluidBlock)
		{
			setState(LayerRegistrar.FLUID, x, y, z, block.getFluidState(state), flag);
			info.setReturnValue(state);
		}
		else
		{
			info.setReturnValue(setState(LayerRegistrar.BLOCK, x, y, z, state, flag));
		}
	}
	
	@Inject(at = @At("HEAD"), method = "getFluidState(III)Lnet/minecraft/fluid/FluidState;", cancellable = true)
	public void onGetFluidState(int x, int y, int z, CallbackInfoReturnable<FluidState> info)
	{
		info.setReturnValue(getState(LayerRegistrar.FLUID, x, y, z));
	}
	
	@Inject(at = @At("RETURN"), method = "toPacket(Lnet/minecraft/util/PacketByteBuf;)V")
	public void onToPacket(PacketByteBuf buffer, CallbackInfo info)
	{
		palettedContainers.values().forEach(data ->
		{
			buffer.writeShort(data.getMiddle());
			data.getLeft().toPacket(buffer);
		});
	}
	
	@Inject(at = @At("RETURN"), method = "getPacketSize()I", cancellable = true)
	public void onGetPacketSize(CallbackInfoReturnable<Integer> info)
	{
		final int total = palettedContainers.entrySet().stream().map(Map.Entry::getValue).map(Triple::getLeft).mapToInt(PalettedContainer::getPacketSize).sum();
		info.setReturnValue(info.getReturnValue() + (2 * palettedContainers.size()) + total);
	}
	
	@Override
	public <O, S extends PropertyContainer<S>> S setState(LayerData<O, S> layer, int x, int y, int z, S state, boolean synchronous)
	{
		final MutableTriple<PalettedContainer<?>, Short, Short> data = palettedContainers.get(LayerRegistrar.LAYERS.getId(layer));
		@SuppressWarnings("unchecked")
		final PalettedContainer<S> container = ((PalettedContainer<S>) data.getLeft());
		final S old_state = synchronous ? container.setSync(x, y, z, state) : container.set(x, y, z, state);
		
		if (!layer.isEmpty(old_state))
		{
			data.setMiddle((short) (data.getMiddle() - 1));
			if (layer.hasRandomTicks(old_state))
			{
				data.setRight((short) (data.getRight() - 1));
			}
		}
		
		if (!layer.isEmpty(state))
		{
			data.setMiddle((short) (data.getMiddle() + 1));
			if (layer.hasRandomTicks(state))
			{
				data.setRight((short) (data.getRight() + 1));
			}
		}
		
		return old_state;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <O, S extends PropertyContainer<S>> S getState(LayerData<O, S> layer, int x, int y, int z)
	{
		return (S) palettedContainers.get(LayerRegistrar.LAYERS.getId(layer)).getLeft().get(x, y, z);
	}
}
