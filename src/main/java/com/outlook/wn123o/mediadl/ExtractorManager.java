package com.outlook.wn123o.mediadl;

import com.outlook.wn123o.mediadl.extractor.twentyfourbit.TwentyFourBitExtractor;
import com.outlook.wn123o.mediadl.interfaces.Extractor;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ExtractorManager {

    private ExtractorManager() {}
    private final Map<String, Extractor> extractors = new ConcurrentHashMap<>();

    public Extractor getExtractor(@NotNull String tag) {
        return extractors.get(tag);
    }

    private static final class InstanceHolder {
        private static final ExtractorManager INSTANCE = new ExtractorManager();

        static {
            Extractor extractor = new TwentyFourBitExtractor();
            INSTANCE.extractors.put(extractor.getTag(), extractor);
        }
    }

    public static ExtractorManager getInstance() {
        return InstanceHolder.INSTANCE;
    }
}
