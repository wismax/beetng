DECLARE
	CURSOR EVENTS IS
		SELECT SEQ_BEHAVIOR_TRACKING_EVENT.NEXTVAL as event_id_pk, 
		 extractValue(value(x), '/event/@id') event_id,
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
        FROM BEHAVIOR_EVENT_XML  t,
		TABLE(XMLSequence(extract(t.SYS_NC_ROWINFO$,'/event-log/event'))) x;
BEGIN

	FOR evt IN EVENTS LOOP
		INSERT INTO BEHAVIOR_TRACKING_EVENT (event_id_pk, event_id, parent_event_id, event_type, event_name, 
			   		event_start, user_id, session_id, application, duration_ms, event_data, error)
			VALUES (evt.event_id_pk, evt.event_id, evt.parent_event_id, evt.event_type, evt.event_name, 
			   		evt.event_start, evt.user_id, evt.session_id, evt.application, evt.duration_ms, evt.event_data, evt.error);
		COMMIT;
	END LOOP;

	DELETE FROM BEHAVIOR_EVENT_XML;
	COMMIT;

END;
/

EXIT;
/