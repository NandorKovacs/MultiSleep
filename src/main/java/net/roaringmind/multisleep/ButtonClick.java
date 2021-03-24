package net.roaringmind.multisleep;

import net.minecraft.entity.player.PlayerEntity;
import net.roaringmind.multisleep.callbacks.VoteClickCallback;

public class ButtonClick implements Runnable {
  public ClickTypes vote;
  public PlayerEntity player;

  ButtonClick(ClickTypes vote, PlayerEntity player) {
    this.vote = vote;
    this.player = player;
  }

  public void run() {
    VoteClickCallback.EVENT.invoker().interact(player, vote);
  }
}
