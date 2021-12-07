package net.roaringmind.multisleep.gui;

import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.WGridPanel;
import io.github.cottonmc.cotton.gui.widget.WToggleButton;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.LiteralText;
import net.roaringmind.multisleep.MultiSleep;

public class SleepGUI extends LightweightGuiDescription {
  public SleepGUI(boolean phantomSwitch, boolean permaSwitch) {
    MultiSleep.log("sleepgui constructor");
    
    WGridPanel root = new WGridPanel();
    setRootPanel(root);
    root.setSize(20, 5);

    WButton yes = new WButton(new LiteralText("Sleep"));
    WButton no = new WButton(new LiteralText("Don't sleep"));
    WToggleButton permaSleep = new WToggleButton(new LiteralText("auto sleep"));
    WToggleButton phantoms = new WToggleButton(new LiteralText("phantoms"));

    yes.setOnClick(() -> {
      PacketByteBuf pckt = PacketByteBufs.create();
      pckt.writeInt(ClickTypes.YES.toInt());
      ClientPlayNetworking.send(MultiSleep.VOTE_PACKET_ID, pckt);
    });
    no.setOnClick(() -> {
      PacketByteBuf pckt = PacketByteBufs.create();
      pckt.writeInt(ClickTypes.NO.toInt());
      ClientPlayNetworking.send(MultiSleep.VOTE_PACKET_ID, pckt);
    });

    phantoms.setToggle(!phantomSwitch);
    phantoms.setOnToggle(on -> {
      PacketByteBuf pckt = PacketByteBufs.create();
      if (on) {
        pckt.writeInt(ClickTypes.PHANTOMYES.toInt());
        ClientPlayNetworking.send(MultiSleep.VOTE_PACKET_ID, pckt);
        return;
      }
      pckt.writeInt(ClickTypes.PHANTOMNO.toInt());
      ClientPlayNetworking.send(MultiSleep.VOTE_PACKET_ID, pckt);
    });

    permaSleep.setToggle(permaSwitch);
    permaSleep.setOnToggle(on -> {
      PacketByteBuf pckt = PacketByteBufs.create();
      if (on) {
        pckt.writeInt(ClickTypes.PERMAYES.toInt());
        ClientPlayNetworking.send(MultiSleep.VOTE_PACKET_ID, pckt);
        return;
      }
      pckt.writeInt(ClickTypes.PERMANO.toInt());
      ClientPlayNetworking.send(MultiSleep.VOTE_PACKET_ID, pckt);
    });

    root.add(permaSleep, 0, 2, 4, 1);
    root.add(yes, 0, 0, 4, 1);
    root.add(no, 5, 0, 4, 1);
    root.add(phantoms, 5, 2, 4, 1);

    root.validate(this);
  }
}