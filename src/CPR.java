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

public class CPR {

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
    
    // Global, airborne CPR test data:
    private class cprGlobalAirborneTest {

        public final int even_cprlat;
        public final int even_cprlon;    // input: raw CPR values, even message
        public final int odd_cprlat;
        public final int odd_cprlon;     // input: raw CPR values, odd message
        public final int even_result;    // verify: expected result from decoding with fflag=0 (even message is latest)
        public final double even_rlat;
        public final double even_rlon;   // verify: expected position from decoding with fflag=0 (even message is latest)
        public final int odd_result;     // verify: expected result from decoding with fflag=1 (odd message is latest)
        public final double odd_rlat;
        public final double odd_rlon;    // verify: expected position from decoding with fflag=1 (odd message is latest)

        public cprGlobalAirborneTest(int val1, int val2, int val3, int val4, int val5,
                double val6, double val7, int val8, double val9, double val10) {
            even_cprlat = val1;
            even_cprlon = val2;
            odd_cprlat = val3;
            odd_cprlon = val4;
            even_result = val5;
            even_rlat = val6;
            even_rlon = val7;
            odd_result = val8;
            odd_rlat = val9;
            odd_rlon = val10;
        }
    }

    private final cprGlobalAirborneTest cprGlobalAirborneTests[] = {
        new cprGlobalAirborneTest(80536, 9432, 61720, 9192, 0, 51.686646, 0.700156, 0, 51.686763, 0.701294),
        new cprGlobalAirborneTest(80534, 9413, 61714, 9144, 0, 51.686554, 0.698745, 0, 51.686484, 0.697632)
    };

    // Global, surface CPR test data:
    private class cprGlobalSurfaceTest {

        public final double reflat;
        public final double reflon;    // input: reference location for decoding
        public final int even_cprlat;
        public final int even_cprlon;  // input: raw CPR values, even message
        public final int odd_cprlat;
        public final int odd_cprlon;   // input: raw CPR values, odd message
        public final int even_result;  // verify: expected result from decoding with fflag=0 (even message is latest)
        public final double even_rlat;
        public final double even_rlon; // verify: expected position from decoding with fflag=0 (even message is latest)
        public final int odd_result;   // verify: expected result from decoding with fflag=1 (odd message is latest)
        public final double odd_rlat;
        public final double odd_rlon;  // verify: expected position from decoding with fflag=1 (odd message is latest)

        public cprGlobalSurfaceTest(double val1, double val2, int val3, int val4, int val5,
                int val6, int val7, double val8, double val9, int val10,
                double val11, double val12) {
            reflat = val1;
            reflon = val2;
            even_cprlat = val3;
            even_cprlon = val4;
            odd_cprlat = val5;
            odd_cprlon = val6;
            even_result = val7;
            even_rlat = val8;
            even_rlon = val9;
            odd_result = val10;
            odd_rlat = val11;
            odd_rlon = val12;
        }
    }

