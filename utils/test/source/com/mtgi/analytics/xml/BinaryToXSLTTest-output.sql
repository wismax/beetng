
INSERT INTO BEHAVIOR_TRACKING_EVENT (
	event_id_pk, event_id, parent_id, event_type,
	event_name, event_start, user_id, session_id,
	application, event_duration, event_data, 
	error
) VALUES (
	SEQ_BEHAVIOR_TRACKING_EVENT.NEXTVAL, 
	'd1684167-5484-4afc-bf85-c94ce06203d3',
	'3fba2ea7-5dcc-424d-8e33-92a80a05dff3',
	'request',
	'world',
	TO_DATE('2008-11-14 10:34:38','YYYY-MM-DD HH24:MI:SS'),
	'him',
	'him1',
	'test',
	0,
	'value<2>',
	'error[2]'
);

INSERT INTO BEHAVIOR_TRACKING_EVENT (
	event_id_pk, event_id, parent_id, event_type,
	event_name, event_start, user_id, session_id,
	application, event_duration, event_data, 
	error
) VALUES (
	SEQ_BEHAVIOR_TRACKING_EVENT.NEXTVAL, 
	'47f93ee6-241e-4713-ae7a-7c29c4a05c6c',
	'3fba2ea7-5dcc-424d-8e33-92a80a05dff3',
	'app',
	'hello',
	TO_DATE('2008-11-14 10:34:38','YYYY-MM-DD HH24:MI:SS'),
	'her',
	'her2',
	'test',
	16,
	'value<3>',
	null
);

INSERT INTO BEHAVIOR_TRACKING_EVENT (
	event_id_pk, event_id, parent_id, event_type,
	event_name, event_start, user_id, session_id,
	application, event_duration, event_data, 
	error
) VALUES (
	SEQ_BEHAVIOR_TRACKING_EVENT.NEXTVAL, 
	'cce29d81-ab7a-4548-9773-1b4ce7de58ea',
	'3fba2ea7-5dcc-424d-8e33-92a80a05dff3',
	'request',
	'goodbye',
	TO_DATE('2008-11-14 10:34:38','YYYY-MM-DD HH24:MI:SS'),
	null,
	null,
	'test',
	16,
	null,
	'error[4]'
);

INSERT INTO BEHAVIOR_TRACKING_EVENT (
	event_id_pk, event_id, parent_id, event_type,
	event_name, event_start, user_id, session_id,
	application, event_duration, event_data, 
	error
) VALUES (
	SEQ_BEHAVIOR_TRACKING_EVENT.NEXTVAL, 
	'3fba2ea7-5dcc-424d-8e33-92a80a05dff3',
	'fe44b28b-498b-43c2-9689-1258fe095c32',
	'app',
	'goodbye',
	TO_DATE('2008-11-14 10:34:38','YYYY-MM-DD HH24:MI:SS'),
	'you',
	'you2',
	'test',
	47,
	null,
	null
);

INSERT INTO BEHAVIOR_TRACKING_EVENT (
	event_id_pk, event_id, parent_id, event_type,
	event_name, event_start, user_id, session_id,
	application, event_duration, event_data, 
	error
) VALUES (
	SEQ_BEHAVIOR_TRACKING_EVENT.NEXTVAL, 
	'06467fa6-c48d-461b-8697-0b61aa0b8e5e',
	'1433de10-a3fa-48d4-8d31-6ad34a49bbe1',
	'request',
	'hello',
	TO_DATE('2008-11-14 10:34:38','YYYY-MM-DD HH24:MI:SS'),
	'you',
	'you1',
	'test',
	16,
	'value<6>',
	'error[6]'
);

