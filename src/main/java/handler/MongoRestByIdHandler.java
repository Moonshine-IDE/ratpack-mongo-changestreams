package handler;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.reactivestreams.client.MongoCollection;
import domain.Grade;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.reactivestreams.Publisher;
import ratpack.handling.Context;
import ratpack.handling.Handler;
import ratpack.jackson.Jackson;
import ratpack.rx.RxRatpack;
import rx.Observable;
import rx.RxReactiveStreams;
import service.RxDevExtremeService;

import static com.mongodb.client.model.Filters.eq;

public class MongoRestByIdHandler implements Handler {

  MongoCollection<Document> collection;
  RxDevExtremeService devExtremeService;

  public MongoRestByIdHandler(MongoCollection collection, RxDevExtremeService devExtremeService) {
    this.collection = collection;
    this.devExtremeService = devExtremeService;
  }

  @Override
  public void handle(Context ctx) throws Exception {
    String id = ctx.getPathTokens().get("id");
    ctx.byMethod(methodSpec -> {
      methodSpec
          .get(() -> {
            Publisher<Document> singlePub = collection.find(eq("_id", new ObjectId(id))).first();
            Observable<Document> singleObservable = RxReactiveStreams.toObservable(singlePub);

            RxRatpack.promise(singleObservable).then(u -> {
              ctx.render(Jackson.json(u.stream().findFirst()));
            });
          })
          .put(() -> {
            ctx.parse(Grade.class).then(grade -> {

              Publisher<Document> singlePub = collection.findOneAndUpdate(
                  eq("_id", new ObjectId(id)), MongoRestByIdHandler.getUpdateBson(grade));

              Observable<Document> singleObservable = RxReactiveStreams.toObservable(singlePub);
              RxRatpack.promise(singleObservable).then(u -> ctx.render(Jackson.json(u)));
            });
          })
          .delete(() -> {
            Publisher<DeleteResult> singlePub = collection.deleteOne(eq("_id", new ObjectId(id)));
            Observable<DeleteResult> singleObservable = RxReactiveStreams.toObservable(singlePub);
            RxRatpack.promise(singleObservable).then(u -> ctx.render(Jackson.json(u.stream().findFirst())));
          });
    });
  }

  public static Document getUpdateBson(Grade grade) {
    Document updateInstruction = new Document();

    if (grade.getExamScore() != null) {
      updateInstruction.append("examScore", grade.getExamScore());
    }

    if (grade.getHomeworkScore() != null) {
      updateInstruction.append("homeworkScore", grade.getHomeworkScore());
    }

    if (grade.getQuizScore() != null) {
      updateInstruction.append("quizScore", grade.getQuizScore());
    }

    if (grade.getStudentId() != null) {
      updateInstruction.append("studentId", grade.getStudentId());
    }

    if (grade.getClassId() != null) {
      updateInstruction.append("classId", grade.getClassId());
    }

    Document updateOperation = new Document();
    updateOperation.append("$set", updateInstruction);

    return updateOperation;
  }
}
