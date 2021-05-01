package chordProtocol;

import java.net.InetSocketAddress;

public class FingerTableEntry {
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
}