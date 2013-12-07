import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import edu.stanford.nlp.trees.TypedDependency;

public class FeatureOpinion {
    HashMap<Integer, String> posByIndex;
    List<TypedDependency> typeDP;
    HashMap<String, HashMap<String, Double>> posFOList;
    HashMap<String, HashMap<String, Double>> negFOList;
    HashMap<Integer, Boolean> opinionIndeces;
    Review reviewObj;
    Set<Integer> negatedIndeces;

    public FeatureOpinion(HashMap<Integer, String> posByIndex,
            List<TypedDependency> typeDP,
            HashMap<Integer, Boolean> opinionIndeces, Review reviewObj,
            Set<Integer> negatedIndeces) {
        super();
        this.posByIndex = posByIndex;
        this.typeDP = typeDP;
        this.opinionIndeces = opinionIndeces;
        this.posFOList = new HashMap<String, HashMap<String, Double>>();
        this.negFOList = new HashMap<String, HashMap<String, Double>>();
        this.reviewObj = reviewObj;
        this.negatedIndeces = negatedIndeces;
    }

    public void extractFeaturesFromOpinions() {
        for (TypedDependency dp : typeDP) {
            if (this.opinionIndeces.containsKey(dp.dep().index())
                    || this.opinionIndeces.containsKey(dp.gov().index())) {
                // Check for NN-amod-JJ or NN-nsubj-JJ
                if (dp.reln().toString().trim() == "nsubj"
                        || dp.reln().toString().trim() == "amod") {

                    String depPos = posByIndex.get(dp.dep().index());
                    String govPos = posByIndex.get(dp.gov().index());
                    if ((govPos.startsWith("NN") && depPos.startsWith("JJ"))
                            || (govPos.startsWith("JJ") && depPos
                                    .startsWith("NN"))) {

                        String nnWord = (govPos.startsWith("NN") ? dp.gov()
                                .toString() : dp.dep().toString());
                        int nnIndex = (govPos.startsWith("NN") ? dp.gov()
                                .index() : dp.dep().index());
                        String jjWord = (govPos.startsWith("JJ") ? dp.gov()
                                .toString() : dp.dep().toString());
                        int jjIndex = (govPos.startsWith("JJ") ? dp.gov()
                                .index() : dp.dep().index());
                        String extendedFeature = extendFeature(nnIndex, nnWord,
                                jjIndex);

                        if (opinionIndeces.get(jjIndex))
                            addFeatureOpinion(posFOList, extendedFeature,
                                    jjWord, true,
                                    negatedIndeces.contains(jjIndex));
                        else
                            addFeatureOpinion(negFOList, extendedFeature,
                                    jjWord, false,
                                    negatedIndeces.contains(jjIndex));

                    }
                }

                // Check for VB-advmod-RB
                if (dp.reln().toString().trim() == "advmod") {
                    String depPos = posByIndex.get(dp.dep().index());
                    String govPos = posByIndex.get(dp.gov().index());
                    if ((govPos.startsWith("VB") && depPos.startsWith("RB"))
                            || (govPos.startsWith("RB") && depPos
                                    .startsWith("VB"))) {

                        String vbWord = (govPos.startsWith("VB") ? dp.gov()
                                .toString() : dp.dep().toString());
                        String rbWord = (govPos.startsWith("RB") ? dp.gov()
                                .toString() : dp.dep().toString());
                        int rbIndex = (govPos.startsWith("RB") ? dp.gov()
                                .index() : dp.dep().index());
                        if (opinionIndeces.get(rbIndex))
                            addFeatureOpinion(posFOList, vbWord, rbWord, true,
                                    negatedIndeces.contains(rbIndex));
                        else
                            addFeatureOpinion(negFOList, vbWord, rbWord, false,
                                    negatedIndeces.contains(rbIndex));

                    }
                }
            }
        }
        System.out.println(this.posFOList);
        System.out.println(this.negFOList);
    }

