package com.outlook.wn123o.mediadl.extractor.twentyfourbit;

import lombok.Data;

@Data
public class TwentyFourBitResponse<T> {
    private boolean status;
    private T result;
}
