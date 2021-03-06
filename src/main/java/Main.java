import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import handler.MongoChangeStreamHandler;
import handler.RouterHandler;
import org.bson.Document;
import ratpack.server.BaseDir;
import ratpack.server.RatpackServer;
import service.RxDevExtremeService;

public class Main {

  public void runApplication() {
    try {
      MongoClient mongoClient = MongoClients.create(System.getProperty("mongo.uri"));

      MongoDatabase database = mongoClient.getDatabase("test");
      MongoCollection<Document> collection = database.getCollection("grades");

      RxDevExtremeService devExtremeService = new RxDevExtremeService();
      RouterHandler routerHandler = new RouterHandler(collection, devExtremeService);

      RatpackServer.start(server -> server
          .serverConfig(c -> c.baseDir(BaseDir.find()))
          .handlers(chain -> chain
              .files(f -> f.dir("public").indexFiles("index.html"))
              .files(f -> f.path("frontend").dir("public").indexFiles("index.html"))
              .path("api/grades", routerHandler)
              .path("api/grades/:id", routerHandler)
              .get("grades/stream", new MongoChangeStreamHandler(collection))
          )
      );
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) throws Exception {
    new Main().runApplication();
  }
}
