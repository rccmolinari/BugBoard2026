--
-- PostgreSQL database dump
--

\restrict G6Dabyf5HYVNsG9gFhw4OnauF4JgPmhvDzU9KXauVoR8WnuEbbl3oXV21NnB9cx

-- Dumped from database version 17.6
-- Dumped by pg_dump version 17.6

-- Started on 2026-05-06 20:34:16

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 852 (class 1247 OID 33200)
-- Name: issuestato; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public.issuestato AS ENUM (
    'TODO',
    'IN_PROGRESS',
    'DONE'
);


ALTER TYPE public.issuestato OWNER TO postgres;

--
-- TOC entry 849 (class 1247 OID 33190)
-- Name: issuetipo; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public.issuetipo AS ENUM (
    'QUESTION',
    'BUG',
    'DOCUMENTATION',
    'FEATURE'
);


ALTER TYPE public.issuetipo OWNER TO postgres;

--
-- TOC entry 861 (class 1247 OID 33239)
-- Name: utenteruolo; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public.utenteruolo AS ENUM (
    'ADMIN',
    'USER',
    'READONLY'
);


ALTER TYPE public.utenteruolo OWNER TO postgres;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 220 (class 1259 OID 33219)
-- Name: issue; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.issue (
    id integer NOT NULL,
    titolo character varying(255) NOT NULL,
    descrizione text NOT NULL,
    priorita integer,
    immagine character varying(500),
    tipo public.issuetipo NOT NULL,
    stato public.issuestato DEFAULT 'TODO'::public.issuestato NOT NULL,
    datascadenza timestamp without time zone,
    etichetta text[],
    commento text[],
    idcreatore integer NOT NULL,
    idassegnatario integer
);


ALTER TABLE public.issue OWNER TO postgres;

--
-- TOC entry 219 (class 1259 OID 33218)
-- Name: issue_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.issue_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.issue_id_seq OWNER TO postgres;

--
-- TOC entry 4921 (class 0 OID 0)
-- Dependencies: 219
-- Name: issue_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.issue_id_seq OWNED BY public.issue.id;


--
-- TOC entry 218 (class 1259 OID 33208)
-- Name: utente; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.utente (
    id integer NOT NULL,
    email character varying(255) NOT NULL,
    password character varying(255) NOT NULL,
    ruolo public.utenteruolo NOT NULL,
    name character varying(100) NOT NULL,
    surname character varying(100) NOT NULL
);


ALTER TABLE public.utente OWNER TO postgres;

--
-- TOC entry 217 (class 1259 OID 33207)
-- Name: utente_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.utente_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.utente_id_seq OWNER TO postgres;

--
-- TOC entry 4922 (class 0 OID 0)
-- Dependencies: 217
-- Name: utente_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.utente_id_seq OWNED BY public.utente.id;


--
-- TOC entry 4757 (class 2604 OID 33222)
-- Name: issue id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.issue ALTER COLUMN id SET DEFAULT nextval('public.issue_id_seq'::regclass);


--
-- TOC entry 4756 (class 2604 OID 33211)
-- Name: utente id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.utente ALTER COLUMN id SET DEFAULT nextval('public.utente_id_seq'::regclass);


--
-- TOC entry 4915 (class 0 OID 33219)
-- Dependencies: 220
-- Data for Name: issue; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.issue (id, titolo, descrizione, priorita, immagine, tipo, stato, datascadenza, etichetta, commento, idcreatore, idassegnatario) FROM stdin;
\.


--
-- TOC entry 4913 (class 0 OID 33208)
-- Dependencies: 218
-- Data for Name: utente; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.utente (id, email, password, ruolo, name, surname) FROM stdin;
\.


--
-- TOC entry 4923 (class 0 OID 0)
-- Dependencies: 219
-- Name: issue_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.issue_id_seq', 1, false);


--
-- TOC entry 4924 (class 0 OID 0)
-- Dependencies: 217
-- Name: utente_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.utente_id_seq', 1, false);


--
-- TOC entry 4764 (class 2606 OID 33227)
-- Name: issue issue_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.issue
    ADD CONSTRAINT issue_pkey PRIMARY KEY (id);


--
-- TOC entry 4760 (class 2606 OID 33217)
-- Name: utente utente_email_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.utente
    ADD CONSTRAINT utente_email_key UNIQUE (email);


--
-- TOC entry 4762 (class 2606 OID 33215)
-- Name: utente utente_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.utente
    ADD CONSTRAINT utente_pkey PRIMARY KEY (id);


--
-- TOC entry 4765 (class 2606 OID 33250)
-- Name: issue issue_idassegnatario_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.issue
    ADD CONSTRAINT issue_idassegnatario_fkey FOREIGN KEY (idassegnatario) REFERENCES public.utente(id) ON DELETE SET NULL;


--
-- TOC entry 4766 (class 2606 OID 33245)
-- Name: issue issue_idcreatore_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.issue
    ADD CONSTRAINT issue_idcreatore_fkey FOREIGN KEY (idcreatore) REFERENCES public.utente(id) ON DELETE CASCADE;


-- Completed on 2026-05-06 20:34:16

--
-- PostgreSQL database dump complete
--

\unrestrict G6Dabyf5HYVNsG9gFhw4OnauF4JgPmhvDzU9KXauVoR8WnuEbbl3oXV21NnB9cx

