#!/bin/bash
# this script just wraps a java call
# but the classpath is so annoying it is well worth it.

if [[ -z "$THRAX" ]]
then
    THRAX="`basename $0`/.."
fi
if [[ -z "$HADOOP" ]]
then
    echo "Please set the \$HADOOP environment variable to your hadoop install."
    exit 1
fi

if (($# < 1))
then
    echo "usage: create_glue_grammar.sh <conf file>"
    exit 1
fi

java -cp $THRAX/bin/thrax.jar:$HADOOP/hadoop-0.20.2-core.jar:$HADOOP/lib/commons-logging-1.0.4.jar edu.jhu.thrax.util.CreateGlueGrammar $1

