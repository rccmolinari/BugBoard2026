--
-- PostgreSQL database dump
--

\restrict oS5DryZlepOVNjIHmtJFs4AUG2Tyh08WHMBGRdVNtE7qvRybK8XJKerXxNRUQgD

-- Dumped from database version 17.6
-- Dumped by pg_dump version 17.6

-- Started on 2026-05-12 13:05:01

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
-- TOC entry 854 (class 1247 OID 33200)
-- Name: issuestato; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public.issuestato AS ENUM (
    'TODO',
    'IN_PROGRESS',
    'DONE'
);


ALTER TYPE public.issuestato OWNER TO postgres;

--
-- TOC entry 851 (class 1247 OID 33190)
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
-- TOC entry 863 (class 1247 OID 33239)
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
    idassegnatario integer,
    assegnato_a integer
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
-- TOC entry 4937 (class 0 OID 0)
-- Dependencies: 219
-- Name: issue_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.issue_id_seq OWNED BY public.issue.id;


--
-- TOC entry 222 (class 1259 OID 33261)
-- Name: notifica; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.notifica (
    id integer NOT NULL,
    idissue integer NOT NULL,
    idassegnatario integer NOT NULL,
    assegnato_a integer NOT NULL,
    letta boolean DEFAULT false NOT NULL,
    datacreazione timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.notifica OWNER TO postgres;

--
-- TOC entry 221 (class 1259 OID 33260)
-- Name: notifica_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.notifica_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.notifica_id_seq OWNER TO postgres;

--
-- TOC entry 4938 (class 0 OID 0)
-- Dependencies: 221
-- Name: notifica_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.notifica_id_seq OWNED BY public.notifica.id;


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
-- TOC entry 4939 (class 0 OID 0)
-- Dependencies: 217
-- Name: utente_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.utente_id_seq OWNED BY public.utente.id;


--
-- TOC entry 4762 (class 2604 OID 33222)
-- Name: issue id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.issue ALTER COLUMN id SET DEFAULT nextval('public.issue_id_seq'::regclass);


--
-- TOC entry 4764 (class 2604 OID 33264)
-- Name: notifica id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.notifica ALTER COLUMN id SET DEFAULT nextval('public.notifica_id_seq'::regclass);


--
-- TOC entry 4761 (class 2604 OID 33211)
-- Name: utente id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.utente ALTER COLUMN id SET DEFAULT nextval('public.utente_id_seq'::regclass);


--
-- TOC entry 4929 (class 0 OID 33219)
-- Dependencies: 220
-- Data for Name: issue; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.issue (id, titolo, descrizione, priorita, immagine, tipo, stato, datascadenza, etichetta, commento, idcreatore, idassegnatario, assegnato_a) FROM stdin;
\.


--
-- TOC entry 4931 (class 0 OID 33261)
-- Dependencies: 222
-- Data for Name: notifica; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.notifica (id, idissue, idassegnatario, assegnato_a, letta, datacreazione) FROM stdin;
\.


--
-- TOC entry 4927 (class 0 OID 33208)
-- Dependencies: 218
-- Data for Name: utente; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.utente (id, email, password, ruolo, name, surname) FROM stdin;
\.


--
-- TOC entry 4940 (class 0 OID 0)
-- Dependencies: 219
-- Name: issue_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.issue_id_seq', 1, false);


--
-- TOC entry 4941 (class 0 OID 0)
-- Dependencies: 221
-- Name: notifica_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.notifica_id_seq', 1, false);


--
-- TOC entry 4942 (class 0 OID 0)
-- Dependencies: 217
-- Name: utente_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.utente_id_seq', 1, false);


--
-- TOC entry 4772 (class 2606 OID 33227)
-- Name: issue issue_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.issue
    ADD CONSTRAINT issue_pkey PRIMARY KEY (id);


--
-- TOC entry 4774 (class 2606 OID 33268)
-- Name: notifica notifica_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.notifica
    ADD CONSTRAINT notifica_pkey PRIMARY KEY (id);


--
-- TOC entry 4768 (class 2606 OID 33217)
-- Name: utente utente_email_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.utente
    ADD CONSTRAINT utente_email_key UNIQUE (email);


--
-- TOC entry 4770 (class 2606 OID 33215)
-- Name: utente utente_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.utente
    ADD CONSTRAINT utente_pkey PRIMARY KEY (id);


--
-- TOC entry 4778 (class 2606 OID 33274)
-- Name: notifica fk_notifica_assegnatario; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.notifica
    ADD CONSTRAINT fk_notifica_assegnatario FOREIGN KEY (idassegnatario) REFERENCES public.utente(id) ON DELETE CASCADE;


--
-- TOC entry 4779 (class 2606 OID 33279)
-- Name: notifica fk_notifica_assegnato_a; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.notifica
    ADD CONSTRAINT fk_notifica_assegnato_a FOREIGN KEY (assegnato_a) REFERENCES public.utente(id) ON DELETE CASCADE;


--
-- TOC entry 4780 (class 2606 OID 33269)
-- Name: notifica fk_notifica_issue; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.notifica
    ADD CONSTRAINT fk_notifica_issue FOREIGN KEY (idissue) REFERENCES public.issue(id) ON DELETE CASCADE;


--
-- TOC entry 4775 (class 2606 OID 33255)
-- Name: issue issue_assegnato_a_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.issue
    ADD CONSTRAINT issue_assegnato_a_fkey FOREIGN KEY (assegnato_a) REFERENCES public.utente(id) ON DELETE SET NULL;


--
-- TOC entry 4776 (class 2606 OID 33250)
-- Name: issue issue_idassegnatario_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.issue
    ADD CONSTRAINT issue_idassegnatario_fkey FOREIGN KEY (idassegnatario) REFERENCES public.utente(id) ON DELETE SET NULL;


--
-- TOC entry 4777 (class 2606 OID 33245)
-- Name: issue issue_idcreatore_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.issue
    ADD CONSTRAINT issue_idcreatore_fkey FOREIGN KEY (idcreatore) REFERENCES public.utente(id) ON DELETE CASCADE;


-- Completed on 2026-05-12 13:05:02

--
-- PostgreSQL database dump complete
--

\unrestrict oS5DryZlepOVNjIHmtJFs4AUG2Tyh08WHMBGRdVNtE7qvRybK8XJKerXxNRUQgD

