
INSERT INTO BEHAVIOR_TRACKING_EVENT (event_id, parent_event_id, event_type, event_name, 
	   		event_start, user_id, session_id, application, duration_ms, event_data, error)
	SELECT extractValue(value(x), '/event/@id') event_id,
	     extractValue(value(x), '/event/@parent-id') parent_event_id,
	     extractValue(value(x), '/event/type') event_type, 
	     extractValue(value(x), '/event/name') event_name,
	     TO_DATE(REPLACE(SUBSTR(extractValue(value(x), '/event/start'), 1, 19),'T',' '),'YYYY-MM-DD HH24:MI:SS') event_start,
		 extractValue(value(x), '/event/user-id') user_id,
	     extractValue(value(x), '/event/session-id') session_id,
		 extractValue(value(x), '/event/application') application,
		 extractValue(value(x), '/event/duration-ms') duration_ms,
		 extract(value(x), '/event/event-data').getClobVal() event_data,
	     extractValue(value(x), '/event/error') error
    FROM BEHAVIOR_EVENT_XML x;
COMMIT;

DELETE FROM BEHAVIOR_EVENT_XML;
COMMIT;
/

EXIT;
/