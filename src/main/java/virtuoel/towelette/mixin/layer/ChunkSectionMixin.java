package virtuoel.towelette.mixin.layer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.PalettedContainer;
import virtuoel.towelette.Towelette;
import virtuoel.towelette.api.ChunkSectionFluidLayer;
import virtuoel.towelette.util.StateUtils;

@Mixin(ChunkSection.class)
public class ChunkSectionMixin implements ChunkSectionFluidLayer
{
	@Shadow short nonEmptyFluidCount;
	
	public final PalettedContainer<FluidState> fluidContainer = new PalettedContainer<FluidState>(StateUtils.FLUID_STATE_PALETTE, Fluid.STATE_IDS, StateUtils::deserializeFluidState, StateUtils::serializeFluidState, Fluids.EMPTY.getDefaultState());
	
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
