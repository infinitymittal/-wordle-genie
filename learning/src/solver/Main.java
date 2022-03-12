package solver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;  

public class Main {
    
    public static final Integer MAX_BUCKETS = 243;
    private static final int MAX_ATTEMPTS = 10;
    private static final int WORD_LENGTH = 5;
    private static final String WORD_LIST = "./wordList.txt";
    
    private static int getMatchBucket(String secretWord, String attemptWord) {
        int ans = 0;
        for(int i=0;i<WORD_LENGTH;i++) {
            ans*=3;
            if(attemptWord.charAt(i)==secretWord.charAt(i))
                ans+=2;
            else if(secretWord.contains(""+attemptWord.charAt(i))) {
                for(int j=0;j<WORD_LENGTH;j++) {
                    if(i!=j && secretWord.charAt(j)==attemptWord.charAt(i) && secretWord.charAt(j)!=attemptWord.charAt(j)) {
                        ans+=1;
                        break;
                    }
                }
            }
        }
        return ans;
    }

    private static int getMatchBucketOld(String secretWord, String attemptWord) {
        int ans = 0;
        for(int i=0;i<WORD_LENGTH;i++) {
            ans*=3;
            if(attemptWord.charAt(i)==secretWord.charAt(i))
                ans+=2;
            else if(secretWord.contains(""+attemptWord.charAt(i))) 
                ans+=1;
        }
        return ans;
    }
    
    private static String readableBucket(int bucket) {
        char[] ans = new char[WORD_LENGTH];
        for(int i=0;i<WORD_LENGTH;i++) {
            switch(bucket%3) {
            case 0: ans[WORD_LENGTH-i-1] = 'B'; break;
            case 1: ans[WORD_LENGTH-i-1] = 'Y'; break;
            case 2: ans[WORD_LENGTH-i-1] = 'G'; break;
            }
            bucket = bucket/3;
        }
        return new String(ans);
    }

    public static void main(String args[]) {
        //solve();
        List<String> allWords = readWords();
//        simulate("fills", allWords);
//        System.out.println(move.bucketToMove.get(39).bucketToMove.get(39).word);
        //simulate("bares", allWords);
        System.out.println(simulateAll());
//        System.out.println(readableBucket(getMatchBucket("repay", "meter")));
    }

    public static int simulate(String secretWord, List<String> allWords) {
        Function<String, Integer> bucketProvider = (attempt) -> getMatchBucket(secretWord, attempt);
        Move start = importMoves("bestresult");
        int attempts = play(bucketProvider, start, allWords);
        //System.out.println(attempts);
        return attempts;
    }

    public static double simulateAll() {
        List<String> allWords = readWords();
        return allWords.stream().map(word->simulate(word, allWords)).mapToInt(x->x).average().getAsDouble();
    }

    private static int play(Function<String,Integer> bucketProvider, Move move, List<String> words) {
        if(move.word.isEmpty())
            return 100;
        int bucket = bucketProvider.apply(move.word);
        words = getBucketToWords(words, move.word).get(bucket);
        String bucketName = readableBucket(bucket);
/*        System.out.println(String.join(" ", Integer.toString(move.moveNumber), Integer.toString(move.wordCount), move.word,
           bucketName, move.missed==null?"?":Integer.toString(move.missed.size())));
        if(words.size()<40)
            System.out.println(words);
*/        if(bucketName.equals("GGGGG"))
            return move.moveNumber;
        move = move.bucketToMove.get(bucket);
        return play(bucketProvider, move, words);
    }

