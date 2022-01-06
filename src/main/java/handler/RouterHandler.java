package handler;

import com.mongodb.reactivestreams.client.MongoCollection;
import org.bson.Document;
import ratpack.handling.Context;
import ratpack.handling.Handler;
import service.DevExtremeService;

public class RouterHandler implements Handler {

  private Handler mongoRestByIdHandler;
  private Handler mongoRestHandler;
  MongoCollection<Document> collection;
  DevExtremeService devExtremeService;

  public RouterHandler(MongoCollection collection, DevExtremeService devExtremeService) {
    this.mongoRestByIdHandler = new MongoRestByIdHandler(collection, devExtremeService);
    this.mongoRestHandler = new MongoRestHandler(collection, devExtremeService);
    this.collection = collection;
    this.devExtremeService = devExtremeService;
  }

  @Override
  public void handle(Context ctx) throws Exception {
    String id = ctx.getPathTokens().get("id");

    if (id != null) {
      ctx.insert(mongoRestByIdHandler);
    } else {
      ctx.insert(mongoRestHandler);
    }
  }
}
