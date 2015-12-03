package com.joechang.loco.client;

import com.joechang.loco.config.Routes;
import com.joechang.loco.logging.LogLocationEntry;
import com.joechang.loco.logging.StatusResponse;
import com.joechang.loco.model.User;
import retrofit.http.*;

import java.util.Set;

/**
 * Created by joechang on 5/13/15.
 */
public interface UserClient {

    @POST(Routes.USER_LOCATION)
    public void postLocation(
            @Path(User.ID) String userId,
            @Body LogLocationEntry lle,
            StatusResponseCallback resp);

    @GET(Routes.USER_BY_ID)
    public void getUser(
            @Path(User.ID) String userId,
            Callback resp);

    @GET(Routes.USER_RESOURCE)
    public void findUser(
            @Query(User.PHONE_NUMBER) String byPhoneNumber,
            Callback resp);

    @GET(Routes.USERS_ALL)
    public void getAllUsers(SetCallback resp);

    @POST(Routes.USER_AUTH)
    public void verifyJwt(
            @Body String tokenBody,
            retrofit.Callback<String> resp);

    public interface StatusResponseCallback extends retrofit.Callback<StatusResponse> {}
    public interface Callback extends retrofit.Callback<User> {}
    public interface SetCallback extends retrofit.Callback<Set<User>> {}
}
