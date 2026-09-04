package spaceicebreaker.models;

public enum GameClass {
    SCOUT,
    TANK,
    DAMAGE_DEALER;

    @Override
    public String toString() {
        if (this.equals(SCOUT)) {
            return "Scout";
        } else if (this.equals(TANK)) {
            return "Tank";
        }

        return "Damage dealer";
    }
}
