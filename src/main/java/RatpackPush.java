import ratpack.sse.ServerSentEvents;
import ratpack.server.RatpackServer;
import ratpack.server.BaseDir;
import ratpack.jackson.Jackson;
import static ratpack.jackson.Jackson.chunkedJsonList;

import com.mongodb.client.model.Aggregates;
import static com.mongodb.client.model.Filters.*;

import com.mongodb.reactivestreams.client.ChangeStreamPublisher;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;

import java.util.Arrays;
import java.util.List;
import org.bson.BsonString;
import org.bson.BsonDocument;
import org.bson.Document;

public class RatpackPush {

  public static void main(String[] args) throws Exception {

    MongoClient mongoClient = MongoClients.create(System.getProperty("mongo.uri"));

    MongoDatabase database = mongoClient.getDatabase("test");
    MongoCollection<Document> collection = database.getCollection("grades");

    List operationTypes = Arrays.asList("insert", "update", "delete", "replace");
    List pipeline = Arrays.asList(Aggregates.match(in("operationType", operationTypes)));

    ChangeStreamPublisher<Document> changes;
    String rToken = System.getProperty("rToken");

    if(rToken == null){
      changes = collection.watch(pipeline);
    }else{
      BsonDocument resumeToken = new BsonDocument("_data", new BsonString(rToken));
      changes = collection.watch(pipeline).resumeAfter(resumeToken);
    }

    RatpackServer.start(server -> server
      .serverConfig(c -> c.baseDir(BaseDir.find()))
      .handlers(chain -> chain
        .files(f -> f.dir("public").indexFiles("index.html"))
        .get("jsonData", ctx -> {
          ctx.render(chunkedJsonList(ctx, collection.find(lte("classId", 100d))));
        })
        .get("dataGrid", ctx -> {
            ServerSentEvents events = ServerSentEvents.serverSentEvents(changes, dataGridEvent -> {
                dataGridEvent.event("message");
                dataGridEvent.id(Long.toString(System.currentTimeMillis()));

                dataGridEvent.data(eventData -> {
                  eventData.getDocumentKey().put("hexaId",
                          new BsonString(eventData.getDocumentKey().getObjectId("_id").getValue().toString()));

                  return Jackson.getObjectWriter(ctx).writeValueAsString(eventData);
                });
            });
            ctx.render(events);
        })
      )
    );
  }
}
