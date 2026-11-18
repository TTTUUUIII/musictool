package com.outlook.wn123o.mediadl.extractor.twentyfourbit;

import com.outlook.wn123o.mediadl.bean.SearchResult;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class SearchResultExt extends SearchResult {
    private String id;

    public static SearchResultExt of(TwentyFourBitSearchResult result) {
        SearchResultExt metadata = new SearchResultExt();
        metadata.setId(result.getId());
        metadata.setAlbum(result.getAlbum());
        metadata.setArtist(result.getPlayer());
        metadata.setTitle(result.getName());
        metadata.setCoverUrl(result.getCover());
        return metadata;
    }
}
