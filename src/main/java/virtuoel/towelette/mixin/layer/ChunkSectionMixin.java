package virtuoel.towelette.mixin.layer;

import java.util.Map;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
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
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.state.PropertyContainer;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.Palette;
import net.minecraft.world.chunk.PalettedContainer;
import virtuoel.towelette.api.ChunkSectionStateLayer;
import virtuoel.towelette.api.PaletteData;
import virtuoel.towelette.api.PaletteRegistrar;

@Mixin(ChunkSection.class)
public class ChunkSectionMixin implements ChunkSectionStateLayer
{
	@Shadow @Final static Palette<BlockState> palette;
	@Shadow @Final PalettedContainer<BlockState> container;
	
	@Unique private Object2ObjectLinkedOpenHashMap<Identifier, MutablePair<PalettedContainer<?>, Integer>> palettes;
	
	static
	{
		PaletteRegistrar.PALETTES.add(PaletteRegistrar.BLOCK_STATE,
			PaletteData.<Block, BlockState>builder()
			.palette(palette)
			.ids(Block.STATE_IDS)
			.deserializer(PaletteRegistrar::deserializeBlockState)
			.serializer(PaletteRegistrar::serializeBlockState)
			.emptyPredicate(BlockState::isAir)
			.invalidPositionSupplier(Blocks.VOID_AIR::getDefaultState)
			.lightUpdatePredicate(PaletteRegistrar::shouldUpdateBlockStateLight)
			.registry(Registry.BLOCK)
			.entryFunction(BlockState::getBlock)
			.defaultStateFunction(Block::getDefaultState)
			.managerFunction(Block::getStateFactory)
			.emptyStateSupplier(Blocks.AIR::getDefaultState)
			.build()
		);
	}
	
	@Inject(at = @At("RETURN"), method = "<init>(ISSS)V")
	private void onConstruct(int yOffset, short nonEmptyBlockCount, short randomTickableBlockCount, short nonEmptyFluidCount, CallbackInfo info)
	{
		palettes = new Object2ObjectLinkedOpenHashMap<Identifier, MutablePair<PalettedContainer<?>, Integer>>();
		
		palettes.put(PaletteRegistrar.BLOCK_STATE, new MutablePair<PalettedContainer<?>, Integer>(container, 0));
		
		boolean blockState = false;
		for(final Identifier id : PaletteRegistrar.PALETTES.getIds())
		{
			if(!blockState && id.equals(PaletteRegistrar.BLOCK_STATE))
			{
				blockState = true;
				continue;
			}
			
			palettes.put(id, new MutablePair<PalettedContainer<?>, Integer>(PaletteRegistrar.getPaletteData(id).createContainer(), 0));
		}
	}
	
	@Inject(at = @At("HEAD"), method = "lock()V", cancellable = true)
	public void onLock(CallbackInfo info)
	{
		palettes.values().forEach(pair ->
		{
			pair.getLeft().lock();
		});
		
		info.cancel();
	}
	
	@Inject(at = @At("HEAD"), method = "unlock()V", cancellable = true)
	public void onUnlock(CallbackInfo info)
	{
		palettes.values().forEach(pair ->
		{
			pair.getLeft().unlock();
		});
		
		info.cancel();
	}
	
	@Inject(at = @At("RETURN"), method = "isEmpty()Z", cancellable = true)
	public void onIsEmpty(CallbackInfoReturnable<Boolean> info)
	{
		info.setReturnValue(info.getReturnValue() && palettes.values().stream().allMatch(p -> p.getRight() == 0));
	}
	
	@Inject(at = @At("HEAD"), method = "getFluidState(III)Lnet/minecraft/fluid/FluidState;", cancellable = true)
	public void onGetFluidState(int x, int y, int z, CallbackInfoReturnable<FluidState> info)
	{
		info.setReturnValue(this.<Fluid, FluidState>getState(PaletteRegistrar.FLUID_STATE, x, y, z));
	}
	
	@Inject(require = 0, at = @At("RETURN"), method = "fromPacket(Lnet/minecraft/util/PacketByteBuf;)V")
	public void onFromPacket(PacketByteBuf buffer, CallbackInfo info)
	{
		palettes.values().forEach(pair ->
		{
			final int count = buffer.readShort();
			pair.setRight(count);
			pair.getLeft().fromPacket(buffer);
		});
	}
	
	@Inject(at = @At("RETURN"), method = "toPacket(Lnet/minecraft/util/PacketByteBuf;)V")
	public void onToPacket(PacketByteBuf buffer, CallbackInfo info)
	{
		palettes.values().forEach(pair ->
		{
			buffer.writeShort(pair.getRight());
			pair.getLeft().toPacket(buffer);
		});
	}
	
	@Inject(at = @At("RETURN"), method = "getPacketSize()I", cancellable = true)
	public void onGetPacketSize(CallbackInfoReturnable<Integer> info)
	{
		final int total = palettes.entrySet().stream().map(Map.Entry::getValue).map(Pair::getLeft).mapToInt(PalettedContainer::getPacketSize).sum();
		info.setReturnValue(info.getReturnValue() + (2 * palettes.size()) + total);
	}
	
	@Override
	public <O, S extends PropertyContainer<S>> S setState(Identifier layer, int x, int y, int z, S state, boolean synchronous)
	{
		final MutablePair<PalettedContainer<?>, Integer> holder = palettes.get(layer);
		@SuppressWarnings("unchecked")
		final PalettedContainer<S> container = ((PalettedContainer<S>) holder.getLeft());
		final S old_state = synchronous ? container.setSync(x, y, z, state) : container.set(x, y, z, state);
		
		final PaletteData<O, S> data = PaletteRegistrar.getPaletteData(layer);
		
		if(!data.isEmpty(old_state))
		{
			holder.setRight(holder.getRight() - 1);
		}
		
		if(!data.isEmpty(state))
		{
			holder.setRight(holder.getRight() + 1);
		}
		
		return old_state;
	}
	
	@Override
	public <O, S extends PropertyContainer<S>> S getState(Identifier layer, int x, int y, int z)
	{
		final MutablePair<PalettedContainer<?>, Integer> data = palettes.get(layer);
		@SuppressWarnings("unchecked")
		final PalettedContainer<S> container = ((PalettedContainer<S>) data.getLeft());
		
		return container.get(x, y, z);
	}
}
