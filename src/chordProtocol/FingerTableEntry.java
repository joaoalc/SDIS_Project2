package chordProtocol;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.Objects;

public class FingerTableEntry implements Serializable {
    private int id;
    private InetSocketAddress value;

    public FingerTableEntry(int id, InetSocketAddress value){
        this.id = id;
        this.value = value;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public InetSocketAddress getValue() {
        return value;
    }

    public void setValue(InetSocketAddress value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FingerTableEntry that = (FingerTableEntry) o;
        return id == that.id && Objects.equals(value, that.value);
    }
}