INSERT INTO BEHAVIOR_TRACKING_EVENT (
	event_id_pk, event_id, parent_id, event_type,
	event_name, event_start, user_id, session_id,
	application, event_duration, event_data, 
	error
) VALUES (
	SEQ_BEHAVIOR_TRACKING_EVENT.NEXTVAL, 
	'28a15b5c-5851-47c1-92a6-312386908f89',
	'1433de10-a3fa-48d4-8d31-6ad34a49bbe1',
	'app',
	'goodbye',
	TO_DATE('2008-11-14 10:34:38','YYYY-MM-DD HH24:MI:SS'),
	'him',
	'him2',
	'test',
	0,
	null,
	null
);

INSERT INTO BEHAVIOR_TRACKING_EVENT (
	event_id_pk, event_id, parent_id, event_type,
	event_name, event_start, user_id, session_id,
	application, event_duration, event_data, 
	error
) VALUES (
	SEQ_BEHAVIOR_TRACKING_EVENT.NEXTVAL, 
	'c0d10a43-0282-427b-aba0-8c4df1f3b56e',
	'1433de10-a3fa-48d4-8d31-6ad34a49bbe1',
	'request',
	'world',
	TO_DATE('2008-11-14 10:34:38','YYYY-MM-DD HH24:MI:SS'),
	'her',
	'her1',
	'test',
	16,
	'value<8>',
	'error[8]'
);
COMMIT;

INSERT INTO BEHAVIOR_TRACKING_EVENT (
	event_id_pk, event_id, parent_id, event_type,
	event_name, event_start, user_id, session_id,
	application, event_duration, event_data, 
	error
) VALUES (
	SEQ_BEHAVIOR_TRACKING_EVENT.NEXTVAL, 
	'1433de10-a3fa-48d4-8d31-6ad34a49bbe1',
	'fe44b28b-498b-43c2-9689-1258fe095c32',
	'app',
	'world',
	TO_DATE('2008-11-14 10:34:38','YYYY-MM-DD HH24:MI:SS'),
	'me',
	'me2',
	'test',
	47,
	'value<5>',
	null
);

INSERT INTO BEHAVIOR_TRACKING_EVENT (
	event_id_pk, event_id, parent_id, event_type,
	event_name, event_start, user_id, session_id,
	application, event_duration, event_data, 
	error
) VALUES (
	SEQ_BEHAVIOR_TRACKING_EVENT.NEXTVAL, 
	'c62dc15a-1de5-49d8-9c3e-935bad9641ac',
	'a6fdef7e-8083-46a2-854d-b2df3e5e5b0e',
	'request',
	'goodbye',
	TO_DATE('2008-11-14 10:34:38','YYYY-MM-DD HH24:MI:SS'),
	'me',
	'me1',
	'test',
	15,
	null,
	'error[10]'
);

INSERT INTO BEHAVIOR_TRACKING_EVENT (
	event_id_pk, event_id, parent_id, event_type,
	event_name, event_start, user_id, session_id,
	application, event_duration, event_data, 
	error
) VALUES (
	SEQ_BEHAVIOR_TRACKING_EVENT.NEXTVAL, 
	'5599b0ea-4167-4cf4-bc1d-4e57b92a0c4d',
	'a6fdef7e-8083-46a2-854d-b2df3e5e5b0e',
	'app',
	'world',
	TO_DATE('2008-11-14 10:34:38','YYYY-MM-DD HH24:MI:SS'),
	'you',
	'you2',
	'test',
	16,
	'value<11>',
	null
);

INSERT INTO BEHAVIOR_TRACKING_EVENT (
	event_id_pk, event_id, parent_id, event_type,
	event_name, event_start, user_id, session_id,
	application, event_duration, event_data, 
	error
) VALUES (
	SEQ_BEHAVIOR_TRACKING_EVENT.NEXTVAL, 
	'17566eda-0290-44ab-b5c1-e88da36f517b',
	'a6fdef7e-8083-46a2-854d-b2df3e5e5b0e',
	'request',
	'hello',
	TO_DATE('2008-11-14 10:34:38','YYYY-MM-DD HH24:MI:SS'),
	'him',
	'him1',
	'test',
	15,
	'value<12>',
	'error[12]'
);

