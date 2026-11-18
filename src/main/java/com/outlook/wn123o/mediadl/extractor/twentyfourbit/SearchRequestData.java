package com.outlook.wn123o.mediadl.extractor.twentyfourbit;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SearchRequestData {
    private int page;
    private String keyword;
}
