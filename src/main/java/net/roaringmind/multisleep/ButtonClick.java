package net.roaringmind.multisleep;

import net.minecraft.entity.player.PlayerEntity;

public class ButtonClick implements Runnable {
  public ClickTypes vote;
  public MultiSleep instance;
  public PlayerEntity player;

  ButtonClick(ClickTypes vote, PlayerEntity player, MultiSleep instance) {
    this.vote = vote;
    this.instance = instance;
    this.player = player;
  }

  public void run() {
    switch (vote) {
    case YES:
      break;
    case NO:
      break;
    case AFK:
      break;
    }

  }
}
