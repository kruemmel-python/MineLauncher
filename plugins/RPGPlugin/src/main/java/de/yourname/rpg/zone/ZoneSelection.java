package de.yourname.rpg.zone;

public class ZoneSelection {
    private ZonePosition pos1;
    private ZonePosition pos2;

    public ZonePosition getPos1() {
        return pos1;
    }

    public void setPos1(ZonePosition pos1) {
        this.pos1 = pos1;
    }

    public ZonePosition getPos2() {
        return pos2;
    }

    public void setPos2(ZonePosition pos2) {
        this.pos2 = pos2;
    }

    public void reset() {
        this.pos1 = null;
        this.pos2 = null;
    }

    public boolean isComplete() {
        return pos1 != null && pos2 != null;
    }
}
