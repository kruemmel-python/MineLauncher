package de.yourname.rpg.npc;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RpgNpc {
    private String id;
    private UUID entityUuid;
    private String role;
    private String dialogId;
    private List<String> linkedQuests = new ArrayList<>();

    public RpgNpc() {
    }

    public RpgNpc(String id, UUID entityUuid) {
        this.id = id;
        this.entityUuid = entityUuid;
    }

    public String getId() {
        return id;
    }

    public UUID getEntityUuid() {
        return entityUuid;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getDialogId() {
        return dialogId;
    }

    public void setDialogId(String dialogId) {
        this.dialogId = dialogId;
    }

    public List<String> getLinkedQuests() {
        return linkedQuests;
    }
}
