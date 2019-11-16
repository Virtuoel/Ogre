package virtuoel.towelette.util;

import io.netty.buffer.Unpooled;
import net.minecraft.client.network.packet.CustomPayloadS2CPacket;
import net.minecraft.state.PropertyContainer;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.chunk.WorldChunk;
import virtuoel.towelette.Towelette;
import virtuoel.towelette.api.BlockViewStateLayer;
import virtuoel.towelette.api.ChunkStateLayer;
import virtuoel.towelette.api.PaletteData;
import virtuoel.towelette.api.PaletteRegistrar;

public class PacketUtils
{
	public static final Identifier UPDATE = Towelette.id("state_update");
	public static final Identifier DELTA = Towelette.id("delta_update");
	
	public static CustomPayloadS2CPacket stateUpdate(PaletteData<?, ?> data, BlockView world, BlockPos pos)
	{
		final PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer()).writeBlockPos(pos);
		buffer.writeVarInt(PaletteRegistrar.PALETTES.getRawId(data));
		buffer.writeVarInt(data.getIds().getId(((BlockViewStateLayer) world).getState(PaletteRegistrar.PALETTES.getId(data), pos)));
		return new CustomPayloadS2CPacket(UPDATE, buffer);
	}
	
	public static CustomPayloadS2CPacket deltaUpdate(PaletteData<?, ?> data, int records, short[] positions, WorldChunk chunk)
	{
		return new CustomPayloadS2CPacket(DELTA, writeDelta(data, new PacketByteBuf(Unpooled.buffer()), records, positions, chunk));
	}
	
	private static <O, S extends PropertyContainer<S>> PacketByteBuf writeDelta(PaletteData<O, S> data, PacketByteBuf buffer, int records, short[] positions, WorldChunk chunk)
	{
		final ChunkPos chunkPos = chunk.getPos();
		
		buffer.writeVarInt(PaletteRegistrar.PALETTES.getRawId(data));
		
		buffer.writeInt(chunkPos.x);
		buffer.writeInt(chunkPos.z);
		buffer.writeVarInt(records);
		
		for (int i = 0; i < records; ++i)
		{
			buffer.writeShort(positions[i]);
			buffer.writeVarInt(data.getIds().getId(((ChunkStateLayer) chunk).getState(PaletteRegistrar.PALETTES.getId(data), toBlockPos(chunkPos, positions[i]))));
		}
		
		return buffer;
	}
	
	private static BlockPos toBlockPos(ChunkPos chunkPos, short pos)
	{
		return new BlockPos(chunkPos.toBlockPos(pos >> 12 & 15, pos & 255, pos >> 8 & 15));
	}
}
