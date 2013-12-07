import java.awt.Color;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
//Changes-start
import java.util.Random;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.mcavallo.opencloud.Cloud;
import org.mcavallo.opencloud.Tag;
//changes-end

public class Driver {
    String DB_PATH = "yelp.db";
    Connection c = null;
    Statement stmt = null;
    String[] WORDS;
    
    public static void main(String args[]) {
        //Driver conn = new Driver();
        //String businessId = "'h_FP6Lho52MIorjH8GuerQ'";
        //conn.dbConnection(businessId);
        //changes
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Driver().dbConnection("'In93N5Jqu85xAh0qQ2jb1w'");
            }
        });
        //changes
    }
    
    public void dbConnection(String businessId) {
        String reviewText = null;
        int reviewStars;
        int reviewVotes;
        int userVotes;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH);
            System.out.println("Opened database successfully");
            stmt = c.createStatement();
            ResultSet rs = stmt
                    .executeQuery("select r.review_text as reviewText, r.stars as reviewStars,r.votes_useful as reviewVotes,u.votes_useful as userVotes from review r,user u  where r.user_id = u.user_id and r.business_id ="
                            + businessId);
            Parser p = new Parser();
            FeatureOpinion fe = null;
            // int columnCount = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                reviewText = rs.getString("reviewText");
                reviewStars = rs.getInt("reviewStars");
                reviewVotes = rs.getInt("reviewVotes");
                userVotes = rs.getInt("userVotes");
                Review r = new Review(reviewText, reviewStars, reviewVotes,
                        userVotes);
                fe = p.parseReview(r, fe);
            }
            System.out.println("-----------------------------Finally ----------------------------------");
            HashMap<String, Double> posScores = extractFeaturesWithScores(fe.posFOList);
            HashMap<String, Double> negScores = extractFeaturesWithScores(fe.negFOList);
            HashMap<String, String> ambiguousFeatures = findAmbiguities(posScores, negScores);
            System.out.println(posScores);
            System.out.println(negScores);
            System.out.println(ambiguousFeatures);
            //changes-start
            JFrame frame = new JFrame(Driver.class.getSimpleName());
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            JPanel panel = new JPanel();
            //JPanel Npanel = new JPanel();
            panel.setBackground(Color.white);
            //Npanel.setBackground(Color.white);
            Cloud cloud = new Cloud();
            //panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            for (Map.Entry<String, Double> orderedEntry : posScores
                    .entrySet()) {
            	//System.out.println(orderedEntry);
            		String tag = orderedEntry.getKey();
            		Double tagValue = Math.abs(orderedEntry.getValue());
                    cloud.addTag(orderedEntry.getKey());
                    final JLabel label = new JLabel(tag);
                    final JLabel addSpace = new JLabel("    ");
                    label.setOpaque(true);
                    label.setBackground(Color.WHITE);
                    label.setForeground(Color.RED);
                    label.setFont(label.getFont().deriveFont((float) (tagValue  * 60)));
                    panel.add(label);
                    panel.add(addSpace);
                    
            }
            /*
            for (Tag tag : cloud.tags()) {
                final JLabel label = new JLabel(tag.getName());
                label.setOpaque(false);
                label.setFont(label.getFont().deriveFont((float) tag.getWeight() * 10));
                panel.add(label);
            }
            */
            frame.add(panel);
            frame.setSize(800, 600);
            frame.setVisible(true);
            //changes-end
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    private HashMap<String, String> findAmbiguities(
            HashMap<String, Double> posScores, HashMap<String, Double> negScores) {
        HashMap<String, String> ambiguousFeatures = new HashMap<String, String>();
        for(Map.Entry<String, Double> posEntry : posScores.entrySet()) {
            String feature = posEntry.getKey();
            if(negScores.containsKey(feature)) {
                String posNegVal = posEntry.getValue().toString();
                posNegVal += ", " + negScores.get(feature).toString();
                ambiguousFeatures.put(feature, posNegVal);
            }
        }
       
        for(String aFeature : ambiguousFeatures.keySet()) {
            posScores.remove(aFeature);
            negScores.remove(aFeature);
        }
        return ambiguousFeatures;
    }

    private HashMap<String, Double> extractFeaturesWithScores(
            HashMap<String, HashMap<String, Double>> foList) {
        HashMap<String, Double> finalFeatures = new HashMap<String, Double>();
        for (Map.Entry<String, HashMap<String, Double>> entry : foList
                .entrySet()) {
            String[] featureArray = entry.getKey().split(" ");
            TreeMap<Integer, String> featureList = new TreeMap<Integer, String>();
            for (String f : featureArray) {
                String[] arr = f.split("-");
                int index = Integer.parseInt(arr[1]);
                featureList.put(index, arr[0]);
            }
            for (Map.Entry<String, Double> scoreEntry : entry.getValue()
                    .entrySet()) {
                String[] opinionArr = scoreEntry.getKey().split("-");
                int opinionIndex = Integer.parseInt(opinionArr[1]);
                featureList.put(opinionIndex, opinionArr[0]);
                String finalFeature = "";
                for (Map.Entry<Integer, String> orderedEntry : featureList
                        .entrySet()) {
                    finalFeature += orderedEntry.getValue() + " ";
                }
                finalFeatures.put(finalFeature.trim(), scoreEntry.getValue());
                featureList.remove(opinionIndex);
            }
        }
        return finalFeatures;
    }
}