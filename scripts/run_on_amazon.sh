#!/bin/bash

if [[ -z "$2" ]]
then
    cat <<END_USAGE
usage: run_on_amazon.sh <conf file> <num instances>
END_USAGE
    exit 1
fi

checked_put() {
    clean=`echo "$2" | sed s/^s3n/s3/`
    if [[ -n "`s3cmd ls $clean`" ]]
    then
        read -p "File $2 already exists on S3. Overwrite [y/N]? "
        if [[ $REPLY = y*  || $REPLY = Y* ]]
        then
            s3cmd put $1 $clean
        fi
    else
        s3mcd put $1 $clean
    fi
}

thrax_option() {
    THRAX_OPT_RESULT=`egrep "^$2" $1 | awk '{ print $2 }'`
    if [[ -z "$THRAX_OPT_RESULT" ]]
    then
        if [[ -n "$3" ]]
        then
            echo "No value found for key '$2'. Using default value $3."
            THRAX_OPT_RESULT=$3
        else
            echo "Key '$2' not set in conf file!"
            exit 1
        fi
    fi
}

conf=$1
instances=$2
thrax_option $conf "amazon-work"
workdir=$THRAX_OPT_RESULT

remoteconf="$workdir/`basename $conf`"
checked_put $conf $remoteconf

thrax_option $conf "amazon-jar"
thraxjar=$THRAX_OPT_RESULT
checked_put $THRAX/bin/thrax.jar $thraxjar
