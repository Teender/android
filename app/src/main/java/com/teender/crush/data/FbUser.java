package com.teender.crush.data;

public class FbUser {
    public String name;
    public String id;
    public Picture picture;

    public static class Picture {
        public Data data;
    }

    public static class Data {
        public int height;
        public int width;
        public boolean is_silhouette;
        public String url;
    }

}
