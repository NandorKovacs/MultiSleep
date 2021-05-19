package net.roaringmind.multisleep.gui;

import net.minecraft.entity.player.PlayerEntity;
import net.roaringmind.multisleep.MultiSleep;

public class ButtonClick implements Runnable {
  public ClickTypes vote;
  public PlayerEntity player;

  ButtonClick(ClickTypes vote, PlayerEntity player) {
    this.vote = vote;
    this.player = player;
  }

  public void run() {
    System.out.println("im here");
    MultiSleep.playerClick(player, vote);
  }
}
