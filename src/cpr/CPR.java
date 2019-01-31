/*
 * Copyright (C) 2015 by Oliver Jowett <oliver@mutability.co.uk>
 * Copyright (C) 2012 by Salvatore Sanfilippo <antirez@gmail.com>
 *
 * All rights reserved
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *  *  Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *  *  Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package cpr;

public final class CPR {

    private static final double TAU = Math.PI * 2.0;
    private static final double[] NL = new double[59];       // NL[0..58] Number of Longitude Zones

    /*
     * Initialize the NL Table (Number of Longitude Zones as a function of
     * latitude)
     *
     * This has been verified with a published ICAO fixed table.
     * I like this algorithm better than the dump1090 one.
     */
     public CPR() {
        double tmp = (1.0 - Math.cos(Math.PI / 30.0));

        NL[0] = 90.0;

        for (int i = 2; i < 60; i++) {
            NL[i - 1] = Math.toDegrees(Math.acos(Math.sqrt(tmp / (1.0 - Math.cos(TAU / (double) i)))));
        }
    }

    private int cprNLFunction(double lat) {
        int i = 58;

        lat = Math.abs(lat);

        if (lat == 0.0) {
            return 59;                  // Equator
        } else if (lat == 87.0) {
            return 2;
        } else if (lat > 87.0) {
            return 1;                   // Pole
        }

        while (lat > NL[i]) {
            i--;
        }

        return (i + 1);     // Java is Arabic - starts at zero...
    }

    /*
     * Always positive MOD operation, used for CPR decoding.
     */
    private int cprModInt(int a, int b) {
        int res = a % b;

        if (res < 0) {
            res += b;
        }

        return res;
    }

    private double cprModDouble(double a, double b) {
        if (b == 0.0) {
            return Double.NaN;
        }
        
        double res = Math.IEEEremainder(a, b);

        if (res < 0.0) {
            res += b;
        }

        return res;
    }

    private int cprNFunction(double lat, boolean fflag) {
        int nl = cprNLFunction(lat) - ((fflag == true) ? 1 : 0);

        if (nl < 1) {
            nl = 1;
        }

        return nl;
    }

    private double cprDlonFunction(double lat, boolean fflag, boolean surface) {
        return ((surface == true) ? 90.0 : 360.0) / cprNFunction(lat, fflag);
    }

    public int decodeCPRairborne(int even_cprlat, int even_cprlon,
            int odd_cprlat, int odd_cprlon, boolean fflag, double[] out_lat, double[] out_lon) {
        double AirDlat0 = 360.0 / 60.0;
        double AirDlat1 = 360.0 / 59.0;
        double lat0 = even_cprlat;
        double lat1 = odd_cprlat;
        double lon0 = even_cprlon;
        double lon1 = odd_cprlon;

        double rlat, rlon;

        // Compute the Latitude Index "j"
        int j = (int) Math.floor(((59.0 * lat0 - 60.0 * lat1) / 131072.0) + 0.5);
        double rlat0 = AirDlat0 * (cprModInt(j, 60) + lat0 / 131072.0);
        double rlat1 = AirDlat1 * (cprModInt(j, 59) + lat1 / 131072.0);

        if (rlat0 >= 270.0) {
            rlat0 -= 360.0;
        }

        if (rlat1 >= 270.0) {
            rlat1 -= 360.0;
        }

        // Check to see that the latitude is in range: -90 .. +90
        if ((rlat0 < -90.0) || (rlat0 > 90.0) ||
                (rlat1 < -90.0) || (rlat1 > 90.0)) {
            return -2; // bad data
        }
        
        // Check that both are in the same latitude zone, or abort.
        if (cprNLFunction(rlat0) != cprNLFunction(rlat1)) {
            return -1; // positions crossed a latitude zone, try again later
        }
        
        // Compute ni and the Longitude Index "m"
        if (fflag == true) { // Use odd packet.
            int ni = cprNFunction(rlat1, true);
            int m = (int) Math.floor((((lon0 * (cprNLFunction(rlat1) - 1))
                    - (lon1 * cprNLFunction(rlat1))) / 131072.0) + 0.5);
            rlon = cprDlonFunction(rlat1, true, false) * (cprModInt(m, ni) + lon1 / 131072.0);
            rlat = rlat1;
        } else {     // Use even packet.
            int ni = cprNFunction(rlat0, false);
            int m = (int) Math.floor((((lon0 * (cprNLFunction(rlat0) - 1))
                    - (lon1 * cprNLFunction(rlat0))) / 131072.0) + 0.5);
            rlon = cprDlonFunction(rlat0, false, false) * (cprModInt(m, ni) + lon0 / 131072.0);
            rlat = rlat0;
        }

        // Renormalize to -180 .. +180
        rlon -= Math.floor((rlon + 180.0) / 360.0) * 360.0;

        out_lat[0] = rlat;
        out_lon[0] = rlon;

        return 0;
    }

    public int decodeCPRsurface(double reflat, double reflon,
            int even_cprlat, int even_cprlon, int odd_cprlat, int odd_cprlon,
            boolean fflag, double[] out_lat, double[] out_lon) {
        double AirDlat0 = 90.0 / 60.0;
        double AirDlat1 = 90.0 / 59.0;
        double lat0 = even_cprlat;
        double lat1 = odd_cprlat;
        double lon0 = even_cprlon;
        double lon1 = odd_cprlon;
        double rlon, rlat;

        // Compute the Latitude Index "j"
        int j = (int) Math.floor(((59.0 * lat0 - 60.0 * lat1) / 131072.0) + 0.5);
        double rlat0 = AirDlat0 * (cprModInt(j, 60) + lat0 / 131072.0);
        double rlat1 = AirDlat1 * (cprModInt(j, 59) + lat1 / 131072.0);

        /*
         * Pick the quadrant that's closest to the reference location -
         * this is not necessarily the same quadrant that contains the
         * reference location.
         *
         * There are also only two valid quadrants:
         * -90..0 and 0..90;
         * no correct message would try to encoding a latitude in the
         * ranges -180..-90 and 90..180.
         *
         * If the computed latitude is more than 45 degrees north of
         * the reference latitude (using the northern hemisphere
         * solution), then the southern hemisphere solution will be
         * closer to the reference latitude.
         *
         * e.g. reflat=0, rlat=44, use rlat=44
         * reflat=0, rlat=46, use rlat=46
         * -90 = -44
         * reflat=40, rlat=84, use rlat=84
         * reflat=40, rlat=86, use rlat=86
         * -90 = -4
         * reflat=-40, rlat=4, use rlat=4
         * reflat=-40, rlat=6, use rlat=6
         * -90 = -84
         *
         * As a special case, -90, 0 and +90 all encode to zero, so
         * there's a little extra work to do there.
         */
        if (rlat0 == 0.0) {
            if (reflat < -45.0) {
                rlat0 = -90.0;
            } else if (reflat > 45.0) {
                rlat0 = 90.0;
            }
        } else if ((rlat0 - reflat) > 45.0) {
            rlat0 -= 90.0;
        }

        if (rlat1 == 0.0) {
            if (reflat < -45.0) {
                rlat1 = -90.0;
            } else if (reflat > 45.0) {
                rlat1 = 90.0;
            }
        } else if ((rlat1 - reflat) > 45.0) {
            rlat1 -= 90.0;
        }
        
        // Check to see that the latitude is in range: -90 .. +90
        if ((rlat0 < -90.0) || (rlat0 > 90.0) ||
                (rlat1 < -90.0) || (rlat1 > 90.0)) {
            return -2; // bad data
        }

        // Check that both are in the same latitude zone, or abort.
        if (cprNLFunction(rlat0) != cprNLFunction(rlat1)) {
            return -1; // positions crossed a latitude zone, try again later
        }

        // Compute ni and the Longitude Index "m"
        if (fflag == true) { // Use odd packet.
            int ni = cprNFunction(rlat1, true);
            int m = (int) Math.floor((((lon0 * (cprNLFunction(rlat1) - 1))
                    - (lon1 * cprNLFunction(rlat1))) / 131072.0) + 0.5);
            rlon = cprDlonFunction(rlat1, true, true) * (cprModInt(m, ni) + lon1 / 131072.0);
            rlat = rlat1;
        } else {     // Use even packet.
            int ni = cprNFunction(rlat0, false);
            int m = (int) Math.floor((((lon0 * (cprNLFunction(rlat0) - 1))
                    - (lon1 * cprNLFunction(rlat0))) / 131072.0) + 0.5);
            rlon = cprDlonFunction(rlat0, false, true) * (cprModInt(m, ni) + lon0 / 131072.0);
            rlat = rlat0;
        }

        /*
         * Pick the quadrant that's closest to the reference location -
         * this is not necessarily the same quadrant that contains the
         * reference location. Unlike the latitude case, all four
         * quadrants are valid.
         *
         * if reflon is more than 45 degrees away, move some multiple
         * of 90 degrees towards it.
         */
        rlon += Math.floor((reflon - rlon + 45.0) / 90.0) * 90.0;  // this might move us outside (-180..+180), we fix this below

        // Renormalize to -180 .. +180
        rlon -= Math.floor((rlon + 180.0) / 360.0) * 360.0;

        out_lat[0] = rlat;
        out_lon[0] = rlon;

        return 0;
    }

    public int decodeCPRrelative(double reflat, double reflon,
            int cprlat, int cprlon, boolean fflag, boolean surface,
            double[] out_lat, double[] out_lon) {
        double AirDlat;
        double AirDlon;
        double fractional_lat = cprlat / 131072.0;
        double fractional_lon = cprlon / 131072.0;
        double rlon, rlat;
        int j, m;

        AirDlat = ((surface == true) ? 90.0 : 360.0) / ((fflag == true) ? 59.0 : 60.0);

        // Compute the Latitude Index "j"
        j = (int) (Math.floor(reflat / AirDlat)
                + Math.floor(0.5 + cprModDouble(reflat, AirDlat) / AirDlat - fractional_lat));

        rlat = AirDlat * (j + fractional_lat);

        if (rlat >= 270.0) {
            rlat -= 360.0;
        }

        // Check to see that the latitude is in range: -90 .. +90
        if ((rlat < -90.0) || (rlat > 90.0)) {
            return -1;  // Time to give up - Latitude error
        }

        // Check to see that answer is reasonable - ie no more than 1/2 cell away
        if (Math.abs(rlat - reflat) > (AirDlat / 2.0)) {
            return -1; // Time to give up - Latitude error
        }

        // Compute the Longitude Index "m"
        AirDlon = cprDlonFunction(rlat, fflag, surface);

        m = (int) (Math.floor(reflon / AirDlon)
                + Math.floor(0.5 + cprModDouble(reflon, AirDlon) / AirDlon - fractional_lon));

        rlon = AirDlon * (m + fractional_lon);

        if (rlon > 180.0) {
            rlon -= 360.0;
        }

        // Check to see that answer is reasonable - ie no more than 1/2 cell away
        if (Math.abs(rlon - reflon) > (AirDlon / 2.0)) {
            return -1;   // Time to give up - Longitude error
        }

        out_lat[0] = rlat;
        out_lon[0] = rlon;

        return 0;
    }
}
