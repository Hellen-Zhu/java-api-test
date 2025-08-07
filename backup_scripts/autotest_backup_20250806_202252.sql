--
-- PostgreSQL database dump
--

-- Dumped from database version 15.13 (Homebrew)
-- Dumped by pg_dump version 15.13 (Homebrew)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: auto_baseurl; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.auto_baseurl (
    id integer NOT NULL,
    service_name character varying,
    profile_name character varying,
    base_url character varying
);


ALTER TABLE public.auto_baseurl OWNER TO admin;

--
-- Name: auto_baseurl_id_seq; Type: SEQUENCE; Schema: public; Owner: admin
--

CREATE SEQUENCE public.auto_baseurl_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.auto_baseurl_id_seq OWNER TO admin;

--
-- Name: auto_baseurl_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: admin
--

ALTER SEQUENCE public.auto_baseurl_id_seq OWNED BY public.auto_baseurl.id;


--
-- Name: auto_case; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.auto_case (
    id integer NOT NULL,
    enable boolean,
    is_e2e boolean,
    is_sanity boolean,
    component character varying,
    component_like character varying,
    scenario character varying,
    issue_key character varying,
    description character varying,
    label character varying,
    path character varying,
    method character varying,
    profile_name character varying,
    parameter character varying,
    update_at timestamp(6) without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.auto_case OWNER TO admin;

--
-- Name: auto_case_id_seq; Type: SEQUENCE; Schema: public; Owner: admin
--

CREATE SEQUENCE public.auto_case_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.auto_case_id_seq OWNER TO admin;

--
-- Name: auto_case_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: admin
--

ALTER SEQUENCE public.auto_case_id_seq OWNED BY public.auto_case.id;


--
-- Name: auto_configuration; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.auto_configuration (
    id integer NOT NULL,
    name character varying(255) NOT NULL,
    component character varying(255),
    daily_run_version character varying(255),
    qa character varying(255),
    module character varying(50),
    labels character varying(255),
    sanity boolean,
    regions character varying(255),
    delete_dependency jsonb,
    qalist jsonb,
    enable boolean,
    service_name character varying(255)
);


ALTER TABLE public.auto_configuration OWNER TO admin;

--
-- Name: auto_configuration_id_seq; Type: SEQUENCE; Schema: public; Owner: admin
--

CREATE SEQUENCE public.auto_configuration_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.auto_configuration_id_seq OWNER TO admin;

--
-- Name: auto_configuration_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: admin
--

ALTER SEQUENCE public.auto_configuration_id_seq OWNED BY public.auto_configuration.id;


--
-- Name: auto_endpoint; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.auto_endpoint (
    id integer NOT NULL,
    component character varying,
    path character varying,
    method character varying,
    class_name character varying,
    service_name character varying,
    qa_owner character varying
);


ALTER TABLE public.auto_endpoint OWNER TO admin;

--
-- Name: auto_endpoint_all; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.auto_endpoint_all (
    id integer NOT NULL,
    path character varying,
    method character varying,
    service_name character varying
);


ALTER TABLE public.auto_endpoint_all OWNER TO admin;

--
-- Name: auto_endpoint_all_id_seq; Type: SEQUENCE; Schema: public; Owner: admin
--

CREATE SEQUENCE public.auto_endpoint_all_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.auto_endpoint_all_id_seq OWNER TO admin;

--
-- Name: auto_endpoint_all_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: admin
--

ALTER SEQUENCE public.auto_endpoint_all_id_seq OWNED BY public.auto_endpoint_all.id;


--
-- Name: auto_endpoint_id_seq; Type: SEQUENCE; Schema: public; Owner: admin
--

CREATE SEQUENCE public.auto_endpoint_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.auto_endpoint_id_seq OWNER TO admin;

--
-- Name: auto_endpoint_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: admin
--

ALTER SEQUENCE public.auto_endpoint_id_seq OWNED BY public.auto_endpoint.id;


--
-- Name: auto_progress; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.auto_progress (
    id integer NOT NULL,
    runid character varying(50) NOT NULL,
    version_id character varying(35),
    component character varying(50),
    total_cases integer,
    passes integer,
    failures integer,
    skips integer,
    begin_time timestamp(3) without time zone,
    end_time timestamp(6) without time zone,
    releaseversion character varying(200),
    task_status character varying(25),
    run_by character varying(50),
    label character varying(1000),
    runmode character varying(255),
    profile character varying(200),
    update_time timestamp(6) without time zone
);


ALTER TABLE public.auto_progress OWNER TO admin;

--
-- Name: auto_progress_id_seq; Type: SEQUENCE; Schema: public; Owner: admin
--

CREATE SEQUENCE public.auto_progress_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.auto_progress_id_seq OWNER TO admin;

--
-- Name: auto_progress_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: admin
--

ALTER SEQUENCE public.auto_progress_id_seq OWNED BY public.auto_progress.id;


--
-- Name: auto_system_variable; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.auto_system_variable (
    id integer NOT NULL,
    component character varying,
    component_like character varying,
    profile character varying,
    config_key character varying,
    value character varying
);


ALTER TABLE public.auto_system_variable OWNER TO admin;

--
-- Name: auto_system_variable_id_seq; Type: SEQUENCE; Schema: public; Owner: admin
--

CREATE SEQUENCE public.auto_system_variable_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.auto_system_variable_id_seq OWNER TO admin;

--
-- Name: auto_system_variable_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: admin
--

ALTER SEQUENCE public.auto_system_variable_id_seq OWNED BY public.auto_system_variable.id;


--
-- Name: auto_testngresult; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.auto_testngresult (
    id integer NOT NULL,
    runid character varying(60),
    component character varying(50),
    suite_result character varying,
    insertdatetime timestamp(6) without time zone,
    config character varying(255)
);


ALTER TABLE public.auto_testngresult OWNER TO admin;

--
-- Name: auto_testngresult_id_seq; Type: SEQUENCE; Schema: public; Owner: admin
--

CREATE SEQUENCE public.auto_testngresult_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.auto_testngresult_id_seq OWNER TO admin;

--
-- Name: auto_testngresult_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: admin
--

ALTER SEQUENCE public.auto_testngresult_id_seq OWNED BY public.auto_testngresult.id;


--
-- Name: carriers; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.carriers (
    id character varying(50) NOT NULL,
    name character varying(200) NOT NULL,
    code character varying(50),
    contact_person character varying(100),
    contact_phone character varying(20),
    address text,
    status character varying(20),
    created_at timestamp without time zone,
    updated_at timestamp without time zone
);


ALTER TABLE public.carriers OWNER TO admin;

--
-- Name: materials; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.materials (
    id character varying(50) NOT NULL,
    name character varying(200),
    description text,
    created_at timestamp without time zone,
    updated_at timestamp without time zone
);


ALTER TABLE public.materials OWNER TO admin;

--
-- Name: orders; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.orders (
    id character varying(50) NOT NULL,
    order_number character varying(100) NOT NULL,
    user_id character varying(50),
    status character varying(50),
    total_amount numeric(10,2),
    order_data json,
    created_at timestamp without time zone,
    updated_at timestamp without time zone
);


ALTER TABLE public.orders OWNER TO admin;

--
-- Name: products; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.products (
    id character varying(50) NOT NULL,
    source_type integer,
    offer_id bigint,
    category_id bigint,
    subject character varying(500),
    image_urls character varying[],
    sku_props json,
    sku_maps json,
    created_at timestamp without time zone,
    updated_at timestamp without time zone
);


ALTER TABLE public.products OWNER TO admin;

--
-- Name: report_progress; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.report_progress (
    group_id character varying(50),
    run_id character varying(50),
    component character varying(100),
    report character varying(25),
    date date,
    processed character varying(10),
    module character varying(25),
    label character varying(100)
);


ALTER TABLE public.report_progress OWNER TO admin;

--
-- Name: users; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.users (
    id character varying(50) NOT NULL,
    username character varying(100) NOT NULL,
    password character varying(100) NOT NULL,
    role_id character varying(50) NOT NULL,
    dates timestamp without time zone,
    phone character varying(20) NOT NULL,
    created_at timestamp without time zone,
    updated_at timestamp without time zone
);


ALTER TABLE public.users OWNER TO admin;

--
-- Name: auto_baseurl id; Type: DEFAULT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.auto_baseurl ALTER COLUMN id SET DEFAULT nextval('public.auto_baseurl_id_seq'::regclass);


--
-- Name: auto_case id; Type: DEFAULT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.auto_case ALTER COLUMN id SET DEFAULT nextval('public.auto_case_id_seq'::regclass);


--
-- Name: auto_configuration id; Type: DEFAULT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.auto_configuration ALTER COLUMN id SET DEFAULT nextval('public.auto_configuration_id_seq'::regclass);


--
-- Name: auto_endpoint id; Type: DEFAULT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.auto_endpoint ALTER COLUMN id SET DEFAULT nextval('public.auto_endpoint_id_seq'::regclass);


--
-- Name: auto_endpoint_all id; Type: DEFAULT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.auto_endpoint_all ALTER COLUMN id SET DEFAULT nextval('public.auto_endpoint_all_id_seq'::regclass);


--
-- Name: auto_progress id; Type: DEFAULT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.auto_progress ALTER COLUMN id SET DEFAULT nextval('public.auto_progress_id_seq'::regclass);


--
-- Name: auto_system_variable id; Type: DEFAULT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.auto_system_variable ALTER COLUMN id SET DEFAULT nextval('public.auto_system_variable_id_seq'::regclass);


--
-- Name: auto_testngresult id; Type: DEFAULT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.auto_testngresult ALTER COLUMN id SET DEFAULT nextval('public.auto_testngresult_id_seq'::regclass);


--
-- Data for Name: auto_baseurl; Type: TABLE DATA; Schema: public; Owner: admin
--

COPY public.auto_baseurl (id, service_name, profile_name, base_url) FROM stdin;
1	user_svc	uat	http://127.0.0.1:8787
\.


--
-- Data for Name: auto_case; Type: TABLE DATA; Schema: public; Owner: admin
--

COPY public.auto_case (id, enable, is_e2e, is_sanity, component, component_like, scenario, issue_key, description, label, path, method, profile_name, parameter, update_at) FROM stdin;
2	t	f	f	auto_test	\N	user_login	\N	Should unable to login with empty password	\N	\N	/dar/user/login	\N	{"1":{"path":"/dar/user/login","method":"post","comment":"","request":{"body":{"username":"admin","password":""}},"caseHelp":{},"profileName":"uat","description":"Should unable to login with empty password","serviceName":"user_svc","checkpoints":{"expectedStatusCode":401,"jsonPath":{"message":"Invalid username or password"}}}}	2025-08-06 15:36:12.643822
1	t	f	f	auto_test	\N	user_login	\N	Should able to login with right auth	\N	\N	/dar/user/login	\N	{"1":{"path":"/dar/user/login","method":"post","comment":"","request":{"body":{"username":"admin","password":"admin123"}},"caseHelp":{},"profileName":"uat","description":"Verify should able to login with right auth","serviceName":"user_svc","checkpoints":{"expectedStatusCode":200,"jsonPath":{"message":"Login Successful"},"partialJson":{"data":{"user":{"created_at":"2025-08-06T16:00:47.627830","dates":"2023-12-31","id":"84451413593","password":"admin123","phone":"13800000000","role_id":"123456789","updated_at":"2025-08-06T16:00:47.627831","username":"admin"}}}},"notNullFields":["data.token"]}}	2025-08-06 15:36:12.643822
\.


--
-- Data for Name: auto_configuration; Type: TABLE DATA; Schema: public; Owner: admin
--

COPY public.auto_configuration (id, name, component, daily_run_version, qa, module, labels, sanity, regions, delete_dependency, qalist, enable, service_name) FROM stdin;
1	auto_test	auto_test	QA-DailyRun	Hellen	Auto	Auto-Api-Test	t	UAT	\N	\N	t	\N
\.


--
-- Data for Name: auto_endpoint; Type: TABLE DATA; Schema: public; Owner: admin
--

COPY public.auto_endpoint (id, component, path, method, class_name, service_name, qa_owner) FROM stdin;
1	auto_test	/dar/user/login	post	Test_userLogin	user_svc	Hellen
\.


--
-- Data for Name: auto_endpoint_all; Type: TABLE DATA; Schema: public; Owner: admin
--

COPY public.auto_endpoint_all (id, path, method, service_name) FROM stdin;
\.


--
-- Data for Name: auto_progress; Type: TABLE DATA; Schema: public; Owner: admin
--

COPY public.auto_progress (id, runid, version_id, component, total_cases, passes, failures, skips, begin_time, end_time, releaseversion, task_status, run_by, label, runmode, profile, update_time) FROM stdin;
1	01K1Z6TF7CE6FFWP577JEHNQ3G	v1.0.0	auto_test	2	0	0	0	2025-08-06 16:09:05.103	2025-08-06 16:09:05.513934	1.0.0	TIMEOUT	local-debug		\N	\N	\N
2	01K1Z72Y7EHJJYTPT4CK21EBB4	v1.0.0	auto_test	2	0	0	0	2025-08-06 16:13:42.665	2025-08-06 16:13:43.340875	1.0.0	TIMEOUT	local-debug		\N	\N	\N
3	01K1Z74MN5XDNEX02BQAVFJTED	v1.0.0	auto_test	2	0	0	0	2025-08-06 16:14:38.351	2025-08-06 16:14:38.809228	1.0.0	TIMEOUT	local-debug		\N	\N	\N
4	01K1Z758AXPC4B80ME8KNVZVT7	v1.0.0	auto_test	2	0	0	0	2025-08-06 16:14:58.501	2025-08-06 16:14:58.903984	1.0.0	TIMEOUT	local-debug		\N	\N	\N
5	01K1ZA2B47HCQZK0DQF36BMJ92	v1.0.0	auto_test	2	0	0	0	2025-08-06 17:05:48.779	2025-08-06 17:05:49.231248	1.0.0	TIMEOUT	local-debug		\N	\N	\N
6	01K1ZAE55F50KTT4269J3NYNR3	v1.0.0	auto_test	2	0	0	0	2025-08-06 17:12:15.922	2025-08-06 17:12:16.549953	1.0.0	TIMEOUT	local-debug		\N	\N	\N
7	01K1ZAGHAZ5JMA76F8DA49RPC6	v1.0.0	auto_test	2	0	0	0	2025-08-06 17:13:33.888	2025-08-06 17:13:34.29865	1.0.0	TIMEOUT	local-debug		\N	\N	\N
8	01K1ZARGMJQBVFC72FM6XNMZER	v1.0.0	auto_test	2	0	0	0	2025-08-06 17:17:55.366	2025-08-06 17:17:55.819623	1.0.0	TIMEOUT	local-debug		\N	\N	\N
9	01K1ZAYPAJTHHG8E8JAKKRBB9J	v1.0.0	auto_test	2	0	0	0	2025-08-06 17:21:17.782	2025-08-06 17:21:18.275798	1.0.0	TIMEOUT	local-debug		\N	\N	\N
10	01K1ZAZE2JP0K754NE9CYDYW0J	v1.0.0	auto_test	2	0	0	0	2025-08-06 17:21:42.104	2025-08-06 17:21:42.688233	1.0.0	TIMEOUT	local-debug		\N	\N	\N
11	01K1ZB1385JD6MZ98F97RKSRNA	v1.0.0	auto_test	2	0	0	0	2025-08-06 17:22:36.561	2025-08-06 17:22:37.117432	1.0.0	TIMEOUT	local-debug		\N	\N	\N
12	01K1ZBA130NTYGH9N48JJV2BSR	v1.0.0	auto_test	2	0	0	0	2025-08-06 17:27:29.222	2025-08-06 17:27:29.627112	1.0.0	TIMEOUT	local-debug		\N	\N	\N
13	01K1ZBEGVXAGXWJWYHT0164R11	v1.0.0	auto_test	2	0	0	0	2025-08-06 17:29:56.454	2025-08-06 17:29:57.115881	1.0.0	TIMEOUT	local-debug		\N	\N	\N
14	01K1ZBKVZ3B051XQ743BRM9NQ9	v1.0.0	auto_test	2	0	0	0	2025-08-06 17:32:51.693	2025-08-06 17:32:52.496322	1.0.0	TIMEOUT	local-debug		\N	\N	\N
15	01K1ZBMKZ4REYW2BGGDDW9YK2M	v1.0.0	auto_test	2	0	0	0	2025-08-06 17:33:16.258	2025-08-06 17:33:16.928454	1.0.0	TIMEOUT	local-debug		\N	\N	\N
16	01K1ZBQZ3V2ZZ073H97R8E4W1F	v1.0.0	auto_test	2	0	0	0	2025-08-06 17:35:05.956	2025-08-06 17:35:06.556428	1.0.0	TIMEOUT	local-debug		\N	\N	\N
17	01K1ZBWATAVZTCHRJ54A20FYQB	v1.0.0	auto_test	2	0	0	0	2025-08-06 17:37:29.003	2025-08-06 17:37:29.631942	1.0.0	TIMEOUT	local-debug		\N	\N	\N
18	01K1ZCGG977J2Z6N9Q68D2JCCS	v1.0.0	auto_test	2	0	0	0	2025-08-06 17:48:29.989	2025-08-06 17:48:30.704359	1.0.0	TIMEOUT	local-debug		\N	\N	\N
19	01K1ZCHJTP406BBDZPT1FJ4Z2C	v1.0.0	auto_test	2	0	0	0	2025-08-06 17:49:05.343	2025-08-06 17:49:05.973133	1.0.0	TIMEOUT	local-debug		\N	\N	\N
20	01K1ZCVFENF215BFM4E50ZGXYQ	v1.0.0	auto_test	2	0	0	0	2025-08-06 17:54:29.553	2025-08-06 17:54:30.081778	1.0.0	TIMEOUT	local-debug		\N	\N	\N
21	01K1ZCWE4MKRHKEYWWD2ZXK8FM	v1.0.0	auto_test	1	0	0	0	2025-08-06 17:55:00.981	2025-08-06 17:55:01.541035	1.0.0	TIMEOUT	local-debug		\N	\N	\N
22	01K1ZCWWC7MHWXSXC1N7P84169	v1.0.0	auto_test	1	0	0	0	2025-08-06 17:55:15.553	2025-08-06 17:55:16.066528	1.0.0	TIMEOUT	local-debug		\N	\N	\N
23	01K1ZCXNDQZB2TR0XGNHP2ZCB0	v1.0.0	auto_test	1	0	0	0	2025-08-06 17:55:48.922	\N	1.0.0	PROCESSING	local-debug		\N	\N	\N
24	01K1ZCY5HD07RYBN0Y41KQVWN1	v1.0.0	auto_test	1	0	0	0	2025-08-06 17:55:57.845	\N	1.0.0	PROCESSING	local-debug		\N	\N	\N
25	01K1ZCZTRMS6D852WFJA7E46J8	v1.0.0	auto_test	1	0	0	0	2025-08-06 17:56:52.315	\N	1.0.0	PROCESSING	local-debug		\N	\N	\N
26	01K1ZD2Q84BANFTJG1FBESFM7J	v1.0.0	auto_test	1	0	0	0	2025-08-06 17:58:26.937	2025-08-06 17:58:27.610557	1.0.0	TIMEOUT	local-debug		\N	\N	\N
27	01K1ZDQ43BGRSWRXBK5CGWVGQ8	v1.0.0	auto_test	1	0	0	0	2025-08-06 18:09:35.435	2025-08-06 18:09:35.965325	1.0.0	TIMEOUT	local-debug		\N	\N	\N
28	01K1ZDR7VYRDWY105EY3J18GN3	v1.0.0	auto_test	1	0	0	0	2025-08-06 18:10:12.072	2025-08-06 18:10:12.661669	1.0.0	TIMEOUT	local-debug		\N	\N	\N
29	01K1ZDTVKS36PHYZKHMMJ8W2VG	v1.0.0	auto_test	1	0	0	0	2025-08-06 18:11:38.155	2025-08-06 18:11:39.054174	1.0.0	TIMEOUT	local-debug		\N	\N	\N
30	01K1ZDWPJ2J862MAG819BJ66T5	v1.0.0	auto_test	1	0	0	0	2025-08-06 18:12:38.174	2025-08-06 18:12:38.669798	1.0.0	TIMEOUT	local-debug		\N	\N	\N
31	01K1ZEKTGN46Z8CEJ9D8HH4E7X	v1.0.0	auto_test	1	0	0	0	2025-08-06 18:25:15.913	2025-08-06 18:25:16.624167	1.0.0	TIMEOUT	local-debug		\N	\N	\N
32	01K1ZEPHBFB7X02Z86BRE888GP	v1.0.0	auto_test	1	0	0	0	2025-08-06 18:26:44.87	2025-08-06 18:26:45.516688	1.0.0	TIMEOUT	local-debug		\N	\N	\N
33	01K1ZERFQ1R1NB7M3T2SZGRQNQ	v1.0.0	auto_test	1	0	0	0	2025-08-06 18:27:48.703	2025-08-06 18:27:49.200059	1.0.0	TIMEOUT	local-debug		\N	\N	\N
34	01K1ZES6SQD0Q9EKZMF0FV4C2D	v1.0.0	auto_test	1	0	0	0	2025-08-06 18:28:12.383	2025-08-06 18:28:13.147391	1.0.0	TIMEOUT	local-debug		\N	\N	\N
35	01K1ZESQEB1NKB6VZ7XGHCMV0K	v1.0.0	auto_test	1	0	0	0	2025-08-06 18:28:29.376	2025-08-06 18:28:29.851183	1.0.0	TIMEOUT	local-debug		\N	\N	\N
36	01K1ZET7787HCPD38R9N6GE3MK	v1.0.0	auto_test	1	0	0	0	2025-08-06 18:28:45.54	2025-08-06 18:28:46.049979	1.0.0	TIMEOUT	local-debug		\N	\N	\N
37	01K1ZEY28TTFDG2SE3WK7K2J5K	v1.0.0	auto_test	2	0	0	0	2025-08-06 18:30:51.529	2025-08-06 18:30:52.067081	1.0.0	TIMEOUT	local-debug		\N	\N	\N
38	01K1ZF2DN5X8X6K4D98AJZ78XY	v1.0.0	auto_test	1	0	0	0	2025-08-06 18:33:14.257	2025-08-06 18:33:14.74853	1.0.0	TIMEOUT	local-debug		\N	\N	\N
39	01K1ZF5586JHK862GC2DZ70F3K	v1.0.0	auto_test	1	0	0	0	2025-08-06 18:34:43.997	2025-08-06 18:34:44.496715	1.0.0	TIMEOUT	local-debug		\N	\N	\N
40	01K1ZF7S9MTFR2R3DTCA82X7T1	v1.0.0	auto_test	1	1	0	0	2025-08-06 18:36:10.009	2025-08-06 18:36:10.526915	1.0.0	COMPLETED	local-debug		\N	\N	\N
45	01K1ZFAATAET3WGMKYXMKRCKH7	v1.0.0	auto_test	2	1	1	0	2025-08-06 18:37:33.531	2025-08-06 18:37:34.146209	1.0.0	COMPLETED	local-debug		\N	\N	\N
41	01K1ZF7XYQ2BNP4WH81W9C9GW1	v1.0.0	auto_test	1	1	0	0	2025-08-06 18:36:14.784	2025-08-06 18:36:15.307867	1.0.0	COMPLETED	local-debug		\N	\N	\N
44	01K1ZF9WQBZPSMKTT0NAHD6QP0	v1.0.0	auto_test	2	1	1	0	2025-08-06 18:37:19.068	2025-08-06 18:37:19.79873	1.0.0	COMPLETED	local-debug		\N	\N	\N
42	01K1ZF875Y0NW0HADTBXFJ46X8	v1.0.0	auto_test	2	1	1	0	2025-08-06 18:36:24.224	2025-08-06 18:36:24.734516	1.0.0	COMPLETED	local-debug		\N	\N	\N
46	01K1ZFC5VKE4G7YZD6VYW1V9VW	v1.0.0	auto_test	2	2	0	0	2025-08-06 18:38:33.951	2025-08-06 18:38:34.670145	1.0.0	COMPLETED	local-debug		\N	\N	\N
43	01K1ZF8NQ8DSQNK01XGF8WG7XW	v1.0.0	auto_test	2	1	1	0	2025-08-06 18:36:39.134	2025-08-06 18:36:39.846501	1.0.0	COMPLETED	local-debug		\N	\N	\N
47	01K1ZN9X03RBHZ0DPP9590BZHC	v1.0.0	auto_test	2	2	0	0	2025-08-06 20:22:10.802	2025-08-06 20:22:11.536332	1.0.0	COMPLETED	local-debug		\N	\N	\N
\.


--
-- Data for Name: auto_system_variable; Type: TABLE DATA; Schema: public; Owner: admin
--

COPY public.auto_system_variable (id, component, component_like, profile, config_key, value) FROM stdin;
1	\N	\N	\N	1.0.0.versionId	v1.0.0
\.


--
-- Data for Name: auto_testngresult; Type: TABLE DATA; Schema: public; Owner: admin
--

COPY public.auto_testngresult (id, runid, component, suite_result, insertdatetime, config) FROM stdin;
1	01K1Z4ZB5E00C1N3AY12TTYGYA	auto_test	{"featureScenarioMap":{"user_login":[]},"suiteParameters":{"dailyRunVersion":"QA-DailyRun","sanityOnly":"false","testType":"Regression","scenarios":"user_login","isRelease":"true","labels":"","isDebug":"false","component":"auto_test","suite":"auto_test","build":"","ids":"1,2","testScope":"Regression","runId":"01K1Z4ZB5E00C1N3AY12TTYGYA","region":"nam","actualFixVersion":"1.0.0","group":"Regression","runBy":"local-debug"}}	2025-08-06 15:36:47.586881	\N
2	01K1Z5DT8MYFWWWWT880A8B18X	auto_test	{"featureScenarioMap":{"user_login":[]},"suiteParameters":{"dailyRunVersion":"QA-DailyRun","sanityOnly":"false","testType":"Regression","scenarios":"user_login","isRelease":"true","labels":"","isDebug":"false","component":"auto_test","suite":"auto_test","build":"","ids":"1,2","testScope":"Regression","runId":"01K1Z5DT8MYFWWWWT880A8B18X","region":"nam","actualFixVersion":"1.0.0","group":"Regression","runBy":"local-debug"}}	2025-08-06 15:44:42.134333	\N
3	01K1Z6MPJZXD94NSF2S6VV6HRM	auto_test	{"featureScenarioMap":{"user_login":[]},"suiteParameters":{"dailyRunVersion":"QA-DailyRun","sanityOnly":"false","testType":"Regression","scenarios":"user_login","isRelease":"true","labels":"","isDebug":"false","component":"auto_test","suite":"auto_test","build":"","ids":"1,2","testScope":"Regression","runId":"01K1Z6MPJZXD94NSF2S6VV6HRM","region":"nam","actualFixVersion":"1.0.0","group":"Regression","runBy":"local-debug"}}	2025-08-06 16:05:56.038246	\N
4	01K1Z6TF7CE6FFWP577JEHNQ3G	auto_test	{"featureScenarioMap":{},"suiteParameters":{"dailyRunVersion":"QA-DailyRun","sanityOnly":"false","testType":"Regression","scenarios":"user_login","isRelease":"true","labels":"","isDebug":"false","component":"auto_test","suite":"auto_test","build":"","ids":"1,2","testScope":"Regression","runId":"01K1Z6TF7CE6FFWP577JEHNQ3G","region":"nam","actualFixVersion":"1.0.0","group":"Regression","runBy":"local-debug"}}	2025-08-06 16:09:05.516593	\N
5	01K1Z72Y7EHJJYTPT4CK21EBB4	auto_test	{"featureScenarioMap":{},"suiteParameters":{"dailyRunVersion":"QA-DailyRun","sanityOnly":"false","testType":"Regression","scenarios":"user_login","isRelease":"true","labels":"","isDebug":"false","component":"auto_test","suite":"auto_test","build":"","ids":"1,2","testScope":"Regression","runId":"01K1Z72Y7EHJJYTPT4CK21EBB4","region":"nam","actualFixVersion":"1.0.0","group":"Regression","runBy":"local-debug"}}	2025-08-06 16:13:43.345008	\N
6	01K1Z74MN5XDNEX02BQAVFJTED	auto_test	{"featureScenarioMap":{},"suiteParameters":{"dailyRunVersion":"QA-DailyRun","sanityOnly":"false","testType":"Regression","scenarios":"user_login","isRelease":"true","labels":"","isDebug":"false","component":"auto_test","suite":"auto_test","build":"","ids":"1,2","testScope":"Regression","runId":"01K1Z74MN5XDNEX02BQAVFJTED","region":"nam","actualFixVersion":"1.0.0","group":"Regression","runBy":"local-debug"}}	2025-08-06 16:14:38.811148	\N
7	01K1Z758AXPC4B80ME8KNVZVT7	auto_test	{"featureScenarioMap":{},"suiteParameters":{"dailyRunVersion":"QA-DailyRun","sanityOnly":"false","testType":"Regression","scenarios":"user_login","isRelease":"true","labels":"","isDebug":"false","component":"auto_test","suite":"auto_test","build":"","ids":"1,2","testScope":"Regression","runId":"01K1Z758AXPC4B80ME8KNVZVT7","region":"nam","actualFixVersion":"1.0.0","group":"Regression","runBy":"local-debug"}}	2025-08-06 16:14:58.905739	\N
8	01K1Z7GRX1ZNYJ9RC0X176H0D2	auto_test	{"featureScenarioMap":{"user_login":[]},"suiteParameters":{"dailyRunVersion":"QA-DailyRun","sanityOnly":"false","testType":"Regression","scenarios":"user_login","isRelease":"true","labels":"","isDebug":"false","component":"auto_test","suite":"auto_test","build":"","ids":"1,2","testScope":"Regression","runId":"01K1Z7GRX1ZNYJ9RC0X176H0D2","region":"nam","actualFixVersion":"1.0.0","group":"Regression","runBy":"local-debug"}}	2025-08-06 16:21:15.908194	\N
9	01K1Z8DZZF5T1QQ3BFXYKA68FZ	auto_test	{"featureScenarioMap":{"user_login":[]},"suiteParameters":{"dailyRunVersion":"QA-DailyRun","sanityOnly":"false","testType":"Regression","scenarios":"user_login","isRelease":"true","labels":"","isDebug":"false","component":"auto_test","suite":"auto_test","build":"","ids":"1,2","testScope":"Regression","runId":"01K1Z8DZZF5T1QQ3BFXYKA68FZ","region":"nam","actualFixVersion":"1.0.0","group":"Regression","runBy":"local-debug"}}	2025-08-06 16:37:13.391168	\N
10	01K1Z9C35117WG13A3M6HTHGKK	auto_test	{"featureScenarioMap":{"user_login":[]},"suiteParameters":{"dailyRunVersion":"QA-DailyRun","sanityOnly":"false","testType":"Regression","scenarios":"user_login","isRelease":"true","labels":"","isDebug":"false","component":"auto_test","suite":"auto_test","build":"","ids":"1,2","testScope":"Regression","runId":"01K1Z9C35117WG13A3M6HTHGKK","region":"nam","actualFixVersion":"1.0.0","group":"Regression","runBy":"local-debug"}}	2025-08-06 16:53:39.673277	\N
11	01K1Z9P50MXM3S3EBA7K7ZRD4N	auto_test	{"featureScenarioMap":{"user_login":[]},"suiteParameters":{"dailyRunVersion":"QA-DailyRun","sanityOnly":"false","testType":"Regression","scenarios":"user_login","isRelease":"true","labels":"","isDebug":"false","component":"auto_test","suite":"auto_test","build":"","ids":"1,2","testScope":"Regression","runId":"01K1Z9P50MXM3S3EBA7K7ZRD4N","region":"nam","actualFixVersion":"1.0.0","group":"Regression","runBy":"local-debug"}}	2025-08-06 16:59:09.297385	\N
12	01K1Z9S8T401DD9FC4AQK7B4V7	auto_test	{"featureScenarioMap":{"user_login":[]},"suiteParameters":{"dailyRunVersion":"QA-DailyRun","sanityOnly":"false","testType":"Regression","scenarios":"user_login","isRelease":"true","labels":"","isDebug":"false","component":"auto_test","suite":"auto_test","build":"","ids":"1,2","testScope":"Regression","runId":"01K1Z9S8T401DD9FC4AQK7B4V7","region":"nam","actualFixVersion":"1.0.0","group":"Regression","runBy":"local-debug"}}	2025-08-06 17:00:51.463142	\N
13	01K1ZA2B47HCQZK0DQF36BMJ92	auto_test	{"featureScenarioMap":{},"suiteParameters":{"dailyRunVersion":"QA-DailyRun","sanityOnly":"false","testType":"Regression","scenarios":"user_login","isRelease":"true","labels":"","isDebug":"false","component":"auto_test","suite":"auto_test","build":"","ids":"1,2","testScope":"Regression","runId":"01K1ZA2B47HCQZK0DQF36BMJ92","region":"nam","actualFixVersion":"1.0.0","group":"Regression","runBy":"local-debug"}}	2025-08-06 17:05:49.233353	\N
14	01K1ZAE55F50KTT4269J3NYNR3	auto_test	{"featureScenarioMap":{},"suiteParameters":{"dailyRunVersion":"QA-DailyRun","sanityOnly":"false","testType":"Regression","scenarios":"user_login","isRelease":"true","labels":"","isDebug":"false","component":"auto_test","suite":"auto_test","build":"","ids":"1,2","testScope":"Regression","runId":"01K1ZAE55F50KTT4269J3NYNR3","region":"nam","actualFixVersion":"1.0.0","group":"Regression","runBy":"local-debug"}}	2025-08-06 17:12:16.552253	\N
15	01K1ZAGHAZ5JMA76F8DA49RPC6	auto_test	{"featureScenarioMap":{},"suiteParameters":{"dailyRunVersion":"QA-DailyRun","sanityOnly":"false","testType":"Regression","scenarios":"user_login","isRelease":"true","labels":"","isDebug":"false","component":"auto_test","suite":"auto_test","build":"","ids":"1,2","testScope":"Regression","runId":"01K1ZAGHAZ5JMA76F8DA49RPC6","region":"nam","actualFixVersion":"1.0.0","group":"Regression","runBy":"local-debug"}}	2025-08-06 17:13:34.300764	\N
16	01K1ZARGMJQBVFC72FM6XNMZER	auto_test	{"featureScenarioMap":{},"suiteParameters":{"dailyRunVersion":"QA-DailyRun","sanityOnly":"false","testType":"Regression","scenarios":"user_login","isRelease":"true","labels":"","isDebug":"false","component":"auto_test","suite":"auto_test","build":"","ids":"1,2","testScope":"Regression","runId":"01K1ZARGMJQBVFC72FM6XNMZER","region":"nam","actualFixVersion":"1.0.0","group":"Regression","runBy":"local-debug"}}	2025-08-06 17:17:55.822469	\N
17	01K1ZAYPAJTHHG8E8JAKKRBB9J	auto_test	{"featureScenarioMap":{},"suiteParameters":{"dailyRunVersion":"QA-DailyRun","sanityOnly":"false","testType":"Regression","scenarios":"user_login","isRelease":"true","labels":"","isDebug":"false","component":"auto_test","suite":"auto_test","build":"","ids":"1,2","testScope":"Regression","runId":"01K1ZAYPAJTHHG8E8JAKKRBB9J","region":"nam","actualFixVersion":"1.0.0","group":"Regression","runBy":"local-debug"}}	2025-08-06 17:21:18.278053	\N
18	01K1ZAZE2JP0K754NE9CYDYW0J	auto_test	{"featureScenarioMap":{},"suiteParameters":{"dailyRunVersion":"QA-DailyRun","sanityOnly":"false","testType":"Regression","scenarios":"user_login","isRelease":"true","labels":"","isDebug":"false","component":"auto_test","suite":"auto_test","build":"","ids":"1,2","testScope":"Regression","runId":"01K1ZAZE2JP0K754NE9CYDYW0J","region":"nam","actualFixVersion":"1.0.0","group":"Regression","runBy":"local-debug"}}	2025-08-06 17:21:42.690718	\N
19	01K1ZB1385JD6MZ98F97RKSRNA	auto_test	{"featureScenarioMap":{},"suiteParameters":{"dailyRunVersion":"QA-DailyRun","sanityOnly":"false","testType":"Regression","scenarios":"user_login","isRelease":"true","labels":"","isDebug":"false","component":"auto_test","suite":"auto_test","build":"","ids":"1,2","testScope":"Regression","runId":"01K1ZB1385JD6MZ98F97RKSRNA","region":"nam","actualFixVersion":"1.0.0","group":"Regression","runBy":"local-debug"}}	2025-08-06 17:22:37.119754	\N
20	01K1ZBA130NTYGH9N48JJV2BSR	auto_test	{"featureScenarioMap":{},"suiteParameters":{"dailyRunVersion":"QA-DailyRun","sanityOnly":"false","testType":"Regression","scenarios":"user_login","isRelease":"true","labels":"","isDebug":"false","component":"auto_test","suite":"auto_test","build":"","ids":"1,2","testScope":"Regression","runId":"01K1ZBA130NTYGH9N48JJV2BSR","region":"nam","actualFixVersion":"1.0.0","group":"Regression","runBy":"local-debug"}}	2025-08-06 17:27:29.629235	\N
21	01K1ZBEGVXAGXWJWYHT0164R11	auto_test	{"featureScenarioMap":{},"suiteParameters":{"dailyRunVersion":"QA-DailyRun","sanityOnly":"false","testType":"Regression","scenarios":"user_login","isRelease":"true","labels":"","isDebug":"false","component":"auto_test","suite":"auto_test","build":"","ids":"1,2","testScope":"Regression","runId":"01K1ZBEGVXAGXWJWYHT0164R11","region":"nam","actualFixVersion":"1.0.0","group":"Regression","runBy":"local-debug"}}	2025-08-06 17:29:57.11911	\N
22	01K1ZBKVZ3B051XQ743BRM9NQ9	auto_test	{"featureScenarioMap":{},"suiteParameters":{"dailyRunVersion":"QA-DailyRun","sanityOnly":"false","testType":"Regression","scenarios":"user_login","isRelease":"true","labels":"","isDebug":"false","component":"auto_test","suite":"auto_test","build":"","ids":"1,2","testScope":"Regression","runId":"01K1ZBKVZ3B051XQ743BRM9NQ9","region":"nam","actualFixVersion":"1.0.0","group":"Regression","runBy":"local-debug"}}	2025-08-06 17:32:52.498575	\N
23	01K1ZBMKZ4REYW2BGGDDW9YK2M	auto_test	{"featureScenarioMap":{},"suiteParameters":{"dailyRunVersion":"QA-DailyRun","sanityOnly":"false","testType":"Regression","scenarios":"user_login","isRelease":"true","labels":"","isDebug":"false","component":"auto_test","suite":"auto_test","build":"","ids":"1,2","testScope":"Regression","runId":"01K1ZBMKZ4REYW2BGGDDW9YK2M","region":"nam","actualFixVersion":"1.0.0","group":"Regression","runBy":"local-debug"}}	2025-08-06 17:33:16.93237	\N
24	01K1ZBQZ3V2ZZ073H97R8E4W1F	auto_test	{"featureScenarioMap":{},"suiteParameters":{"dailyRunVersion":"QA-DailyRun","sanityOnly":"false","testType":"Regression","scenarios":"user_login","isRelease":"true","labels":"","isDebug":"false","component":"auto_test","suite":"auto_test","build":"","ids":"1,2","testScope":"Regression","runId":"01K1ZBQZ3V2ZZ073H97R8E4W1F","region":"nam","actualFixVersion":"1.0.0","group":"Regression","runBy":"local-debug"}}	2025-08-06 17:35:06.558579	\N
25	01K1ZBWATAVZTCHRJ54A20FYQB	auto_test	{"featureScenarioMap":{},"suiteParameters":{"dailyRunVersion":"QA-DailyRun","sanityOnly":"false","testType":"Regression","scenarios":"user_login","isRelease":"true","labels":"","isDebug":"false","component":"auto_test","suite":"auto_test","build":"","ids":"1,2","testScope":"Regression","runId":"01K1ZBWATAVZTCHRJ54A20FYQB","region":"nam","actualFixVersion":"1.0.0","group":"Regression","runBy":"local-debug"}}	2025-08-06 17:37:29.634752	\N
26	01K1ZCGG977J2Z6N9Q68D2JCCS	auto_test	{"featureScenarioMap":{},"suiteParameters":{"dailyRunVersion":"QA-DailyRun","sanityOnly":"false","testType":"Regression","scenarios":"user_login","isRelease":"true","labels":"","isDebug":"false","component":"auto_test","suite":"auto_test","build":"","ids":"1,2","testScope":"Regression","runId":"01K1ZCGG977J2Z6N9Q68D2JCCS","region":"nam","actualFixVersion":"1.0.0","group":"Regression","runBy":"local-debug"}}	2025-08-06 17:48:30.707435	\N
27	01K1ZCHJTP406BBDZPT1FJ4Z2C	auto_test	{"featureScenarioMap":{},"suiteParameters":{"dailyRunVersion":"QA-DailyRun","sanityOnly":"false","testType":"Regression","scenarios":"user_login","isRelease":"true","labels":"","isDebug":"false","component":"auto_test","suite":"auto_test","build":"","ids":"1,2","testScope":"Regression","runId":"01K1ZCHJTP406BBDZPT1FJ4Z2C","region":"nam","actualFixVersion":"1.0.0","group":"Regression","runBy":"local-debug"}}	2025-08-06 17:49:05.975243	\N
28	01K1ZCVFENF215BFM4E50ZGXYQ	auto_test	{"featureScenarioMap":{},"suiteParameters":{"dailyRunVersion":"QA-DailyRun","sanityOnly":"false","testType":"Regression","scenarios":"user_login","isRelease":"true","labels":"","isDebug":"false","component":"auto_test","suite":"auto_test","build":"","ids":"1,2","testScope":"Regression","runId":"01K1ZCVFENF215BFM4E50ZGXYQ","region":"nam","actualFixVersion":"1.0.0","group":"Regression","runBy":"local-debug"}}	2025-08-06 17:54:30.083948	\N
29	01K1ZCWE4MKRHKEYWWD2ZXK8FM	auto_test	{"featureScenarioMap":{},"suiteParameters":{"dailyRunVersion":"QA-DailyRun","sanityOnly":"false","testType":"Regression","scenarios":"user_login","isRelease":"true","labels":"","isDebug":"false","component":"auto_test","suite":"auto_test","build":"","ids":"1","testScope":"Regression","runId":"01K1ZCWE4MKRHKEYWWD2ZXK8FM","region":"nam","actualFixVersion":"1.0.0","group":"Regression","runBy":"local-debug"}}	2025-08-06 17:55:01.543245	\N
30	01K1ZCWWC7MHWXSXC1N7P84169	auto_test	{"featureScenarioMap":{},"suiteParameters":{"dailyRunVersion":"QA-DailyRun","sanityOnly":"false","testType":"Regression","scenarios":"user_login","isRelease":"true","labels":"","isDebug":"false","component":"auto_test","suite":"auto_test","build":"","ids":"2","testScope":"Regression","runId":"01K1ZCWWC7MHWXSXC1N7P84169","region":"nam","actualFixVersion":"1.0.0","group":"Regression","runBy":"local-debug"}}	2025-08-06 17:55:16.068927	\N
31	01K1ZCXNDQZB2TR0XGNHP2ZCB0	auto_test	\N	\N	\N
32	01K1ZCY5HD07RYBN0Y41KQVWN1	auto_test	\N	\N	\N
33	01K1ZCZTRMS6D852WFJA7E46J8	auto_test	\N	\N	\N
34	01K1ZD2Q84BANFTJG1FBESFM7J	auto_test	{"featureScenarioMap":{},"suiteParameters":{"dailyRunVersion":"QA-DailyRun","sanityOnly":"false","testType":"Regression","scenarios":"user_login","isRelease":"true","labels":"","isDebug":"false","component":"auto_test","suite":"auto_test","build":"","ids":"2","testScope":"Regression","runId":"01K1ZD2Q84BANFTJG1FBESFM7J","region":"nam","actualFixVersion":"1.0.0","group":"Regression","runBy":"local-debug"}}	2025-08-06 17:58:27.613542	\N
35	01K1ZDQ43BGRSWRXBK5CGWVGQ8	auto_test	{"featureScenarioMap":{},"suiteParameters":{"dailyRunVersion":"QA-DailyRun","sanityOnly":"false","testType":"Regression","scenarios":"user_login","isRelease":"true","labels":"","isDebug":"false","component":"auto_test","suite":"auto_test","build":"","ids":"2","testScope":"Regression","runId":"01K1ZDQ43BGRSWRXBK5CGWVGQ8","region":"nam","actualFixVersion":"1.0.0","group":"Regression","runBy":"local-debug"}}	2025-08-06 18:09:35.967839	\N
36	01K1ZDR7VYRDWY105EY3J18GN3	auto_test	{"featureScenarioMap":{},"suiteParameters":{"dailyRunVersion":"QA-DailyRun","sanityOnly":"false","testType":"Regression","scenarios":"user_login","isRelease":"true","labels":"","isDebug":"false","component":"auto_test","suite":"auto_test","build":"","ids":"2","testScope":"Regression","runId":"01K1ZDR7VYRDWY105EY3J18GN3","region":"nam","actualFixVersion":"1.0.0","group":"Regression","runBy":"local-debug"}}	2025-08-06 18:10:12.664151	\N
37	01K1ZDTVKS36PHYZKHMMJ8W2VG	auto_test	{"featureScenarioMap":{},"suiteParameters":{"dailyRunVersion":"QA-DailyRun","sanityOnly":"false","testType":"Regression","scenarios":"user_login","isRelease":"true","labels":"","isDebug":"false","component":"auto_test","suite":"auto_test","build":"","ids":"2","testScope":"Regression","runId":"01K1ZDTVKS36PHYZKHMMJ8W2VG","region":"nam","actualFixVersion":"1.0.0","group":"Regression","runBy":"local-debug"}}	2025-08-06 18:11:39.056431	\N
38	01K1ZDWPJ2J862MAG819BJ66T5	auto_test	{"featureScenarioMap":{},"suiteParameters":{"dailyRunVersion":"QA-DailyRun","sanityOnly":"false","testType":"Regression","scenarios":"user_login","isRelease":"true","labels":"","isDebug":"false","component":"auto_test","suite":"auto_test","build":"","ids":"2","testScope":"Regression","runId":"01K1ZDWPJ2J862MAG819BJ66T5","region":"nam","actualFixVersion":"1.0.0","group":"Regression","runBy":"local-debug"}}	2025-08-06 18:12:38.671991	\N
39	01K1ZEKTGN46Z8CEJ9D8HH4E7X	auto_test	{"featureScenarioMap":{},"suiteParameters":{"dailyRunVersion":"QA-DailyRun","sanityOnly":"false","testType":"Regression","scenarios":"user_login","isRelease":"true","labels":"","isDebug":"false","component":"auto_test","suite":"auto_test","build":"","ids":"2","testScope":"Regression","runId":"01K1ZEKTGN46Z8CEJ9D8HH4E7X","region":"nam","actualFixVersion":"1.0.0","group":"Regression","runBy":"local-debug"}}	2025-08-06 18:25:16.626563	\N
40	01K1ZEPHBFB7X02Z86BRE888GP	auto_test	{"featureScenarioMap":{},"suiteParameters":{"dailyRunVersion":"QA-DailyRun","sanityOnly":"false","testType":"Regression","scenarios":"user_login","isRelease":"true","labels":"","isDebug":"false","component":"auto_test","suite":"auto_test","build":"","ids":"2","testScope":"Regression","runId":"01K1ZEPHBFB7X02Z86BRE888GP","region":"nam","actualFixVersion":"1.0.0","group":"Regression","runBy":"local-debug"}}	2025-08-06 18:26:45.518765	\N
41	01K1ZERFQ1R1NB7M3T2SZGRQNQ	auto_test	{"featureScenarioMap":{},"suiteParameters":{"dailyRunVersion":"QA-DailyRun","sanityOnly":"false","testType":"Regression","scenarios":"user_login","isRelease":"true","labels":"","isDebug":"false","component":"auto_test","suite":"auto_test","build":"","ids":"2","testScope":"Regression","runId":"01K1ZERFQ1R1NB7M3T2SZGRQNQ","region":"nam","actualFixVersion":"1.0.0","group":"Regression","runBy":"local-debug"}}	2025-08-06 18:27:49.202692	\N
42	01K1ZES6SQD0Q9EKZMF0FV4C2D	auto_test	{"featureScenarioMap":{},"suiteParameters":{"dailyRunVersion":"QA-DailyRun","sanityOnly":"false","testType":"Regression","scenarios":"user_login","isRelease":"true","labels":"","isDebug":"false","component":"auto_test","suite":"auto_test","build":"","ids":"2","testScope":"Regression","runId":"01K1ZES6SQD0Q9EKZMF0FV4C2D","region":"nam","actualFixVersion":"1.0.0","group":"Regression","runBy":"local-debug"}}	2025-08-06 18:28:13.153941	\N
43	01K1ZESQEB1NKB6VZ7XGHCMV0K	auto_test	{"featureScenarioMap":{},"suiteParameters":{"dailyRunVersion":"QA-DailyRun","sanityOnly":"false","testType":"Regression","scenarios":"user_login","isRelease":"true","labels":"","isDebug":"false","component":"auto_test","suite":"auto_test","build":"","ids":"2","testScope":"Regression","runId":"01K1ZESQEB1NKB6VZ7XGHCMV0K","region":"nam","actualFixVersion":"1.0.0","group":"Regression","runBy":"local-debug"}}	2025-08-06 18:28:29.853956	\N
44	01K1ZET7787HCPD38R9N6GE3MK	auto_test	{"featureScenarioMap":{},"suiteParameters":{"dailyRunVersion":"QA-DailyRun","sanityOnly":"false","testType":"Regression","scenarios":"user_login","isRelease":"true","labels":"","isDebug":"false","component":"auto_test","suite":"auto_test","build":"","ids":"2","testScope":"Regression","runId":"01K1ZET7787HCPD38R9N6GE3MK","region":"nam","actualFixVersion":"1.0.0","group":"Regression","runBy":"local-debug"}}	2025-08-06 18:28:46.052422	\N
45	01K1ZEY28TTFDG2SE3WK7K2J5K	auto_test	{"featureScenarioMap":{},"suiteParameters":{"dailyRunVersion":"QA-DailyRun","sanityOnly":"false","testType":"Regression","scenarios":"user_login","isRelease":"true","labels":"","isDebug":"false","component":"auto_test","suite":"auto_test","build":"","ids":"1,2","testScope":"Regression","runId":"01K1ZEY28TTFDG2SE3WK7K2J5K","region":"nam","actualFixVersion":"1.0.0","group":"Regression","runBy":"local-debug"}}	2025-08-06 18:30:52.068964	\N
46	01K1ZF2DN5X8X6K4D98AJZ78XY	auto_test	{"featureScenarioMap":{},"suiteParameters":{"dailyRunVersion":"QA-DailyRun","sanityOnly":"false","testType":"Regression","scenarios":"user_login","isRelease":"true","labels":"","isDebug":"false","component":"auto_test","suite":"auto_test","build":"","ids":"2","testScope":"Regression","runId":"01K1ZF2DN5X8X6K4D98AJZ78XY","region":"nam","actualFixVersion":"1.0.0","group":"Regression","runBy":"local-debug"}}	2025-08-06 18:33:14.750947	\N
47	01K1ZF5586JHK862GC2DZ70F3K	auto_test	{"featureScenarioMap":{},"suiteParameters":{"dailyRunVersion":"QA-DailyRun","sanityOnly":"false","testType":"Regression","scenarios":"user_login","isRelease":"true","labels":"","isDebug":"false","component":"auto_test","suite":"auto_test","build":"","ids":"2","testScope":"Regression","runId":"01K1ZF5586JHK862GC2DZ70F3K","region":"nam","actualFixVersion":"1.0.0","group":"Regression","runBy":"local-debug"}}	2025-08-06 18:34:44.498748	\N
48	01K1ZF7S9MTFR2R3DTCA82X7T1	auto_test	{"featureScenarioMap":{"user_login":[{"passedTestResults":[{"endMillis":1754476570521,"output":{},"startMillis":1754476570506,"status":1,"stepId":1,"testClassDescription":"","testClassName":"6a62689d","testMethodDescription":"Should unable to login with empty password->verify_response","testMethodName":"2 -> 1 -> verify_response","throwable":{}},{"endMillis":1754476570503,"output":{},"startMillis":1754476570028,"status":1,"stepId":1,"testClassDescription":"","testClassName":"6a62689d","testMethodDescription":"Should unable to login with empty password->trigger_login_API","testMethodName":"2 -> 1 -> trigger_login_API","throwable":{}}],"scenarioName":"null Should unable to login with empty password"}]},"suiteParameters":{"dailyRunVersion":"QA-DailyRun","sanityOnly":"false","testType":"Regression","scenarios":"user_login","isRelease":"true","labels":"","isDebug":"false","component":"auto_test","suite":"auto_test","build":"","ids":"2","testScope":"Regression","runId":"01K1ZF7S9MTFR2R3DTCA82X7T1","region":"nam","actualFixVersion":"1.0.0","group":"Regression","runBy":"local-debug"}}	2025-08-06 18:36:10.533046	\N
49	01K1ZF7XYQ2BNP4WH81W9C9GW1	auto_test	{"featureScenarioMap":{"user_login":[{"passedTestResults":[{"endMillis":1754476575286,"output":{},"startMillis":1754476574797,"status":1,"stepId":1,"testClassDescription":"","testClassName":"94f6bfb","testMethodDescription":"Should unable to login with empty password->trigger_login_API","testMethodName":"2 -> 1 -> trigger_login_API","throwable":{}},{"endMillis":1754476575302,"output":{},"startMillis":1754476575288,"status":1,"stepId":1,"testClassDescription":"","testClassName":"94f6bfb","testMethodDescription":"Should unable to login with empty password->verify_response","testMethodName":"2 -> 1 -> verify_response","throwable":{}}],"scenarioName":"null Should unable to login with empty password"}]},"suiteParameters":{"dailyRunVersion":"QA-DailyRun","sanityOnly":"false","testType":"Regression","scenarios":"user_login","isRelease":"true","labels":"","isDebug":"false","component":"auto_test","suite":"auto_test","build":"","ids":"2","testScope":"Regression","runId":"01K1ZF7XYQ2BNP4WH81W9C9GW1","region":"nam","actualFixVersion":"1.0.0","group":"Regression","runBy":"local-debug"}}	2025-08-06 18:36:15.311157	\N
\.


--
-- Data for Name: carriers; Type: TABLE DATA; Schema: public; Owner: admin
--

COPY public.carriers (id, name, code, contact_person, contact_phone, address, status, created_at, updated_at) FROM stdin;
\.


--
-- Data for Name: materials; Type: TABLE DATA; Schema: public; Owner: admin
--

COPY public.materials (id, name, description, created_at, updated_at) FROM stdin;
1676511586856882178	材料_1676511586856882178	材料描述_1676511586856882178	2025-08-06 16:00:13.52284	2025-08-06 16:00:13.522841
1676511586856882134	材料_1676511586856882134	材料描述_1676511586856882134	2025-08-06 16:00:13.52326	2025-08-06 16:00:13.523261
1676511524756882178	材料_1676511524756882178	材料描述_1676511524756882178	2025-08-06 16:00:13.523801	2025-08-06 16:00:13.523803
1676590766856882178	材料_1676590766856882178	材料描述_1676590766856882178	2025-08-06 16:00:13.524083	2025-08-06 16:00:13.524084
1676511586812182178	材料_1676511586812182178	材料描述_1676511586812182178	2025-08-06 16:00:13.524291	2025-08-06 16:00:13.524292
\.


--
-- Data for Name: orders; Type: TABLE DATA; Schema: public; Owner: admin
--

COPY public.orders (id, order_number, user_id, status, total_amount, order_data, created_at, updated_at) FROM stdin;
order_order_num	order_num	46323013642	pending	100.00	{"order_number": "order_num"}	2025-08-06 16:00:13.52985	2025-08-06 16:00:13.529853
order_user_id	user_id	46323013642	pending	100.00	{"order_number": "user_id"}	2025-08-06 16:00:13.530553	2025-08-06 16:00:13.530555
\.


--
-- Data for Name: products; Type: TABLE DATA; Schema: public; Owner: admin
--

COPY public.products (id, source_type, offer_id, category_id, subject, image_urls, sku_props, sku_maps, created_at, updated_at) FROM stdin;
product_001	6	1622566	8484	testװ	{https://omsproductionimg.yangkeduo.com/images/2aa17-12-12/bcf848aa71c63896aa7ae7a84b7aaf1543.jpeg,https://omsproductionimg.yangkeduo.com/images/2aa17-12-12/176aa19babfdecffa1d9f98f4aab7e99b4.jpeg,https://omsproductionimg.yangkeduo.com/images/2aa17-12-12/efb5db4239755aabffd3211ca6f197498.jpeg,https://omsproductionimg.yangkeduo.com/images/2aa17-12-12/d2aa9ef7bcc9183c3bb8ca1dfdb1aa8d49.jpeg,https://omsproductionimg.yangkeduo.com/images/2aa17-12-12/74257ab65f3faaaada7a9aafde9aa42fe64aa.jpeg,https://taaaaimg.yangkeduo.com/goods/images/2aa19-aa8-17/e8fbd9cb-cc74-4caa-938aa-84c46d27baaaa8.jpg,https://taaaaimg.yangkeduo.com/goods/images/2aa19-aa8-17/d76f515b-e375-4aa6aa-b94e-cf64f6baa964e.jpg,https://taaaaimg.yangkeduo.com/goods/images/2aa19-aa8-17/f2f279b5-6aaaaaa-4fbe-b99b-7c1cbd7884ea.jpg}	[{"IsImg": true, "Prop": "aaa", "Value": [{"name": "aaaaaaaaa", "value": "1215:11155aaaa378", "imageUrl": "http://taaaaimg.yangkeduo.com/goods/images/2aa18-aa8-28/aa62d42b525a7c78692aacbb83ac772af7.jpeg"}, {"name": "aaaaaaaaaaaaaa+aaaa", "value": "1215:11155aa1666", "imageUrl": "http://taaaaimg.yangkeduo.com/goods/images/2aa18-aa8-28/415bed99d5a925123d7b7c395472aa5de.jpeg"}, {"name": "aaaaaaaaaaaaaa+aaaa", "value": "1215:1aa53aa9781", "imageUrl": "http://taaaaimg.yangkeduo.com/goods/images/2aa18-11-17/48b3aa664faaf7e526ab1b956e813f25cf.jpeg"}]}, {"IsImg": false, "Prop": "aaaa", "Value": [{"name": "Saa9aaaaaaa\\u00a3a", "value": "1226:119128"}, {"name": "Maa9aa-1aaaaaa", "value": "1226:96784"}, {"name": "Laa1aaaa-11aaaa", "value": "1226:33651"}, {"name": "XLaa11aa-12aaaa", "value": "1226:33652"}, {"name": "2XLaa12aa-13aaaa", "value": "1226:33653"}, {"name": "3XLaa13aa-14aaaa", "value": "1226:33656"}]}]	[{"SkuId": "57114357891", "Key": "1215:11155aaaa378;1226:119128", "SpecAttributes": {"aaa": "aaaaaaaaa(aaaa)", "aaaa": "Saa9aaaaaaaa"}, "Price": 95.4, "OriginalPrice": 115, "AmountOnSale": 73, "ImageUrl": "http://taaaaimg.yangkeduo.com/goods/images/2aa18-aa8-28/aa62d42b525a7c78692aacbb83ac772af7.jpeg"}, {"SkuId": "57114357892", "Key": "1215:11155aaaa378;1226:96784", "SpecAttributes": {"aaa": "aaaaaaaaa(aaaa)", "aaaa": "Maa9aa-1aaaaaa"}, "Price": 95.4, "OriginalPrice": 115, "AmountOnSale": 65, "ImageUrl": "http://taaaaimg.yangkeduo.com/goods/images/2aa18-aa8-28/aa62d42b525a7c78692aacbb83ac772af7.jpeg"}, {"SkuId": "57114357893", "Key": "1215:11155aaaa378;1226:33651", "SpecAttributes": {"aaa": "aaaaaaaaa(aaaa)", "aaaa": "Laa1aaaa-11aaaa"}, "Price": 95.4, "OriginalPrice": 115, "AmountOnSale": 82, "ImageUrl": "http://taaaaimg.yangkeduo.com/goods/images/2aa18-aa8-28/aa62d42b525a7c78692aacbb83ac772af7.jpeg"}, {"SkuId": "57114357894", "Key": "1215:11155aaaa378;1226:33652", "SpecAttributes": {"aaa": "aaaaaaaaa(aaaa)", "aaaa": "XLaa11aa-12aaaa"}, "Price": 95.4, "OriginalPrice": 115, "AmountOnSale": 89, "ImageUrl": "http://taaaaimg.yangkeduo.com/goods/images/2aa18-aa8-28/aa62d42b525a7c78692aacbb83ac772af7.jpeg"}, {"SkuId": "57114357895", "Key": "1215:11155aaaa378;1226:33653", "SpecAttributes": {"aaa": "aaaaaaaaa(aaaa)", "aaaa": "2XLaa12aa-13aaaa"}, "Price": 95.4, "OriginalPrice": 115, "AmountOnSale": 94, "ImageUrl": "http://taaaaimg.yangkeduo.com/goods/images/2aa18-aa8-28/aa62d42b525a7c78692aacbb83ac772af7.jpeg"}, {"SkuId": "57114357896", "Key": "1215:11155aaaa378;1226:33656", "SpecAttributes": {"aaa": "aaaaaaaaa(aaaa)", "aaaa": "3XLaa13aa-14aaaa"}, "Price": 95.4, "OriginalPrice": 115, "AmountOnSale": 165, "ImageUrl": "http://taaaaimg.yangkeduo.com/goods/images/2aa18-aa8-28/aa62d42b525a7c78692aacbb83ac772af7.jpeg"}, {"SkuId": "57114357897", "Key": "1215:11155aa1666;1226:119128", "SpecAttributes": {"aaa": "aaaaaaaaaaaaaa+aaaa", "aaaa": "Saa9aaaaaaa\\u00a3a"}, "Price": 95.4, "OriginalPrice": 115, "AmountOnSale": 151, "ImageUrl": "http://taaaaimg.yangkeduo.com/goods/images/2aa18-aa8-28/415bed99d5a925123d7b7c395472aa5de.jpeg"}, {"SkuId": "57114357898", "Key": "1215:11155aa1666;1226:96784", "SpecAttributes": {"aaa": "aaaaaaaaaaaaaa+aaaa", "aaaa": "Maa9aa-1aaaaaa"}, "Price": 95.4, "OriginalPrice": 115, "AmountOnSale": 88, "ImageUrl": "http://taaaaimg.yangkeduo.com/goods/images/2aa18-aa8-28/415bed99d5a925123d7b7c395472aa5de.jpeg"}, {"SkuId": "57114357899", "Key": "1215:11155aa1666;1226:33651", "SpecAttributes": {"aaa": "aaaaaaaaaaaaaa+aaaa", "aaaa": "Laa1aaaa-11aaaa"}, "Price": 95.4, "OriginalPrice": 115, "AmountOnSale": 8, "ImageUrl": "http://taaaaimg.yangkeduo.com/goods/images/2aa18-aa8-28/415bed99d5a925123d7b7c395472aa5de.jpeg"}, {"SkuId": "571143579aaaa", "Key": "1215:11155aa1666;1226:33652", "SpecAttributes": {"aaa": "aaaaaaaaaaaaaa+aaaa", "aaaa": "XLaa11aa-12aaaa"}, "Price": 95.4, "OriginalPrice": 115, "AmountOnSale": 66, "ImageUrl": "http://taaaaimg.yangkeduo.com/goods/images/2aa18-aa8-28/415bed99d5a925123d7b7c395472aa5de.jpeg"}, {"SkuId": "571143579aa1", "Key": "1215:11155aa1666;1226:33653", "SpecAttributes": {"aaa": "aaaaaaaaaaaaaa+aaaa", "aaaa": "2XLaa12aa-13aaaa"}, "Price": 95.4, "OriginalPrice": 115, "AmountOnSale": 126, "ImageUrl": "http://taaaaimg.yangkeduo.com/goods/images/2aa18-aa8-28/415bed99d5a925123d7b7c395472aa5de.jpeg"}, {"SkuId": "571143579aa2", "Key": "1215:11155aa1666;1226:33656", "SpecAttributes": {"aaa": "aaaaaaaaaaaaaa+aaaa", "aaaa": "3XLaa13aa-14aaaa"}, "Price": 95.4, "OriginalPrice": 115, "AmountOnSale": 89, "ImageUrl": "http://taaaaimg.yangkeduo.com/goods/images/2aa18-aa8-28/415bed99d5a925123d7b7c395472aa5de.jpeg"}, {"SkuId": "1aa38511aa7855", "Key": "1215:1aa53aa9781;1226:119128", "SpecAttributes": {"aaa": "aaaaaaaaaaaaaa+aaaa", "aaaa": "Saa9aaaaaaa\\u00a3a"}, "Price": 95.4, "OriginalPrice": 115, "AmountOnSale": 98, "ImageUrl": "http://taaaaimg.yangkeduo.com/goods/images/2aa18-11-17/48b3aa664faaf7e526ab1b956e813f25cf.jpeg"}, {"SkuId": "1aa38511aa7856", "Key": "1215:1aa53aa9781;1226:96784", "SpecAttributes": {"aaa": "aaaaaaaaaaaaaa+aaaa", "aaaa": "Maa9aa-1aaaaa"}, "Price": 95.4, "OriginalPrice": 115, "AmountOnSale": 95, "ImageUrl": "http://taaaaimg.yangkeduo.com/goods/images/2aa18-11-17/366aab7893ca5cda1ffcea745d1aab25aa6.jpeg"}, {"SkuId": "1aa38511aa7857", "Key": "1215:1aa53aa9781;1226:33651", "SpecAttributes": {"aaa": "aaaaaaaaaaaaaa+aaaa", "aaaa": "Laa1aaaa-11aaaa"}, "Price": 95.4, "OriginalPrice": 115, "AmountOnSale": 97, "ImageUrl": "http://taaaaimg.yangkeduo.com/goods/images/2aa18-11-17/a4a5a671afbb2d8e1f4c21caaced3bea8.jpeg"}, {"SkuId": "1aa38511aa7858", "Key": "1215:1aa53aa9781;1226:33652", "SpecAttributes": {"aaa": "aaaaaaaaaaaaaa+aaaa", "aaaa": "XLaa11aa-12aaaa"}, "Price": 95.4, "OriginalPrice": 115, "AmountOnSale": 97, "ImageUrl": "http://taaaaimg.yangkeduo.com/goods/images/2aa18-11-17/faa551176629bf81f25757c16aa198dba1.jpeg"}, {"SkuId": "1aa38511aa7853", "Key": "1215:1aa53aa9781;1226:33653", "SpecAttributes": {"aaa": "aaaaaaaaaaaaaa+aaaa", "aaaa": "2XLaa12aa-13aaaa"}, "Price": 95.4, "OriginalPrice": 115, "AmountOnSale": 99, "ImageUrl": "http://taaaaimg.yangkeduo.com/goods/images/2aa18-11-17/8aa1a2e2314aabfe76229faa2aabaaa8a5fe.jpeg"}, {"SkuId": "1aa38511aa7854", "Key": "1215:1aa53aa9781;1226:33656", "SpecAttributes": {"aaa": "aaaaaaaaaaaaaa+aaaa", "aaaa": "3XLaa13aa-14aaaa"}, "Price": 95.4, "OriginalPrice": 115, "AmountOnSale": 100, "ImageUrl": "http://taaaaimg.yangkeduo.com/goods/images/2aa18-11-17/2f2aa2934f16faaeee41257b77bf489262.jpeg"}]	2025-08-06 16:00:13.526895	2025-08-06 16:00:13.526898
\.


--
-- Data for Name: report_progress; Type: TABLE DATA; Schema: public; Owner: admin
--

COPY public.report_progress (group_id, run_id, component, report, date, processed, module, label) FROM stdin;
\.


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: admin
--

COPY public.users (id, username, password, role_id, dates, phone, created_at, updated_at) FROM stdin;
84451413593	admin	admin123	123456789	2023-12-31 00:00:00	13800000000	2025-08-06 16:00:47.62783	2025-08-06 16:00:47.627831
46323013642	user1	user123	123456	2023-12-31 00:00:00	13800000001	2025-08-06 16:00:47.628661	2025-08-06 16:00:47.628662
73238329686	user2	user123	123456	2023-12-31 00:00:00	13800000002	2025-08-06 16:00:47.62924	2025-08-06 16:00:47.629242
27613096516	user3	user123	123456	2023-12-31 00:00:00	13800000003	2025-08-06 16:00:47.629625	2025-08-06 16:00:47.629625
85551557264	user4	user123	123456	2023-12-31 00:00:00	13800000004	2025-08-06 16:00:47.630012	2025-08-06 16:00:47.630013
\.


--
-- Name: auto_baseurl_id_seq; Type: SEQUENCE SET; Schema: public; Owner: admin
--

SELECT pg_catalog.setval('public.auto_baseurl_id_seq', 4, true);


--
-- Name: auto_case_id_seq; Type: SEQUENCE SET; Schema: public; Owner: admin
--

SELECT pg_catalog.setval('public.auto_case_id_seq', 1, false);


--
-- Name: auto_configuration_id_seq; Type: SEQUENCE SET; Schema: public; Owner: admin
--

SELECT pg_catalog.setval('public.auto_configuration_id_seq', 2, true);


--
-- Name: auto_endpoint_all_id_seq; Type: SEQUENCE SET; Schema: public; Owner: admin
--

SELECT pg_catalog.setval('public.auto_endpoint_all_id_seq', 1, false);


--
-- Name: auto_endpoint_id_seq; Type: SEQUENCE SET; Schema: public; Owner: admin
--

SELECT pg_catalog.setval('public.auto_endpoint_id_seq', 1, true);


--
-- Name: auto_progress_id_seq; Type: SEQUENCE SET; Schema: public; Owner: admin
--

SELECT pg_catalog.setval('public.auto_progress_id_seq', 47, true);


--
-- Name: auto_system_variable_id_seq; Type: SEQUENCE SET; Schema: public; Owner: admin
--

SELECT pg_catalog.setval('public.auto_system_variable_id_seq', 1, true);


--
-- Name: auto_testngresult_id_seq; Type: SEQUENCE SET; Schema: public; Owner: admin
--

SELECT pg_catalog.setval('public.auto_testngresult_id_seq', 49, true);


--
-- Name: auto_baseurl auto_baseurl_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.auto_baseurl
    ADD CONSTRAINT auto_baseurl_pkey PRIMARY KEY (id);


--
-- Name: auto_case auto_case_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.auto_case
    ADD CONSTRAINT auto_case_pkey PRIMARY KEY (id);


--
-- Name: auto_configuration auto_configuration_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.auto_configuration
    ADD CONSTRAINT auto_configuration_pkey PRIMARY KEY (id);


--
-- Name: auto_endpoint_all auto_endpoint_all_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.auto_endpoint_all
    ADD CONSTRAINT auto_endpoint_all_pkey PRIMARY KEY (id);


--
-- Name: auto_endpoint auto_endpoint_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.auto_endpoint
    ADD CONSTRAINT auto_endpoint_pkey PRIMARY KEY (id);


--
-- Name: auto_progress auto_progress_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.auto_progress
    ADD CONSTRAINT auto_progress_pkey PRIMARY KEY (id);


--
-- Name: auto_system_variable auto_system_variable_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.auto_system_variable
    ADD CONSTRAINT auto_system_variable_pkey PRIMARY KEY (id);


--
-- Name: auto_testngresult auto_testngresult_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.auto_testngresult
    ADD CONSTRAINT auto_testngresult_pkey PRIMARY KEY (id);


--
-- Name: carriers carriers_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.carriers
    ADD CONSTRAINT carriers_pkey PRIMARY KEY (id);


--
-- Name: materials materials_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.materials
    ADD CONSTRAINT materials_pkey PRIMARY KEY (id);


--
-- Name: orders orders_order_number_key; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.orders
    ADD CONSTRAINT orders_order_number_key UNIQUE (order_number);


--
-- Name: orders orders_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.orders
    ADD CONSTRAINT orders_pkey PRIMARY KEY (id);


--
-- Name: products products_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.products
    ADD CONSTRAINT products_pkey PRIMARY KEY (id);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: users users_username_key; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_username_key UNIQUE (username);


--
-- Name: idx_component_auto_progress; Type: INDEX; Schema: public; Owner: admin
--

CREATE INDEX idx_component_auto_progress ON public.auto_progress USING btree (component);


--
-- Name: idx_run_id_auto_progress; Type: INDEX; Schema: public; Owner: admin
--

CREATE INDEX idx_run_id_auto_progress ON public.auto_progress USING btree (runid);


--
-- PostgreSQL database dump complete
--

