#!/bin/bash

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
	JAVA=java
else
	JAVA=$JAVA_HOME/bin/java
fi

##stop the script on error
#set -e

echo "Loading $2 into behavior tracking database at $1."
echo "Start: " $(date)

# sqlldr doesn't support stdin on windows, so we have to use a temp file in cygwin.
# on other unices, we push it all through a single process pipeline.

case "`uname`" in
CYGWIN*)
	TEMPFILE=`mktemp --tmpdir=.`
	zcat "$2" | "$JAVA" -jar bt-utils.jar -tool csv > $TEMPFILE
	sqlldr $1 control=etl/load_event_csv.ctl,data="$TEMPFILE",silent=FEEDBACK
	rm $TEMPFILE
	;;

SunOS*)
	gzcat "$2" | 
		"$JAVA" -jar bt-utils.jar -tool csv | 
		sqlldr $1 control=etl/load_event_csv.ctl,data=-,silent=FEEDBACK
	;;

*)
	zcat "$2" | 
		"$JAVA" -jar bt-utils.jar -tool csv | 
		sqlldr $1 control=etl/load_event_csv.ctl,data=-,silent=FEEDBACK
	;;
esac

echo "End: " $(date)
