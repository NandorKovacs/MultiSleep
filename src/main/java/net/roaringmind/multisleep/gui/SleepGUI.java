package net.roaringmind.multisleep.gui;

import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.WGridPanel;
import io.github.cottonmc.cotton.gui.widget.WToggleButton;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.LiteralText;

public class SleepGUI extends LightweightGuiDescription {
  public SleepGUI(boolean phantomState) {
    WGridPanel root = new WGridPanel();
    // root.setSize(128, 10);
    setRootPanel(root);

    WButton yes = new WButton(new LiteralText("Sleep"));
    WButton no = new WButton(new LiteralText("Don't sleep"));
    WButton afk = new WButton(new LiteralText("AFK"));
    WToggleButton phantoms = new WToggleButton(new LiteralText("Phantoms"));

    MinecraftClient mc = MinecraftClient.getInstance();

    ButtonClick clickYes = new ButtonClick(ClickTypes.YES, mc.player);
    ButtonClick clickNo = new ButtonClick(ClickTypes.NO, mc.player);
    ButtonClick clickAFK = new ButtonClick(ClickTypes.AFK, mc.player);

    yes.setOnClick(clickYes);
    no.setOnClick(clickNo);
    afk.setOnClick(clickAFK);

    phantoms.setToggle(phantomState);
    phantoms.setOnToggle(on -> {
      //something something phantoms
    });

    root.add(afk, 0, 5, 4, 1);
    root.add(yes, 0, 3, 4, 1);
    root.add(no, 5, 3, 4, 1);
    root.add(phantoms, 5, 5, 4, 1);

    root.validate(this);
  }
}