    private static Move importMoves(String name) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(Paths.get(name+".json").toFile(), Move.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void solve() {
        List<String> allWords = readWords();
        System.out.println(findBestAttempts(allWords, allWords));
        Move start = findBestMoveTopLayer(allWords, 1, allWords);
        System.out.println(start.word);
        System.out.println(start.missed.size());
        System.out.println(start.missed);
        System.out.println(start.bucketToMove.get(0).word);
        export(start, "best");
    }

    private static void export(Move start, String prefix) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(Paths.get(prefix+"result.json").toFile(), start);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Move findBestMoveTopLayer(List<String> words, int attemptCount, List<String> allWords) {
      String[] attempts = {"nares"};
//        String[] attempts = {"nares"};
        Move bestMove = null;
        int missedMax = Integer.MAX_VALUE;
        for(int i=0;i<attempts.length;i++) {
            String attempt = attempts[i];
            System.out.println("Attempting "+attempt);
            Instant start = Instant.now();
            System.out.println("Started at "+start);
            Move move = new Move(attempt, words.size(), attemptCount);
            Map<Integer, List<String>> bucketToWords = getBucketToWords(words, attempt);
            move.bucketToMove = bucketToWords.keySet().stream()
                .collect(Collectors.toMap(Function.identity(), 
                    bucket-> //findBestMove(bucketToWords.get(bucket), attemptCount+1, allWords)
                        {
                        Move findBestMove = findBestMove(bucketToWords.get(bucket), attemptCount+1, allWords);
                        System.out.println("done "+bucket+" sized "+bucketToWords.get(bucket).size());
                        return findBestMove;
                        }
                    ));
            System.out.println("Took minutes: "+Duration.between(start, Instant.now()).toMinutes());
            move.missed = move.bucketToMove.values().stream().parallel().unordered()
                .map(x->x.missed).flatMap(Set::stream)
                .collect(Collectors.toSet());
            System.out.println("Took minutes: "+Duration.between(start, Instant.now()).toMinutes());
            System.out.println("Found misses for "+attempt+" "+move.missed.size());
            export(move, attempt);
            if(move.missed.isEmpty())
                return move;
            if(move.missed.size()<missedMax) {
                missedMax = move.missed.size();
                bestMove = move;
            }
        }
        return bestMove;
    }

    private static Move findBestMove(List<String> words, int attemptCount, List<String> allWords) {
        if(attemptCount>6) {
            Move move = new Move("", 0, attemptCount);
            move.missed = new HashSet<String>(words);
            return move;
        }
        if(words.size()==1)
            return new Move(words.get(0), 1, attemptCount);
        List<String> attempts = findBestAttempts(words, allWords);
        Move bestMove = null;
        int missedMax = Integer.MAX_VALUE;
        for(int i=0;i<MAX_ATTEMPTS;i++) {
            String attempt = attempts.get(i);
            Move move = new Move(attempt, words.size(), attemptCount);
            Map<Integer, List<String>> bucketToWords = getBucketToWords(words, attempt);
            move.bucketToMove = bucketToWords.keySet().stream().parallel().unordered()
                .collect(Collectors.toConcurrentMap(Function.identity(), 
                    bucket->findBestMove(bucketToWords.get(bucket), attemptCount+1, allWords)));
            move.missed = move.bucketToMove.values().stream().parallel().unordered()
                .map(x->x.missed).flatMap(Set::stream)
                .collect(Collectors.toSet());
            if(move.missed.isEmpty()) {
                return move;
            }
            if(move.missed.size()<missedMax) {
                missedMax = move.missed.size();
                bestMove = move;
            }
        }
        return bestMove;
    }

    private static Map<Integer, List<String>> getBucketToWords(List<String> words, String attempt) {
        Map<Integer,List<String>> bucketToWords = words.stream()
            .collect(Collectors.groupingBy(word-> getMatchBucket(word, attempt)));
        return bucketToWords;
    }

    private static List<String> findBestAttempts(List<String> words, List<String> allWords) {
        Map<String, Long> attemptToMetric = allWords.stream()
            .collect(Collectors.toMap(Function.identity(), attemptWord->maxBucketSize(words, attemptWord)));
        List<String> attemptsSorted = attemptToMetric.keySet().stream()
            .sorted((w1,w2)->Long.compare(attemptToMetric.get(w1), attemptToMetric.get(w2)))
            .limit(MAX_ATTEMPTS)
            .collect(Collectors.toList());
        return attemptsSorted;
    }

    private static long maxBucketSize(List<String> words, String attemptWord) {
        return words.stream()
            .collect(Collectors.groupingBy(secretWord->getMatchBucket(secretWord, attemptWord), Collectors.counting()))
            .values().stream().max(Comparator.naturalOrder()).get();
    }

    private static List<String> readWords() {
        try {
            return Files.lines(Paths.get(WORD_LIST))
                .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}