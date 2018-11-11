/*
 * Copyright (C) 2015 by Oliver Jowett <oliver@mutability.co.uk>
 * Copyright (C) 2012 by Salvatore Sanfilippo <antirez@gmail.com>
 *
 * All rights reserved
 */
package cpr;

public final class Main {

    static CPR cpr;

    public static void main(String[] args) {
        boolean ok;
        
        cpr = new CPR();

        ok = cpr.testCPRGlobalAirborne() && cpr.testCPRGlobalSurface() && cpr.testCPRRelative();

        if (ok == true) {
            System.out.println("\nTests Successful");
        } else {
            System.out.println("\nTests Failed");
        }
    }
}
