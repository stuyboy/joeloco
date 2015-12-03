package com.joechang.loco;

import com.joechang.loco.config.Routes;
import com.joechang.loco.firebase.FirebaseManager;
import com.joechang.loco.listener.DeferredResultValueListener;
import com.joechang.loco.model.*;
import com.joechang.loco.response.GroupResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by joechang on 5/20/15.
 * For basic group operations
 */
@RestController
public class GroupController {

    private FirebaseManager firebaseManager;

    @RequestMapping(Routes.GROUP_BY_ID)
    public GroupResult getGroups (
            @PathVariable(value = Group.ID) String groupId
    ) {
        GroupResult gr = new GroupResult();
        firebaseManager.getGroupFirebase(groupId)
                .addListenerForSingleValueEvent(DeferredResultValueListener.instance(gr, groupId));
        return gr;
    }

    @RequestMapping(value = Routes.GROUP_RESOURCE, method = RequestMethod.GET)
    public GroupResult.Set getGroupsByUserIds (
            @RequestParam(value = User.ID, required = false) String userIds[]
    ) {
        final GroupResult.Set gr = new GroupResult().set();
        if (userIds != null && userIds.length > 0) {
            Set userSet = new LinkedHashSet();
            userSet.addAll(Arrays.asList(userIds));
            firebaseManager.findGroupFromUsers(userSet, new PostQueryAction<Group>() {
                @Override
                public void doAction(Group p) {
                    if (p != null) {
                        gr.setSingleResult(p);
                    } else {
                        gr.empty();
                    }
                }

                @Override
                public void onError(Group p) {
                    gr.empty();
                }
            });
        }
        return gr;
    }

    @RequestMapping(value = Routes.GROUP_RESOURCE, method = RequestMethod.PUT)
    public GroupResult createGroup (
            @RequestBody Group newGroup
    ) {
        final GroupResult gr = new GroupResult();
        if (newGroup != null && newGroup.getMembers().size() > 0) {
            firebaseManager.addGroup(
                    newGroup.getMembers().keySet().iterator().next(),
                    newGroup,
                    new PostWriteAction<Group>(newGroup) {
                        @Override
                        public void doAction(Group group) {
                            gr.setResult(group);
                        }

                        @Override
                        public void onError(Group group) {
                            gr.canceled();
                        }
                    }
            );

        } else {
            gr.canceled();
        }

        return gr;
    }

    @Autowired
    public void setFirebaseManager(FirebaseManager fm) {
        this.firebaseManager = fm;
    }
}
