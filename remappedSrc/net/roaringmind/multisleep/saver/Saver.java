package net.roaringmind.multisleep.saver;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.PersistentState;

public class Saver extends PersistentState {
  Set<UUID> noPhantomPlayers = new HashSet<>();
  Set<UUID> permaSleepPlayers = new HashSet<>();

  public Saver(String key) {
    super(key);
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

  @Override
  public void fromNbt(NbtCompound tag) {
    NbtCompound phantomTag = tag.getCompound("phantom");
    for (String k : phantomTag.getKeys()) {
      noPhantomPlayers.add(UUID.fromString(k));
    }

    NbtCompound permaTag = tag.getCompound("perma");
    for (String k : permaTag.getKeys()) {
      permaSleepPlayers.add(UUID.fromString(k));
    }
  }

  @Override
  public NbtCompound writeNbt(NbtCompound tag) {
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
    return tag;
  }

}
