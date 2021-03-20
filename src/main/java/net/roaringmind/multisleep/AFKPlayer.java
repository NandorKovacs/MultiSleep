package net.roaringmind.multisleep;

import net.minecraft.entity.player.PlayerEntity;

public class AFKPlayer {
  public PlayerEntity player;
  public double x;
  public double y;
  public double z;

  public AFKPlayer(PlayerEntity player) {
    this.player = player;
    x = player.getX();
    y = player.getY();
    z = player.getZ();
  }

  public boolean check() {
    if (player.getX() == x && player.getY() == y && player.getZ() == z) {
      return true;
    }
    return false;
  }
}