    private final cprGlobalSurfaceTest cprGlobalSurfaceTests[] = {
        // The real position received here was on the Cambridge (UK) airport apron at 52.21N 0.177E
        // We mess with the reference location to check that the right quadrant is used.

        // longitude quadrants:
        new cprGlobalSurfaceTest(52.00, -180.00, 105730, 9259, 29693, 8997, 0, 52.209984, 0.176601 - 180.0, 0, 52.209976, 0.176507 - 180.0),
        new cprGlobalSurfaceTest(52.00, -140.00, 105730, 9259, 29693, 8997, 0, 52.209984, 0.176601 - 180.0, 0, 52.209976, 0.176507 - 180.0),
        new cprGlobalSurfaceTest(52.00, -130.00, 105730, 9259, 29693, 8997, 0, 52.209984, 0.176601 - 90.0, 0, 52.209976, 0.176507 - 90.0),
        new cprGlobalSurfaceTest(52.00, -50.00, 105730, 9259, 29693, 8997, 0, 52.209984, 0.176601 - 90.0, 0, 52.209976, 0.176507 - 90.0),
        new cprGlobalSurfaceTest(52.00, -40.00, 105730, 9259, 29693, 8997, 0, 52.209984, 0.176601, 0, 52.209976, 0.176507),
        new cprGlobalSurfaceTest(52.00, -10.00, 105730, 9259, 29693, 8997, 0, 52.209984, 0.176601, 0, 52.209976, 0.176507),
        new cprGlobalSurfaceTest(52.00, 0.00, 105730, 9259, 29693, 8997, 0, 52.209984, 0.176601, 0, 52.209976, 0.176507),
        new cprGlobalSurfaceTest(52.00, 10.00, 105730, 9259, 29693, 8997, 0, 52.209984, 0.176601, 0, 52.209976, 0.176507),
        new cprGlobalSurfaceTest(52.00, 40.00, 105730, 9259, 29693, 8997, 0, 52.209984, 0.176601, 0, 52.209976, 0.176507),
        new cprGlobalSurfaceTest(52.00, 50.00, 105730, 9259, 29693, 8997, 0, 52.209984, 0.176601 + 90.0, 0, 52.209976, 0.176507 + 90.0),
        new cprGlobalSurfaceTest(52.00, 130.00, 105730, 9259, 29693, 8997, 0, 52.209984, 0.176601 + 90.0, 0, 52.209976, 0.176507 + 90.0),
        new cprGlobalSurfaceTest(52.00, 140.00, 105730, 9259, 29693, 8997, 0, 52.209984, 0.176601 - 180.0, 0, 52.209976, 0.176507 - 180.0),
        new cprGlobalSurfaceTest(52.00, 180.00, 105730, 9259, 29693, 8997, 0, 52.209984, 0.176601 - 180.0, 0, 52.209976, 0.176507 - 180.0),
        // latitude quadrants (but only 2). The decoded longitude also changes because the cell size changes with latitude
        new cprGlobalSurfaceTest(90.00, 0.00, 105730, 9259, 29693, 8997, 0, 52.209984, 0.176601, 0, 52.209976, 0.176507),
        new cprGlobalSurfaceTest(52.00, 0.00, 105730, 9259, 29693, 8997, 0, 52.209984, 0.176601, 0, 52.209976, 0.176507),
        new cprGlobalSurfaceTest(8.00, 0.00, 105730, 9259, 29693, 8997, 0, 52.209984, 0.176601, 0, 52.209976, 0.176507),
        new cprGlobalSurfaceTest(7.00, 0.00, 105730, 9259, 29693, 8997, 0, 52.209984 - 90.0, 0.135269, 0, 52.209976 - 90.0, 0.134299),
        new cprGlobalSurfaceTest(-52.00, 0.00, 105730, 9259, 29693, 8997, 0, 52.209984 - 90.0, 0.135269, 0, 52.209976 - 90.0, 0.134299),
        new cprGlobalSurfaceTest(-90.00, 0.00, 105730, 9259, 29693, 8997, 0, 52.209984 - 90.0, 0.135269, 0, 52.209976 - 90.0, 0.134299),
        // poles/equator cases
        new cprGlobalSurfaceTest(-46.00, -180.00, 0, 0, 0, 0, 0, -90.0, -180.000000, 0, -90.0, -180.0), // south pole
        new cprGlobalSurfaceTest(-44.00, -180.00, 0, 0, 0, 0, 0, 0.0, -180.000000, 0, 0.0, -180.0), // equator
        new cprGlobalSurfaceTest(44.00, -180.00, 0, 0, 0, 0, 0, 0.0, -180.000000, 0, 0.0, -180.0), // equator
        new cprGlobalSurfaceTest(46.00, -180.00, 0, 0, 0, 0, 0, 90.0, -180.000000, 0, 90.0, -180.0) // north pole
    };

    // Relative CPR test data:
    private class cprRelativeTest {

        public final double reflat;
        public final double reflon;    // input: reference location for decoding
        public final int cprlat;
        public final int cprlon;       // input: raw CPR values, even or odd message
        public final boolean fflag;    // input: fflag in raw message
        public final boolean surface;  // input: decode as air (false) or surface (true) position
        public final int result;       // verify: expected result
        public final double rlat;
        public final double rlon;      // verify: expected position

        public cprRelativeTest(double val1, double val2, int val3, int val4, boolean val5,
                boolean val6, int val7, double val8, double val9) {
            reflat = val1;
            reflon = val2;
            cprlat = val3;
            cprlon = val4;
            fflag = val5;
            surface = val6;
            result = val7;
            rlat = val8;
            rlon = val9;
        }
    }

