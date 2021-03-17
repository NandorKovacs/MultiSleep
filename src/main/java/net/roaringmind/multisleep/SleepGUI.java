package net.roaringmind.multisleep;

import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.WGridPanel;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.LiteralText;

public class SleepGUI extends LightweightGuiDescription {
  public SleepGUI(MultiSleep instance) {
      WGridPanel root = new WGridPanel();
      setRootPanel(root);
      root.setSize(128, 40);

      WButton yes = new WButton(new LiteralText("Sleep"));
      WButton no = new WButton(new LiteralText("Don't sleep"));
      WButton afk = new WButton(new LiteralText("AFK"));

      MinecraftClient mc = MinecraftClient.getInstance();
      ButtonClick clickYes = new ButtonClick(ClickTypes.YES, mc.player, instance);
      ButtonClick clickNo = new ButtonClick(ClickTypes.NO, mc.player, instance);
      ButtonClick clickAFK = new ButtonClick(ClickTypes.AFK, mc.player, instance);

      yes.setOnClick(clickYes);
      no.setOnClick(clickNo);
      afk.setOnClick(clickAFK);

      root.add(afk, 2, 5, 5, 1);
      root.add(yes, 0, 3, 4, 1);
      root.add(no, 5, 3, 4, 1);


      root.validate(this);
  }
}
