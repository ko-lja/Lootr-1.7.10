package lu.kolja.lootr.worldgen;

import com.github.bsideup.jabel.Desugar;

@Desugar
public record ChestPos(int dimension, int x, int y, int z) {

    @Override
    public boolean equals(Object o) {
        return o instanceof ChestPos other && dimension == other.dimension
            && x == other.x
            && y == other.y
            && z == other.z;
    }

    @Override
    public int hashCode() {
        int result = dimension;
        result = 31 * result + x;
        result = 31 * result + y;
        result = 31 * result + z;
        return result;
    }

    @Override
    public String toString() {
        return "dim " + dimension + " at " + x + "," + y + "," + z;
    }
}
