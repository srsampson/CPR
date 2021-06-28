/*
 * Copyright (C) 2015 by Oliver Jowett <oliver@mutability.co.uk>
 * Copyright (C) 2012 by Salvatore Sanfilippo <antirez@gmail.com>
 *
 * All rights reserved
 */
package test;

import cpr.CPR;
import cpr.CPRException;
import cpr.LatLon;

public final class Main {

    private static CPR cpr;

    // Global, airborne CPR test data:
    private static class cprGlobalAirborneTest {

        public final int even_cprlat;
        public final int even_cprlon;    // input: raw CPR values, even message
        public final int odd_cprlat;
        public final int odd_cprlon;     // input: raw CPR values, odd message
        public final double even_rlat;
        public final double even_rlon;   // verify: expected position from decoding with fflag=0 (even message is latest)
        public final double odd_rlat;
        public final double odd_rlon;    // verify: expected position from decoding with fflag=1 (odd message is latest)

        public cprGlobalAirborneTest(int val1, int val2, int val3, int val4,
                double val6, double val7, double val9, double val10) {
            even_cprlat = val1;
            even_cprlon = val2;
            odd_cprlat = val3;
            odd_cprlon = val4;
            even_rlat = val6;
            even_rlon = val7;
            odd_rlat = val9;
            odd_rlon = val10;
        }
    }

    private static final cprGlobalAirborneTest cprGlobalAirborneTests[] = {
        new cprGlobalAirborneTest(80536, 9432, 61720, 9192, 51.686646, 0.700156, 51.686763, 0.701294),
        new cprGlobalAirborneTest(80534, 9413, 61714, 9144, 51.686554, 0.698745, 51.686484, 0.697632)
    };

    // Global, surface CPR test data:
    private static class cprGlobalSurfaceTest {

        public final double reflat;
        public final double reflon;    // input: reference location for decoding
        public final int even_cprlat;
        public final int even_cprlon;  // input: raw CPR values, even message
        public final int odd_cprlat;
        public final int odd_cprlon;   // input: raw CPR values, odd message
        public final double even_rlat;
        public final double even_rlon; // verify: expected position from decoding with fflag=0 (even message is latest)
        public final double odd_rlat;
        public final double odd_rlon;  // verify: expected position from decoding with fflag=1 (odd message is latest)

        public cprGlobalSurfaceTest(double val1, double val2, int val3, int val4, int val5,
                int val6, double val8, double val9, double val11, double val12) {
            reflat = val1;
            reflon = val2;
            even_cprlat = val3;
            even_cprlon = val4;
            odd_cprlat = val5;
            odd_cprlon = val6;
            even_rlat = val8;
            even_rlon = val9;
            odd_rlat = val11;
            odd_rlon = val12;
        }
    }

