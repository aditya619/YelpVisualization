import java.io.IOException;
import java.io.StringReader;
import java.text.BreakIterator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;

class Parser {

    private final static String PCG_MODEL = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";

    private final TokenizerFactory<CoreLabel> tokenizerFactory = PTBTokenizer
            .factory(new CoreLabelTokenFactory(), "invertible=true");

    private final LexicalizedParser parser = LexicalizedParser.loadModel(
            PCG_MODEL, "-maxLength", "80", "-retainTmpSubcategories");

    HashMap<Integer, String> posByIndex;
    HashMap<Integer, Boolean> opinionIndeces;

    public Tree parse(String str) {
        List<CoreLabel> tokens = tokenize(str);
        Tree tree = parser.apply(tokens);
        return tree;
    }

    private List<CoreLabel> tokenize(String str) {
        Tokenizer<CoreLabel> tokenizer = tokenizerFactory
                .getTokenizer(new StringReader(str));
        return tokenizer.tokenize();
    }

    public FeatureOpinion parseReview(Review reviewObj, FeatureOpinion fe)
            throws IOException {
        String sentence = null;
        SentimentWords s = new SentimentWords();
        BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.US);
        String reviewText = reviewObj.reviewText;
        iterator.setText(reviewText);
        int start = iterator.first();
        for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator
                .next()) {
            sentence = reviewText.substring(start, end);
            sentence = sentence.replaceAll("[^a-zA-Z]+$", "");
            System.out.println(sentence);

            this.posByIndex = new HashMap<Integer, String>();
            this.opinionIndeces = new HashMap<Integer, Boolean>();
            int indexCount = 1;
            Tree tree = parser.parse(sentence.toLowerCase(Locale.US));
            List<Tree> leaves = tree.getLeaves();
            // Print words and Pos Tags
            for (Tree leaf : leaves) {
                Tree parent = leaf.parent(tree);
                System.out.print(leaf.label().value() + "-"
                        + parent.label().value() + " ");
                if (parent.label().value().startsWith("JJ")
                        || parent.label().value().startsWith("RB")) {
                    if (s.isPositive(leaf.label().value())) {
                        this.opinionIndeces.put(indexCount, true);
                    } else if (s.isNegative(leaf.label().value())) {
                        this.opinionIndeces.put(indexCount, false);
                    }
                }
                this.posByIndex.put(indexCount, parent.label().value());
                indexCount++;
            }
            System.out.println();
            System.out.println("Opinion Indeces " + this.opinionIndeces);
            TreebankLanguagePack tlp = new PennTreebankLanguagePack();
            GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
            GrammaticalStructure gs = gsf.newGrammaticalStructure(tree);
            List<TypedDependency> tdl = gs.typedDependenciesCCprocessed();
            // Handling negation - 2 words before and after
            Set<Integer> negatedIndeces = new HashSet<Integer>();
            for (TypedDependency t : tdl) {
                if (t.reln().toString().trim() == "neg") {
                    int notIndex = t.dep().index();
                    for (int i = notIndex - 2; i < notIndex + 3; i++) {
                        if (this.opinionIndeces.containsKey(i)) {
                            boolean val = this.opinionIndeces.get(i);
                            this.opinionIndeces.put(i, !val);
                            negatedIndeces.add(i);
                        }
                    }
                }
            }
            if (fe == null)
                fe = new FeatureOpinion(this.posByIndex, tdl,
                        this.opinionIndeces, reviewObj, negatedIndeces);
            else
                fe.updateForNewSentence(this.posByIndex, tdl,
                        this.opinionIndeces, reviewObj, negatedIndeces);
            System.out.println("TDL : " + tdl);
            if (this.opinionIndeces.size() > 0) {
                fe.extractFeaturesFromOpinions();
            }
        }
        return fe;
    }
}