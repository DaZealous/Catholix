package Service;

import java.io.File;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import model.Feeds;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface FeedsService {
    @FormUrlEncoded
    @POST("chats/{username}")
        /*@HeaderMap Map<String, String> header,*/
    Call<ResponseBody> sendMessage(@Path("username") String username, @Field("msg_body") String msg_body, @Field("msg_type") String msg_type,
                                   @Field("image") String image, @Field("time_stamp") String time_stamp, @Field("from") String from);

    @GET("GET/req.php?qdata=all&table=feed")
   Observable<List<Feeds>> getNewsFeeds();

    @FormUrlEncoded
    @POST("POST/post_feeds.php")
    Observable<ResponseBody> postNewsFeed(@Field("feed_media") File feed, @Field("title") String title, @Field("userID") String userID,
                                    @Field("category") String category, @Field("reach") String reach, @Field("article") String article,
                                    @Field("post_feed_req") String req);

    @DELETE("chats/{username}")
    Completable deleteMessage(@Path("username") String username, @Field("id") String id);

}
