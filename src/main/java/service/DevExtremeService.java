package service;

import com.mongodb.reactivestreams.client.FindPublisher;
import domain.DevExtremePojo;
import java.util.Optional;
import org.bson.Document;
import rx.Observable;

public interface DevExtremeService {

    /**
     * Retrieves an observable stream.
     *
     * @return an Observable of {@link DevExtremePojo}
     */
    Observable<DevExtremePojo> find(FindPublisher<Document> publisher, Optional<Long> count);
}
