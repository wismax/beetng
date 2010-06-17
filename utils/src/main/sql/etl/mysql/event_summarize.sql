-- FIXME benchmark this median impl against the median UDF and the Wikipedia impl
-- (the following one uses a self-join...)
INSERT INTO BEHAVIOR_TRACKING_SUMMARY
 (SUMMARY_DATE, EVENT_TYPE, EVENT_NAME, PERIOD_START, PERIOD_END, COUNT, 
  AVERAGE_NS, MINIMUM_NS, MAXIMUM_NS, median_ns, STANDARD_DEVIATION, ERROR_COUNT)
SELECT NOW(), evt.*
    FROM (
        SELECT event.event_type, event.event_name, 
            MIN(event_start) period_start,
            MAX(event_start) period_end,
            COUNT(*) event_count,
            AVG(duration_ns) average_ns,
            MIN(duration_ns) min_ns,
            MAX(duration_ns) max_ns,

	    (SELECT AVG(tmp.median) AS median_ns
	        FROM (
		    SELECT x.duration_ns as median
		    FROM BEHAVIOR_TRACKING_EVENT x, BEHAVIOR_TRACKING_EVENT y
		    GROUP BY x.duration_ns
		    HAVING
		        ((SUM(SIGN(1 - SIGN(y.duration_ns - x.duration_ns)))) >= FLOOR((COUNT(*) + 1) / 2))
			AND
			((SUM(SIGN(1 + SIGN(y.duration_ns - x.duration_ns)))) >= FLOOR((COUNT(*) + 1) / 2))
		) AS tmp
	    ),
 
            STDDEV(duration_ns) std_dev_ns,
            (SELECT COUNT(*) 
             FROM BEHAVIOR_TRACKING_EVENT tmp 
             WHERE tmp.summarized is NULL 
	            and event_name = event.event_name 
	            and event_type = event.event_type
	            and error IS NOT NULL) error_cnt
        FROM BEHAVIOR_TRACKING_EVENT event
        WHERE event.summarized IS NULL
        GROUP BY event_name, event_type
    ) evt;
 UPDATE BEHAVIOR_TRACKING_EVENT SET summarized='Y' WHERE summarized IS NULL;
 COMMIT;