    private static final cprGlobalSurfaceTest cprGlobalSurfaceTests[] = {
        // The real position received here was on the Cambridge (UK) airport apron at 52.21N 0.177E
        // We mess with the reference location to check that the right quadrant is used.

        // longitude quadrants:
        new cprGlobalSurfaceTest(52.00, -180.00, 105730, 9259, 29693, 8997, 52.209984, 0.176601 - 180.0, 52.209976, 0.176507 - 180.0),
        new cprGlobalSurfaceTest(52.00, -140.00, 105730, 9259, 29693, 8997, 52.209984, 0.176601 - 180.0, 52.209976, 0.176507 - 180.0),
        new cprGlobalSurfaceTest(52.00, -130.00, 105730, 9259, 29693, 8997, 52.209984, 0.176601 - 90.0, 52.209976, 0.176507 - 90.0),
        new cprGlobalSurfaceTest(52.00, -50.00, 105730, 9259, 29693, 8997, 52.209984, 0.176601 - 90.0, 52.209976, 0.176507 - 90.0),
        new cprGlobalSurfaceTest(52.00, -40.00, 105730, 9259, 29693, 8997, 52.209984, 0.176601, 52.209976, 0.176507),
        new cprGlobalSurfaceTest(52.00, -10.00, 105730, 9259, 29693, 8997, 52.209984, 0.176601, 52.209976, 0.176507),
        new cprGlobalSurfaceTest(52.00, 0.00, 105730, 9259, 29693, 8997, 52.209984, 0.176601, 52.209976, 0.176507),
        new cprGlobalSurfaceTest(52.00, 10.00, 105730, 9259, 29693, 8997, 52.209984, 0.176601, 52.209976, 0.176507),
        new cprGlobalSurfaceTest(52.00, 40.00, 105730, 9259, 29693, 8997, 52.209984, 0.176601, 52.209976, 0.176507),
        new cprGlobalSurfaceTest(52.00, 50.00, 105730, 9259, 29693, 8997, 52.209984, 0.176601 + 90.0, 52.209976, 0.176507 + 90.0),
        new cprGlobalSurfaceTest(52.00, 130.00, 105730, 9259, 29693, 8997, 52.209984, 0.176601 + 90.0, 52.209976, 0.176507 + 90.0),
        new cprGlobalSurfaceTest(52.00, 140.00, 105730, 9259, 29693, 8997, 52.209984, 0.176601 - 180.0, 52.209976, 0.176507 - 180.0),
        new cprGlobalSurfaceTest(52.00, 180.00, 105730, 9259, 29693, 8997, 52.209984, 0.176601 - 180.0, 52.209976, 0.176507 - 180.0),
        // latitude quadrants (but only 2). The decoded longitude also changes because the cell size changes with latitude
        new cprGlobalSurfaceTest(90.00, 0.00, 105730, 9259, 29693, 8997, 52.209984, 0.176601, 52.209976, 0.176507),
        new cprGlobalSurfaceTest(52.00, 0.00, 105730, 9259, 29693, 8997, 52.209984, 0.176601, 52.209976, 0.176507),
        new cprGlobalSurfaceTest(8.00, 0.00, 105730, 9259, 29693, 8997, 52.209984, 0.176601, 52.209976, 0.176507),
        new cprGlobalSurfaceTest(7.00, 0.00, 105730, 9259, 29693, 8997, 52.209984 - 90.0, 0.135269, 52.209976 - 90.0, 0.134299),
        new cprGlobalSurfaceTest(-52.00, 0.00, 105730, 9259, 29693, 8997, 52.209984 - 90.0, 0.135269, 52.209976 - 90.0, 0.134299),
        new cprGlobalSurfaceTest(-90.00, 0.00, 105730, 9259, 29693, 8997, 52.209984 - 90.0, 0.135269, 52.209976 - 90.0, 0.134299),
        // poles/equator cases
        new cprGlobalSurfaceTest(-46.00, -180.00, 0, 0, 0, 0, -90.0, -180.000000, -90.0, -180.0), // south pole
        new cprGlobalSurfaceTest(-44.00, -180.00, 0, 0, 0, 0, 0.0, -180.000000, 0.0, -180.0), // equator
        new cprGlobalSurfaceTest(44.00, -180.00, 0, 0, 0, 0, 0.0, -180.000000, 0.0, -180.0), // equator
        new cprGlobalSurfaceTest(46.00, -180.00, 0, 0, 0, 0, 90.0, -180.000000, 90.0, -180.0) // north pole
    };

    // Relative CPR test data:
    private static class cprRelativeTest {

        public final double reflat;
        public final double reflon;    // input: reference location for decoding
        public final int cprlat;
        public final int cprlon;       // input: raw CPR values, even or odd message
        public final boolean fflag;    // input: fflag in raw message
        public final boolean surface;  // input: decode as air (false) or surface (true) position
        public final double rlat;
        public final double rlon;      // verify: expected position

        public cprRelativeTest(double val1, double val2, int val3, int val4, boolean val5,
                boolean val6, double val8, double val9) {
            reflat = val1;
            reflon = val2;
            cprlat = val3;
            cprlon = val4;
            fflag = val5;
            surface = val6;
            rlat = val8;
            rlon = val9;
        }
    }