INSERT INTO BEHAVIOR_TRACKING_EVENT (
	event_id_pk, event_id, parent_id, event_type,
	event_name, event_start, user_id, session_id,
	application, event_duration, event_data, 
	error
) VALUES (
	SEQ_BEHAVIOR_TRACKING_EVENT.NEXTVAL, 
	'a6fdef7e-8083-46a2-854d-b2df3e5e5b0e',
	'fe44b28b-498b-43c2-9689-1258fe095c32',
	'app',
	'hello',
	TO_DATE('2008-11-14 10:34:38','YYYY-MM-DD HH24:MI:SS'),
	null,
	null,
	'test',
	46,
	'value<9>',
	null
);

INSERT INTO BEHAVIOR_TRACKING_EVENT (
	event_id_pk, event_id, parent_id, event_type,
	event_name, event_start, user_id, session_id,
	application, event_duration, event_data, 
	error
) VALUES (
	SEQ_BEHAVIOR_TRACKING_EVENT.NEXTVAL, 
	'fe44b28b-498b-43c2-9689-1258fe095c32',
	null,
	'request',
	'hello',
	TO_DATE('2008-11-14 10:34:38','YYYY-MM-DD HH24:MI:SS'),
	'me',
	'me1',
	'test',
	171,
	'value<0>',
	'error[0]'
);

INSERT INTO BEHAVIOR_TRACKING_EVENT (
	event_id_pk, event_id, parent_id, event_type,
	event_name, event_start, user_id, session_id,
	application, event_duration, event_data, 
	error
) VALUES (
	SEQ_BEHAVIOR_TRACKING_EVENT.NEXTVAL, 
	'b692302d-314c-4763-b620-8f3863e1e996',
	'9df98411-30ef-4995-8cce-fd34a47537d7',
	'app',
	'hello',
	TO_DATE('2008-11-14 10:34:38','YYYY-MM-DD HH24:MI:SS'),
	'me',
	'me2',
	'test',
	15,
	'value<15>',
	null
);
COMMIT;

INSERT INTO BEHAVIOR_TRACKING_EVENT (
	event_id_pk, event_id, parent_id, event_type,
	event_name, event_start, user_id, session_id,
	application, event_duration, event_data, 
	error
) VALUES (
	SEQ_BEHAVIOR_TRACKING_EVENT.NEXTVAL, 
	'6e090149-ab2d-4b0b-b1e4-8252675dbd36',
	'9df98411-30ef-4995-8cce-fd34a47537d7',
	'request',
	'goodbye',
	TO_DATE('2008-11-14 10:34:38','YYYY-MM-DD HH24:MI:SS'),
	'you',
	'you1',
	'test',
	16,
	null,
	'error[16]'
);

INSERT INTO BEHAVIOR_TRACKING_EVENT (
	event_id_pk, event_id, parent_id, event_type,
	event_name, event_start, user_id, session_id,
	application, event_duration, event_data, 
	error
) VALUES (
	SEQ_BEHAVIOR_TRACKING_EVENT.NEXTVAL, 
	'82a92c4e-04af-4b9b-a5df-6db7c745eeaf',
	'9df98411-30ef-4995-8cce-fd34a47537d7',
	'app',
	'world',
	TO_DATE('2008-11-14 10:34:38','YYYY-MM-DD HH24:MI:SS'),
	'him',
	'him2',
	'test',
	16,
	'value<17>',
	null
);

INSERT INTO BEHAVIOR_TRACKING_EVENT (
	event_id_pk, event_id, parent_id, event_type,
	event_name, event_start, user_id, session_id,
	application, event_duration, event_data, 
	error
) VALUES (
	SEQ_BEHAVIOR_TRACKING_EVENT.NEXTVAL, 
	'9df98411-30ef-4995-8cce-fd34a47537d7',
	'33640bde-bb93-4867-87b4-891e255a6efc',
	'request',
	'world',
	TO_DATE('2008-11-14 10:34:38','YYYY-MM-DD HH24:MI:SS'),
	null,
	null,
	'test',
	63,
	'value<14>',
	'error[14]'
);

