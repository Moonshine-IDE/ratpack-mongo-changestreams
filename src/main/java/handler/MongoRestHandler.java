package handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.reactivestreams.client.MongoCollection;
import domain.DevExtremePojo;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.reactivestreams.Publisher;
import ratpack.handling.Context;
import ratpack.handling.Handler;
import ratpack.jackson.Jackson;
import ratpack.rx.RxRatpack;
import rx.Observable;
import rx.RxReactiveStreams;
import service.DevExtremeHelper;
import service.RxDevExtremeService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MongoRestHandler implements Handler {

  private MongoCollection<Document> collection;
  private RxDevExtremeService devExtremeService;

  public MongoRestHandler(MongoCollection<Document> collection, RxDevExtremeService devExtremeService) {
    this.collection = collection;
    this.devExtremeService = devExtremeService;
  }

  @Override
  public void handle(Context ctx) throws Exception {
    ctx.byMethod(methodSpec -> {
      methodSpec.get(() -> {
        String dataFilters = ctx.getRequest().getQueryParams().get("filter");
        JsonNode jsonNode = (dataFilters == null) ? NullNode.getInstance() : new ObjectMapper().readTree(dataFilters);

        List<Object> filtersList = new ArrayList<Object>();
        List<Object> bsonFiltersList = DevExtremeHelper.convertFiltersToBson(jsonNode, filtersList);

        final Bson filter = DevExtremeHelper.combineFilters(bsonFiltersList);
        final Bson sort = DevExtremeHelper.getSortBson(ctx.getRequest().getQueryParams());

        int skip = DevExtremeHelper.getSkip(ctx.getRequest().getQueryParams());
        int take = DevExtremeHelper.getTake(ctx.getRequest().getQueryParams());

        Observable<Long> countObservable = devExtremeService.count(collection, filter);

        RxRatpack.promise(countObservable).then(countResult -> {

          Optional<Long> optionalCount = countResult.stream().findFirst();
          Observable<Document> summaryObservable = devExtremeService.getGradeAverage(collection, filter);

          RxRatpack.promise(summaryObservable).then(summaryResult -> {

            Optional<Document> optionalSummary = summaryResult.stream().findFirst();
            Observable<DevExtremePojo> findObservable = devExtremeService.find(collection, filter, sort,
                skip, take, optionalCount.orElse(0L).longValue(), optionalSummary.orElse(new Document()));

            RxRatpack.promiseSingle(findObservable).then(dataPojo ->
                ctx.render(Jackson.json(dataPojo))
            );
          });
        });
      }).post(() -> {
        ctx.parse(Map.class).then(grade -> {
          Document newDocument = new Document(grade);
          newDocument.put("_id", new ObjectId(String.valueOf(grade.get("hexaId"))));

          Publisher<InsertOneResult> result = collection.insertOne(newDocument);
          Observable<InsertOneResult> singleObservable = RxReactiveStreams.toObservable(result);
          RxRatpack.promise(singleObservable).then(u -> ctx.render(Jackson.json(u)));
        });
      });
    });
  }
}
