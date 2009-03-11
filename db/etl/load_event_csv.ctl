OPTIONS (SKIP=1)
LOAD DATA
 INFILE '-' "str '\n'"
 BADFILE 'BAD_EVENT_CSV.log' 
 DISCARDFILE 'DISCARD_EVENT_CSV.log' 
 APPEND
 INTO TABLE BEHAVIOR_TRACKING_EVENT
 FIELDS TERMINATED BY "," OPTIONALLY ENCLOSED BY '"'
 TRAILING NULLCOLS
 (  
	EVENT_ID		CHAR(100),
	PARENT_EVENT_ID	CHAR(100)		NULLIF (PARENT_EVENT_ID=BLANKS),
	EVENT_TYPE		CHAR(20),
	EVENT_NAME		CHAR(256),
	APPLICATION		CHAR(64),
	EVENT_START		DATE			"YYYY-MM-DD HH24:MI:SS",
	DURATION_MS		INTEGER EXTERNAL,
	USER_ID			CHAR(64)		NULLIF (USER_ID=BLANKS),
	SESSION_ID		CHAR(64)		NULLIF (SESSION_ID=BLANKS),
	ERROR			CHAR(512)		NULLIF (ERROR=BLANKS),
	EVENT_DATA		CHAR(100000)	NULLIF (EVENT_DATA=BLANKS)
 )
  