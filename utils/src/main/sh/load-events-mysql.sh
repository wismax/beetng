#!/bin/bash

## function to echo a usage message and exit.
function usage {
	cat <<EOF

Usage: $0 [mysql client args] [event log]

	Example:

	> $0 '-h 127.0.0.1 -P 3306 -D analysis' log.bxml.gz.20080101000000

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

echo "Loading $2 into behavior tracking database at $1."
echo "Start: " $(date)

case "`uname`" in
# FIXME Cygwin support
Darwin)
	# FIXME use a randfile
	mkfifo /tmp/import-mysql.fifo
	gzcat "$2" | "$JAVA" -jar beet-utils.jar -tool csv > /tmp/import-mysql.fifo &
	mysql $1 < sql/etl/mysql/load_event_csv.ctl
	rm /tmp/import-mysql.fifo
	;;

*)
	mkfifo /tmp/import-mysql.fifo
	zcat "$2" | "$JAVA" -jar beet-utils.jar -tool csv > /tmp/import-mysql.fifo &
	mysql $1 < sql/etl/mysql/load_event_csv.ctl
	rm /tmp/import-mysql.fifo
	;;
esac

echo "Summarizing data..."
mysql $1 < sql/etl/mysql/event_summarize.sql

echo "End: " $(date)
