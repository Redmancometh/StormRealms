package org.stormrealms.stormspigot;

import net.minecraft.server.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.NetworkManager;
import net.minecraft.server.PlayerConnection;

import net.minecraft.server.PacketPlayInSteerVehicle;
import net.minecraft.server.PacketPlayInVehicleMove;
import net.minecraft.server.PacketPlayInTeleportAccept;
import net.minecraft.server.PacketPlayInRecipeDisplayed;
import net.minecraft.server.PacketPlayInAdvancements;
import net.minecraft.server.PacketPlayInTabComplete;
import net.minecraft.server.PacketPlayInSetCommandBlock;
import net.minecraft.server.PacketPlayInSetCommandMinecart;
import net.minecraft.server.PacketPlayInPickItem;
import net.minecraft.server.PacketPlayInItemName;
import net.minecraft.server.PacketPlayInBeacon;
import net.minecraft.server.PacketPlayInStruct;
import net.minecraft.server.PacketPlayInSetJigsaw;
import net.minecraft.server.PacketPlayInTrSel;
import net.minecraft.server.PacketPlayInBEdit;
import net.minecraft.server.PacketPlayInEntityNBTQuery;
import net.minecraft.server.PacketPlayInTileNBTQuery;
import net.minecraft.server.PacketPlayInFlying;
import net.minecraft.server.PacketPlayInBlockDig;
import net.minecraft.server.PacketPlayInUseItem;
import net.minecraft.server.PacketPlayInBlockPlace;
import net.minecraft.server.PacketPlayInSpectate;
import net.minecraft.server.PacketPlayInResourcePackStatus;
import net.minecraft.server.PacketPlayInBoatMove;
import net.minecraft.server.PacketPlayInHeldItemSlot;
import net.minecraft.server.PacketPlayInChat;
import net.minecraft.server.PacketPlayInArmAnimation;
import net.minecraft.server.PacketPlayInEntityAction;
import net.minecraft.server.PacketPlayInUseEntity;
import net.minecraft.server.PacketPlayInClientCommand;
import net.minecraft.server.PacketPlayInCloseWindow;
import net.minecraft.server.PacketPlayInWindowClick;
import net.minecraft.server.PacketPlayInAutoRecipe;
import net.minecraft.server.PacketPlayInEnchantItem;
import net.minecraft.server.PacketPlayInSetCreativeSlot;
import net.minecraft.server.PacketPlayInTransaction;
import net.minecraft.server.PacketPlayInUpdateSign;
import net.minecraft.server.PacketPlayInKeepAlive;
import net.minecraft.server.PacketPlayInAbilities;
import net.minecraft.server.PacketPlayInSettings;
import net.minecraft.server.PacketPlayInCustomPayload;
import net.minecraft.server.PacketPlayInDifficultyChange;
import net.minecraft.server.PacketPlayInDifficultyLock;

public class PlayerConnectionOverride extends PlayerConnection {
    public PlayerConnectionOverride(MinecraftServer minecraftserver, NetworkManager networkmanager, EntityPlayer entityplayer) {
        super(minecraftserver, networkmanager, entityplayer);
    }

