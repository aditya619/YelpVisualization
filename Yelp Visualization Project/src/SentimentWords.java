import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class SentimentWords {
    Set<String> positiveWords;
    Set<String> negativeWords;

    public SentimentWords() throws IOException {
        File posWordsFile = new File("positive-words.txt");
        BufferedReader posReader = new BufferedReader(new FileReader(
                posWordsFile));
        File negWordsFile = new File("negative-words.txt");
        BufferedReader negReader = new BufferedReader(new FileReader(
                negWordsFile));
        positiveWords = new HashSet<String>();
        negativeWords = new HashSet<String>();

        for (String posWord; (posWord = posReader.readLine()) != null;) {
            positiveWords.add(posWord.trim());
        }

        for (String negWord; (negWord = negReader.readLine()) != null;) {
            negativeWords.add(negWord.trim());
        }

        posReader.close();
        negReader.close();
    }

    public boolean isPositive(String word) {
        return this.positiveWords.contains(word);
    }

    public boolean isNegative(String word) {
        return this.negativeWords.contains(word);
    }

    public static void main(String[] args) throws IOException {
        SentimentWords s = new SentimentWords();
        System.out.println(s.positiveWords.size());
        System.out.println(s.negativeWords.size());
    }
}