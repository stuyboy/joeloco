package com.joechang.kursor.commands;

import com.joechang.kursor.AbstractCliBodyProcessor;
import com.joechang.kursor.CliProcessedCallback;
import com.joechang.kursor.CommandLineProcessor;
import com.joechang.loco.Configuration;
import com.joechang.loco.client.GiphyClient;
import com.joechang.loco.client.RestClientFactory;
import com.joechang.loco.model.Search;
import org.json.simple.JSONObject;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.regex.Pattern;

/**
 * Author:    joechang
 * Created:   10/5/15 4:22 PM
 * Purpose:
 */
public class GiphyCliBodyProcessor extends AbstractCliBodyProcessor {
    public static final String GIF_PHRASE = "(?:@gif|@giphy|@sticker)";

    private static final String SEARCH_WHAT = GIF_PHRASE + "\\s+(.*)"; //(?:\\s+(?:in|near)\\s+|$)(.+)?";
    private static final Pattern mWhatMatcher = Pattern.compile(SEARCH_WHAT);

    private GiphyClient gClient;

    public GiphyCliBodyProcessor(CommandLineProcessor cli) {
        super(cli, GIF_PHRASE);

        gClient = RestClientFactory.getInstance()
                .createRestAdapter(Configuration.getGiphyApiEndpoint())
                .create(GiphyClient.class);

    }

    @Override
    public void doOnFind(final String[] address, String body, CliProcessedCallback callback) {
        String what = regexForSearchString(body)[0];

        if (body.indexOf("@sticker") >= 0) {
            gClient.searchStickers(what, 10, GiphyClient.Rating.PG13, GiphyClient.API_KEY, new GiphyCallback(what, address));
        } else {
            gClient.searchImages(what, 10, GiphyClient.Rating.PG13, GiphyClient.API_KEY, new GiphyCallback(what, address));
        }
    }

    /**
     * Giphy api has problems.  The sizes are not always accurate.  The ones that are:
     * downsized.  If this size is < 300k, use it!  otherwise
     * *downsampled.  If any of these are < 300k, use it!  otherwise
     * use downsized_still.  better than nothing.
     *
     * @param images
     * @return
     */
    private String pickSizedUrl(Map images) {
        List<Map> consideredAlts = new ArrayList<Map>();

        consideredAlts.add((Map) images.get("downsized"));
        consideredAlts.add((Map) images.get("fixed_height_downsampled"));
        consideredAlts.add((Map) images.get("fixed_width_downsampled"));

        for (Map m : consideredAlts) {
            String size = m.get("size") == null ? null : m.get("size").toString();
            if (size != null) {
                Long ls = Long.parseLong(size);
                if (ls < 300000) {
                    return m.get("url").toString();
                }
            }
        }
        return ((Map) images.get("downsized_still")).get("url").toString();
    }

    protected String[] regexForSearchString(String body) {
        return doRegex(mWhatMatcher, body);
    }

    class GiphyCallback implements Callback<JSONObject> {
        private String what;
        private String[] address;

        protected GiphyCallback(String what, String[] address) {
            this.what = what;
            this.address = address;
        }

        @Override
        public void success(JSONObject jsonObject, Response response) {
            Search s = new Search();
            s.setJson(jsonObject.toJSONString());
            s.setQuery(what);
            s.setSource("Giphy");

            try {
                List<JSONObject> array = (List<JSONObject>) jsonObject.get("data");

                //Choose a random image in the array
                int totalSize = array.size();
                Random r = new Random();
                int randomIdx = r.nextInt(totalSize);

                Map item = array.get(randomIdx);
                Map images = (Map) item.get("images");
                String url = pickSizedUrl(images);
                s.setResponse(new String[]{url});
                log.info("Choosing image: " + url);
                getCommandLineProcessor().doOutput(address, s, new URL(url));
            } catch (Exception mue) {
                log.log(Level.SEVERE, "Could not retrieve image.", mue);
                getCommandLineProcessor().doError(address, "No image found");
            }
        }

        @Override
        public void failure(RetrofitError error) {

        }
    }
}