INSERT INTO BEHAVIOR_TRACKING_EVENT (
	event_id_pk, event_id, parent_id, event_type,
	event_name, event_start, user_id, session_id,
	application, event_duration, event_data, 
	error
) VALUES (
	SEQ_BEHAVIOR_TRACKING_EVENT.NEXTVAL, 
	'11ce0a23-9da3-4cb0-81ea-fed8e8ea6346',
	'e4252fd0-78df-4375-be84-a0af668d5e44',
	'app',
	'goodbye',
	TO_DATE('2008-11-14 10:34:38','YYYY-MM-DD HH24:MI:SS'),
	null,
	null,
	'test',
	31,
	null,
	null
);

INSERT INTO BEHAVIOR_TRACKING_EVENT (
	event_id_pk, event_id, parent_id, event_type,
	event_name, event_start, user_id, session_id,
	application, event_duration, event_data, 
	error
) VALUES (
	SEQ_BEHAVIOR_TRACKING_EVENT.NEXTVAL, 
	'e5012a82-dcba-4d41-9e2a-f2d68bd531e5',
	'e4252fd0-78df-4375-be84-a0af668d5e44',
	'request',
	'world',
	TO_DATE('2008-11-14 10:34:38','YYYY-MM-DD HH24:MI:SS'),
	'me',
	'me1',
	'test',
	0,
	'value<20>',
	'error[20]'
);

INSERT INTO BEHAVIOR_TRACKING_EVENT (
	event_id_pk, event_id, parent_id, event_type,
	event_name, event_start, user_id, session_id,
	application, event_duration, event_data, 
	error
) VALUES (
	SEQ_BEHAVIOR_TRACKING_EVENT.NEXTVAL, 
	'8f5d3a81-f5d9-4512-b6a6-4e65efeb6e29',
	'e4252fd0-78df-4375-be84-a0af668d5e44',
	'app',
	'hello',
	TO_DATE('2008-11-14 10:34:38','YYYY-MM-DD HH24:MI:SS'),
	'you',
	'you2',
	'test',
	0,
	'value<21>',
	null
);

INSERT INTO BEHAVIOR_TRACKING_EVENT (
	event_id_pk, event_id, parent_id, event_type,
	event_name, event_start, user_id, session_id,
	application, event_duration, event_data, 
	error
) VALUES (
	SEQ_BEHAVIOR_TRACKING_EVENT.NEXTVAL, 
	'e4252fd0-78df-4375-be84-a0af668d5e44',
	'33640bde-bb93-4867-87b4-891e255a6efc',
	'request',
	'hello',
	TO_DATE('2008-11-14 10:34:38','YYYY-MM-DD HH24:MI:SS'),
	'her',
	'her1',
	'test',
	46,
	'value<18>',
	'error[18]'
);
COMMIT;

INSERT INTO BEHAVIOR_TRACKING_EVENT (
	event_id_pk, event_id, parent_id, event_type,
	event_name, event_start, user_id, session_id,
	application, event_duration, event_data, 
	error
) VALUES (
	SEQ_BEHAVIOR_TRACKING_EVENT.NEXTVAL, 
	'eca6650d-ef5c-4abd-8ca8-3d9b8c9deb68',
	'1da28348-9c1a-412c-9c7e-025e4ceb59ae',
	'app',
	'world',
	TO_DATE('2008-11-14 10:34:38','YYYY-MM-DD HH24:MI:SS'),
	'her',
	'her2',
	'test',
	16,
	'value<23>',
	null
);

