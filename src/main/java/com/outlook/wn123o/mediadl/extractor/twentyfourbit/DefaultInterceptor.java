package com.outlook.wn123o.mediadl.extractor.twentyfourbit;

import com.outlook.wn123o.mediadl.util.Settings;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;

public class DefaultInterceptor implements Interceptor {
    private static final String UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/157.0.0.0 Safari/537.36";
    public DefaultInterceptor(String origin) {
        this.origin = URI.create(origin);
    }

    private final URI origin;
    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        Request originRequest = chain.request();
        Request newRequest = originRequest.newBuilder()
                .header("Origin", origin.toString())
                .header("User-Agent", UA)
                .header("Cookie", Settings.getString(String.format("cookie.%s", origin.getHost()), ""))
                .build();
        return chain.proceed(newRequest);
    }
}
