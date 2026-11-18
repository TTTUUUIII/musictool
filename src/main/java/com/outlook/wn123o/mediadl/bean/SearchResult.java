package com.outlook.wn123o.mediadl.bean;

import lombok.Data;

@Data
public abstract class SearchResult {
    protected String title;
    protected String coverUrl;
    protected String album;
    protected String artist;
}
