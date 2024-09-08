package me.shedaniel.materialisation.network;

import me.shedaniel.materialisation.Materialisation;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public class RenamePayload implements CustomPayload {
    public static final CustomPayload.Id<RenamePayload> ID = new Id<>(Materialisation.MATERIALISING_TABLE_RENAME);
    public static final PacketCodec<PacketByteBuf, RenamePayload> CODEC = PacketCodecs.STRING.xmap(RenamePayload::new, RenamePayload::getData).cast();
    public String data;

    public RenamePayload(String data) {
        this.data = data;
    }

    public String getData() {
        return data;
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