    @Override
    public void a(PacketPlayInSteerVehicle packetPlayInSteerVehicle) {
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInSteerVehicle, this, (PacketPlayInSteerVehicle packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInVehicleMove packetPlayInVehicleMove) {
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInVehicleMove, this, (PacketPlayInVehicleMove packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInTeleportAccept packetPlayInTeleportAccept) {
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInTeleportAccept, this, (PacketPlayInTeleportAccept packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInRecipeDisplayed packetPlayInRecipeDisplayed) {
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInRecipeDisplayed, this, (PacketPlayInRecipeDisplayed packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInAdvancements packetPlayInAdvancements) {
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInAdvancements, this, (PacketPlayInAdvancements packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInTabComplete packetPlayInTabComplete) {
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInTabComplete, this, (PacketPlayInTabComplete packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInSetCommandBlock packetPlayInSetCommandBlock) {
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInSetCommandBlock, this, (PacketPlayInSetCommandBlock packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInSetCommandMinecart packetPlayInSetCommandMinecart) {
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInSetCommandMinecart, this, (PacketPlayInSetCommandMinecart packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInPickItem packetPlayInPickItem) {
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInPickItem, this, (PacketPlayInPickItem packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInItemName packetPlayInItemName) {
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInItemName, this, (PacketPlayInItemName packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInBeacon packetPlayInBeacon) {
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInBeacon, this, (PacketPlayInBeacon packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInStruct packetPlayInStruct) {
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInStruct, this, (PacketPlayInStruct packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInSetJigsaw packetPlayInSetJigsaw) {
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInSetJigsaw, this, (PacketPlayInSetJigsaw packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInTrSel packetPlayInTrSel) {
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInTrSel, this, (PacketPlayInTrSel packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInBEdit packetPlayInBEdit) {
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInBEdit, this, (PacketPlayInBEdit packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInEntityNBTQuery packetPlayInEntityNBTQuery) {
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInEntityNBTQuery, this, (PacketPlayInEntityNBTQuery packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInTileNBTQuery packetPlayInTileNBTQuery) {
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInTileNBTQuery, this, (PacketPlayInTileNBTQuery packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInFlying packetPlayInFlying) {
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInFlying, this, (PacketPlayInFlying packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInBlockDig packetPlayInBlockDig) {
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInBlockDig, this, (PacketPlayInBlockDig packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInUseItem packetPlayInUseItem) {
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInUseItem, this, (PacketPlayInUseItem packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInBlockPlace packetPlayInBlockPlace) {
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInBlockPlace, this, (PacketPlayInBlockPlace packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInSpectate packetPlayInSpectate) {
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInSpectate, this, (PacketPlayInSpectate packet) -> super.a(packet));
    }

    // CraftBukkit start
    public void a(PacketPlayInResourcePackStatus packetPlayInResourcePackStatus) {
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInResourcePackStatus, this, (PacketPlayInResourcePackStatus packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInBoatMove packetPlayInBoatMove) {
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInBoatMove, this, (PacketPlayInBoatMove packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInHeldItemSlot packetPlayInHeldItemSlot) {
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInHeldItemSlot, this, (PacketPlayInHeldItemSlot packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInChat packetPlayInChat) {
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInChat, this, (PacketPlayInChat packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInArmAnimation packetPlayInArmAnimation) {
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInArmAnimation, this, (PacketPlayInArmAnimation packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInEntityAction packetPlayInEntityAction) {
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInEntityAction, this, (PacketPlayInEntityAction packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInUseEntity packetPlayInUseEntity) {
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInUseEntity, this, (PacketPlayInUseEntity packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInClientCommand packetPlayInClientCommand) {
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInClientCommand, this, (PacketPlayInClientCommand packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInCloseWindow packetPlayInCloseWindow) {
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInCloseWindow, this, (PacketPlayInCloseWindow packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInWindowClick packetPlayInWindowClick) {
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInWindowClick, this, (PacketPlayInWindowClick packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInAutoRecipe packetPlayInAutoRecipe) {
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInAutoRecipe, this, (PacketPlayInAutoRecipe packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInEnchantItem packetPlayInEnchantItem) {
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInEnchantItem, this, (PacketPlayInEnchantItem packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInSetCreativeSlot packetPlayInSetCreativeSlot) {
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInSetCreativeSlot, this, (PacketPlayInSetCreativeSlot packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInTransaction packetPlayInTransaction) {
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInTransaction, this, (PacketPlayInTransaction packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInUpdateSign packetPlayInUpdateSign) {
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInUpdateSign, this, (PacketPlayInUpdateSign packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInKeepAlive packetPlayInKeepAlive) {
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInKeepAlive, this, (PacketPlayInKeepAlive packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInAbilities packetPlayInAbilities) {
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInAbilities, this, (PacketPlayInAbilities packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInSettings packetPlayInSettings) {
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInSettings, this, (PacketPlayInSettings packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInCustomPayload packetPlayInCustomPayload) {
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInCustomPayload, this, (PacketPlayInCustomPayload packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInDifficultyChange packetPlayInDifficultyChange) {
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInDifficultyChange, this, (PacketPlayInDifficultyChange packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInDifficultyLock packetPlayInDifficultyLock) {
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInDifficultyLock, this, (PacketPlayInDifficultyLock packet) -> super.a(packet));
    }

}
