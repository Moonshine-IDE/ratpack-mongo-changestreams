package domain;

import org.bson.types.ObjectId;
import java.util.Objects;

public class Grade {

  private ObjectId id;
  private Double studentId;
  private Double classId;
  private String hexaId;
  private String studentName;
  private Double quizScore;
  private Double examScore;
  private Double homeworkScore;

  public ObjectId getId() {
    return id;
  }

  public Grade setId(ObjectId id) {
    this.id = id;
    return this;
  }

  public Double getStudentId() {
    return studentId;
  }

  public Grade setStudentId(Double studentId) {
    this.studentId = studentId;
    return this;
  }

  public Double getClassId() {
    return classId;
  }

  public Grade setClassId(Double classId) {
    this.classId = classId;
    return this;
  }

  public String getHexaId(){
    return hexaId;
  }

  public Grade setHexaId(String hexaId){
    this.hexaId = hexaId;
    return this;
  }

  public String getStudentName(){
    return "Student " + studentId.intValue();
  }

  public Double getQuizScore(){
    return quizScore;
  }

  public Grade setQuizScore(Double quizScore) {
    this.quizScore = quizScore;
    return this;
  }

  public Double getExamScore(){
    return examScore;
  }

  public Grade setExamScore(Double examScore) {
    this.examScore = examScore;
    return this;
  }

  public Double getHomeworkScore(){
    return homeworkScore;
  }

  public Grade setHomeworkScore(Double homeworkScore) {
    this.homeworkScore = homeworkScore;
    return this;
  }

  @Override
  public String toString() {
    final StringBuffer sb = new StringBuffer("Grade{");
    sb.append("id=").append(id);
    sb.append(", studentId=").append(studentId);
    sb.append(", classId=").append(classId);
    sb.append(", hexaId=").append(hexaId);
    sb.append(", quizScore=").append(quizScore);
    sb.append(", examScore=").append(examScore);
    sb.append(", homeworkScore=").append(homeworkScore);
    sb.append('}');
    return sb.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    Grade grade = (Grade) o;
    return Objects.equals(id, grade.id) && Objects.equals(studentId, grade.studentId) && Objects.equals(classId,
                                                                                                          grade.classId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, studentId, classId);
  }
}
