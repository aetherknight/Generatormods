package generatormods.common;

public enum RoofStyle {
    CRENEL("Crenel"), STEEP("Steep"), TRIM("Seep Trim"), SHALLOW("Shallow"), DOME("Dome"), CONE(
            "Cone"), TWO_SIDED("Two Sided");

    public static final RoofStyle[] styles = new RoofStyle[] {CRENEL, STEEP, TRIM, SHALLOW, DOME,
            CONE, TWO_SIDED};
    public static final String[] names;
    static {
        names = new String[styles.length];
        for (int i = 0; i < styles.length; i++)
            names[i] = styles[i].name;
    }

    public final String name;

    private RoofStyle(String name) {
        this.name = name;
    }
}
