INSERT INTO BEHAVIOR_TRACKING_EVENT (event_id_pk, event_id, parent_event_id, event_type, event_name, 
	   		event_start, user_id, session_id, application, duration_ms, event_data, error)
(SELECT SEQ_BEHAVIOR_TRACKING_EVENT.NEXTVAL , extractValue(value(x), '/event/@id') event_id,
         extractValue(value(x), '/event/@parent-id') parent_id,
         extractValue(value(x), '/event/type') event_type, 
         extractValue(value(x), '/event/name') event_name,
         TO_DATE(REPLACE(SUBSTR(extractValue(value(x), '/event/start'), 1, 19),'T',' '),'YYYY-MM-DD HH24:MI:SS'),
		 extractValue(value(x), '/event/user-id') user_id,
         extractValue(value(x), '/event/session-id') session_id,
		 extractValue(value(x), '/event/application') application,
		 extractValue(value(x), '/event/duration-ms') event_duration,
		 extract(value(x), '/event/event-data').getClobVal() data,
         extractValue(value(x), '/event/error') error
         FROM CASPR_EVENT_XML  t,
TABLE(XMLSequence(extract(t.SYS_NC_ROWINFO$,'/event-log/event'))) x);
COMMIT;
DELETE FROM CASPR_EVENT_XML;
COMMIT;
