CREATE TABLE CONFIGURACION_SEGURIDAD (
    ConfiguracionSeguridadId INT IDENTITY(1,1) PRIMARY KEY,
    EmpresaId                 INT NOT NULL,
    PasswordMinLength          INT NOT NULL DEFAULT 8,
    RequireUppercase            BIT NOT NULL DEFAULT 1,
    RequireDigit                 BIT NOT NULL DEFAULT 1,
    RequireSpecial                BIT NOT NULL DEFAULT 0,
    SessionExpirationMinutes       INT NOT NULL DEFAULT 30,
    FechaActualizacion               DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    CONSTRAINT FK_CONFIGSEGURIDAD_EMPRESA FOREIGN KEY (EmpresaId) REFERENCES EMPRESA (EmpresaId),
    CONSTRAINT UQ_CONFIGSEGURIDAD_EMPRESA UNIQUE (EmpresaId)
);

CREATE TABLE API_KEY (
    ApiKeyId       INT IDENTITY(1,1) PRIMARY KEY,
    EmpresaId       INT NOT NULL,
    Nombre           NVARCHAR(150) NOT NULL,
    Prefijo           NVARCHAR(16) NOT NULL,
    KeyHash            NVARCHAR(128) NOT NULL,
    Activa              BIT NOT NULL DEFAULT 1,
    FechaCreacion        DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    FechaRevocacion       DATETIME2 NULL,
    CONSTRAINT FK_APIKEY_EMPRESA FOREIGN KEY (EmpresaId) REFERENCES EMPRESA (EmpresaId),
    CONSTRAINT UQ_APIKEY_HASH UNIQUE (KeyHash)
);

CREATE INDEX IX_APIKEY_EMPRESA ON API_KEY (EmpresaId);
