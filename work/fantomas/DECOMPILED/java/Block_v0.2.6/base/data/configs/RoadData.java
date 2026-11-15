/*
 * Decompiled with CFR 0.152.
 */
package base.data.configs;

public class RoadData {
    public RoadObject[] lights;
    public RoadObject[] stations;
    public Pikets[] pikets;

    public class Pikets {
        public int km;
        public int piket;
        public int coord;
    }

    public class RoadObject {
        public String name;
        public int coord;
    }
}

