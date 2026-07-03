package pl.zpo.app.domain.config;

/** Input type of a dynamic domain field — drives both frontend rendering and backend validation. */
public enum FieldType {
    TEXT,
    TEXTAREA,
    NUMBER,
    BOOLEAN,
    DATE,
    SELECT
}
