import { Alert, Anchor, Button, Group, Loader, Stack, Text, TextInput, Title } from '@mantine/core';
import {
  EditConflictAlert,
  EditConflictError,
  usePageTitle,
  ValidationError,
} from '@orudesk/web-core';
import type { FormEvent } from 'react';
import { Link, useNavigate, useParams } from 'react-router';
import type { SchoolSchemas } from '../api';
import { useEditStudent, useStudent } from '../admissions';

/**
 * Correct a student or move them to another class (Day 50). The child's phone and email aren't on this form:
 * the API keeps them as they are. Guardians are people of their own, edited from the People registry.
 */
export function EditStudentPage() {
  const { studentId = '' } = useParams();
  const student = useStudent(studentId);
  const edit = useEditStudent(studentId);
  const navigate = useNavigate();
  usePageTitle('Edit student');
  const errors = edit.error instanceof ValidationError ? edit.error.fields : {};
  const conflict = edit.error instanceof EditConflictError;

  const reload = async () => {
    await student.refetch();
    edit.reset();
  };

  if (student.isPending) return <Loader size="sm" aria-label="Loading" />;
  if (student.isError) return <Text role="alert">{student.error.message}</Text>;
  if (student.data === null) return <Title order={1}>Student not found</Title>;
  const current = student.data;

  const submit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const text = (name: string) => String(form.get(name) ?? '').trim();
    const changed: SchoolSchemas['EditStudentRequest'] = {
      version: current.version,
      personVersion: current.personVersion,
      firstName: text('firstName'),
      lastName: text('lastName'),
      dateOfBirth: text('dateOfBirth'),
      className: text('className'),
      section: text('section') || null,
    };
    edit.mutate(changed, {
      onSuccess: () => void navigate(`/students/${studentId}`, { replace: true }),
    });
  };

  return (
    <Stack maw={520}>
      <Anchor component={Link} to={`/students/${studentId}`} size="sm">
        ← {current.firstName} {current.lastName}
      </Anchor>
      <Title order={1}>Edit student</Title>
      <Text>Admission no. {current.admissionNumber}</Text>
      {conflict && (
        <EditConflictAlert onReload={() => void reload()} reloading={student.isFetching} />
      )}
      {edit.isError && !conflict && !(edit.error instanceof ValidationError) && (
        <Alert color="red" role="alert" title="Not saved">
          {edit.error.message}
        </Alert>
      )}
      <form key={`${current.version}-${current.personVersion}`} onSubmit={submit} noValidate>
        <Stack>
          <Group grow align="flex-start">
            <TextInput
              name="firstName"
              label="First name"
              withAsterisk
              defaultValue={current.firstName}
              error={errors.firstName}
            />
            <TextInput
              name="lastName"
              label="Last name"
              withAsterisk
              defaultValue={current.lastName}
              error={errors.lastName}
            />
          </Group>
          <TextInput
            name="dateOfBirth"
            label="Date of birth"
            type="date"
            withAsterisk
            maw={200}
            defaultValue={current.dateOfBirth}
            error={errors.dateOfBirth}
          />
          <Group grow align="flex-start">
            <TextInput
              name="className"
              label="Class"
              withAsterisk
              defaultValue={current.className}
              error={errors.className}
            />
            <TextInput
              name="section"
              label="Section"
              defaultValue={current.section ?? ''}
              error={errors.section}
            />
          </Group>
          <Group>
            <Button type="submit" loading={edit.isPending} disabled={conflict}>
              Save
            </Button>
            <Button component={Link} to={`/students/${studentId}`} variant="default">
              Cancel
            </Button>
          </Group>
        </Stack>
      </form>
    </Stack>
  );
}
