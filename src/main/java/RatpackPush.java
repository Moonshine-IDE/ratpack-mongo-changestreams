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

import static java.util.Arrays.asList;
import java.util.List;
import org.bson.BsonString;
import org.bson.BsonDocument;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import domain.Grade;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class RatpackPush {

  public static void main(String[] args) throws Exception {

    ConnectionString connectionString = new ConnectionString("mongodb://localhost:27017");
    CodecRegistry pojoCodecRegistry = fromProviders(PojoCodecProvider.builder().automatic(true).build());
    CodecRegistry codecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), pojoCodecRegistry);
    MongoClientSettings clientSettings = MongoClientSettings.builder()
                                                            .applyConnectionString(connectionString)
                                                            .codecRegistry(codecRegistry)
                                                            .build();

    MongoClient mongoClient = MongoClients.create(clientSettings);
    MongoDatabase database = mongoClient.getDatabase("test");
    MongoCollection<Grade> collection = database.getCollection("grades", Grade.class);

    List operationTypes = asList("insert", "update", "delete", "replace");
    List pipeline = asList(Aggregates.match(in("operationType", operationTypes)));

    ChangeStreamPublisher<Grade> changes;
    String rToken = System.getProperty("rToken");

    if(rToken == null){
      changes = collection.watch(pipeline, Grade.class);
    }else{
      BsonDocument resumeToken = new BsonDocument("_data", new BsonString(rToken));
      changes = collection.watch(pipeline, Grade.class).resumeAfter(resumeToken);
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
