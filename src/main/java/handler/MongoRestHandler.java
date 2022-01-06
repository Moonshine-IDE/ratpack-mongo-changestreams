package handler;

import domain.DevExtremePojo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.client.result.InsertOneResult;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.reactivestreams.Publisher;
import ratpack.handling.Context;
import ratpack.handling.Handler;
import ratpack.jackson.Jackson;
import ratpack.rx.RxRatpack;
import rx.RxReactiveStreams;
import rx.Observable;
import service.DevExtremeService;
import service.DevExtremeHelper;

public class MongoRestHandler implements Handler {

  MongoCollection<Document> collection;
  DevExtremeService devExtremeService;

  public MongoRestHandler(MongoCollection collection, DevExtremeService devExtremeService) {
    this.collection = collection;
    this.devExtremeService = devExtremeService;
  }

  @Override
  public void handle(Context ctx) throws Exception {
    ctx.byMethod(methodSpec -> {
      methodSpec.get(() -> {
        String dataFilters = ctx.getRequest().getQueryParams().get("filter");

        JsonNode jsonNode = (dataFilters == null)? NullNode.getInstance() : new ObjectMapper().readTree(dataFilters);

        List<Object> filtersList = new ArrayList<Object>();
        List<Object> bsonFiltersList = DevExtremeHelper.convertFiltersToBson(jsonNode, filtersList);

        final Bson combinedFilter = DevExtremeHelper.combineFilters(bsonFiltersList);
        final Bson sortBson = DevExtremeHelper.getSortBson(ctx.getRequest().getQueryParams());

        Publisher<Long> findPub = collection.countDocuments(combinedFilter);
        Observable<Long> findObservable = RxReactiveStreams.toObservable(findPub);

        int skip = DevExtremeHelper.getSkip(ctx.getRequest().getQueryParams());
        int take = DevExtremeHelper.getTake(ctx.getRequest().getQueryParams());

        RxRatpack.promise(findObservable).then(count -> {

          Observable<DevExtremePojo> data = devExtremeService.find(
            collection.find(combinedFilter).skip(skip).limit(take).sort(sortBson),
            count.stream().findFirst());

          RxRatpack.promiseSingle(data).then(dataPojo ->
            ctx.render(Jackson.json(dataPojo))
          );
        });
      }).post(() -> {
          ctx.parse(Map.class).then(grade -> {
            Document newDocument = new Document(grade);
            newDocument.put("_id", new ObjectId(String.valueOf(grade.get("_id"))));

            Publisher<InsertOneResult> result = collection.insertOne(newDocument);
            Observable<InsertOneResult> singleObservable = RxReactiveStreams.toObservable(result);
            RxRatpack.promise(singleObservable).then(u -> ctx.render(Jackson.json(u)));
          });
      });
    });
  }
}
