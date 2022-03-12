package solver;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Move {
    public String word;
    public Integer wordCount;
    public Integer moveNumber;
    public Set<String> missed;
    public Map<Integer,Move> bucketToMove;
    
    public Move() {}
    
    public Move(String word, Integer wordCount, Integer moveNumber) {
        this.word = word;
        this.bucketToMove = new HashMap<Integer, Move>(Main.MAX_BUCKETS);
        this.missed = new HashSet<String>();
        this.wordCount = wordCount;
        this.moveNumber = moveNumber;
    }
}
