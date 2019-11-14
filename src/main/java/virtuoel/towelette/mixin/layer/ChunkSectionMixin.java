package virtuoel.towelette.mixin.layer;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.tuple.MutablePair;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.Palette;
import net.minecraft.world.chunk.PalettedContainer;
import virtuoel.towelette.Towelette;
import virtuoel.towelette.api.ChunkSectionFluidLayer;
import virtuoel.towelette.api.PaletteRegistrar;
import virtuoel.towelette.api.ToweletteApi;

@Mixin(ChunkSection.class)
public class ChunkSectionMixin implements ChunkSectionFluidLayer
{
	@Shadow @Final static Palette<BlockState> palette;
	
	private final Map<Identifier, MutablePair<PalettedContainer<?>, Integer>> palettes = new HashMap<Identifier, MutablePair<PalettedContainer<?>,Integer>>();
	
	static
	{
		PaletteRegistrar.registerPaletteBuilder(PaletteRegistrar.PALETTES.add(new Identifier(ToweletteApi.MOD_ID, "block_states"), palette), Block.STATE_IDS, PaletteRegistrar::deserializeBlockState, PaletteRegistrar::serializeBlockState, Blocks.AIR.getDefaultState());
		PaletteRegistrar.registerPaletteBuilder(new Identifier(ToweletteApi.MOD_ID, "fluid_states"), Fluid.STATE_IDS, PaletteRegistrar::deserializeFluidState, PaletteRegistrar::serializeFluidState, Fluids.EMPTY.getDefaultState());
	}
	
	@Shadow short nonEmptyFluidCount;
	
	public final PalettedContainer<FluidState> fluidContainer = PaletteRegistrar.getBuilder(PaletteRegistrar.FLUID_STATES).get();
	
	@Override
	public PalettedContainer<FluidState> getFluidStateContainer()
	{
		return fluidContainer;
	}
	
	@Inject(at = @At("RETURN"), method = "lock()V")
	public void onLock(CallbackInfo info)
	{
		getFluidStateContainer().lock();
	}
	
	@Inject(at = @At("RETURN"), method = "unlock()V")
	public void onUnlock(CallbackInfo info)
	{
		getFluidStateContainer().unlock();
	}
	
	@Inject(at = @At("RETURN"), method = "isEmpty()Z", cancellable = true)
	public void onIsEmpty(CallbackInfoReturnable<Boolean> info)
	{
		info.setReturnValue(info.getReturnValue() && nonEmptyFluidCount == 0);
	}
	
	@Override
	public FluidState setFluidState(int x, int y, int z, FluidState state, boolean synchronous)
	{
		FluidState old_state = synchronous ? getFluidStateContainer().setSync(x, y, z, state) : getFluidStateContainer().set(x, y, z, state);
		
		if(!old_state.isEmpty())
		{
			--this.nonEmptyFluidCount;
		}
		
		if(!state.isEmpty())
		{
			++this.nonEmptyFluidCount;
		}
		
		return old_state;
	}
	
	@Inject(at = @At("HEAD"), method = "getFluidState(III)Lnet/minecraft/fluid/FluidState;", cancellable = true)
	public void onGetFluidState(int x, int y, int z, CallbackInfoReturnable<FluidState> info)
	{
		final FluidState state = getFluidStateContainer().get(x, y, z);
		if(Towelette.isLayerView((ChunkSection) (Object) this, x, y, z, state))
		{
			info.setReturnValue(state);
		}
	}
	
	@Inject(at = @At("RETURN"), method = "toPacket(Lnet/minecraft/util/PacketByteBuf;)V")
	public void onToPacket(PacketByteBuf buffer, CallbackInfo info)
	{
		buffer.writeShort(this.nonEmptyFluidCount);
		getFluidStateContainer().toPacket(buffer);
	}
	
	@Inject(at = @At("RETURN"), method = "getPacketSize()I", cancellable = true)
	public void onGetPacketSize(CallbackInfoReturnable<Integer> info)
	{
		info.setReturnValue(info.getReturnValue() + 2 + getFluidStateContainer().getPacketSize());
	}
}
