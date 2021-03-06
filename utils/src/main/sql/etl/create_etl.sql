-- event data table for basic analysis.
create table BEHAVIOR_TRACKING_EVENT (
	EVENT_ID		VARCHAR2(100)	NOT NULL,
	PARENT_EVENT_ID	VARCHAR2(100),
	EVENT_TYPE		VARCHAR2(20)	NOT NULL,
	EVENT_NAME		VARCHAR2(256)	NOT NULL,
	EVENT_START		DATE			NOT NULL,
	USER_ID			VARCHAR2(64),
	SESSION_ID		VARCHAR2(64),
	APPLICATION		VARCHAR2(64)	NOT NULL,
	DURATION_NS		NUMBER(16)		NOT NULL,
	EVENT_DATA		CLOB,
	ERROR			VARCHAR2(512),
	SUMMARIZED		CHAR(1)
);
create index EVENT_NAME_IDX on BEHAVIOR_TRACKING_EVENT (
   EVENT_NAME ASC
);

-- event summary table for trending over long periods of time
create table BEHAVIOR_TRACKING_SUMMARY (
	SUMMARY_DATE		VARCHAR2(100)	NOT NULL,
	EVENT_TYPE			VARCHAR2(20)	NOT NULL,
	EVENT_NAME			VARCHAR2(256)	NOT NULL,
	PERIOD_START		DATE			NOT NULL,
	PERIOD_END			DATE			NOT NULL,
	"COUNT"				NUMBER(16)		NOT NULL,
	AVERAGE_NS			NUMBER(16)		NOT NULL,
	MINIMUM_NS			NUMBER(16)		NOT NULL,
	MAXIMUM_NS			NUMBER(16)		NOT NULL,
	MEDIAN_NS			NUMBER(16)		NOT NULL,
	STANDARD_DEVIATION	NUMBER(16)		NOT NULL,
	ERROR_COUNT			NUMBER(16)		NOT NULL
);
