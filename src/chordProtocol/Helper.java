package chordProtocol;

public class Helper {

    public static boolean between(int n1, int n2, int n3){
        if (n1 < 0 || n2 < 0 || n3 < 0) return false;
        if (n1 < n3) return (n1 < n2 && n2 < n3);
        else return (n1 < n2 || n2 < n3);
    }
}