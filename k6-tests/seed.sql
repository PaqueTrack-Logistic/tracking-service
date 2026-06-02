-- ============================================================
-- SEED: Inserta 20 shipments en estado CREATED para las pruebas
-- Ejecutar ANTES de correr k6
-- Compatible con PostgreSQL
-- ============================================================

INSERT INTO shipment (id, tracking_id, status, created_at, updated_at)
VALUES
  ('00000000-0000-0000-0000-000000000001', 'TRK-001', 'CREATED', NOW(), NOW()),
  ('00000000-0000-0000-0000-000000000002', 'TRK-002', 'CREATED', NOW(), NOW()),
  ('00000000-0000-0000-0000-000000000003', 'TRK-003', 'CREATED', NOW(), NOW()),
  ('00000000-0000-0000-0000-000000000004', 'TRK-004', 'CREATED', NOW(), NOW()),
  ('00000000-0000-0000-0000-000000000005', 'TRK-005', 'CREATED', NOW(), NOW()),
  ('00000000-0000-0000-0000-000000000006', 'TRK-006', 'CREATED', NOW(), NOW()),
  ('00000000-0000-0000-0000-000000000007', 'TRK-007', 'CREATED', NOW(), NOW()),
  ('00000000-0000-0000-0000-000000000008', 'TRK-008', 'CREATED', NOW(), NOW()),
  ('00000000-0000-0000-0000-000000000009', 'TRK-009', 'CREATED', NOW(), NOW()),
  ('00000000-0000-0000-0000-000000000010', 'TRK-010', 'CREATED', NOW(), NOW()),
  ('00000000-0000-0000-0000-000000000011', 'TRK-011', 'CREATED', NOW(), NOW()),
  ('00000000-0000-0000-0000-000000000012', 'TRK-012', 'CREATED', NOW(), NOW()),
  ('00000000-0000-0000-0000-000000000013', 'TRK-013', 'CREATED', NOW(), NOW()),
  ('00000000-0000-0000-0000-000000000014', 'TRK-014', 'CREATED', NOW(), NOW()),
  ('00000000-0000-0000-0000-000000000015', 'TRK-015', 'CREATED', NOW(), NOW()),
  ('00000000-0000-0000-0000-000000000016', 'TRK-016', 'CREATED', NOW(), NOW()),
  ('00000000-0000-0000-0000-000000000017', 'TRK-017', 'CREATED', NOW(), NOW()),
  ('00000000-0000-0000-0000-000000000018', 'TRK-018', 'CREATED', NOW(), NOW()),
  ('00000000-0000-0000-0000-000000000019', 'TRK-019', 'CREATED', NOW(), NOW()),
  ('00000000-0000-0000-0000-000000000020', 'TRK-020', 'CREATED', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- ============================================================
-- Para re-ejecutar el test: resetear estados a CREATED
-- ============================================================
-- DELETE FROM tracking_event
-- WHERE shipment_id::text LIKE '00000000-0000-0000-0000-%';
--
-- UPDATE shipment SET status = 'CREATED', updated_at = NOW()
-- WHERE id::text LIKE '00000000-0000-0000-0000-%';