    private static final cprRelativeTest cprRelativeTests[] = {
        //
        // AIRBORNE
        //

        new cprRelativeTest(52.00, 0.00, 80536, 9432, false, false, 51.686646, 0.700156), // even, airborne
        new cprRelativeTest(52.00, 0.00, 61720, 9192, true, false, 51.686763, 0.701294), // odd, airborne
        new cprRelativeTest(52.00, 0.00, 80534, 9413, false, false, 51.686554, 0.698745), // even, airborne
        new cprRelativeTest(52.00, 0.00, 61714, 9144, true, false, 51.686484, 0.697632), // odd, airborne

        // test moving the receiver around a bit
        // We cannot move it more than 1/2 cell away before ambiguity happens.

        // latitude must be within about 3 degrees (cell size is 360/60 = 6 degrees)
        new cprRelativeTest(48.70, 0.00, 80536, 9432, false, false, 51.686646, 0.700156), // even, airborne
        new cprRelativeTest(48.70, 0.00, 61720, 9192, true, false, 51.686763, 0.701294), // odd, airborne
        new cprRelativeTest(48.70, 0.00, 80534, 9413, false, false, 51.686554, 0.698745), // even, airborne
        new cprRelativeTest(48.70, 0.00, 61714, 9144, true, false, 51.686484, 0.697632), // odd, airborne
        new cprRelativeTest(54.60, 0.00, 80536, 9432, false, false, 51.686646, 0.700156), // even, airborne
        new cprRelativeTest(54.60, 0.00, 61720, 9192, true, false, 51.686763, 0.701294), // odd, airborne
        new cprRelativeTest(54.60, 0.00, 80534, 9413, false, false, 51.686554, 0.698745), // even, airborne
        new cprRelativeTest(54.60, 0.00, 61714, 9144, true, false, 51.686484, 0.697632), // odd, airborne

        // longitude must be within about 4.8 degrees at this latitude
        new cprRelativeTest(52.00, 5.40, 80536, 9432, false, false, 51.686646, 0.700156), // even, airborne
        new cprRelativeTest(52.00, 5.40, 61720, 9192, true, false, 51.686763, 0.701294), // odd, airborne
        new cprRelativeTest(52.00, 5.40, 80534, 9413, false, false, 51.686554, 0.698745), // even, airborne
        new cprRelativeTest(52.00, 5.40, 61714, 9144, true, false, 51.686484, 0.697632), // odd, airborne
        new cprRelativeTest(52.00, -4.10, 80536, 9432, false, false, 51.686646, 0.700156), // even, airborne
        new cprRelativeTest(52.00, -4.10, 61720, 9192, true, false, 51.686763, 0.701294), // odd, airborne
        new cprRelativeTest(52.00, -4.10, 80534, 9413, false, false, 51.686554, 0.698745), // even, airborne
        new cprRelativeTest(52.00, -4.10, 61714, 9144, true, false, 51.686484, 0.697632), // odd, airborne

        //
        // SURFACE
        //

        // Surface position on the Cambridge (UK) airport apron at 52.21N 0.18E
        new cprRelativeTest(52.00, 0.00, 105730, 9259, false, true, 52.209984, 0.176601), // even, surface
        new cprRelativeTest(52.00, 0.00, 29693, 8997, true, true, 52.209976, 0.176507), // odd, surface

        // test moving the receiver around a bit
        // We cannot move it more than 1/2 cell away before ambiguity happens.

        // latitude must be within about 0.75 degrees (cell size is 90/60 = 1.5 degrees)
        new cprRelativeTest(51.46, 0.00, 105730, 9259, false, true, 52.209984, 0.176601), // even, surface
        new cprRelativeTest(51.46, 0.00, 29693, 8997, true, true, 52.209976, 0.176507), // odd, surface
        new cprRelativeTest(52.95, 0.00, 105730, 9259, false, true, 52.209984, 0.176601), // even, surface
        new cprRelativeTest(52.95, 0.00, 29693, 8997, true, true, 52.209976, 0.176507), // odd, surface

        // longitude must be within about 1.25 degrees at this latitude
        new cprRelativeTest(52.00, 1.40, 105730, 9259, false, true, 52.209984, 0.176601), // even, surface
        new cprRelativeTest(52.00, 1.40, 29693, 8997, true, true, 52.209976, 0.176507), // odd, surface
        new cprRelativeTest(52.00, -1.05, 105730, 9259, false, true, 52.209984, 0.176601), // even, surface
        new cprRelativeTest(52.00, -1.05, 29693, 8997, true, true, 52.209976, 0.176507), // odd, surface
    };

