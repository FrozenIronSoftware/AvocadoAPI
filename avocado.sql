--
-- PostgreSQL database dump
--

-- Dumped from database version 10.4 (Debian 10.4-2.pgdg90+1)
-- Dumped by pg_dump version 10.6 (Debian 10.6-1.pgdg90+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: avocado; Type: SCHEMA; Schema: -; Owner: root
--

CREATE SCHEMA avocado;


ALTER SCHEMA avocado OWNER TO root;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: episodes; Type: TABLE; Schema: avocado; Owner: root
--

CREATE TABLE avocado.episodes (
    id bigint NOT NULL,
    podcast_id bigint NOT NULL,
    episode_id bigint NOT NULL,
    title character varying(500) NOT NULL,
    description character varying(5000) NOT NULL,
    plays bigint DEFAULT 0,
    type character varying(10),
    url character varying(500),
    guid character varying(500),
    date_released timestamp without time zone
);


ALTER TABLE avocado.episodes OWNER TO root;

--
-- Name: episodes_id_seq; Type: SEQUENCE; Schema: avocado; Owner: root
--

CREATE SEQUENCE avocado.episodes_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE avocado.episodes_id_seq OWNER TO root;

--
-- Name: episodes_id_seq; Type: SEQUENCE OWNED BY; Schema: avocado; Owner: root
--

ALTER SEQUENCE avocado.episodes_id_seq OWNED BY avocado.episodes.id;


--
-- Name: podcasts; Type: TABLE; Schema: avocado; Owner: root
--

CREATE TABLE avocado.podcasts (
    id bigint NOT NULL,
    title character varying(500) NOT NULL,
    image character varying(500) NOT NULL,
    description character varying(5000) NOT NULL,
    plays bigint DEFAULT 0,
    author character varying(500) NOT NULL,
    itunes_id bigint DEFAULT 0 NOT NULL,
    feed_url character varying(500),
    genre character varying(100),
    last_update timestamp without time zone
);


ALTER TABLE avocado.podcasts OWNER TO root;

--
-- Name: podcasts_id_seq; Type: SEQUENCE; Schema: avocado; Owner: root
--

CREATE SEQUENCE avocado.podcasts_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE avocado.podcasts_id_seq OWNER TO root;

--
-- Name: podcasts_id_seq; Type: SEQUENCE OWNED BY; Schema: avocado; Owner: root
--

ALTER SEQUENCE avocado.podcasts_id_seq OWNED BY avocado.podcasts.id;


--
-- Name: tokens; Type: TABLE; Schema: avocado; Owner: root
--

CREATE TABLE avocado.tokens (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    token character varying(100) NOT NULL,
    name character varying(100) NOT NULL
);


ALTER TABLE avocado.tokens OWNER TO root;

--
-- Name: tokens_id_seq; Type: SEQUENCE; Schema: avocado; Owner: root
--

CREATE SEQUENCE avocado.tokens_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE avocado.tokens_id_seq OWNER TO root;

--
-- Name: tokens_id_seq; Type: SEQUENCE OWNED BY; Schema: avocado; Owner: root
--

ALTER SEQUENCE avocado.tokens_id_seq OWNED BY avocado.tokens.id;


--
-- Name: user_favorites; Type: TABLE; Schema: avocado; Owner: root
--

CREATE TABLE avocado.user_favorites (
    id bigint NOT NULL,
    podcast_id bigint NOT NULL,
    user_id bigint NOT NULL,
    date_favorited timestamp without time zone DEFAULT now()
);


ALTER TABLE avocado.user_favorites OWNER TO root;

--
-- Name: user_favorites_id_seq; Type: SEQUENCE; Schema: avocado; Owner: root
--

CREATE SEQUENCE avocado.user_favorites_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE avocado.user_favorites_id_seq OWNER TO root;

--
-- Name: user_favorites_id_seq; Type: SEQUENCE OWNED BY; Schema: avocado; Owner: root
--

ALTER SEQUENCE avocado.user_favorites_id_seq OWNED BY avocado.user_favorites.id;


--
-- Name: user_plays; Type: TABLE; Schema: avocado; Owner: root
--

CREATE TABLE avocado.user_plays (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    podcast_id bigint NOT NULL,
    episode_guid character varying(500),
    "position" integer NOT NULL,
    progress integer NOT NULL
);


ALTER TABLE avocado.user_plays OWNER TO root;

--
-- Name: user_plays_id_seq; Type: SEQUENCE; Schema: avocado; Owner: root
--

CREATE SEQUENCE avocado.user_plays_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE avocado.user_plays_id_seq OWNER TO root;

--
-- Name: user_plays_id_seq; Type: SEQUENCE OWNED BY; Schema: avocado; Owner: root
--

ALTER SEQUENCE avocado.user_plays_id_seq OWNED BY avocado.user_plays.id;


--
-- Name: user_recents; Type: TABLE; Schema: avocado; Owner: root
--

CREATE TABLE avocado.user_recents (
    id bigint NOT NULL,
    podcast_id bigint NOT NULL,
    user_id bigint NOT NULL,
    added timestamp without time zone DEFAULT now()
);


ALTER TABLE avocado.user_recents OWNER TO root;

--
-- Name: user_recents_id_seq; Type: SEQUENCE; Schema: avocado; Owner: root
--

CREATE SEQUENCE avocado.user_recents_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE avocado.user_recents_id_seq OWNER TO root;

--
-- Name: user_recents_id_seq; Type: SEQUENCE OWNED BY; Schema: avocado; Owner: root
--

ALTER SEQUENCE avocado.user_recents_id_seq OWNED BY avocado.user_recents.id;


--
-- Name: users; Type: TABLE; Schema: avocado; Owner: root
--

CREATE TABLE avocado.users (
    id bigint NOT NULL,
    account_level integer DEFAULT 0 NOT NULL,
    email character varying(100),
    password character varying(100)
);


ALTER TABLE avocado.users OWNER TO root;

--
-- Name: users_id_seq; Type: SEQUENCE; Schema: avocado; Owner: root
--

CREATE SEQUENCE avocado.users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE avocado.users_id_seq OWNER TO root;

--
-- Name: users_id_seq; Type: SEQUENCE OWNED BY; Schema: avocado; Owner: root
--

ALTER SEQUENCE avocado.users_id_seq OWNED BY avocado.users.id;


--
-- Name: episodes id; Type: DEFAULT; Schema: avocado; Owner: root
--

ALTER TABLE ONLY avocado.episodes ALTER COLUMN id SET DEFAULT nextval('avocado.episodes_id_seq'::regclass);


--
-- Name: podcasts id; Type: DEFAULT; Schema: avocado; Owner: root
--

ALTER TABLE ONLY avocado.podcasts ALTER COLUMN id SET DEFAULT nextval('avocado.podcasts_id_seq'::regclass);


--
-- Name: tokens id; Type: DEFAULT; Schema: avocado; Owner: root
--

ALTER TABLE ONLY avocado.tokens ALTER COLUMN id SET DEFAULT nextval('avocado.tokens_id_seq'::regclass);


--
-- Name: user_favorites id; Type: DEFAULT; Schema: avocado; Owner: root
--

ALTER TABLE ONLY avocado.user_favorites ALTER COLUMN id SET DEFAULT nextval('avocado.user_favorites_id_seq'::regclass);


--
-- Name: user_plays id; Type: DEFAULT; Schema: avocado; Owner: root
--

ALTER TABLE ONLY avocado.user_plays ALTER COLUMN id SET DEFAULT nextval('avocado.user_plays_id_seq'::regclass);


--
-- Name: user_recents id; Type: DEFAULT; Schema: avocado; Owner: root
--

ALTER TABLE ONLY avocado.user_recents ALTER COLUMN id SET DEFAULT nextval('avocado.user_recents_id_seq'::regclass);


--
-- Name: users id; Type: DEFAULT; Schema: avocado; Owner: root
--

ALTER TABLE ONLY avocado.users ALTER COLUMN id SET DEFAULT nextval('avocado.users_id_seq'::regclass);


--
-- Name: episodes episodes_pkey; Type: CONSTRAINT; Schema: avocado; Owner: root
--

ALTER TABLE ONLY avocado.episodes
    ADD CONSTRAINT episodes_pkey PRIMARY KEY (id);


--
-- Name: podcasts podcasts_pkey; Type: CONSTRAINT; Schema: avocado; Owner: root
--

ALTER TABLE ONLY avocado.podcasts
    ADD CONSTRAINT podcasts_pkey PRIMARY KEY (id);


--
-- Name: tokens tokens_pkey; Type: CONSTRAINT; Schema: avocado; Owner: root
--

ALTER TABLE ONLY avocado.tokens
    ADD CONSTRAINT tokens_pkey PRIMARY KEY (id);


--
-- Name: user_favorites user_favorites_pkey; Type: CONSTRAINT; Schema: avocado; Owner: root
--

ALTER TABLE ONLY avocado.user_favorites
    ADD CONSTRAINT user_favorites_pkey PRIMARY KEY (id);


--
-- Name: user_plays user_plays_pkey; Type: CONSTRAINT; Schema: avocado; Owner: root
--

ALTER TABLE ONLY avocado.user_plays
    ADD CONSTRAINT user_plays_pkey PRIMARY KEY (id);


--
-- Name: user_recents user_recents_pkey; Type: CONSTRAINT; Schema: avocado; Owner: root
--

ALTER TABLE ONLY avocado.user_recents
    ADD CONSTRAINT user_recents_pkey PRIMARY KEY (id);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: avocado; Owner: root
--

ALTER TABLE ONLY avocado.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: episodes_id_uindex; Type: INDEX; Schema: avocado; Owner: root
--

CREATE UNIQUE INDEX episodes_id_uindex ON avocado.episodes USING btree (id);


--
-- Name: podcasts_id_uindex; Type: INDEX; Schema: avocado; Owner: root
--

CREATE UNIQUE INDEX podcasts_id_uindex ON avocado.podcasts USING btree (id);


--
-- Name: tokens_id_uindex; Type: INDEX; Schema: avocado; Owner: root
--

CREATE UNIQUE INDEX tokens_id_uindex ON avocado.tokens USING btree (id);


--
-- Name: user_favorites_id_uindex; Type: INDEX; Schema: avocado; Owner: root
--

CREATE UNIQUE INDEX user_favorites_id_uindex ON avocado.user_favorites USING btree (id);


--
-- Name: user_plays_id_uindex; Type: INDEX; Schema: avocado; Owner: root
--

CREATE UNIQUE INDEX user_plays_id_uindex ON avocado.user_plays USING btree (id);


--
-- Name: user_recents_id_uindex; Type: INDEX; Schema: avocado; Owner: root
--

CREATE UNIQUE INDEX user_recents_id_uindex ON avocado.user_recents USING btree (id);


--
-- Name: users_id_uindex; Type: INDEX; Schema: avocado; Owner: root
--

CREATE UNIQUE INDEX users_id_uindex ON avocado.users USING btree (id);


--
-- PostgreSQL database dump complete
--

