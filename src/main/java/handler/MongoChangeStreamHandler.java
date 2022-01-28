package handler;

import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.reactivestreams.client.ChangeStreamPublisher;
import com.mongodb.reactivestreams.client.MongoCollection;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.Document;
import ratpack.handling.Context;
import ratpack.handling.Handler;
import ratpack.jackson.Jackson;
import ratpack.sse.ServerSentEvents;

import java.util.Arrays;
import java.util.List;

public class MongoChangeStreamHandler implements Handler {

  ChangeStreamPublisher<Document> changes;

  public MongoChangeStreamHandler(MongoCollection collection) {
    List operationTypes = Arrays.asList("insert", "update", "delete", "replace");
    List pipeline = Arrays.asList(Aggregates.match(Filters.in("operationType", operationTypes)));
    String rToken = System.getProperty("rToken");

    if(rToken == null) {
      changes = collection.watch(pipeline);
    } else {
      BsonDocument resumeToken = new BsonDocument("_data", new BsonString(rToken));
      changes = collection.watch(pipeline).resumeAfter(resumeToken);
    }
  }

  @Override
  public void handle(Context ctx) throws Exception {
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
  }
}
