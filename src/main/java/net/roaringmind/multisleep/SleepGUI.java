package net.roaringmind.multisleep;

import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.WGridPanel;
import net.minecraft.text.LiteralText;

public class SleepGUI extends LightweightGuiDescription {
  public SleepGUI() {
      WGridPanel root = new WGridPanel();
      setRootPanel(root);
      root.setSize(128, 40);

      WButton yes = new WButton(new LiteralText("Sleep"));
      WButton no = new WButton(new LiteralText("Don't sleep"));
      WButton permanent = new WButton(new LiteralText("AFK"));



      root.add(permanent, 2, 5, 5, 1);
      root.add(yes, 0, 3, 4, 1);
      root.add(no, 5, 3, 4, 1);


      root.validate(this);
  }
}
