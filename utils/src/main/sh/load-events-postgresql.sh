#!/bin/bash -x

## function to echo a usage message and exit.
function usage {
	cat <<EOF

Usage: $0 [psql client args] [event log]

	Example:

	> $0 '-U beet analysis' log.bxml.gz.20080101000000

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
	gzcat "$2" |
	"$JAVA" -jar beet-utils.jar -tool csv |
	sed 's/,$//' |
	psql -c "$(cat sql/etl/postgresql/load_event_csv.ctl)" $1
	;;

*)
	zcat "$2" |
	"$JAVA" -jar beet-utils.jar -tool csv |
	sed 's/,$//' |
	psql -c "$(cat sql/etl/postgresql/load_event_csv.ctl)" $1
	;;
esac

echo "Summarizing data..."
psql -f sql/etl/postgresql/event_summarize.sql $1

echo "End: " $(date)
