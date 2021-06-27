/*
 * Copyright (C) 2015 by Oliver Jowett <oliver@mutability.co.uk>
 * Copyright (C) 2012 by Salvatore Sanfilippo <antirez@gmail.com>
 *
 * All rights reserved
 */
package cpr;

public class LatLon {
    private double lat;
    private double lon;

    public LatLon(double latval, double lonval) {
        lat = latval;
        lon = lonval;
    }
    
    public LatLon() {
        this(0.0, 0.0);
    }
    
    public double getLat() {
        return lat;
    }
    
    public double getLon() {
        return lon;
    }
}