    private final cprRelativeTest cprRelativeTests[] = {
        //
        // AIRBORNE
        //

        new cprRelativeTest(52.00, 0.00, 80536, 9432, false, false, 0, 51.686646, 0.700156), // even, airborne
        new cprRelativeTest(52.00, 0.00, 61720, 9192, true, false, 0, 51.686763, 0.701294), // odd, airborne
        new cprRelativeTest(52.00, 0.00, 80534, 9413, false, false, 0, 51.686554, 0.698745), // even, airborne
        new cprRelativeTest(52.00, 0.00, 61714, 9144, true, false, 0, 51.686484, 0.697632), // odd, airborne

        // test moving the receiver around a bit
        // We cannot move it more than 1/2 cell away before ambiguity happens.

        // latitude must be within about 3 degrees (cell size is 360/60 = 6 degrees)
        new cprRelativeTest(48.70, 0.00, 80536, 9432, false, false, 0, 51.686646, 0.700156), // even, airborne
        new cprRelativeTest(48.70, 0.00, 61720, 9192, true, false, 0, 51.686763, 0.701294), // odd, airborne
        new cprRelativeTest(48.70, 0.00, 80534, 9413, false, false, 0, 51.686554, 0.698745), // even, airborne
        new cprRelativeTest(48.70, 0.00, 61714, 9144, true, false, 0, 51.686484, 0.697632), // odd, airborne
        new cprRelativeTest(54.60, 0.00, 80536, 9432, false, false, 0, 51.686646, 0.700156), // even, airborne
        new cprRelativeTest(54.60, 0.00, 61720, 9192, true, false, 0, 51.686763, 0.701294), // odd, airborne
        new cprRelativeTest(54.60, 0.00, 80534, 9413, false, false, 0, 51.686554, 0.698745), // even, airborne
        new cprRelativeTest(54.60, 0.00, 61714, 9144, true, false, 0, 51.686484, 0.697632), // odd, airborne

        // longitude must be within about 4.8 degrees at this latitude
        new cprRelativeTest(52.00, 5.40, 80536, 9432, false, false, 0, 51.686646, 0.700156), // even, airborne
        new cprRelativeTest(52.00, 5.40, 61720, 9192, true, false, 0, 51.686763, 0.701294), // odd, airborne
        new cprRelativeTest(52.00, 5.40, 80534, 9413, false, false, 0, 51.686554, 0.698745), // even, airborne
        new cprRelativeTest(52.00, 5.40, 61714, 9144, true, false, 0, 51.686484, 0.697632), // odd, airborne
        new cprRelativeTest(52.00, -4.10, 80536, 9432, false, false, 0, 51.686646, 0.700156), // even, airborne
        new cprRelativeTest(52.00, -4.10, 61720, 9192, true, false, 0, 51.686763, 0.701294), // odd, airborne
        new cprRelativeTest(52.00, -4.10, 80534, 9413, false, false, 0, 51.686554, 0.698745), // even, airborne
        new cprRelativeTest(52.00, -4.10, 61714, 9144, true, false, 0, 51.686484, 0.697632), // odd, airborne

        //
        // SURFACE
        //

        // Surface position on the Cambridge (UK) airport apron at 52.21N 0.18E
        new cprRelativeTest(52.00, 0.00, 105730, 9259, false, true, 0, 52.209984, 0.176601), // even, surface
        new cprRelativeTest(52.00, 0.00, 29693, 8997, true, true, 0, 52.209976, 0.176507), // odd, surface

        // test moving the receiver around a bit
        // We cannot move it more than 1/2 cell away before ambiguity happens.

        // latitude must be within about 0.75 degrees (cell size is 90/60 = 1.5 degrees)
        new cprRelativeTest(51.46, 0.00, 105730, 9259, false, true, 0, 52.209984, 0.176601), // even, surface
        new cprRelativeTest(51.46, 0.00, 29693, 8997, true, true, 0, 52.209976, 0.176507), // odd, surface
        new cprRelativeTest(52.95, 0.00, 105730, 9259, false, true, 0, 52.209984, 0.176601), // even, surface
        new cprRelativeTest(52.95, 0.00, 29693, 8997, true, true, 0, 52.209976, 0.176507), // odd, surface

        // longitude must be within about 1.25 degrees at this latitude
        new cprRelativeTest(52.00, 1.40, 105730, 9259, false, true, 0, 52.209984, 0.176601), // even, surface
        new cprRelativeTest(52.00, 1.40, 29693, 8997, true, true, 0, 52.209976, 0.176507), // odd, surface
        new cprRelativeTest(52.00, -1.05, 105730, 9259, false, true, 0, 52.209984, 0.176601), // even, surface
        new cprRelativeTest(52.00, -1.05, 29693, 8997, true, true, 0, 52.209976, 0.176507), // odd, surface
    };

