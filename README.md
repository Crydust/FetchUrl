# FetchUrl

The "[Java Pain](https://www.tbray.org/ongoing/When/201x/2014/06/20/Hating-Java-in-2014)" article By Tim Bray really hit me.
How hard could it be to download a https page in Java 7?

A comment by Ivan Ristic enlightened me to the real problem Tim was facing.
Not the download of a random https page was troublesome, but the download of a keybase.io page.
The Keybase server doesn't offer any cipher suites Java 7 could use.
At least not without the "[Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files 7](http://www.oracle.com/technetwork/java/javase/downloads/jce-7-download-432124.html)".
See the [SSL Labs simulator report for keybase.io](https://www.ssllabs.com/ssltest/analyze.html?d=keybase.io&s=54.84.133.185).

This repository is my exploration of the problem.

Usage:

    java -jar FetchUrl.jar https://keybase.io
