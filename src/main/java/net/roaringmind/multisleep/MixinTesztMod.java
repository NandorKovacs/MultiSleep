package net.roaringmind.multisleep;

import java.util.HashMap;
import java.util.Set;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;

public class MixinTesztMod implements ModInitializer {

  public static Logger LOGGER = LogManager.getLogger();

  public static final String MOD_ID = "mixinteszt";
  public static final String MOD_NAME = "MixinTeszt";

  @Override
  public void onInitialize() {
    log(Level.INFO, "Initializing");

    registerEvents();

  }

  public void registerEvents() {
    PlayerSleepCallback.EVENT.register((player, bed) -> {
      vote(player, VoteStates.YES);

      return ActionResult.FAIL;
    });

  }

  boolean voting = false;
  Set<PlayerEntity> afk;
  HashMap<PlayerEntity, VoteStates> votes;

  public void vote(PlayerEntity player, VoteStates vote) {
    if (!voting) {
      voting = true;
      votes.put(player, VoteStates.YES);

      for (PlayerEntity p : player.getEntityWorld().getPlayers()) {
        if (afk.contains(p)) {
          votes.put(p, VoteStates.YES);
        }
        //TODO: message them
      }
    }
  }

  public static void log(Level level, String message) {
    LOGGER.log(level, "[" + MOD_NAME + "] " + message);
  }

}
