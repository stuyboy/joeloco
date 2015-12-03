package com.joechang.loco.response;

import com.joechang.loco.model.Search;

/**
 * Author:    joechang
 * Created:   7/17/15 3:50 PM
 * Purpose:
 */
public class SearchResult extends AbstractDeferredResult<Search> {
    @Override
    public Class baseClass() {
        return Search.class;
    }
}
