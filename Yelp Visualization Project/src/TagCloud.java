import java.util.HashMap;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.mcavallo.opencloud.Cloud;
import org.mcavallo.opencloud.Tag;

public class TagCloud {
	HashMap<String,Double> posScores;
	HashMap<String,Double> negScores;
    
    public TagCloud(HashMap<String, Double> posScores,
			HashMap<String, Double> negScores) {
		this.posScores = posScores;
		this.negScores = negScores;
	}


	public static void main(String[] args) {
    	Driver d= new Driver();
    	String businessId = "'h_FP6Lho52MIorjH8GuerQ'";
        d.dbConnection(businessId);
        
        /*
    	SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new TagCloud().initUI();
            }
        });*/
       // System.out.println(posScores);
       // System.out.println(negScores);
        
    }

}