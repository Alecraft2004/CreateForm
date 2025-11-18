-- Esquema para FormularioDB compatible con las entidades JPA
-- Si las tablas ya existen, ajusta o elimina este archivo para evitar conflictos

-- Usuario
CREATE SEQUENCE IF NOT EXISTS public.usuario_id_usuario_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE IF NOT EXISTS public.usuario (
    id_usuario      INTEGER PRIMARY KEY DEFAULT nextval('public.usuario_id_usuario_seq'),
    nombre          VARCHAR(100) NOT NULL,
    correo_electronico VARCHAR(150) NOT NULL UNIQUE,
    contrasena      VARCHAR(200) NOT NULL,
    tipo_usuario    VARCHAR(20)  NOT NULL
);

-- Encuesta
CREATE SEQUENCE IF NOT EXISTS public.encuesta_id_encuesta_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE IF NOT EXISTS public.encuesta (
    id_encuesta     INTEGER PRIMARY KEY DEFAULT nextval('public.encuesta_id_encuesta_seq'),
    titulo          VARCHAR(200) NOT NULL,
    descripcion     TEXT,
    estado          VARCHAR(20),
    es_votacion     BOOLEAN NOT NULL DEFAULT FALSE,
    id_usuario      INTEGER NOT NULL,
    CONSTRAINT fk_encuesta_usuario FOREIGN KEY (id_usuario)
        REFERENCES public.usuario(id_usuario) ON UPDATE CASCADE ON DELETE RESTRICT
);
CREATE INDEX IF NOT EXISTS idx_encuesta_estado ON public.encuesta(estado);
CREATE INDEX IF NOT EXISTS idx_encuesta_usuario ON public.encuesta(id_usuario);

-- Pregunta
CREATE SEQUENCE IF NOT EXISTS public.pregunta_id_pregunta_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE IF NOT EXISTS public.pregunta (
    id_pregunta     INTEGER PRIMARY KEY DEFAULT nextval('public.pregunta_id_pregunta_seq'),
    texto_pregunta  TEXT NOT NULL,
    tipo_pregunta   VARCHAR(50),
    id_encuesta     INTEGER NOT NULL,
    CONSTRAINT fk_pregunta_encuesta FOREIGN KEY (id_encuesta)
        REFERENCES public.encuesta(id_encuesta) ON UPDATE CASCADE ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_pregunta_encuesta ON public.pregunta(id_encuesta);

-- Opcion de Pregunta
CREATE SEQUENCE IF NOT EXISTS public.opcion_encuesta_id_opcion_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE IF NOT EXISTS public.opcion_encuesta (
    id_opcion       INTEGER PRIMARY KEY DEFAULT nextval('public.opcion_encuesta_id_opcion_seq'),
    texto_opcion    VARCHAR(200) NOT NULL,
    id_pregunta     INTEGER NOT NULL,
    CONSTRAINT fk_opcion_pregunta FOREIGN KEY (id_pregunta)
        REFERENCES public.pregunta(id_pregunta) ON UPDATE CASCADE ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_opcion_pregunta ON public.opcion_encuesta(id_pregunta);

-- Añadir columna es_votacion si la tabla ya existía sin esta columna
-- Migración idempotente sin usar DO $$
ALTER TABLE public.encuesta
    ADD COLUMN IF NOT EXISTS es_votacion BOOLEAN NOT NULL DEFAULT FALSE;
CREATE INDEX IF NOT EXISTS idx_encuesta_es_votacion ON public.encuesta(es_votacion);
