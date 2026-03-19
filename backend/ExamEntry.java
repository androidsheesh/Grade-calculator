/**
 * ExamEntry — Simple data class representing one exam row.
 */
public class ExamEntry {
    private String name;
    private double score;
    private double totalScore;
    private double percentage;  // e.g. 50 means 50%

    public ExamEntry() {}

    public ExamEntry(String name, double score, double totalScore, double percentage) {
        this.name = name;
        this.score = score;
        this.totalScore = totalScore;
        this.percentage = percentage;
    }

    // ---- Getters ----
    public String getName()       { return name; }
    public double getScore()      { return score; }
    public double getTotalScore() { return totalScore; }
    public double getPercentage() { return percentage; }

    // ---- Setters ----
    public void setName(String name)             { this.name = name; }
    public void setScore(double score)           { this.score = score; }
    public void setTotalScore(double totalScore) { this.totalScore = totalScore; }
    public void setPercentage(double percentage) { this.percentage = percentage; }

    @Override
    public String toString() {
        return "ExamEntry{name='" + name + "', score=" + score +
               ", totalScore=" + totalScore + ", percentage=" + percentage + "}";
    }
}
