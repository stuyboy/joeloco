package com.joechang.loco.avatar;

import com.joechang.loco.Configuration;
import com.joechang.loco.client.RestClientFactory;
import com.joechang.loco.config.Routes;
import com.joechang.loco.model.User;

/**
 * Author:    joechang
 * Created:   6/13/15 1:28 PM
 * Purpose:   General purpose class to construct the GET URL for an avatar.
 */
public class AvatarUrlBuilder {
    private enum Shape {
        CIRCLE("CIRCLE"),
        ROUNDED("ROUNDED");

        private String shapeValue;

        Shape(String s) {
            this.shapeValue = s;
        }
    }

    private String baseRoute;
    private String userId;
    private Shape shape = Shape.CIRCLE;
    private int size = 50;

    public AvatarUrlBuilder(String baseRoute) {
        this.baseRoute = baseRoute;
    }

    public static String url(String userId, int pixelSize, Shape shape) {
        return new AvatarUrlBuilder(Routes.USER_AVATAR).setUserId(userId).setSize(pixelSize).setShape(shape).toUrl();
    }

    public static String circleUrl(String userId, int pixelSize) {
        return url(userId, pixelSize, Shape.CIRCLE);
    }

    public static String squareUrl(String userId, int pixelSize) {
        return url(userId, pixelSize, Shape.ROUNDED);
    }

    public static String mapPointerUrl(String userId) {
        return new AvatarUrlBuilder(Routes.USER_MAP_POINTER).setUserId(userId).setSize(85).toUrl();
    }

    public AvatarUrlBuilder setUserId(String id) {
        this.userId = id;
        return this;
    }

    public AvatarUrlBuilder setSize(int pixels) {
        this.size = pixels;
        return this;
    }

    public AvatarUrlBuilder setShape(Shape shape) {
        this.shape = shape;
        return this;
    }



    public String toUrl() {
        String host = Configuration.getProdServerAddress();
        String base = this.baseRoute;

        switch (base) {
            case Routes.USER_MAP_POINTER:
                return (new StringBuilder(host)
                        .append(base.replace("{" + User.ID + "}", String.valueOf(this.userId)))
                        .append("?sz=")
                        .append(this.size)
                        .toString());
            default:
            case Routes.USER_AVATAR:
                return (new StringBuilder(host)
                        .append(base.replace("{" + User.ID + "}", String.valueOf(this.userId)))
                        .append("?sz=")
                        .append(this.size)
                        .append("&sp=")
                        .append(this.shape)
                        .toString());
        }
    }
}
