package net.yigitguven.petting.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.yigitguven.petting.Petting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class PetSavedData extends SavedData {

    public record PlayerPetsEntry(UUID playerUuid, List<UUID> petUuids) {
        public static final Codec<PlayerPetsEntry> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        UUIDUtil.CODEC.fieldOf("player").forGetter(PlayerPetsEntry::playerUuid),
                        UUIDUtil.CODEC.listOf().fieldOf("pets").forGetter(PlayerPetsEntry::petUuids)
                ).apply(instance, PlayerPetsEntry::new)
        );
    }

    public static final Codec<PetSavedData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    PlayerPetsEntry.CODEC.listOf().fieldOf("entries").forGetter(PetSavedData::toEntries)
            ).apply(instance, PetSavedData::new)
    );

    public static final SavedDataType<PetSavedData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(Petting.MODID, "pets"),
            PetSavedData::new,
            CODEC
    );

    private final Map<UUID, Set<UUID>> playerPets = new HashMap<>();
    private final Map<UUID, UUID> petOwner = new HashMap<>();

    public PetSavedData() {
    }

    public PetSavedData(List<PlayerPetsEntry> entries) {
        for (PlayerPetsEntry entry : entries) {
            Set<UUID> pets = new HashSet<>(entry.petUuids());
            playerPets.put(entry.playerUuid(), pets);
            for (UUID petUuid : pets) {
                petOwner.put(petUuid, entry.playerUuid());
            }
        }
    }

    public List<PlayerPetsEntry> toEntries() {
        List<PlayerPetsEntry> entries = new ArrayList<>();
        for (Map.Entry<UUID, Set<UUID>> entry : playerPets.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                entries.add(new PlayerPetsEntry(entry.getKey(), new ArrayList<>(entry.getValue())));
            }
        }
        return entries;
    }

    public static PetSavedData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public int getPetCount(UUID playerUuid) {
        Set<UUID> pets = playerPets.get(playerUuid);
        return pets != null ? pets.size() : 0;
    }

    public boolean addPet(UUID playerUuid, UUID petUuid) {
        UUID previousOwner = petOwner.get(petUuid);
        if (previousOwner != null && !previousOwner.equals(playerUuid)) {
            Set<UUID> prevPets = playerPets.get(previousOwner);
            if (prevPets != null) {
                prevPets.remove(petUuid);
                if (prevPets.isEmpty()) {
                    playerPets.remove(previousOwner);
                }
            }
        }
        petOwner.put(petUuid, playerUuid);
        boolean added = playerPets.computeIfAbsent(playerUuid, k -> new HashSet<>()).add(petUuid);
        if (added || previousOwner != null) {
            setDirty();
        }
        return added;
    }

    public boolean removePet(UUID petUuid) {
        UUID owner = petOwner.remove(petUuid);
        if (owner != null) {
            Set<UUID> pets = playerPets.get(owner);
            if (pets != null) {
                boolean removed = pets.remove(petUuid);
                if (pets.isEmpty()) {
                    playerPets.remove(owner);
                }
                setDirty();
                return removed;
            }
        }
        return false;
    }

    public Optional<UUID> getOwner(UUID petUuid) {
        return Optional.ofNullable(petOwner.get(petUuid));
    }
}
