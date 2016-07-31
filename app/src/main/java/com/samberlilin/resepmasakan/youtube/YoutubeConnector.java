package com.samberlilin.resepmasakan.youtube;

import android.content.Context;
import android.util.Log;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.VideoListResponse;
import com.samberlilin.resepmasakan.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class YoutubeConnector {
    private static YouTube youtube;
    private YouTube.Search.List query;
    private YouTube.Videos.List query2;
    private YouTube.Playlists.List query3;
    private YouTube.PlaylistItems.List playlist_list_query;

    // Your developer key goes here

    //public static final String KEY = "AIzaSyAQmYZkhPE9de8zHfJW7criFyL1PZnoXpI";
    //public static final String KEY = "AIzaSyA4ce0kwiMOCVQ323JHCw2jLHNtplwrHBA";
    public static final String KEY = "AIzaSyCsElfqSpIPU0x5U7LACfPrkCqrp8p9PMA";


    public YoutubeConnector(Context context) {
        youtube = new YouTube.Builder(new NetHttpTransport(),
                new JacksonFactory(), new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest hr) throws IOException {
            }
        }).setApplicationName(context.getString(R.string.app_name)).build();

        try {
            query = youtube.search().list("id,snippet");
            query.setKey(KEY);
            query.setType("video");
            query.setFields("items(id/videoId,snippet/title,snippet/description,snippet/thumbnails/default/url,snippet/publishedAt)");
            query.setMaxResults((long) 50);

            query2 =  youtube.videos().list("statistics");
            query2.setKey(KEY);

        } catch (IOException e) {
            Log.d("YC", "Could not initialize: " + e);
        }
    }

    public List<VideoItem> search(String keywords) {
        query.setQ(keywords);
        try {
            SearchListResponse response = query.execute();
            List<SearchResult> results = response.getItems();

            List<VideoItem> items = new ArrayList<VideoItem>();
            for (SearchResult result : results) {
                VideoItem item = new VideoItem();
                item.setTitle(result.getSnippet().getTitle());
                item.setDescription(result.getSnippet().getDescription());
                item.setThumbnailURL(result.getSnippet().getThumbnails().getDefault().getUrl());
                item.setId(result.getId().getVideoId());

                query2.setId(result.getId().getVideoId().toString());
                System.out.println(query2);
                VideoListResponse videoResponse = query2.execute();
                item.setTotalViews(videoResponse.getItems().get(0).getStatistics().getViewCount().toString());

                try {
                    item.setCreationTime((result.getSnippet().getPublishedAt().toString().split("T"))[0]);

                }
                catch(Exception e) {
                    e.printStackTrace();
                    item.setCreationTime(result.getSnippet().getPublishedAt().toString());
                    Log.d("YC", "From the date convertion exception");
                }

                //item.setViews(result.getSnippet().getViews());
                items.add(item);
            }
            return items;
        } catch (IOException e) {
            Log.d("YC", "Could not search: " + e);
            return null;
        }
    }
}