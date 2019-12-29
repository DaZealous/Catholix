package Service;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;
import model.Feeds;
import model.Users;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface UserService {
    @GET("req.php?qdata=all&table=users")
    Call<List<Users>>getUsers();
}
