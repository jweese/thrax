#!/bin/bash

if (($# < 1))
then
    echo "usage: filter_rules.sh <test set> [test set ...]"
    exit 1
fi

java -Dfile.encoding=utf8 -cp $THRAX/bin/thrax.jar edu.jhu.thrax.util.TestSetFilter $*

