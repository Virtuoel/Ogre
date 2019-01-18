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
import net.minecraft.world.chunk.IdListPalette;
import net.minecraft.world.chunk.PalettedContainer;
import virtuoel.towelette.api.ChunkSectionFluidLayer;
import virtuoel.towelette.util.StateUtils;

@Mixin(ChunkSection.class)
public class ChunkSectionMixin implements ChunkSectionFluidLayer
{
	@Shadow short nonEmptyFluidCount;
	
	public final PalettedContainer<FluidState> fluidContainer = new PalettedContainer<FluidState>(new IdListPalette<FluidState>(Fluid.STATE_IDS, Fluids.EMPTY.getDefaultState()), Fluid.STATE_IDS, StateUtils::deserializeFluidState, StateUtils::serializeFluidState, Fluids.EMPTY.getDefaultState());
	
	@Inject(at = @At("RETURN"), method = "lock()V")
	public void onLock(CallbackInfo info)
	{
		fluidContainer.lock();
	}
	
	@Inject(at = @At("RETURN"), method = "unlock()V")
	public void onUnlock(CallbackInfo info)
	{
		fluidContainer.unlock();
	}
	
	@Inject(at = @At("RETURN"), method = "isEmpty()Z", cancellable = true)
	public void onIsEmpty(CallbackInfoReturnable<Boolean> info)
	{
		info.setReturnValue(info.getReturnValue() && nonEmptyFluidCount == 0);
	}
	
	@Override
	public FluidState setFluidState(int x, int y, int z, FluidState state, boolean synchronous)
	{
		FluidState old_state = synchronous ? fluidContainer.setSync(x, y, z, state) : fluidContainer.set(x, y, z, state);
		
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
	
	// TODO method_12256, get fluid state from blockState_1 and add to container if applicable
	
	
	
	
	
	
	@Inject(at = @At("HEAD"), method = "getFluidState(III)Lnet/minecraft/fluid/FluidState;", cancellable = true)
	public void onGetFluidState(int int_1, int int_2, int int_3, CallbackInfoReturnable<FluidState> info)
	{
		FluidState state = fluidContainer.get(int_1, int_2, int_3);
		if(!state.isEmpty())
		{
			info.setReturnValue(state);
		}
	}
	
	// TODO FIXME client-side only. Investigate. Maybe move to client-only mixin.
	@Inject(require = 0, at = @At("RETURN"), method = "fromPacket(Lnet/minecraft/util/PacketByteBuf;)V")
	public void fromPacket(PacketByteBuf buffer, CallbackInfo info)
	{
		this.nonEmptyFluidCount = buffer.readShort();
		this.fluidContainer.fromPacket(buffer);
	}
	
	@Inject(at = @At("RETURN"), method = "toPacket(Lnet/minecraft/util/PacketByteBuf;)V")
	public void toPacket(PacketByteBuf buffer, CallbackInfo info)
	{
		buffer.writeShort(this.nonEmptyFluidCount);
		this.fluidContainer.toPacket(buffer);
	}
	
	@Inject(at = @At("RETURN"), method = "getPacketSize()I", cancellable = true)
	public void onGetPacketSize(CallbackInfoReturnable<Integer> info)
	{
		info.setReturnValue(info.getReturnValue() + 2 + fluidContainer.getPacketSize());
	}
}
