package domain;

import java.util.List;
import org.bson.Document;

public class DevExtremePojo {
  private List<Document> data;
  private Integer totalCount;
  private List<Integer> summary;

  public DevExtremePojo() {
  }

  public List<Document> getData() {
    return data;
  }

  public void setData(List<Document> data) {
    this.data = data;
  }

  public Integer getTotalCount() {
    return totalCount;
  }

  public void setTotalCount(Integer totalCount) {
    this.totalCount = totalCount;
  }

  public List<Integer> getSummary() {
    return summary;
  }

  public void setSummary(List<Integer> summary) {
    this.summary = summary;
  }
}