    // Extending the NN for example cheese burger is a feature not just burger
    private String extendFeature(int nnIndex, String nnWord, int jjIndex) {
        TreeMap<Integer, String> featureSet = new TreeMap<Integer, String>();
        featureSet.put(nnIndex, nnWord);
        for (TypedDependency dp : typeDP) {
            if ((dp.reln().toString() == "amod" || dp.reln().toString() == "nn")
                    && dp.gov().index() == nnIndex) {
                if (dp.dep().index() != jjIndex) {
                    featureSet.put(dp.dep().index(), dp.dep().toString());
                }
            }
        }

        String extendedFeature = "";
        for (Map.Entry<Integer, String> entry : featureSet.entrySet()) {
            extendedFeature += entry.getValue() + " ";
        }
        return extendedFeature.trim();
    }

    /*
    // Check for NN-nsubj-VB-dobj-NN template
    public void checkNNVBNN() {
        List<TypedDependency> nsubjList = new ArrayList<TypedDependency>();
        List<TypedDependency> dobjList = new ArrayList<TypedDependency>();
        for (TypedDependency dp : typeDP) {
            if (dp.reln().toString().equals("nsubj")
                    || dp.reln().toString().equals("dobj")) {
                String govPos = posByIndex.get(dp.gov().index());
                String depPos = posByIndex.get(dp.dep().index());
                boolean isNsubj = dp.reln().toString().equals("nsubj") ? true
                        : false;
                if ((govPos.startsWith("NN") && depPos.startsWith("VB"))
                        || (govPos.startsWith("VB") && depPos.startsWith("NN"))) {
                    if (isNsubj) {
                        nsubjList.add(dp);
                    } else {
                        dobjList.add(dp);
                    }
                }
            }
        }
        if (!nsubjList.isEmpty() && !dobjList.isEmpty()) {
            for (TypedDependency nsub : nsubjList) {
                TreeGraphNode VB;
                TreeGraphNode NN1;
                if (posByIndex.get(nsub.gov().index()).startsWith("VB")) {
                    VB = nsub.gov();
                    NN1 = nsub.dep();
                } else {
                    NN1 = nsub.gov();
                    VB = nsub.dep();
                }
                for (TypedDependency dobj : dobjList) {
                    TreeGraphNode NN2 = null;
                    if (dobj.gov().equals(VB)) {
                        NN2 = dobj.dep();
                    }
                    if (dobj.dep().equals(VB)) {
                        NN2 = dobj.gov();
                    }
                    if (NN2 != null) {
                        addFeatureOpinion(NN1.value(), NN2.value());
                    }
                }
            }
        }
    }
    */

    public void addFeatureOpinion(
            HashMap<String, HashMap<String, Double>> foList, String feature,
            String opinion, boolean isPositive, boolean isNegatedOpinion) {
        Double val;
        if (isPositive)
            val = reviewObj.posReviewScore;
        else
            val = reviewObj.negReviewScore;
        String newOpinion = isNegatedOpinion ? "!" + opinion : opinion;
        HashMap<String, Double> tmpHM;
        if (foList.containsKey(feature)) {
            tmpHM = foList.get(feature);
            if (tmpHM.containsKey(newOpinion)) {
                val = tmpHM.get(newOpinion);
                if (isPositive)
                    val += reviewObj.posReviewScore;
                else
                    val += reviewObj.negReviewScore;
            }
        } else {
            tmpHM = new HashMap<String, Double>();
        }
        tmpHM.put(newOpinion, val);
        foList.put(feature, tmpHM);
    }

    public void updateForNewSentence(HashMap<Integer, String> posByIndex,
            List<TypedDependency> tdl,
            HashMap<Integer, Boolean> opinionIndeces, Review reviewObj,
            Set<Integer> negatedIndeces) {
        this.posByIndex = posByIndex;
        this.typeDP = tdl;
        this.opinionIndeces = opinionIndeces;
        this.reviewObj = reviewObj;
        this.negatedIndeces = negatedIndeces;
    }

}