INSERT INTO BEHAVIOR_TRACKING_EVENT (
	event_id_pk, event_id, parent_id, event_type,
	event_name, event_start, user_id, session_id,
	application, event_duration, event_data, 
	error
) VALUES (
	SEQ_BEHAVIOR_TRACKING_EVENT.NEXTVAL, 
	'b7a3716c-13ff-4d12-8cb5-3b3ba7d3eb89',
	'1da28348-9c1a-412c-9c7e-025e4ceb59ae',
	'request',
	'hello',
	TO_DATE('2008-11-14 10:34:38','YYYY-MM-DD HH24:MI:SS'),
	null,
	null,
	'test',
	15,
	'value<24>',
	'error[24]'
);

INSERT INTO BEHAVIOR_TRACKING_EVENT (
	event_id_pk, event_id, parent_id, event_type,
	event_name, event_start, user_id, session_id,
	application, event_duration, event_data, 
	error
) VALUES (
	SEQ_BEHAVIOR_TRACKING_EVENT.NEXTVAL, 
	'e670e94e-474a-471e-86eb-fc18f686b26a',
	'1da28348-9c1a-412c-9c7e-025e4ceb59ae',
	'app',
	'goodbye',
	TO_DATE('2008-11-14 10:34:38','YYYY-MM-DD HH24:MI:SS'),
	'me',
	'me2',
	'test',
	16,
	null,
	null
);

INSERT INTO BEHAVIOR_TRACKING_EVENT (
	event_id_pk, event_id, parent_id, event_type,
	event_name, event_start, user_id, session_id,
	application, event_duration, event_data, 
	error
) VALUES (
	SEQ_BEHAVIOR_TRACKING_EVENT.NEXTVAL, 
	'1da28348-9c1a-412c-9c7e-025e4ceb59ae',
	'33640bde-bb93-4867-87b4-891e255a6efc',
	'request',
	'goodbye',
	TO_DATE('2008-11-14 10:34:38','YYYY-MM-DD HH24:MI:SS'),
	'him',
	'him1',
	'test',
	63,
	null,
	'error[22]'
);

INSERT INTO BEHAVIOR_TRACKING_EVENT (
	event_id_pk, event_id, parent_id, event_type,
	event_name, event_start, user_id, session_id,
	application, event_duration, event_data, 
	error
) VALUES (
	SEQ_BEHAVIOR_TRACKING_EVENT.NEXTVAL, 
	'33640bde-bb93-4867-87b4-891e255a6efc',
	null,
	'app',
	'goodbye',
	TO_DATE('2008-11-14 10:34:38','YYYY-MM-DD HH24:MI:SS'),
	'her',
	'her2',
	'test',
	188,
	null,
	null
);

INSERT INTO BEHAVIOR_TRACKING_EVENT (
	event_id_pk, event_id, parent_id, event_type,
	event_name, event_start, user_id, session_id,
	application, event_duration, event_data, 
	error
) VALUES (
	SEQ_BEHAVIOR_TRACKING_EVENT.NEXTVAL, 
	'6349c6bd-df56-4fe2-88e7-3b32e9a297e2',
	'baaa7a62-17fe-4359-88dd-f9e59a73cfc1',
	'request',
	'goodbye',
	TO_DATE('2008-11-14 10:34:38','YYYY-MM-DD HH24:MI:SS'),
	'her',
	'her1',
	'test',
	31,
	null,
	'error[28]'
);

INSERT INTO BEHAVIOR_TRACKING_EVENT (
	event_id_pk, event_id, parent_id, event_type,
	event_name, event_start, user_id, session_id,
	application, event_duration, event_data, 
	error
) VALUES (
	SEQ_BEHAVIOR_TRACKING_EVENT.NEXTVAL, 
	'159eeffc-7fc1-4272-b550-977c73899020',
	'baaa7a62-17fe-4359-88dd-f9e59a73cfc1',
	'app',
	'world',
	TO_DATE('2008-11-14 10:34:38','YYYY-MM-DD HH24:MI:SS'),
	null,
	null,
	'test',
	0,
	'value<29>',
	null
);
COMMIT;

