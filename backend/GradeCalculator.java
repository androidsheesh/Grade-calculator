import java.util.List;

/**
 * GradeCalculator — Contains all grading formulas.
 *
 * Formula per exam:   (score / totalScore * 85 + 15) * (percentage / 100)
 * Section grade:      sum of all exam grades in that section
 * Final grade:        lectureGrade * 0.30  +  labGrade * 0.70
 */
public class GradeCalculator {

    /**
     * Compute a single exam's contribution to the section grade.
     *
     * @param score      the student's raw score
     * @param totalScore the maximum possible score
     * @param percentage the weight of this exam within its section (0–100)
     * @return the weighted exam grade
     */
    public static double computeExamGrade(double score, double totalScore, double percentage) {
        if (totalScore <= 0) {
            throw new IllegalArgumentException("Total score must be greater than 0.");
        }
        double transmutedGrade = (score / totalScore) * 85.0 + 15.0;
        return transmutedGrade * (percentage / 100.0);
    }

    /**
     * Compute the total grade for a section (lecture or laboratory).
     *
     * @param entries list of exam entries in the section
     * @return the sum of each exam's weighted grade
     */
    public static double computeSectionGrade(List<ExamEntry> entries) {
        double sectionGrade = 0.0;
        for (ExamEntry entry : entries) {
            sectionGrade += computeExamGrade(
                entry.getScore(),
                entry.getTotalScore(),
                entry.getPercentage()
            );
        }
        return sectionGrade;
    }

    /**
     * Compute the final grade from lecture and laboratory section grades.
     *
     * @param lectureGrade    total lecture section grade
     * @param laboratoryGrade total laboratory section grade
     * @return the overall final grade
     */
    public static double computeFinalGrade(double lectureGrade, double laboratoryGrade) {
        return lectureGrade * 0.30 + laboratoryGrade * 0.70;
    }
}