    private static boolean testCPRGlobalAirborne() {
        LatLon rlatlon = new LatLon();
        boolean ok = true;

        for (int i = 0; i < cprGlobalAirborneTests.length; i++) {
            try {
                rlatlon = cpr.decodeCPRairborne(cprGlobalAirborneTests[i].even_cprlat, cprGlobalAirborneTests[i].even_cprlon,
                        cprGlobalAirborneTests[i].odd_cprlat, cprGlobalAirborneTests[i].odd_cprlon,
                        false);
            } catch (CPRException e) {
                ok = false;
                System.out.printf("testCPRGlobalAirborne[%d,EVEN]: FAIL\n", i);
            }

            if (Math.abs(rlatlon.getLat() - cprGlobalAirborneTests[i].even_rlat) > 1e-6
                    || Math.abs(rlatlon.getLon() - cprGlobalAirborneTests[i].even_rlon) > 1e-6) {
                ok = false;
                System.out.printf("testCPRGlobalAirborne[%d,EVEN]: FAIL\n\ndecodeCPRairborne(%d,%d,%d,%d,EVEN) failed:\n"
                        + " lat %.6f   (expected %.6f)\n"
                        + " lon %.6f   (expected %.6f)\n\n",
                        i,
                        cprGlobalAirborneTests[i].even_cprlat, cprGlobalAirborneTests[i].even_cprlon,
                        cprGlobalAirborneTests[i].odd_cprlat, cprGlobalAirborneTests[i].odd_cprlon,
                        rlatlon.getLat(), cprGlobalAirborneTests[i].even_rlat,
                        rlatlon.getLon(), cprGlobalAirborneTests[i].even_rlon);
            } else {
                ok = true;
                System.out.printf("testCPRGlobalAirborne[%d,EVEN]: PASS\n", i);
            }

            try {
                rlatlon = cpr.decodeCPRairborne(cprGlobalAirborneTests[i].even_cprlat, cprGlobalAirborneTests[i].even_cprlon,
                        cprGlobalAirborneTests[i].odd_cprlat, cprGlobalAirborneTests[i].odd_cprlon,
                        true);
            } catch (CPRException e) {
                ok = false;
                System.out.printf("testCPRGlobalAirborne[%d,ODD]:  FAIL\n", i);
            }

            if (Math.abs(rlatlon.getLat() - cprGlobalAirborneTests[i].odd_rlat) > 1e-6
                    || Math.abs(rlatlon.getLon() - cprGlobalAirborneTests[i].odd_rlon) > 1e-6) {
                ok = false;
                System.out.printf("testCPRGlobalAirborne[%d,ODD]:  FAIL\n\ndecodeCPRairborne(%d,%d,%d,%d,ODD) failed:\n"
                        + " lat %.6f   (expected %.6f)\n"
                        + " lon %.6f   (expected %.6f)\n\n",
                        i,
                        cprGlobalAirborneTests[i].even_cprlat, cprGlobalAirborneTests[i].even_cprlon,
                        cprGlobalAirborneTests[i].odd_cprlat, cprGlobalAirborneTests[i].odd_cprlon,
                        rlatlon.getLat(), cprGlobalAirborneTests[i].odd_rlat,
                        rlatlon.getLon(), cprGlobalAirborneTests[i].odd_rlon);
            } else {
                ok = true;
                System.out.printf("testCPRGlobalAirborne[%d,ODD]:  PASS\n", i);
            }
        }

        return ok;
    }

