#!/bin/sh

## function to echo a usage message and exit.
function usage {
	cat <<EOF

Usage: $0 [db connection] [event log]

	Example:

	> $0 user/pass@sid log.bxml.gz.20080101000000

EOF
	exit 1
}

## check arg count.
if [[ $# < 2 ]]; then
	usage;
fi

## verify that second arg is an actual file.
if [ ! -f $2 ]; then
	echo
	echo "\"$2\" does not exist or is not a regular file."
	usage;
fi

if [ -z "$JAVA_HOME" ]; then
	TRANSFORM_CMD=java
else
	TRANSFORM_CMD=$JAVA_HOME/bin/java
fi

UNPACK=zcat
TRANSFORM_ARGS="-jar bt-utils.jar -tool xslt -xsl etl/insert_events.xsl -split event"
LOAD="sqlldr $1 control=etl/load_event_csv.ctl,data=events.csv"

##stop the script on error
#set -e

echo "Loading $2 into behavior tracking database at $1."
echo "Start: " $(date)

"$UNPACK" "$2" | "$TRANSFORM_CMD" $TRANSFORM_ARGS > events.csv

$LOAD 2>load_progress.log

echo "End: " $(date)
