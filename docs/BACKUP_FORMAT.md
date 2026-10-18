# Backup format contract (proposal)

This is an independent export format; it does not claim compatibility with Days Matter's unknown backup container.

## Container

UTF-8 ZIP with these entries:

```text
manifest.json
events.json
categories.json
reminders.json
milestones.json
background_images.json
media/<image-id>.<ext>
```

`manifest.json` must include `schemaVersion` (integer), `appVersion`, `exportedAt` (ISO-8601 UTC), `counts`, and a SHA-256 digest for each JSON/media entry. A later implementation should reject path traversal, duplicate entry names, unexpected uncompressed sizes and unsupported schema versions before opening a Room transaction.

## Serialization rules

- IDs remain stable UUID strings; dates are ISO-8601 `yyyy-MM-dd` local dates; instants are ISO-8601 UTC; zone IDs are IANA names.
- Entity property names use lower camel case and JSON booleans/numbers remain native JSON scalar values.
- Media is copied into `media/`; JSON references `media/<id>.<ext>` and preserves crop coordinates, rotation, and original URI only as optional provenance.
- No Android private file paths, alarm request codes, ephemeral next-trigger timestamps, or OS notification permission state are exported. Restore recomputes stable request codes and next triggers from event/reminder configuration.

## Restore semantics

1. Validate ZIP entries, schema version, declared counts, hashes, required keys and duplicate IDs before mutating data.
2. Stage media in a temporary app-private directory; verify decodability and size.
3. In one Room `@Transaction`, apply the selected import mode (replace-all or merge-by-ID), write categories/events/reminders/milestones/image metadata, and preserve referential integrity.
4. On any database error, transaction rollback keeps existing rows unchanged; discard staged media. After commit, move staged media into place and reschedule reminders/refresh widgets.
5. Existing external Photo Picker URIs may no longer be readable after restore; bundled media is the portable source of truth.

No exporter/importer is implemented in this first source drop. This contract is a design artifact only and should get serialization, migration, corrupt-file and transaction tests before UI exposure.