    public boolean testCPRGlobalAirborne() {
        boolean ok = true;

        for (int i = 0; i < cprGlobalAirborneTests.length; i++) {
            double[] rlat = new double[1];
            double[] rlon = new double[1];
            int res = decodeCPRairborne(cprGlobalAirborneTests[i].even_cprlat, cprGlobalAirborneTests[i].even_cprlon,
                    cprGlobalAirborneTests[i].odd_cprlat, cprGlobalAirborneTests[i].odd_cprlon,
                    false, rlat, rlon);
            if (res != cprGlobalAirborneTests[i].even_result ||
                    Math.abs(rlat[0] - cprGlobalAirborneTests[i].even_rlat) > 1e-6 ||
                    Math.abs(rlon[0] - cprGlobalAirborneTests[i].even_rlon) > 1e-6) {
                ok = false;
                System.out.printf("testCPRGlobalAirborne[%d,EVEN]: FAIL: decodeCPRairborne(%d,%d,%d,%d,EVEN) failed:\n"
                    + " result %d  (expected %d)\n"
                    + " lat %.6f   (expected %.6f)\n"
                    + " lon %.6f   (expected %.6f)\n",
                    i,
                    cprGlobalAirborneTests[i].even_cprlat, cprGlobalAirborneTests[i].even_cprlon,
                    cprGlobalAirborneTests[i].odd_cprlat, cprGlobalAirborneTests[i].odd_cprlon,
                    res, cprGlobalAirborneTests[i].even_result,
                    rlat[0], cprGlobalAirborneTests[i].even_rlat,
                    rlon[0], cprGlobalAirborneTests[i].even_rlon);
            } else {
                System.out.printf("testCPRGlobalAirborne[%d,EVEN]: PASS\n", i);
            }

            res = decodeCPRairborne(cprGlobalAirborneTests[i].even_cprlat, cprGlobalAirborneTests[i].even_cprlon,
                    cprGlobalAirborneTests[i].odd_cprlat, cprGlobalAirborneTests[i].odd_cprlon,
                    true, rlat, rlon);
            if (res != cprGlobalAirborneTests[i].odd_result ||
                    Math.abs(rlat[0] - cprGlobalAirborneTests[i].odd_rlat) > 1e-6 ||
                    Math.abs(rlon[0] - cprGlobalAirborneTests[i].odd_rlon) > 1e-6) {
                ok = false;
                System.out.printf("testCPRGlobalAirborne[%d,ODD]:  FAIL: decodeCPRairborne(%d,%d,%d,%d,ODD) failed:\n"
                    + " result %d  (expected %d)\n"
                    + " lat %.6f   (expected %.6f)\n"
                    + " lon %.6f   (expected %.6f)\n",
                    i,
                    cprGlobalAirborneTests[i].even_cprlat, cprGlobalAirborneTests[i].even_cprlon,
                    cprGlobalAirborneTests[i].odd_cprlat, cprGlobalAirborneTests[i].odd_cprlon,
                    res, cprGlobalAirborneTests[i].odd_result,
                    rlat[0], cprGlobalAirborneTests[i].odd_rlat,
                    rlon[0], cprGlobalAirborneTests[i].odd_rlon);
            } else {
                System.out.printf("testCPRGlobalAirborne[%d,ODD]:  PASS\n", i);
            }
        }

        return ok;
    }

