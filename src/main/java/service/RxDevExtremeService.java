package service;

import com.mongodb.reactivestreams.client.FindPublisher;
import domain.DevExtremePojo;
import java.util.Arrays;
import java.util.Optional;
import org.bson.Document;
import rx.Observable;
import rx.RxReactiveStreams;

public class RxDevExtremeService implements DevExtremeService {

  @Override
  public Observable<DevExtremePojo> find(
    FindPublisher<Document> publisher, Optional<Long> count) {

    return RxReactiveStreams.toObservable(publisher).toList().map(document -> {
      DevExtremePojo devExtremePojo = new DevExtremePojo();

      devExtremePojo.setData(document);
      devExtremePojo.setSummary(Arrays.asList(10, 20, 30));
      devExtremePojo.setTotalCount(count.get().intValue());

      return devExtremePojo;
    });
  }
}
