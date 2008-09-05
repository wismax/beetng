create or replace view BEHAVIOR_TRACKING_SESSIONS as
    select session_id, 
        min(event_start) as start_time, 
        max(event_start) as end_time, 
        ( (max(event_start) - min(event_start)) * 1440 ) as duration_m
    from behavior_tracking_event 
    where session_id is not null 
    group by session_id;

create or replace view BEHAVIOR_SESSION_EVENTS
as
	    select session_id, min(event_start) as time, 'START' as type
	    from behavior_tracking_event where session_id is not null 
	    group by session_id
    union
        select session_id, max(event_start) as time, 'END' as type
        from behavior_tracking_event where session_id is not null
        group by session_id;

create or replace procedure P_countSessions
AS
	tally NUMERIC;
	largest NUMERIC;
	CURSOR events IS select bse.type from BEHAVIOR_SESSION_EVENTS bse order by bse.time;
BEGIN

	tally := 0;
	largest := 0;
	
	FOR evt IN events LOOP
		
		IF (evt.type = 'START') THEN
			tally := tally + 1;
		ELSE
			tally := tally - 1;
		END IF;
		
		IF (tally > largest) THEN
			largest := tally;
		END IF;
		
	END LOOP;

	DBMS_OUTPUT.PUT_LINE('Max concurrent users: ' || largest);
	
END;