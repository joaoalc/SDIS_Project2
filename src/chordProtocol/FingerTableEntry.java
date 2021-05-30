package chordProtocol;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.Objects;

/**
 * The class FingerTableEntry represents an entry of a chord's finger table and contains a node's address and id
 */
public class FingerTableEntry implements Serializable {
    private int id;
    private InetSocketAddress value;

    /**
     * Constructor for the FingerTableEntry class
     *
     * @param id The id of the node
     * @param value The address of the node
     *
     */
    public FingerTableEntry(int id, InetSocketAddress value){
        this.id = id;
        this.value = value;
    }

    /**
     * Getter for the id attribute
     *
     * @return Returns the id attribute
     */
    public int getId() {
        return id;
    }

    /**
     * Setter for the id attribute
     *
     * @param  id The new value for the id attribute
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Getter for the value attribute
     *
     * @return Returns the value attribute
     */
    public InetSocketAddress getValue() {
        return value;
    }

    /**
     * Setter for the value attribute
     *
     * @param  value The new value for the value attribute
     */
    public void setValue(InetSocketAddress value) {
        this.value = value;
    }

    /**
     * Override of the equals method
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FingerTableEntry that = (FingerTableEntry) o;
        return id == that.id && Objects.equals(value, that.value);
    }
}