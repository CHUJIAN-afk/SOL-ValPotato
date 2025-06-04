package org.mcteampotato.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;
import org.mcteampotato.SOLValpotato;
import org.mcteampotato.attchment.FoodDataAttachment;


public record SyncFoodDataPacket(CompoundTag foodInstances) implements CustomPacketPayload {
    public static final Type<SyncFoodDataPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(SOLValpotato.MOD_ID, "food_data_attachment"));
    public static final StreamCodec<ByteBuf, SyncFoodDataPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG,
            SyncFoodDataPacket::foodInstances,
            SyncFoodDataPacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(()->{
            Player player = context.player();
            if (player.isLocalPlayer()){
                FoodDataAttachment foodData = new FoodDataAttachment();
                foodData.deserialize(this.foodInstances);
                player.setData(SOLValpotato.FOOD_DATA, foodData);
           }
        }).exceptionally(throwable -> {
            context.disconnect(Component.translatable("neoforge.network.invalid_flow", throwable.getMessage()));
            return null;
        });
    }
}
