package com.joechang.loco;

import com.joechang.loco.config.Params;
import com.joechang.loco.config.Routes;
import com.joechang.loco.service.OpenTableAPI;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Author:    joechang
 * Created:   9/14/15 6:31 PM
 * Purpose:   For purposes of redirecting for shortUrl provider krsr.co/
 */
@Controller
public class RedirectController {

    public static String BASE_URL = "http://krsr.co:8080";

    @RequestMapping(Routes.OPENTABLE_REDIRECT + "{" + Params.ID + "}")
    public String redirect(
            @PathVariable(value = Params.ID) String yid
    ) {
        return "redirect:" + OpenTableAPI.MOBILE_RESERVE_URL + yid;
    }

}
