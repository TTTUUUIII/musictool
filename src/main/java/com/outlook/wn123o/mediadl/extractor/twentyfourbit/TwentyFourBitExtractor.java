package com.outlook.wn123o.mediadl.extractor.twentyfourbit;

import com.fasterxml.jackson.core.type.TypeReference;
import com.outlook.wn123o.mediadl.bean.MediaSource;
import com.outlook.wn123o.mediadl.bean.SearchResult;
import com.outlook.wn123o.mediadl.interfaces.Extractor;
import com.outlook.wn123o.mediadl.util.JsonUtils;
import com.outlook.wn123o.mediadl.util.Settings;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TwentyFourBitExtractor implements Extractor {

    private static final String BASE_URL = "https://www.24bit.net/";
    private static final String UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/157.0.0.0 Safari/537.36";
    private OkHttpClient httpClient;

    public TwentyFourBitExtractor() {
        createHttpClient();
    }

    @Override
    public String getTag() {
        return "24bit";
    }

    @Override
    public List<SearchResult> search(String s) {
        RequestBody requestBody = RequestBody.create(JsonUtils.toJson(SearchRequestData.builder()
                .page(1)
                .keyword(s)
                .build()), MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(url("api/player/searchOnlineMusicOne"))
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Origin", BASE_URL)
                .header("User-Agent", UA)
                .post(requestBody)
                .build();
        try (Response response = httpClient.newCall(request).execute()){
            if (response.isSuccessful()) {
                TwentyFourBitResponse<List<TwentyFourBitSearchResult>> data = JsonUtils.fromJson(response.body().string(), new TypeReference<TwentyFourBitResponse<List<TwentyFourBitSearchResult>>>() {});
                if (data.isStatus()) {
                    return data.getResult().stream().map(SearchResultExt::of)
                            .collect(Collectors.toList());
                }
            } else {
                System.err.println(response.code() + "\n" + response.body().string());
            }
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
        return List.of();
    }

    @Override
    public MediaSource extract(SearchResult result) {
        if (!(result instanceof SearchResultExt metadata)) {
            throw new UnsupportedOperationException("Invalid metadata!");
        }
        Request request = new Request.Builder()
                .url(url("music/a/" + metadata.getId()))
                .header("User-Agent", UA)
                .header("Origin", BASE_URL)
                .build();
        try (Response response = httpClient.newCall(request).execute()){
            String s = response.body().string();
            if (response.isSuccessful()) {
                return parseAudioSource(s);
            } else {
                System.err.println(s);
            }
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
        return null;
    }

    @Override
    public void download(@NotNull MediaSource source) {
        ProcessBuilder builder = new ProcessBuilder();
        ArrayList<String> command = new ArrayList<>();
        command.add("ffmpeg");
        command.add("-i");
        command.add(source.getUrl());
        command.add("-i");
        command.add(source.getCoverUrl());
        command.add("-map");
        command.add("0:a");
        command.add("-map");
        command.add("1:v");
        command.add("-disposition:v:0");
        command.add("attached_pic");
        command.add("-vf");
        command.add("scale=600:600");
        command.add("-c:v");
        command.add("mjpeg");
        if (source.getAlbum() != null) {
            command.add("-metadata");
            command.add(String.format("album=%s", source.getAlbum()));
        }
        command.add("-metadata");
        command.add(String.format("title=%s", source.getTitle()));
        command.add("-metadata");
        command.add(String.format("artist=%s", source.getArtist()));
        String fileName = String.format("%s - %s.flac", source.getArtist(), source.getTitle())
                        .replaceAll("[\\\\/:*?\"<>|]", " ");
        command.add(Paths.get(Settings.getString("download.path", ""), fileName).toString());
        builder.command(command);
        builder.inheritIO();
        try {
            Process process = builder.start();
            process.waitFor();
            process.destroy();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onSettingChanged(String k) {
        if ("proxy.http".equals(k)) {
            createHttpClient();
        }
    }

    private void createHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        if (Settings.getString("proxy.http") != null) {
            URI uri = URI.create(Settings.getString("proxy.http"));
            builder.proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(uri.getHost(), uri.getPort())));
        }
        httpClient = builder.build();
    }

    private MediaSource parseAudioSource(String html) {
        String s = buildFlightStream(html);
        ItemMusic item = parseItemMusic(s);
        if (item == null || item.getUrl() == null || item.getUrl().isBlank()) {
            return null;
        }
        MediaSource source = new MediaSource();
        source.setTitle(item.getName());
        source.setCoverUrl(item.getCover());
        source.setAlbum(item.getAlbum());
        source.setArtist(item.getPlayer());
        source.setUrl(item.getUrl());
        source.setSize(item.getSize());
        source.setQuality(item.getQuality());
        source.setFormat(item.getFormat());
        return source;
    }


    private static final Pattern NEXT_F_PUSH = Pattern.compile(
            "self\\.__next_f\\.push\\(\\[1,\"((?:[^\"\\\\]++|\\\\.)*+)\"]\\)",
            Pattern.DOTALL);

    private static final String ITEM_MUSIC_KEY = "\"itemMusic\":";

    private static String buildFlightStream(String html) {
        StringBuilder sb = new StringBuilder(html.length());
        Matcher m = NEXT_F_PUSH.matcher(html);
        while (m.find()) {
            sb.append(JsonUtils.fromJson('"' + m.group(1) + '"', String.class));
        }
        return sb.toString();
    }

    private static ItemMusic parseItemMusic(String stream) {
        int idx = stream.indexOf(ITEM_MUSIC_KEY);
        if (idx < 0) {
            return null;
        }
        try {
            return JsonUtils.fromJson(stream.substring(idx + ITEM_MUSIC_KEY.length()), ItemMusic.class);
        } catch (RuntimeException e) {
            e.printStackTrace(System.err);
            return null;
        }
    }

    private static String url(String path) {
        return BASE_URL + path;
    }
}
