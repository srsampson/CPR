/*
 * Copyright (C) 2015 by Oliver Jowett <oliver@mutability.co.uk>
 * Copyright (C) 2012 by Salvatore Sanfilippo <antirez@gmail.com>
 *
 * All rights reserved
 */
package cpr;

public class CPRException extends Exception {

    public CPRException() {
        super();
    }

    public CPRException(String msg) {
        super(msg);
    }
}
