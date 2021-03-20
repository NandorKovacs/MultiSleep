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
      if (instance.voting == true) {
        instance.vote(true, player);
      }
      break;
    case NO:

      if (instance.voting == true) {
        instance.vote(false, player);
      }
      break;
    case AFK:
    AFKPlayer afkPlayer = new AFKPlayer(player);
      if (!instance.afkPlayers.containsKey(player.getUuid())) {
        instance.afkPlayers.put(player.getUuid(), afkPlayer);
      }
      break;
    }

  }
}
