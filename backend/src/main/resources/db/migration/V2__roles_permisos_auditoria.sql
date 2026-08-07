CREATE TABLE ROL (
    RolId          INT IDENTITY(1,1) PRIMARY KEY,
    EmpresaId      INT NOT NULL,
    Nombre         NVARCHAR(150) NOT NULL,
    CONSTRAINT FK_ROL_EMPRESA FOREIGN KEY (EmpresaId) REFERENCES EMPRESA (EmpresaId),
    CONSTRAINT UQ_ROL_EMPRESA_NOMBRE UNIQUE (EmpresaId, Nombre)
);

CREATE TABLE PERMISO (
    PermisoId      INT IDENTITY(1,1) PRIMARY KEY,
    Codigo         NVARCHAR(100) NOT NULL,
    Descripcion    NVARCHAR(255) NOT NULL,
    CONSTRAINT UQ_PERMISO_CODIGO UNIQUE (Codigo)
);

CREATE TABLE USUARIO_ROL (
    UsuarioId      INT NOT NULL,
    RolId          INT NOT NULL,
    CONSTRAINT PK_USUARIO_ROL PRIMARY KEY (UsuarioId, RolId),
    CONSTRAINT FK_USUARIOROL_USUARIO FOREIGN KEY (UsuarioId) REFERENCES USUARIO (UsuarioId),
    CONSTRAINT FK_USUARIOROL_ROL FOREIGN KEY (RolId) REFERENCES ROL (RolId)
);

CREATE TABLE ROL_PERMISO (
    RolId          INT NOT NULL,
    PermisoId      INT NOT NULL,
    CONSTRAINT PK_ROL_PERMISO PRIMARY KEY (RolId, PermisoId),
    CONSTRAINT FK_ROLPERMISO_ROL FOREIGN KEY (RolId) REFERENCES ROL (RolId),
    CONSTRAINT FK_ROLPERMISO_PERMISO FOREIGN KEY (PermisoId) REFERENCES PERMISO (PermisoId)
);

CREATE TABLE LOG_AUDITORIA (
    LogAuditoriaId BIGINT IDENTITY(1,1) PRIMARY KEY,
    EmpresaId      INT NULL,
    UsuarioId      INT NOT NULL,
    Accion         NVARCHAR(100) NOT NULL,
    DetalleJson    NVARCHAR(MAX) NULL,
    FechaAccion    DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    CONSTRAINT FK_LOGAUDITORIA_EMPRESA FOREIGN KEY (EmpresaId) REFERENCES EMPRESA (EmpresaId),
    CONSTRAINT FK_LOGAUDITORIA_USUARIO FOREIGN KEY (UsuarioId) REFERENCES USUARIO (UsuarioId)
);

CREATE INDEX IX_LOGAUDITORIA_EMPRESA_FECHA ON LOG_AUDITORIA (EmpresaId, FechaAccion DESC);

-- Catálogo de permisos de plataforma (fijo por código, ver docs/05_Seguridad_y_Cumplimiento.md)
INSERT INTO PERMISO (Codigo, Descripcion) VALUES
    ('USER_MANAGE', 'Crear, editar y asignar roles a usuarios'),
    ('DEVICE_MANAGE', 'Registrar, agrupar y eliminar dispositivos'),
    ('DEVICE_WIPE', 'Ejecutar borrado remoto de un dispositivo'),
    ('POLICY_MANAGE', 'Crear y editar perfiles y políticas'),
    ('POLICY_ASSIGN', 'Asignar políticas a dispositivos o grupos'),
    ('APPLICATION_MANAGE', 'Subir, instalar y desinstalar aplicaciones'),
    ('REPORT_VIEW', 'Ver y exportar reportes'),
    ('CONFIG_MANAGE', 'Editar configuración de seguridad e integraciones');
