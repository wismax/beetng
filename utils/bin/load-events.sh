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

UNPACK="zcat $2"
TRANSFORM="java -jar bt-utils.jar -tool xslt -xsl etl/insert_events.xsl -split event"
LOAD="sqlldr $1 control=etl/load_event_csv.ctl 2>load_progress.log"

##stop the script on error
set -e

echo "Loading $2 into behavior tracking database at $1."
echo "Start: " $(date)
$UNPACK | $TRANSFORM | $LOAD
echo "End: " $(date)