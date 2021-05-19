package net.roaringmind.multisleep.countdown;

public class Countdown {
  int ticks;
  int maxTicks;

  public Countdown(int ticks) {
    maxTicks = ticks;
    this.ticks = 0;
  }  

  public void restart() {
    ticks = maxTicks;
  }

  public int tick() {
    ticks -= 1;
    return ticks;
  }
}
