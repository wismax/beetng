alter table BEHAVIOR_TRACKING_EVENT alter column EVENT_ID rename to EVENT_ID_OLD;
alter table BEHAVIOR_TRACKING_EVENT alter column PARENT_EVENT_ID rename to PARENT_EVENT_ID_OLD;

alter table BEHAVIOR_TRACKING_EVENT add column EVENT_ID NUMERIC(10) before APPLICATION;
alter table BEHAVIOR_TRACKING_EVENT add column PARENT_EVENT_ID NUMERIC(10) before APPLICATION;

-- create sequence SEQ_BEHAVIOR_TRACKING_EVENT start with 0;

-- create temporary table TMP_CONVERSION_ID (
create table TMP_CONVERSION_ID (
	ID IDENTITY PRIMARY KEY,
	EVENT_UUID CHAR(36)
);

-- respect the creation order so as to match the dbUnit dataset
insert into TMP_CONVERSION_ID (EVENT_UUID) select EVENT_ID_OLD from BEHAVIOR_TRACKING_EVENT order by EVENT_START;

update BEHAVIOR_TRACKING_EVENT b set b.EVENT_ID = (select ID from TMP_CONVERSION_ID where EVENT_UUID=b.EVENT_ID_OLD);
update BEHAVIOR_TRACKING_EVENT b set b.PARENT_EVENT_ID = (select ID from TMP_CONVERSION_ID where EVENT_UUID=b.PARENT_EVENT_ID_OLD);

-- drop table TMP_CONVERSION_ID;

alter table BEHAVIOR_TRACKING_EVENT drop column EVENT_ID_OLD;
alter table BEHAVIOR_TRACKING_EVENT drop column PARENT_EVENT_ID_OLD;
alter table BEHAVIOR_TRACKING_EVENT add PRIMARY KEY (EVENT_ID);
