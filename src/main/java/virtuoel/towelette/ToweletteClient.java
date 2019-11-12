package virtuoel.towelette;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import virtuoel.towelette.api.ModifiableWorldFluidLayer;
import virtuoel.towelette.util.PacketUtils;

public class ToweletteClient implements ClientModInitializer
{
	@Override
	public void onInitializeClient()
	{
		ClientSidePacketRegistry.INSTANCE.register(PacketUtils.DELTA, (packetContext, packetByteBuf) ->
		{
			final World world = packetContext.getPlayer().world;
			
			final ChunkPos chunkPos = new ChunkPos(packetByteBuf.readInt(), packetByteBuf.readInt());
			final int length = packetByteBuf.readVarInt();
			final BlockPos[] positions = new BlockPos[length];
			final FluidState[] states = new FluidState[length];
			
			for (int i = 0; i < length; i++)
			{
				short pos = packetByteBuf.readShort();
				positions[i] = chunkPos.toBlockPos(pos >> 12 & 15, pos & 255, pos >> 8 & 15);
				states[i] = Fluid.STATE_IDS.get(packetByteBuf.readVarInt());
			}
			
			packetContext.getTaskQueue().execute(() ->
			{
				for (int i = 0; i < length; i++)
				{
					((ModifiableWorldFluidLayer) world).setFluidState(positions[i], states[i], 19);
				}
			});
		});
		
		ClientSidePacketRegistry.INSTANCE.register(PacketUtils.UPDATE, (packetContext, packetByteBuf) ->
		{
			final World world = packetContext.getPlayer().world;
			
			final BlockPos pos = packetByteBuf.readBlockPos();
			final FluidState state = Fluid.STATE_IDS.get(packetByteBuf.readVarInt());
			
			packetContext.getTaskQueue().execute(() ->
			{
				((ModifiableWorldFluidLayer) world).setFluidState(pos, state, 19);
			});
		});
	}
}
