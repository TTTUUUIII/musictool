package com.outlook.wn123o.mediadl.interfaces;

import com.outlook.wn123o.mediadl.bean.MediaSource;
import com.outlook.wn123o.mediadl.bean.SearchResult;

import java.util.List;

public interface Extractor {
    String getTag();
    List<SearchResult> search(String s);

    MediaSource extract(SearchResult meta);
}
