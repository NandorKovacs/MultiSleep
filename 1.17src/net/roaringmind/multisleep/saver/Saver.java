package net.roaringmind.multisleep.saver;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.PersistentState;
import net.roaringmind.multisleep.MultiSleep;

public class Saver extends PersistentState {
  private Set<UUID> noPhantomPlayers = new HashSet<>();
  private Set<UUID> permaSleepPlayers = new HashSet<>();

  public Saver() {
  }

  public void addPhantomPlayer(UUID uuid) {
    noPhantomPlayers.add(uuid);
    markDirty();
  }

  public void removePhantomPlayer(UUID uuid) {
    noPhantomPlayers.remove(uuid);
    markDirty();
  }

  public boolean phantomContainsPlayer(UUID uuid) {
    return noPhantomPlayers.contains(uuid);
  }

  public void addPermaPlayer(UUID uuid) {
    permaSleepPlayers.add(uuid);
    markDirty();
  }

  public void removePermaPlayer(UUID uuid) {
    permaSleepPlayers.remove(uuid);
    markDirty();
  }

  public boolean permaContainsPlayer(UUID uuid) {
    return permaSleepPlayers.contains(uuid);
  }

  public int permaSize() {
    return permaSleepPlayers.size();
  }

  public void log() {
    MultiSleep.log("perma:");
    for (UUID uuid : permaSleepPlayers) {
      MultiSleep.log("          " + uuid);
    }
  
    MultiSleep.log("phantom:");
    for (UUID uuid : noPhantomPlayers) {
      MultiSleep.log("          " + uuid);
    }
  }

  @Override
  public NbtCompound writeNbt(NbtCompound tag) {
    MultiSleep.log("saver write start");
    NbtCompound phantomTag = new NbtCompound();
    for (UUID uuid : noPhantomPlayers) {
      phantomTag.putBoolean(uuid.toString(), true);
    }

    NbtCompound permaTag = new NbtCompound();
    for (UUID uuid : permaSleepPlayers) {
      permaTag.putBoolean(uuid.toString(), true);
    }

    tag.put("phantom", phantomTag);
    tag.put("perma", permaTag);

    MultiSleep.log("saver write over");
    return tag;
  }

}
