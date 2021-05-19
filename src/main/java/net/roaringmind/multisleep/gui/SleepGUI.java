package net.roaringmind.multisleep.gui;

import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.WGridPanel;
import io.github.cottonmc.cotton.gui.widget.WToggleButton;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.LiteralText;
import net.roaringmind.multisleep.MultiSleep;

public class SleepGUI extends LightweightGuiDescription {
  public SleepGUI() {
    WGridPanel root = new WGridPanel();
    setRootPanel(root);

    WButton yes = new WButton(new LiteralText("Sleep"));
    WButton no = new WButton(new LiteralText("Don't sleep"));
    WToggleButton permaSleep = new WToggleButton(new LiteralText("Perma Sleep"));
    WToggleButton phantoms = new WToggleButton(new LiteralText("Phantoms"));

    MinecraftClient mc = MinecraftClient.getInstance();

    ButtonClick clickYes = new ButtonClick(ClickTypes.YES, mc.player);
    ButtonClick clickNo = new ButtonClick(ClickTypes.NO, mc.player);

    yes.setOnClick(clickYes);
    no.setOnClick(clickNo);

    phantoms.setToggle(MultiSleep.wantsPhantoms.contains(mc.player.getUuid()));
    phantoms.setOnToggle(on -> MultiSleep.setPhantomPreferences(mc.player.getUuid(), on));

    permaSleep.setToggle(MultiSleep.permaSleepPlayers.contains(mc.player.getUuid()));
    permaSleep.setOnToggle(on -> MultiSleep.setPermaSleep(mc.player.getUuid(), on));

    root.add(permaSleep, 0, 5, 4, 1);
    root.add(yes, 0, 3, 4, 1);
    root.add(no, 5, 3, 4, 1);
    root.add(phantoms, 5, 5, 4, 1);

    root.validate(this);
  }
}
