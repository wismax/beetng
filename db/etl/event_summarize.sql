INSERT INTO BEHAVIOR_TRACKING_SUMMARY
 (ID, SUMMARY_DATE, EVENT_TYPE, EVENT_NAME, PERIOD_START, PERIOD_END, COUNT, 
 AVERAGE_MS, MINIMUM_MS, MAXIMUM_MS, median_ms, STANDARD_DEVIATION, ERROR_COUNT)
 SELECT SEQ_BEHAVIOR_TRACKING_SUMMARY.NEXTVAL, evnt.* FROM 
 	( SELECT   SYSDATE, event.event_type, event.event_name, 
 		(SELECT MIN(event_start) FROM BEHAVIOR_TRACKING_EVENT tmp WHERE tmp.event_name = event.event_name) period_start, 
 		(SELECT MAX(event_start) FROM BEHAVIOR_TRACKING_EVENT tmp WHERE tmp.event_name = event.event_name) period_end,
 		(SELECT COUNT(*) FROM BEHAVIOR_TRACKING_EVENT tmp WHERE tmp.event_name = event.event_name) event_count,
 		(SELECT AVG(duration_ms) FROM BEHAVIOR_TRACKING_EVENT tmp WHERE tmp.event_name = event.event_name) average_ms,
  		(SELECT MIN(duration_ms) FROM BEHAVIOR_TRACKING_EVENT tmp WHERE tmp.event_name = event.event_name) min_ms,
 		(SELECT MAX(duration_ms) FROM BEHAVIOR_TRACKING_EVENT tmp WHERE tmp.event_name = event.event_name) max_ms,
		(SELECT MEDIAN(duration_ms) FROM BEHAVIOR_TRACKING_EVENT tmp WHERE tmp.event_name = event.event_name) median_ms,
		(SELECT STDDEV(duration_ms) FROM BEHAVIOR_TRACKING_EVENT tmp WHERE tmp.event_name = event.event_name) std_dev_ms,
  		(SELECT COUNT(*) FROM BEHAVIOR_TRACKING_EVENT tmp WHERE tmp.event_name = event.event_name AND error IS NOT NULL) error_cnt
 	FROM BEHAVIOR_TRACKING_EVENT event
 	WHERE event.summarized IS NULL
 	GROUP BY event_name, event_type) evnt;
 COMMIT;
 UPDATE BEHAVIOR_TRACKING_EVENT SET summarized='Y';
 COMMIT;
