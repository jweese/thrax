#!/bin/bash

if (($# < 2))
then
    echo "usage: filter_rules.sh [-p] <phrase length> <test set> [test set ...]"
    echo "  -p will cause blank lines to be output instead of skipping"
    echo "     non-matching rules, and for output to be flushed."
    exit 1
fi

java -Dfile.encoding=utf8 -cp $THRAX/bin/thrax.jar edu.jhu.thrax.util.TestSetFilter $*