    public boolean testCPRGlobalSurface() {
        boolean ok = true;
        int i;

        for (i = 0; i < cprGlobalSurfaceTests.length; i++) {
            double[] rlat = new double[1];
            double[] rlon = new double[1];
            int res = decodeCPRsurface(cprGlobalSurfaceTests[i].reflat, cprGlobalSurfaceTests[i].reflon,
                    cprGlobalSurfaceTests[i].even_cprlat, cprGlobalSurfaceTests[i].even_cprlon,
                    cprGlobalSurfaceTests[i].odd_cprlat, cprGlobalSurfaceTests[i].odd_cprlon,
                    false, rlat, rlon);
            if (res != cprGlobalSurfaceTests[i].even_result ||
                    Math.abs(rlat[0] - cprGlobalSurfaceTests[i].even_rlat) > 1e-6 ||
                    Math.abs(rlon[0] - cprGlobalSurfaceTests[i].even_rlon) > 1e-6) {
                ok = false;
                System.out.printf("testCPRGlobalSurface[%d,EVEN]:  FAIL: decodeCPRsurface(%.6f,%.6f,%d,%d,%d,%d,EVEN) failed:\n"
                    + " result %d  (expected %d)\n"
                    + " lat %.6f   (expected %.6f)\n"
                    + " lon %.6f   (expected %.6f)\n",
                    i,
                    cprGlobalSurfaceTests[i].reflat, cprGlobalSurfaceTests[i].reflon,
                    cprGlobalSurfaceTests[i].even_cprlat, cprGlobalSurfaceTests[i].even_cprlon,
                    cprGlobalSurfaceTests[i].odd_cprlat, cprGlobalSurfaceTests[i].odd_cprlon,
                    res, cprGlobalSurfaceTests[i].even_result,
                    rlat[0], cprGlobalSurfaceTests[i].even_rlat,
                    rlon[0], cprGlobalSurfaceTests[i].even_rlon);
            } else {
                System.out.printf("testCPRGlobalSurface[%d,EVEN]:  PASS\n", i);
            }

            res = decodeCPRsurface(cprGlobalSurfaceTests[i].reflat, cprGlobalSurfaceTests[i].reflon,
                    cprGlobalSurfaceTests[i].even_cprlat, cprGlobalSurfaceTests[i].even_cprlon,
                    cprGlobalSurfaceTests[i].odd_cprlat, cprGlobalSurfaceTests[i].odd_cprlon,
                    true, rlat, rlon);
            if (res != cprGlobalSurfaceTests[i].odd_result ||
                    Math.abs(rlat[0] - cprGlobalSurfaceTests[i].odd_rlat) > 1e-6 ||
                    Math.abs(rlon[0] - cprGlobalSurfaceTests[i].odd_rlon) > 1e-6) {
                ok = false;
                System.out.printf("testCPRGlobalSurface[%d,ODD]:   FAIL: decodeCPRsurface(%.6f,%.6f,%d,%d,%d,%d,ODD) failed:\n"
                    + " result %d  (expected %d)\n"
                    + " lat %.6f   (expected %.6f)\n"
                    + " lon %.6f   (expected %.6f)\n",
                    i,
                    cprGlobalSurfaceTests[i].reflat, cprGlobalSurfaceTests[i].reflon,
                    cprGlobalSurfaceTests[i].even_cprlat, cprGlobalSurfaceTests[i].even_cprlon,
                    cprGlobalSurfaceTests[i].odd_cprlat, cprGlobalSurfaceTests[i].odd_cprlon,
                    res, cprGlobalSurfaceTests[i].odd_result,
                    rlat[0], cprGlobalSurfaceTests[i].odd_rlat,
                    rlon[0], cprGlobalSurfaceTests[i].odd_rlon);
            } else {
                System.out.printf("testCPRGlobalSurface[%d,ODD]:   PASS\n", i);
            }
        }

        return ok;
    }

    public boolean testCPRRelative() {
        boolean ok = true;

        for (int i = 0; i < cprRelativeTests.length; i++) {
            double[] rlat = new double[1];
            double[] rlon = new double[1];
            int res = decodeCPRrelative(cprRelativeTests[i].reflat, cprRelativeTests[i].reflon,
                    cprRelativeTests[i].cprlat, cprRelativeTests[i].cprlon,
                    cprRelativeTests[i].fflag, cprRelativeTests[i].surface,
                    rlat, rlon);
            if (res != cprRelativeTests[i].result ||
                    Math.abs(rlat[0] - cprRelativeTests[i].rlat) > 1e-6 ||
                    Math.abs(rlon[0] - cprRelativeTests[i].rlon) > 1e-6) {
                ok = false;
                System.out.printf("testCPRRelative[%d]:  FAIL:"
                        + " decodeCPRrelative(%.6f,%.6f,%d,%d,%s,%s) failed:\n"
                        + "result %d  (expected %d)\n"
                        + "lat %.6f   (expected %.6f)\n"
                        + "lon %.6f   (expected %.6f)\n",
                    i,
                    cprRelativeTests[i].reflat, cprRelativeTests[i].reflon,
                    cprRelativeTests[i].cprlat, cprRelativeTests[i].cprlon,
                    cprRelativeTests[i].fflag, cprRelativeTests[i].surface,
                    res, cprRelativeTests[i].result,
                    rlat[0], cprRelativeTests[i].rlat,
                    rlon[0], cprRelativeTests[i].rlon);
            } else {
                System.out.printf("testCPRRelative[%d]:  PASS\n", i);
            }
        }

        return ok;
    }
}
