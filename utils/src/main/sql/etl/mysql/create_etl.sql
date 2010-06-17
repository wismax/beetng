-- this script showcases a strange MySQL bug (probably) on case-insensitive FS,
-- where the event table name ends up in lowercase and the summary one in upper
-- You'll need to use the classic 'set-variable=lower_case_table_names=0'

-- event data table for basic analysis
create table BEHAVIOR_TRACKING_EVENT (
	EVENT_ID		VARCHAR(100)	NOT NULL,
	PARENT_EVENT_ID	VARCHAR(100),
	EVENT_TYPE		VARCHAR(20)	NOT NULL,
	EVENT_NAME		VARCHAR(256)	NOT NULL,
	EVENT_START		DATE			NOT NULL,
	USER_ID			VARCHAR(64),
	SESSION_ID		VARCHAR(64),
	APPLICATION		VARCHAR(64)	NOT NULL,
	DURATION_NS		BIGINT(16)		NOT NULL,
	EVENT_DATA		BLOB,
	ERROR			VARCHAR(512),
	SUMMARIZED		CHAR(1)
);
create index EVENT_NAME_IDX on BEHAVIOR_TRACKING_EVENT (
   EVENT_NAME ASC
);

-- event summary table for trending over long periods of time
create table BEHAVIOR_TRACKING_SUMMARY (
	SUMMARY_DATE		VARCHAR(100)	NOT NULL,
	EVENT_TYPE			VARCHAR(20)	NOT NULL,
	EVENT_NAME			VARCHAR(256)	NOT NULL,
	PERIOD_START		DATE			NOT NULL,
	PERIOD_END			DATE			NOT NULL,
	`COUNT`				BIGINT(16)		NOT NULL,
	AVERAGE_NS			BIGINT(16)		NOT NULL,
	MINIMUM_NS			BIGINT(16)		NOT NULL,
	MAXIMUM_NS			BIGINT(16)		NOT NULL,
	MEDIAN_NS			BIGINT(16)		NOT NULL,
	STANDARD_DEVIATION	BIGINT(16)		NOT NULL,
	ERROR_COUNT			BIGINT(16)		NOT NULL
);
