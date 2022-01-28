package service;

import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.reactivestreams.client.AggregatePublisher;
import com.mongodb.reactivestreams.client.FindPublisher;
import com.mongodb.reactivestreams.client.MongoCollection;
import domain.DevExtremePojo;
import org.bson.BsonString;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.reactivestreams.Publisher;
import rx.Observable;
import rx.RxReactiveStreams;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;

public class RxDevExtremeService {

  public Observable<DevExtremePojo> find(MongoCollection collection,
                                         Bson filter, Bson sort, int skip, int take, long totalCount,
                                         Document summaryData) {

    FindPublisher<Document> publisher = collection.find(filter).skip(skip).limit(take).sort(sort);

    return RxReactiveStreams.toObservable(publisher).toList().map(document -> {
      DevExtremePojo devExtremePojo = new DevExtremePojo();

      devExtremePojo.setData(document);
      devExtremePojo.setTotalCount((int) totalCount);

      devExtremePojo.setSummary(Arrays.asList(
          (double) totalCount,
          twoDecimals((double) summaryData.getOrDefault("examAvg", 0d)),
          twoDecimals((double) summaryData.getOrDefault("quizAvg", 0d)),
          twoDecimals((double) summaryData.getOrDefault("homeWorkAvg", 0d))));

      return devExtremePojo;
    });
  }

  public Observable<Long> count(MongoCollection collection, Bson filter) {
    Publisher<Long> findPub = collection.countDocuments(filter);
    return RxReactiveStreams.toObservable(findPub);
  }

  public Observable<Document> getGradeAverage(MongoCollection collection, Bson filter) {
    AggregatePublisher avgPublisher = collection.aggregate(
        Arrays.asList(
            Aggregates.match(filter),
            Aggregates.group(
                null,
                Arrays.asList(
                    Accumulators.avg("examAvg", new BsonString("$examScore")),
                    Accumulators.avg("quizAvg", new BsonString("$quizScore")),
                    Accumulators.avg("homeWorkAvg", new BsonString("$homeworkScore"))
                )
            )
        )
    );

    return RxReactiveStreams.toObservable(avgPublisher);
  }

  private double twoDecimals(double val) {
    return new BigDecimal(Double.toString(val)).setScale(2, RoundingMode.HALF_UP).doubleValue();
  }
}
