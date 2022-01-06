package domain;

public class Grade {
	private String _id;
	private String hexaId;
	private Double examScore;
	private Double homeworkScore;
	private Double quizScore;
	private Integer classId;
	private Integer studentId;

	public Grade() {
	}

	public String getId() {
		return _id;
	}

	public String getHexaId() {
		return hexaId;
	}

	public Double getExamScore() {
		return examScore;
	}

	public Double getHomeworkScore() {
		return homeworkScore;
	}

	public Double getQuizScore() {
		return quizScore;
	}

	public Integer getClassId() {
		return classId;
	}

	public Integer getStudentId() {
		return studentId;
	}

	public void setId(String _id) {
		this._id = _id;
	}

	public void setHexaId(String hexaId) {
		this.hexaId = hexaId;
	}

	public void setExamScore(Double examScore) {
		this.examScore = examScore;
	}

	public void setHomeworkScore(Double homeworkScore) {
		this.homeworkScore = homeworkScore;
	}

	public void setQuizScore(Double quizScore) {
		this.quizScore = quizScore;
	}

	public void setClassId(Integer classId) {
		this.classId = classId;
	}

	public void setStudentId(Integer studentId) {
		this.studentId = studentId;
	}
}
