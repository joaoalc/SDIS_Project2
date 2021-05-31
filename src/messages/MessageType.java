package messages;

/**
 *  Enumeration to represent the various message types
 */
public enum MessageType {
    ALIVE,
    CHECK,
    FIND_PREDECESSOR,
    FIND_SUCCESSOR,
    NOTIFICATION,
    NOTIFIED,
    PREDECESSOR,
    SUCCESSOR,
    JOIN,
    PUTCHUNK,
    STORED,
    FAILED,
    DELETE,
    DELETED,
    GETCHUNK,
    CHUNK,
    INFOSUCC,
    INFOPRED,
    DECREASE_REP_DEGREE
}
