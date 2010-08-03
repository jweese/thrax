#!/usr/bin/perl -w
use strict;

# this little script will read a Thrax config file and output a line that
# has the given key, if it exists

open CONF, $ARGV[0] or die "$!";
my $key = $ARGV[1];
while (<CONF>) {
    s/#.*//;
    if (/^$key\s+/) {
        s/^$key\s+//;
        s/\s+$//;
        print;
    }
}

