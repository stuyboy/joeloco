package com.joechang.loco.client;

import com.joechang.loco.config.Routes;
import com.joechang.loco.model.Event;
import com.joechang.loco.model.Group;
import com.joechang.loco.model.User;
import retrofit.http.*;

import java.util.Collection;

/**
 * Created by joechang on 5/20/15.
 */
public interface GroupClient {

    @GET(Routes.GROUP_BY_ID)
    public void getGroup(
            @Path(Group.ID) String groupId,
            GroupClient.Callback callback
    );

    @GET(Routes.GROUP_RESOURCE)
    public Group[] getGroupByUserIds(
            @Query(User.ID) Collection<String> userIds
    );

    @PUT(Routes.GROUP_RESOURCE)
    public Group createGroup(
            @Body Group newGroup
    );

    public interface Callback extends retrofit.Callback<Group> {}

}
