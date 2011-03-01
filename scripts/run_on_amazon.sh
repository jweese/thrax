#!/bin/bash

if [[ -z "$2" ]]
then
    cat <<END_USAGE
usage: run_on_amazon.sh <conf file> <credentials>
END_USAGE
    exit 1
fi

checked_put() {
    if [[ -n "`s3cmd ls $2`" ]]
    then
        read -p "File $2 already exists on S3. Overwrite [y/N]? "
        if [[ $REPLY = y*  || $REPLY = Y* ]]
        then
            s3cmd put $1 $2
        fi
    else
        s3cmd put $1 $2
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
cred=$2
thrax_option $conf "amazon-work"
workdir=$THRAX_OPT_RESULT

remoteconf="$workdir/`basename $conf`"
checked_put $conf $remoteconf

thrax_option $conf "amazon-jar"
thraxjar=$THRAX_OPT_RESULT
checked_put $THRAX/bin/thrax.jar $thraxjar

thrax_option $conf "amazon-num-instances" "5"
instances=$THRAX_OPT_RESULT
thrax_option $conf "amazon-instance-type" "m1.small"
instance_type=$THRAX_OPT_RESULT

elastic-mapreduce -c $cred \
    --create \
    --log-uri $workdir/logs \
    --num-instances $instances \
    --instance-type $instance_type \
    --jar $thraxjar \
    --arg $remoteconf \
    --arg $workdir
