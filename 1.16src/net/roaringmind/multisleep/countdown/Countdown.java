package net.roaringmind.multisleep.countdown;

public class Countdown {
  int ticks;
  int maxTicks;

  public Countdown(int ticks) {
    maxTicks = ticks;
    this.ticks = -1;
  }

  public void restart() {
    ticks = maxTicks;
  }

  public void set(int tick) {
    ticks = tick;
  }

  public int tick() {
    ticks -= 1;
    return ticks;
  }
}
