package service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import org.bson.BsonDocument;
import org.bson.conversions.Bson;
import ratpack.util.MultiValueMap;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Sorts.ascending;
import static com.mongodb.client.model.Sorts.descending;

/**
 * This class contains helper methods to transform request parameters
 * received from DevExtreme components and produces Bson objects that
 * can be used by MongoDB libraries.
 */
public class DevExtremeHelper {

  public static int getSkip(MultiValueMap<String, String> requestParams) {
    int skip = 0;

    if(requestParams.get("skip") != null) {
      skip = Integer.valueOf(requestParams.get("skip"));
    }
    return skip;
  }

  public static int getTake(MultiValueMap<String, String> requestParams) {
    int take = 10;

    if(requestParams.get("take") != null) {
      take = Integer.valueOf(requestParams.get("take"));
    }

    return take;
  }

  public static Bson getSortBson(MultiValueMap<String, String> requestParams) {
    Bson sortResult = new BsonDocument();

    String sortInstruction = requestParams.get("sort");

    if(sortInstruction == null || sortInstruction.isEmpty()){
      return sortResult;
    }

    try {
      JsonNode sortNode = new ObjectMapper().readTree(sortInstruction);

      if (sortNode.get(0).get("desc").booleanValue()) {
        sortResult = descending(sortNode.get(0).get("selector").asText("hexaId"));
      } else {
        sortResult = ascending(sortNode.get(0).get("selector").asText("hexaId"));
      }
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }

    return sortResult;
  }

  public static List<Object> convertFiltersToBson(
    JsonNode jsonNode, List<Object> processedList) {

    if(jsonNode == null || jsonNode.isNull()){
      return processedList;
    }

    if (jsonNode.isArray() && !jsonNode.get(0).isArray() && jsonNode.get(0).isTextual()) {
      // Last level array node
      if (jsonNode.size() == 3) {
        Bson filter = DevExtremeHelper.getDocument(jsonNode);
        if(filter != null){
          processedList.add(filter);
        }
      } else {
        // Throw exception, filter not supported!
      }

    } else if(jsonNode.isArray() && jsonNode.get(0).isArray()) {
      // Grouping level
      List innerList = new ArrayList<Object>();
      for (final JsonNode objNode : jsonNode) {
        DevExtremeHelper.convertFiltersToBson(objNode, innerList);
      }
      processedList.add(innerList);
    } else if(!jsonNode.isArray()) {
      // Logical connector (or, and)
      processedList.add(jsonNode.asText());
    }

    return processedList;
  }

  // https://js.devexpress.com/Documentation/ApiReference/Data_Layer/CustomStore/LoadOptions/#filter
  public static Bson getDocument(JsonNode jsonNode) {
    Bson result = null;
    if (jsonNode.isArray() && jsonNode.size() == 3) {
      switch(jsonNode.get(1).asText()){
        case "notcontains":
          String patternStr = "."+jsonNode.get(2).asText()+".";
          Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
          result = not(regex(jsonNode.get(0).asText(), pattern));
          break;
        case "contains":
          String patternStr2 = "."+jsonNode.get(2).asText()+".";
          Pattern pattern2 = Pattern.compile(patternStr2, Pattern.CASE_INSENSITIVE);
          result = regex(jsonNode.get(0).asText(), pattern2);
          break;
        case "startswith":
          String patternStr3 = "^"+jsonNode.get(2).asText();
          Pattern pattern3 = Pattern.compile(patternStr3, Pattern.CASE_INSENSITIVE);
          result = regex(jsonNode.get(0).asText(), pattern3);
          break;
        case "endswith":
          String patternStr4 = jsonNode.get(2).asText() + "$";
          Pattern pattern4 = Pattern.compile(patternStr4, Pattern.CASE_INSENSITIVE);
          result = regex(jsonNode.get(0).asText(), pattern4);
          break;
        case "=":
          if (jsonNode.get(2).isNumber()) {
            result = eq(jsonNode.get(0).asText(), jsonNode.get(2).asDouble());
          } else {
            result = eq(jsonNode.get(0).asText(), jsonNode.get(2).asText());
          }
          break;
        case "<>":
          if (jsonNode.get(2).isNumber()) {
            result = ne(jsonNode.get(0).asText(), jsonNode.get(2).asDouble());
          } else {
            result = ne(jsonNode.get(0).asText(), jsonNode.get(2).asText());
          }
          break;
        case ">":
          result = gt(jsonNode.get(0).asText(), jsonNode.get(2).asDouble());
          break;
        case "<":
          result = lt(jsonNode.get(0).asText(), jsonNode.get(2).asDouble());
          break;
        case ">=":
          result = gte(jsonNode.get(0).asText(), jsonNode.get(2).asDouble());
          break;
        case "<=":
          result = lte(jsonNode.get(0).asText(), jsonNode.get(2).asDouble());
          break;
      }

    } else if (!jsonNode.isArray() && jsonNode.size() == 1 && jsonNode.isTextual()) {
    } else {
      throw new RuntimeException("Filter not supported!");
    }
    return result;
  }

  public static Bson combineFilters(List<Object> filters) {

    if(filters == null || filters.isEmpty()){
      return new BsonDocument();
    }

    List<Object> singleLevelFilters = new ArrayList<Object>();

    for (Object o: filters) {
      if (o instanceof List) {
        singleLevelFilters.add(DevExtremeHelper.combineFilters((List<Object>)o));
      } else {
        singleLevelFilters.add(o);
      }
    }

    Bson result = new BsonDocument();

    // @TODO: We may receive an String as the first
    //        element in this array? validate that case!
    Bson combinedFilters = (Bson)singleLevelFilters.get(0);

    for (int x=1; x<singleLevelFilters.size(); x+=2) {
      String connector = (String)singleLevelFilters.get(x);
      Bson b = (Bson)singleLevelFilters.get(x+1);

      combinedFilters = (connector.toLowerCase().equals("and"))? and(combinedFilters, b) : or(combinedFilters, b);
    }

    return combinedFilters;
  }
}
