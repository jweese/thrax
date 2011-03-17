#!/bin/bash

if (($# < 2))
then
    echo "usage: filter_rules.sh <phrase length> <test set> [test set ...]"
    exit 1
fi

java -Dfile.encoding=utf8 -cp $THRAX/bin/thrax.jar edu.jhu.thrax.util.TestSetFilter $*

