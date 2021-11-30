import ratpack.sse.ServerSentEvents;
import ratpack.server.RatpackServer;
import ratpack.server.BaseDir;
import static ratpack.jackson.Jackson.toJson;
import static ratpack.jackson.Jackson.chunkedJsonList;

import com.mongodb.client.model.Aggregates;
import static com.mongodb.client.model.Filters.*;

import com.mongodb.reactivestreams.client.ChangeStreamPublisher;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;

import static java.util.Arrays.asList;
import org.bson.Document;

public class RatpackPush {

  public static void main(String[] args) throws Exception {

    MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
    MongoDatabase database = mongoClient.getDatabase("test");
    MongoCollection<Document> collection = database.getCollection("restaurants");

    ChangeStreamPublisher<Document> changes = collection.watch(asList(
      Aggregates.match(in("operationType", asList("insert", "update")))));

    RatpackServer.start(server -> server
      .serverConfig(c -> c.baseDir(BaseDir.find()))
      .handlers(chain -> chain
        .files(f -> f.dir("public").indexFiles("index.html"))
        .get("jsonData", ctx -> {
          ctx.render(chunkedJsonList(ctx, collection.find()));
        })
        .get("dataGrid", ctx -> {
            ServerSentEvents events = ServerSentEvents.serverSentEvents(changes, dataGridEvent -> {
                dataGridEvent.event("message");
                dataGridEvent.id(Long.toString(System.currentTimeMillis()));
                dataGridEvent.data(toJson(ctx));
            });
            ctx.render(events);
        })
      )
    );
  }
}