INSERT INTO BEHAVIOR_TRACKING_EVENT (
	event_id_pk, event_id, parent_id, event_type,
	event_name, event_start, user_id, session_id,
	application, event_duration, event_data, 
	error
) VALUES (
	SEQ_BEHAVIOR_TRACKING_EVENT.NEXTVAL, 
	'4d13a2d3-967f-4fb3-a561-604520535571',
	'baaa7a62-17fe-4359-88dd-f9e59a73cfc1',
	'request',
	'hello',
	TO_DATE('2008-11-14 10:34:38','YYYY-MM-DD HH24:MI:SS'),
	'me',
	'me1',
	'test',
	15,
	'value<30>',
	'error[30]'
);

INSERT INTO BEHAVIOR_TRACKING_EVENT (
	event_id_pk, event_id, parent_id, event_type,
	event_name, event_start, user_id, session_id,
	application, event_duration, event_data, 
	error
) VALUES (
	SEQ_BEHAVIOR_TRACKING_EVENT.NEXTVAL, 
	'baaa7a62-17fe-4359-88dd-f9e59a73cfc1',
	'f776bdd6-18c5-4481-9b42-e332e5953c24',
	'app',
	'hello',
	TO_DATE('2008-11-14 10:34:38','YYYY-MM-DD HH24:MI:SS'),
	'him',
	'him2',
	'test',
	46,
	'value<27>',
	null
);

INSERT INTO BEHAVIOR_TRACKING_EVENT (
	event_id_pk, event_id, parent_id, event_type,
	event_name, event_start, user_id, session_id,
	application, event_duration, event_data, 
	error
) VALUES (
	SEQ_BEHAVIOR_TRACKING_EVENT.NEXTVAL, 
	'de873101-2f52-48c4-b0ab-38373f2af22c',
	'de222a61-026f-4ec4-89c4-bc1a7f737abf',
	'request',
	'world',
	TO_DATE('2008-11-14 10:34:38','YYYY-MM-DD HH24:MI:SS'),
	'him',
	'him1',
	'test',
	32,
	'value<32>',
	'error[32]'
);

INSERT INTO BEHAVIOR_TRACKING_EVENT (
	event_id_pk, event_id, parent_id, event_type,
	event_name, event_start, user_id, session_id,
	application, event_duration, event_data, 
	error
) VALUES (
	SEQ_BEHAVIOR_TRACKING_EVENT.NEXTVAL, 
	'ecc3fb29-89c4-484a-bd2f-b597ece7e2f6',
	'de222a61-026f-4ec4-89c4-bc1a7f737abf',
	'app',
	'hello',
	TO_DATE('2008-11-14 10:34:38','YYYY-MM-DD HH24:MI:SS'),
	'her',
	'her2',
	'test',
	15,
	'value<33>',
	null
);

INSERT INTO BEHAVIOR_TRACKING_EVENT (
	event_id_pk, event_id, parent_id, event_type,
	event_name, event_start, user_id, session_id,
	application, event_duration, event_data, 
	error
) VALUES (
	SEQ_BEHAVIOR_TRACKING_EVENT.NEXTVAL, 
	'0c3320b9-82c0-4196-8030-79fd38cd0adc',
	'de222a61-026f-4ec4-89c4-bc1a7f737abf',
	'request',
	'goodbye',
	TO_DATE('2008-11-14 10:34:38','YYYY-MM-DD HH24:MI:SS'),
	null,
	null,
	'test',
	16,
	null,
	'error[34]'
);

