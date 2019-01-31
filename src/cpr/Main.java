/*
 * Copyright (C) 2015 by Oliver Jowett <oliver@mutability.co.uk>
 * Copyright (C) 2012 by Salvatore Sanfilippo <antirez@gmail.com>
 *
 * All rights reserved
 */
package cpr;

public final class Main {

    private static CPR cpr;

    // Global, airborne CPR test data:
    public static class cprGlobalAirborneTest {

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

    private static final cprGlobalAirborneTest cprGlobalAirborneTests[] = {
        new cprGlobalAirborneTest(80536, 9432, 61720, 9192, 0, 51.686646, 0.700156, 0, 51.686763, 0.701294),
        new cprGlobalAirborneTest(80534, 9413, 61714, 9144, 0, 51.686554, 0.698745, 0, 51.686484, 0.697632)
    };

    // Global, surface CPR test data:
    public static class cprGlobalSurfaceTest {

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

    private static final cprGlobalSurfaceTest cprGlobalSurfaceTests[] = {
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
    public static class cprRelativeTest {

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

    private static final cprRelativeTest cprRelativeTests[] = {
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

    private static boolean testCPRGlobalAirborne() {
        boolean ok = true;

        for (int i = 0; i < cprGlobalAirborneTests.length; i++) {
            double[] rlat = new double[1];
            double[] rlon = new double[1];
            int res = cpr.decodeCPRairborne(cprGlobalAirborneTests[i].even_cprlat, cprGlobalAirborneTests[i].even_cprlon,
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

            res = cpr.decodeCPRairborne(cprGlobalAirborneTests[i].even_cprlat, cprGlobalAirborneTests[i].even_cprlon,
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

    private static boolean testCPRGlobalSurface() {
        boolean ok = true;
        int i;

        for (i = 0; i < cprGlobalSurfaceTests.length; i++) {
            double[] rlat = new double[1];
            double[] rlon = new double[1];
            int res = cpr.decodeCPRsurface(cprGlobalSurfaceTests[i].reflat, cprGlobalSurfaceTests[i].reflon,
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

            res = cpr.decodeCPRsurface(cprGlobalSurfaceTests[i].reflat, cprGlobalSurfaceTests[i].reflon,
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

    private static boolean testCPRRelative() {
        boolean ok = true;

        for (int i = 0; i < cprRelativeTests.length; i++) {
            double[] rlat = new double[1];
            double[] rlon = new double[1];
            int res = cpr.decodeCPRrelative(cprRelativeTests[i].reflat, cprRelativeTests[i].reflon,
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
