package net.yigitguven.petting.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

import java.util.function.Supplier;

public class SendPetSettingsPacket {
    public int entityId;
    public boolean sitStill;
    public boolean waiting;
    public boolean isTamed;
    public boolean attackIfOwnerAttacks;
    public boolean attackIfOwnerAttacked;
    public boolean attackIfSelfAttacked;
    public boolean damageOwner;
    public boolean ignoreWhistle;
    public int followDistance;
    public int teleportDistance;
    public boolean openScreen;
    public String controlRightClick;
    public String controlShiftRightClick;

    public SendPetSettingsPacket(int entityId, boolean sitStill, boolean waiting, boolean isTamed, boolean attackIfOwnerAttacks, boolean attackIfOwnerAttacked, boolean attackIfSelfAttacked, boolean damageOwner, boolean ignoreWhistle, int followDistance, int teleportDistance, boolean openScreen, String controlRightClick, String controlShiftRightClick) {
        this.entityId = entityId;
        this.sitStill = sitStill;
        this.waiting = waiting;
        this.isTamed = isTamed;
        this.attackIfOwnerAttacks = attackIfOwnerAttacks;
        this.attackIfOwnerAttacked = attackIfOwnerAttacked;
        this.attackIfSelfAttacked = attackIfSelfAttacked;
        this.damageOwner = damageOwner;
        this.ignoreWhistle = ignoreWhistle;
        this.followDistance = followDistance;
        this.teleportDistance = teleportDistance;
        this.openScreen = openScreen;
        this.controlRightClick = controlRightClick;
        this.controlShiftRightClick = controlShiftRightClick;
    }

    public SendPetSettingsPacket(FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
        this.sitStill = buf.readBoolean();
        this.waiting = buf.readBoolean();
        this.isTamed = buf.readBoolean();
        this.attackIfOwnerAttacks = buf.readBoolean();
        this.attackIfOwnerAttacked = buf.readBoolean();
        this.attackIfSelfAttacked = buf.readBoolean();
        this.damageOwner = buf.readBoolean();
        this.ignoreWhistle = buf.readBoolean();
        this.followDistance = buf.readInt();
        this.teleportDistance = buf.readInt();
        this.openScreen = buf.readBoolean();
        this.controlRightClick = buf.readUtf(32767);
        this.controlShiftRightClick = buf.readUtf(32767);
    }

    public static void encode(SendPetSettingsPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.entityId);
        buf.writeBoolean(msg.sitStill);
        buf.writeBoolean(msg.waiting);
        buf.writeBoolean(msg.isTamed);
        buf.writeBoolean(msg.attackIfOwnerAttacks);
        buf.writeBoolean(msg.attackIfOwnerAttacked);
        buf.writeBoolean(msg.attackIfSelfAttacked);
        buf.writeBoolean(msg.damageOwner);
        buf.writeBoolean(msg.ignoreWhistle);
        buf.writeInt(msg.followDistance);
        buf.writeInt(msg.teleportDistance);
        buf.writeBoolean(msg.openScreen);
        buf.writeUtf(msg.controlRightClick);
        buf.writeUtf(msg.controlShiftRightClick);
    }

    public static void handle(SendPetSettingsPacket msg, Supplier<NetworkEvent.Context> contextSupplier) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> net.yigitguven.petting.client.PettingClientPacketHandler.handleSendPetSettings(msg, contextSupplier));
    }
}