INSERT INTO BEHAVIOR_TRACKING_EVENT (
	event_id_pk, event_id, parent_id, event_type,
	event_name, event_start, user_id, session_id,
	application, event_duration, event_data, 
	error
) VALUES (
	SEQ_BEHAVIOR_TRACKING_EVENT.NEXTVAL, 
	'de222a61-026f-4ec4-89c4-bc1a7f737abf',
	'f776bdd6-18c5-4481-9b42-e332e5953c24',
	'app',
	'goodbye',
	TO_DATE('2008-11-14 10:34:38','YYYY-MM-DD HH24:MI:SS'),
	'you',
	'you2',
	'test',
	63,
	null,
	null
);

INSERT INTO BEHAVIOR_TRACKING_EVENT (
	event_id_pk, event_id, parent_id, event_type,
	event_name, event_start, user_id, session_id,
	application, event_duration, event_data, 
	error
) VALUES (
	SEQ_BEHAVIOR_TRACKING_EVENT.NEXTVAL, 
	'0d8d3f5d-587c-443a-afff-8bb49af6a283',
	'559b0881-ac35-4a62-960d-0376d7edd232',
	'request',
	'hello',
	TO_DATE('2008-11-14 10:34:38','YYYY-MM-DD HH24:MI:SS'),
	'you',
	'you1',
	'test',
	15,
	'value<36>',
	'error[36]'
);
COMMIT;

INSERT INTO BEHAVIOR_TRACKING_EVENT (
	event_id_pk, event_id, parent_id, event_type,
	event_name, event_start, user_id, session_id,
	application, event_duration, event_data, 
	error
) VALUES (
	SEQ_BEHAVIOR_TRACKING_EVENT.NEXTVAL, 
	'cd7641d8-b85a-4197-854a-c3b5ed73c2ea',
	'559b0881-ac35-4a62-960d-0376d7edd232',
	'app',
	'goodbye',
	TO_DATE('2008-11-14 10:34:38','YYYY-MM-DD HH24:MI:SS'),
	'him',
	'him2',
	'test',
	0,
	null,
	null
);

INSERT INTO BEHAVIOR_TRACKING_EVENT (
	event_id_pk, event_id, parent_id, event_type,
	event_name, event_start, user_id, session_id,
	application, event_duration, event_data, 
	error
) VALUES (
	SEQ_BEHAVIOR_TRACKING_EVENT.NEXTVAL, 
	'2c6405b3-0b80-4e75-beae-729419a7875d',
	'559b0881-ac35-4a62-960d-0376d7edd232',
	'request',
	'world',
	TO_DATE('2008-11-14 10:34:38','YYYY-MM-DD HH24:MI:SS'),
	'her',
	'her1',
	'test',
	0,
	'value<38>',
	'error[38]'
);

INSERT INTO BEHAVIOR_TRACKING_EVENT (
	event_id_pk, event_id, parent_id, event_type,
	event_name, event_start, user_id, session_id,
	application, event_duration, event_data, 
	error
) VALUES (
	SEQ_BEHAVIOR_TRACKING_EVENT.NEXTVAL, 
	'559b0881-ac35-4a62-960d-0376d7edd232',
	'f776bdd6-18c5-4481-9b42-e332e5953c24',
	'app',
	'world',
	TO_DATE('2008-11-14 10:34:38','YYYY-MM-DD HH24:MI:SS'),
	'me',
	'me2',
	'test',
	31,
	'value<35>',
	null
);

INSERT INTO BEHAVIOR_TRACKING_EVENT (
	event_id_pk, event_id, parent_id, event_type,
	event_name, event_start, user_id, session_id,
	application, event_duration, event_data, 
	error
) VALUES (
	SEQ_BEHAVIOR_TRACKING_EVENT.NEXTVAL, 
	'f776bdd6-18c5-4481-9b42-e332e5953c24',
	null,
	'request',
	'world',
	TO_DATE('2008-11-14 10:34:38','YYYY-MM-DD HH24:MI:SS'),
	'you',
	'you1',
	'test',
	156,
	'value<26>',
	'error[26]'
);
