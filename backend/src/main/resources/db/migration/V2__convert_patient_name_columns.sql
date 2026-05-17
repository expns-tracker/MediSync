DO $$
BEGIN
  IF EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_name='patients' AND column_name='first_name' AND data_type='bytea'
  ) THEN
    RAISE NOTICE 'Converting patients.first_name from bytea to text';
    ALTER TABLE patients ALTER COLUMN first_name TYPE text USING convert_from(first_name, 'UTF8');
  END IF;

  IF EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_name='patients' AND column_name='last_name' AND data_type='bytea'
  ) THEN
    RAISE NOTICE 'Converting patients.last_name from bytea to text';
    ALTER TABLE patients ALTER COLUMN last_name TYPE text USING convert_from(last_name, 'UTF8');
  END IF;
END$$;
