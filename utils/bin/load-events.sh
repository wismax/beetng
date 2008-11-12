#!/bin/sh

echo Unpacking $1...
zcat $1 | java -Xmx256m -jar bt-utils.jar > temp.xml 

echo Loading raw XML...
sqlldr $2 control=db/etl/load_event_xml.ctl data=temp.xml

echo Transforming XML...
sqlplus $2 @db/etl/event_transformation.sql

echo Cleaning up...
rm temp.xml

echo Done.

