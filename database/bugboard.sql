--
-- PostgreSQL database dump
--

\restrict M72x0tyZrglSPYZtTQG2HqJG4uUUVYLCx6paCZPsacDRfeuHVcaDmpo25d2BeGX

-- Dumped from database version 17.6
-- Dumped by pg_dump version 17.6

-- Started on 2026-04-22 18:37:10

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
-- TOC entry 855 (class 1247 OID 33200)
-- Name: issuestato; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public.issuestato AS ENUM (
    'TODO',
    'IN_PROGRESS',
    'DONE'
);


ALTER TYPE public.issuestato OWNER TO postgres;

--
-- TOC entry 852 (class 1247 OID 33190)
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
-- TOC entry 849 (class 1247 OID 33182)
-- Name: utenteruolo; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public.utenteruolo AS ENUM (
    'AMMINISTRATORE',
    'UTENTE',
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
-- TOC entry 4917 (class 0 OID 0)
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
    role public.utenteruolo NOT NULL
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
-- TOC entry 4918 (class 0 OID 0)
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
-- TOC entry 4765 (class 2606 OID 33233)
-- Name: issue issue_idassegnatario_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.issue
    ADD CONSTRAINT issue_idassegnatario_fkey FOREIGN KEY (idassegnatario) REFERENCES public.utente(id);


--
-- TOC entry 4766 (class 2606 OID 33228)
-- Name: issue issue_idcreatore_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.issue
    ADD CONSTRAINT issue_idcreatore_fkey FOREIGN KEY (idcreatore) REFERENCES public.utente(id);


-- Completed on 2026-04-22 18:37:11

--
-- PostgreSQL database dump complete
--

\unrestrict M72x0tyZrglSPYZtTQG2HqJG4uUUVYLCx6paCZPsacDRfeuHVcaDmpo25d2BeGX

