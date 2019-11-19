package virtuoel.towelette;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.state.PropertyContainer;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import virtuoel.towelette.api.ModifiableWorldStateLayer;
import virtuoel.towelette.api.LayerData;
import virtuoel.towelette.api.LayerRegistrar;
import virtuoel.towelette.util.PacketUtils;

public class ToweletteClient implements ClientModInitializer
{
	@Override
	public void onInitializeClient()
	{
		ClientSidePacketRegistry.INSTANCE.register(PacketUtils.DELTA, ToweletteClient::handleDeltaPacket);
		ClientSidePacketRegistry.INSTANCE.register(PacketUtils.UPDATE, ToweletteClient::handleUpdatePacket);
	}
	
	private static <O, S extends PropertyContainer<S>> void handleDeltaPacket(PacketContext packetContext, PacketByteBuf packetByteBuf)
	{
		final World world = packetContext.getPlayer().world;
		
		final LayerData<O, S> layer = LayerRegistrar.getLayerData(packetByteBuf.readVarInt());
		
		final ChunkPos chunkPos = new ChunkPos(packetByteBuf.readInt(), packetByteBuf.readInt());
		final int length = packetByteBuf.readVarInt();
		final BlockPos[] positions = new BlockPos[length];
		@SuppressWarnings("unchecked")
		final S[] states = (S[]) new PropertyContainer[length];
		
		for (int i = 0; i < length; i++)
		{
			short pos = packetByteBuf.readShort();
			positions[i] = chunkPos.toBlockPos(pos >> 12 & 15, pos & 255, pos >> 8 & 15);
			states[i] = layer.getIds().get(packetByteBuf.readVarInt());
		}
		
		packetContext.getTaskQueue().execute(() ->
		{
			for (int i = 0; i < length; i++)
			{
				((ModifiableWorldStateLayer) world).setState(layer, positions[i], states[i], 19);
			}
		});
	}
	
	private static <O, S extends PropertyContainer<S>> void handleUpdatePacket(PacketContext packetContext, PacketByteBuf packetByteBuf)
	{
		final World world = packetContext.getPlayer().world;
		
		final BlockPos pos = packetByteBuf.readBlockPos();
		
		final LayerData<O, S> layer = LayerRegistrar.getLayerData(packetByteBuf.readVarInt());
		
		final S state = layer.getIds().get(packetByteBuf.readVarInt());
		
		packetContext.getTaskQueue().execute(() ->
		{
			((ModifiableWorldStateLayer) world).setState(layer, pos, state, 19);
		});
	}
}
