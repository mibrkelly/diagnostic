package diagnostic;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class ContBetaIdea {
	
	//Increase this value for lower scores
	private double targetScore = 0.36;
	
	//Set to true to compute line by line
	private boolean printByLine = true;
	
	private ArrayList<Skill> powerSkills = new ArrayList<Skill>();
	
	public void run() {
		File folder = new File("diagnostic/data_holder");
		File[] listOfFiles = folder.listFiles();
		
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile() && listOfFiles[i].getName().charAt(0) != '.') {
				System.out.println(listOfFiles[i].getName());
				
				powerSkills.clear();
				
				try {
					BufferedReader br = new BufferedReader(new FileReader(listOfFiles[i]));
				    String line = br.readLine();
					int lineNumber = 0;
					
				    while (line != null) {
						String[] info = line.split(" ");
						powerSkills.add(new Skill(Double.parseDouble(info[0]), info[1], Double.parseDouble(info[2])));
				        line = br.readLine();
						
						if (printByLine) {
							System.out.print(++lineNumber + " ");
							diagnostic(lineNumber);
						}
				    }
				}
				catch (FileNotFoundException e) {
					System.out.println("File not found");
				}
				catch (IOException e) {
					System.out.println("Read line error");
				}
				
				if (!printByLine) {
					diagnostic(0);
				}
				System.out.printf("%n----------%n%n");
		    }
		}
	}
	
	public void diagnostic(int line) {
		double centerScore = computeScore(targetScore, null);
		System.out.println("Confidence: " + 1/scoreDoubt(centerScore));
		
		double boundScore = 0.0;
		for (Skill skill : powerSkills) {
			if (skill.known.equals("r") && skill.score > boundScore) {
				boundScore = skill.score;
			}
		}
		double upperScore = computeScore(targetScore,new Skill[]{new Skill(boundScore, "r", 1.0), new Skill(centerScore, "r", 1.0)});
		
		boundScore = 13.0;
		for (Skill skill : powerSkills) {
			if (skill != null && skill.known.equals("w") && skill.score < boundScore) {
				boundScore = skill.score;
			}
		}
		//Something seems to be cutting off the lowerScore. Put centerScore - 10 into the second vector and it seems to drop the decimal parts.
		double lowerScore = computeScore(targetScore,new Skill[]{new Skill(boundScore, "w", 1.0), new Skill(centerScore, "w", 1.0)});
		
		if (upperScore - lowerScore > 2 || upperScore - lowerScore == 0 || scoreDoubt(centerScore) > 1.0) {
			System.out.printf("Score: %.2f in (%.2f,%.2f) -- Low Confidence --%n",centerScore,lowerScore,upperScore);
		}
		else {
			System.out.printf("Score: %.2f in (%.2f,%.2f)%n",centerScore,lowerScore,upperScore);
		}
	}
	
	public double computeScore(double targetScore, Skill[] dummy) {
				
		double score = 0.0;
		double delta = 1.0;
		
		ScoreVector scoreV;
				
		do {
			scoreV = new ScoreVector(0,0);
			
			for (Skill skill : powerSkills) {
				if (skill != null && skill.known.equals("r")) {
					scoreV.a += addScore(score, skill.score, skill.conf,true);
				}
				else if (skill != null && skill.known.equals("w")) {
					scoreV.b += addScore(score, skill.score, skill.conf,false);
				}
			}
			score+=delta;
			if (score > 13) {
				return 13;
			}
		} while(scoreV.getScore() > targetScore);
		
		score -= 0.5;
				
		ScoreVector scoreVL;
		ScoreVector scoreVR;
		
		double scoreL = score-delta;
		double scoreR = score+delta;
		
		for (int divides = 0; divides < 30; divides++) {
			scoreV = new ScoreVector(0,1);
			scoreVL = new ScoreVector(0,1);
			scoreVR = new ScoreVector(0,1);
			
			for (Skill skill : powerSkills) {
				if (skill != null && skill.known.equals("r")) {
					scoreV.a += addScore(score, skill.score, skill.conf,true);
					scoreVL.a += addScore(scoreL, skill.score, skill.conf,true);
					scoreVR.a += addScore(scoreR, skill.score, skill.conf,true);
				}
				else if (skill != null && skill.known.equals("w")) {
					scoreV.b += addScore(score, skill.score, skill.conf,false);
					scoreVL.b += addScore(scoreL, skill.score, skill.conf,false);
					scoreVR.b += addScore(scoreR, skill.score, skill.conf,false);
				}
			}
			
			if (dummy != null) {
				for (int i = 0; i < dummy.length; i++) {
					if (dummy[i].known.equals("r")) {
						scoreV.a += addScore(score,dummy[i].score,1.1,true);
						scoreVL.a += addScore(scoreL,dummy[i].score,1.1,true);
						scoreVR.a += addScore(scoreR,dummy[i].score,1.1,true);
					}
					else if (dummy[i].known.equals("w")) {
						scoreV.b += addScore(score,dummy[i].score,1.1,false);
						scoreVL.b += addScore(scoreL,dummy[i].score,1.1,false);
						scoreVR.b += addScore(scoreR,dummy[i].score,1.1,false);
					}
				}
			}
			
			if (Math.abs(scoreVL.getScore() - targetScore) <= Math.abs(scoreV.getScore() - targetScore)) {
				score = score-delta;
				if (score < 0) {
					return 0;
				}
			} 
			else if (Math.abs(scoreVR.getScore() - targetScore) <= Math.abs(scoreV.getScore() - targetScore)) {
				score = score+delta;
				if (score > 13) {
					return 13;
				}
			}
			
			delta = delta/2.0;
			scoreL = score-delta;
			scoreR = score+delta;
		}
		
		return score;
	}
	
	private double scoreDoubt (double score) {
		ScoreVector scaleV = new ScoreVector(0,0);
		for (Skill skill : powerSkills) {
			if (skill != null && skill.known.equals("r")) {
				scaleV.a += addScore(score, skill.score, skill.conf,true);
			}
			else if (skill != null && skill.known.equals("w")) {
				scaleV.b += addScore(score, skill.score, skill.conf,false);
			}
		}
		return (scaleV.a + scaleV.b)/(powerSkills.size()*100.0);
	}
	
	private double addScore(double x, double y, double c, boolean reward) {
		if (reward)
			return 100*c*Math.exp(2*(y-x));
		else
			return 100*c*Math.exp(2*(x-y));
	}
	
	public static void main (String[] args) {
		ContBetaIdea cbi = new ContBetaIdea();
		cbi.run();
	}
	
	private class ScoreVector {
		
		public double a;
		public double b;
		
		public ScoreVector(int iA, int iB) {
			a = iA;
			b = iB;
		}
		
		public double getScore() {
			return a/(a+b);
		}
	}
	
	public class Skill {
		
		public double score;
		public String known;
		public double conf;
	
		public Skill(double iScore, String iKnown, double iConf) {
			score = iScore;
			known = iKnown;
			conf = iConf;
		}
	}
}