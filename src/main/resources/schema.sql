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

-- Respuesta
CREATE SEQUENCE IF NOT EXISTS public.respuesta_id_respuesta_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE IF NOT EXISTS public.respuesta (
    id_respuesta    INTEGER PRIMARY KEY DEFAULT nextval('public.respuesta_id_respuesta_seq'),
    id_usuario      INTEGER NOT NULL,
    id_pregunta     INTEGER NOT NULL,
    id_opcion       INTEGER,
    respuesta_texto TEXT,
    CONSTRAINT fk_respuesta_usuario FOREIGN KEY (id_usuario)
        REFERENCES public.usuario(id_usuario) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_respuesta_pregunta FOREIGN KEY (id_pregunta)
        REFERENCES public.pregunta(id_pregunta) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_respuesta_opcion FOREIGN KEY (id_opcion)
        REFERENCES public.opcion_encuesta(id_opcion) ON UPDATE CASCADE ON DELETE SET NULL
);
CREATE INDEX IF NOT EXISTS idx_respuesta_usuario ON public.respuesta(id_usuario);
CREATE INDEX IF NOT EXISTS idx_respuesta_pregunta ON public.respuesta(id_pregunta);

-- Resultado Encuesta
CREATE SEQUENCE IF NOT EXISTS public.resultado_encuesta_id_resultado_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE IF NOT EXISTS public.resultado_encuesta (
    id_resultado        INTEGER PRIMARY KEY DEFAULT nextval('public.resultado_encuesta_id_resultado_seq'),
    id_encuesta         INTEGER NOT NULL,
    resumen_estadistico TEXT,
    CONSTRAINT fk_resultado_encuesta FOREIGN KEY (id_encuesta)
        REFERENCES public.encuesta(id_encuesta) ON UPDATE CASCADE ON DELETE CASCADE
);

-- Votacion
CREATE SEQUENCE IF NOT EXISTS public.votacion_id_votacion_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE IF NOT EXISTS public.votacion (
    id_votacion     INTEGER PRIMARY KEY DEFAULT nextval('public.votacion_id_votacion_seq'),
    titulo          VARCHAR(200) NOT NULL,
    estado          VARCHAR(20),
    id_usuario      INTEGER NOT NULL,
    CONSTRAINT fk_votacion_usuario FOREIGN KEY (id_usuario)
        REFERENCES public.usuario(id_usuario) ON UPDATE CASCADE ON DELETE RESTRICT
);

-- Opcion Votacion
CREATE SEQUENCE IF NOT EXISTS public.opcion_votacion_id_opcion_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE IF NOT EXISTS public.opcion_votacion (
    id_opcion       INTEGER PRIMARY KEY DEFAULT nextval('public.opcion_votacion_id_opcion_seq'),
    texto_opcion    VARCHAR(200) NOT NULL,
    id_votacion     INTEGER NOT NULL,
    CONSTRAINT fk_opcion_votacion FOREIGN KEY (id_votacion)
        REFERENCES public.votacion(id_votacion) ON UPDATE CASCADE ON DELETE CASCADE
);

-- Voto
CREATE SEQUENCE IF NOT EXISTS public.voto_id_voto_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE IF NOT EXISTS public.voto (
    id_voto         INTEGER PRIMARY KEY DEFAULT nextval('public.voto_id_voto_seq'),
    id_usuario      INTEGER NOT NULL,
    id_votacion     INTEGER NOT NULL,
    id_opcion       INTEGER NOT NULL,
    CONSTRAINT fk_voto_usuario FOREIGN KEY (id_usuario)
        REFERENCES public.usuario(id_usuario) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_voto_votacion FOREIGN KEY (id_votacion)
        REFERENCES public.votacion(id_votacion) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_voto_opcion FOREIGN KEY (id_opcion)
        REFERENCES public.opcion_votacion(id_opcion) ON UPDATE CASCADE ON DELETE CASCADE
);

-- Resultado Votacion
CREATE SEQUENCE IF NOT EXISTS public.resultado_votacion_id_resultado_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE IF NOT EXISTS public.resultado_votacion (
    id_resultado    INTEGER PRIMARY KEY DEFAULT nextval('public.resultado_votacion_id_resultado_seq'),
    id_votacion     INTEGER NOT NULL,
    conteo_votos    INTEGER DEFAULT 0,
    CONSTRAINT fk_resultado_votacion FOREIGN KEY (id_votacion)
        REFERENCES public.votacion(id_votacion) ON UPDATE CASCADE ON DELETE CASCADE
);

-- Añadir columna es_votacion si la tabla ya existía sin esta columna
-- Migración idempotente sin usar DO $$
ALTER TABLE public.encuesta
    ADD COLUMN IF NOT EXISTS es_votacion BOOLEAN NOT NULL DEFAULT FALSE;
CREATE INDEX IF NOT EXISTS idx_encuesta_es_votacion ON public.encuesta(es_votacion);
