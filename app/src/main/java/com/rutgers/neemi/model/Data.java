package com.rutgers.neemi.model;

import android.provider.MediaStore;

/**
 * Created by suitcase on 3/15/18.
 */

public class Data{

    private Images images;
    private User user;
    private String link;
    private Caption caption;
    private long created_time;
    private UsersTagged[] users_in_photo;
    private String[] tags;
    private Location location;
    private String id;

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getId() {
        return id;
    }
    public long getCreated_time() {
        return created_time;
    }

    public UsersTagged[] getUsers_in_photo() {
        return users_in_photo;
    }

    public String[] getTags() {
        return tags;
    }

    public Location getLocation() {
        return location;
    }

    public Images getImages() {
        return images;
    }

    public User getUser() {
        return user;
    }


    public Caption getCaption() {return caption;}

    public class Location {
        private String id;
        private double latitude;
        private double longitude;
        private String street_address;
        private String name;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }

        public String getStreet_address() {
            return street_address;
        }

        public void setStreet_address(String street_address) {
            this.street_address = street_address;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public class Caption {
        private String text;

        public String getText() {
            return text;
        }
    }

    public class User {

        private String id;
        private String profile_picture;
        private String full_name;
        private String username;

        public String getProfile_picture() {
            return profile_picture;
        }

        public String getFull_name() {
            return full_name;
        }

        public String getUsername() {
            return username;
        }

        public String getId() {
            return id;
        }
    }

    public class UsersTagged {

        private User user;
        private Position position;

        public User getUser() {
            return user;
        }

        public Position getPosition() {
            return position;
        }

        public class Position {
            private double x;
            private double y;

            public double getX() {
                return x;
            }

            public double getY() {
                return y;
            }
        }
    }


    public class Images {

        private Standard_resolution standard_resolution;
        private Thumbnail thumbnail;

        public Standard_resolution getStandard_resolution() {
            return standard_resolution;
        }

        public Thumbnail getThumbnail() {
            return thumbnail;
        }

        public class Standard_resolution {

            private String url;

            public String getUrl() {
                return url;
            }
        }

        public class Thumbnail {

            private String url;

            public String getUrl() {
                return url;
            }

        }
    }
}