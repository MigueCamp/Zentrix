# Shared — Contratos Compartidos

Artefactos compartidos entre `backend` y `frontend` para evitar duplicación y mantener consistencia entre la API y la consola web.

## Responsabilidades

- Definir el contrato de la API (OpenAPI/Swagger) generado desde el backend.
- Derivar tipos TypeScript para el frontend a partir de ese contrato.
- Centralizar constantes compartidas (nombres de roles, códigos de eventos/alertas, tipos de acciones remotas) para que backend y frontend no diverjan.

## Estructura prevista

```
shared/
├── api-contract/     # Especificación OpenAPI de la API de Zentrix
├── types/             # Tipos TypeScript generados/derivados del contrato (consumidos por frontend)
└── constants/         # Constantes comunes (roles, tipos de política, tipos de evento)
```

> Este directorio aún no contiene archivos: se documenta primero su propósito; el contrato OpenAPI se generará una vez definidos los endpoints del backend en `02_Arquitectura_del_Sistema.md` y documentos posteriores.
