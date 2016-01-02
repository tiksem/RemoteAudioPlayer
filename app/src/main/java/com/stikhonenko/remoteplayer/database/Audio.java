package com.stikhonenko.remoteplayer.database;

public class Audio {
    private String title;
    private String artistsName;
    private String url;

    private Audio() {

    }

    public String getTitle() {
        return title;
    }

    public String getArtistsName() {
        return artistsName;
    }

    public String getUrl() {
        return url;
    }

    public static class Builder {
        private Audio audio;

        public Builder() {
            audio = new Audio();
        }
        
        public Builder setTitle(String title) {
            audio.title = title;
            return this;
        }

        public Builder setArtistsName(String artistsName) {
            audio.artistsName = artistsName;
            return this;
        }

        public Builder setUrl(String url) {
            audio.url = url;
            return this;
        }

        public Audio build() {
            return audio;
        }
    }
}