    private static boolean testCPRGlobalSurface() {
        LatLon rlatlon = new LatLon();
        boolean ok = true;
        int i;

        for (i = 0; i < cprGlobalSurfaceTests.length; i++) {

            try {
                rlatlon = cpr.decodeCPRsurface(cprGlobalSurfaceTests[i].reflat, cprGlobalSurfaceTests[i].reflon,
                        cprGlobalSurfaceTests[i].even_cprlat, cprGlobalSurfaceTests[i].even_cprlon,
                        cprGlobalSurfaceTests[i].odd_cprlat, cprGlobalSurfaceTests[i].odd_cprlon,
                        false);
            } catch (CPRException e) {
                ok = false;
                System.out.printf("testCPRGlobalSurface[%d,EVEN]:  FAIL\n", i);
            }

            if (Math.abs(rlatlon.getLat() - cprGlobalSurfaceTests[i].even_rlat) > 1e-6
                    || Math.abs(rlatlon.getLon() - cprGlobalSurfaceTests[i].even_rlon) > 1e-6) {
                ok = false;
                System.out.printf("testCPRGlobalSurface[%d,EVEN]:  FAIL\n\ndecodeCPRsurface(%.6f,%.6f,%d,%d,%d,%d,EVEN) failed:\n"
                        + " lat %.6f   (expected %.6f)\n"
                        + " lon %.6f   (expected %.6f)\n\n",
                        i,
                        cprGlobalSurfaceTests[i].reflat, cprGlobalSurfaceTests[i].reflon,
                        cprGlobalSurfaceTests[i].even_cprlat, cprGlobalSurfaceTests[i].even_cprlon,
                        cprGlobalSurfaceTests[i].odd_cprlat, cprGlobalSurfaceTests[i].odd_cprlon,
                        rlatlon.getLat(), cprGlobalSurfaceTests[i].even_rlat,
                        rlatlon.getLon(), cprGlobalSurfaceTests[i].even_rlon);
            } else {
                ok = true;
                System.out.printf("testCPRGlobalSurface[%d,EVEN]:  PASS\n", i);
            }

            try {
                rlatlon = cpr.decodeCPRsurface(cprGlobalSurfaceTests[i].reflat, cprGlobalSurfaceTests[i].reflon,
                        cprGlobalSurfaceTests[i].even_cprlat, cprGlobalSurfaceTests[i].even_cprlon,
                        cprGlobalSurfaceTests[i].odd_cprlat, cprGlobalSurfaceTests[i].odd_cprlon,
                        true);
            } catch (CPRException e) {
                ok = false;
                System.out.printf("testCPRGlobalSurface[%d,ODD]:   FAIL\n", i);
            }

            if (Math.abs(rlatlon.getLat() - cprGlobalSurfaceTests[i].odd_rlat) > 1e-6
                    || Math.abs(rlatlon.getLon() - cprGlobalSurfaceTests[i].odd_rlon) > 1e-6) {
                ok = false;
                System.out.printf("testCPRGlobalSurface[%d,ODD]:   FAIL\n\ndecodeCPRsurface(%.6f,%.6f,%d,%d,%d,%d,ODD) failed:\n"
                        + " lat %.6f   (expected %.6f)\n"
                        + " lon %.6f   (expected %.6f)\n\n",
                        i,
                        cprGlobalSurfaceTests[i].reflat, cprGlobalSurfaceTests[i].reflon,
                        cprGlobalSurfaceTests[i].even_cprlat, cprGlobalSurfaceTests[i].even_cprlon,
                        cprGlobalSurfaceTests[i].odd_cprlat, cprGlobalSurfaceTests[i].odd_cprlon,
                        rlatlon.getLat(), cprGlobalSurfaceTests[i].odd_rlat,
                        rlatlon.getLon(), cprGlobalSurfaceTests[i].odd_rlon);
            } else {
                ok = true;
                System.out.printf("testCPRGlobalSurface[%d,ODD]:   PASS\n", i);
            }
        }

        return ok;
    }

    private static boolean testCPRRelative() {
        LatLon rlatlon = new LatLon();
        boolean ok = true;

        for (int i = 0; i < cprRelativeTests.length; i++) {
            try {
                rlatlon = cpr.decodeCPRrelative(cprRelativeTests[i].reflat, cprRelativeTests[i].reflon,
                        cprRelativeTests[i].cprlat, cprRelativeTests[i].cprlon,
                        cprRelativeTests[i].fflag, cprRelativeTests[i].surface);
            } catch (CPRException e) {
                ok = false;
                System.out.printf("testCPRRelative[%d]:  FAIL\n", i);
            }

            if (Math.abs(rlatlon.getLat() - cprRelativeTests[i].rlat) > 1e-6
                    || Math.abs(rlatlon.getLon() - cprRelativeTests[i].rlon) > 1e-6) {
                ok = false;
                System.out.printf("testCPRRelative[%d]:  FAIL\n\n"
                        + "decodeCPRrelative(%.6f,%.6f,%d,%d,%s,%s) failed:\n"
                        + "lat %.6f   (expected %.6f)\n"
                        + "lon %.6f   (expected %.6f)\n\n",
                        i,
                        cprRelativeTests[i].reflat, cprRelativeTests[i].reflon,
                        cprRelativeTests[i].cprlat, cprRelativeTests[i].cprlon,
                        cprRelativeTests[i].fflag, cprRelativeTests[i].surface,
                        rlatlon.getLat(), cprRelativeTests[i].rlat,
                        rlatlon.getLon(), cprRelativeTests[i].rlon);
            } else {
                ok = true;
                System.out.printf("testCPRRelative[%d]:  PASS\n", i);
            }
        }

        return ok;
    }

    public static void main(String[] args) {

        cpr = new CPR();

        boolean ok = testCPRGlobalAirborne() && testCPRGlobalSurface() && testCPRRelative();

        if (ok == true) {
            System.out.println("\nTests Successful");
        } else {
            System.out.println("\nTests Failed");
        }
    }
}
