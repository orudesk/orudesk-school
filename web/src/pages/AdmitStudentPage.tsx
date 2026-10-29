import {
  Alert,
  Button,
  Card,
  Group,
  Loader,
  Radio,
  Select,
  Stack,
  Text,
  TextInput,
  Title,
} from '@mantine/core';
import {
  useCurrentUnit,
  useCurrentUnitAccess,
  usePageTitle,
  ValidationError,
} from '@orudesk/web-core';
import { IconPlus } from '@tabler/icons-react';
import { useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router';
import type { SchoolSchemas } from '../api';
import { RELATIONSHIPS, useAdmitStudent, type Relationship } from '../admissions';

const MAX_GUARDIANS = 3; // as the API allows
const DEFAULT_RELATIONSHIPS: Relationship[] = ['MOTHER', 'FATHER', 'GUARDIAN'];

/**
 * Admit a child at the school picked in the unit switcher. The page first asks the API what that unit is and
 * what the user may do there, so it can say "pick a school" instead of letting the API refuse the form.
 */
export function AdmitStudentPage() {
  usePageTitle('Admit student');
  const { unitId } = useCurrentUnit();
  const unit = useCurrentUnitAccess(unitId);

  if (unit.isPending) return <Loader size="sm" aria-label="Loading" />;
  const mayAdmit =
    unit.data?.type === 'SCHOOL' &&
    unit.data.roles.some((role) => role === 'ADMIN' || role === 'ADMISSIONS_OFFICER');

  return (
    <Stack maw={640}>
      <Title order={1}>Admit student</Title>
      {unit.data && mayAdmit ? (
        <AdmissionForm schoolId={unit.data.id} schoolName={unit.data.name} />
      ) : (
        <Alert color="blue" role="status" title="Pick a school">
          {unit.data?.type === 'SCHOOL'
            ? `You don't manage admissions at ${unit.data.name}. `
            : 'Students are admitted at a school. '}
          Choose a school where you manage admissions in the switcher at the top.
        </Alert>
      )}
    </Stack>
  );
}

function AdmissionForm({ schoolId, schoolName }: { schoolId: string; schoolName: string }) {
  const admit = useAdmitStudent(schoolId);
  const navigate = useNavigate();
  // Each guardian card has a stable key, so removing the second doesn't move the third's typing into it
  const [guardians, setGuardians] = useState<number[]>([0]);
  const [nextKey, setNextKey] = useState(1);
  const errors = admit.error instanceof ValidationError ? admit.error.fields : {};

  const submit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const text = (name: string) => String(form.get(name) ?? '').trim();
    const optional = (name: string) => text(name) || null;
    const primary = Number(form.get('primary') ?? guardians[0]);
    const admission: SchoolSchemas['AdmitStudentRequest'] = {
      firstName: text('firstName'),
      lastName: text('lastName'),
      dateOfBirth: text('dateOfBirth'),
      admissionNumber: text('admissionNumber'),
      className: text('className'),
      section: optional('section'),
      guardians: guardians.map((key) => ({
        firstName: optional(`g${key}.firstName`),
        lastName: optional(`g${key}.lastName`),
        phone: optional(`g${key}.phone`),
        email: optional(`g${key}.email`),
        relationship: text(`g${key}.relationship`) as Relationship,
        primary: key === primary,
      })),
    };
    admit.mutate(admission, {
      onSuccess: (id) => void navigate(`/students/${id}`, { replace: true }),
    });
  };

  const addGuardian = () => {
    setGuardians([...guardians, nextKey]);
    setNextKey(nextKey + 1);
  };

  return (
    <form onSubmit={submit} noValidate>
      <Stack>
        <Text>
          At <strong>{schoolName}</strong>
        </Text>
        {admit.isError && !(admit.error instanceof ValidationError) && (
          <Alert color="red" role="alert" title="Not admitted">
            {admit.error.message}
          </Alert>
        )}
        <Group grow align="flex-start">
          <TextInput name="firstName" label="First name" withAsterisk error={errors.firstName} />
          <TextInput name="lastName" label="Last name" withAsterisk error={errors.lastName} />
        </Group>
        <Group grow align="flex-start">
          <TextInput
            name="dateOfBirth"
            label="Date of birth"
            type="date"
            withAsterisk
            error={errors.dateOfBirth}
          />
          <TextInput
            name="admissionNumber"
            label="Admission no."
            withAsterisk
            error={errors.admissionNumber}
          />
        </Group>
        <Group grow align="flex-start">
          <TextInput name="className" label="Class" withAsterisk error={errors.className} />
          <TextInput name="section" label="Section" error={errors.section} />
        </Group>

        <Title order={2} size="h3" mt="md">
          Guardians
        </Title>
        {errors.guardians && <Text c="red">{errors.guardians}</Text>}
        {/* One radio group across the cards: exactly one primary contact, as the API requires */}
        <Radio.Group name="primary" defaultValue="0" label="Primary contact">
          <Stack mt="xs">
            {guardians.map((key, index) => {
              const at = (field: string) => errors[`guardians[${index}].${field}`];
              return (
                <Card
                  withBorder
                  key={key}
                  component="fieldset"
                  aria-label={`Guardian ${index + 1}`}
                >
                  <Stack gap="sm">
                    <Group justify="space-between">
                      <Radio
                        value={String(key)}
                        label={`Guardian ${index + 1} is the primary contact`}
                      />
                      {index > 0 && (
                        <Button
                          variant="subtle"
                          size="compact-sm"
                          onClick={() => setGuardians(guardians.filter((k) => k !== key))}
                        >
                          Remove
                        </Button>
                      )}
                    </Group>
                    <Select
                      name={`g${key}.relationship`}
                      label="Relationship"
                      data={RELATIONSHIPS}
                      defaultValue={DEFAULT_RELATIONSHIPS[index] ?? 'GUARDIAN'}
                      allowDeselect={false}
                      maw={220}
                    />
                    <Group grow align="flex-start">
                      <TextInput
                        name={`g${key}.firstName`}
                        label="First name"
                        withAsterisk
                        // "identified": the API's rule that a new guardian has a name (this form never picks
                        // an existing person, so say it in the form's words)
                        error={
                          at('firstName') ?? (at('identified') && 'Give a first and last name')
                        }
                      />
                      <TextInput
                        name={`g${key}.lastName`}
                        label="Last name"
                        withAsterisk
                        error={at('lastName')}
                      />
                    </Group>
                    <Group grow align="flex-start">
                      <TextInput
                        name={`g${key}.phone`}
                        label="Phone"
                        type="tel"
                        error={at('phone')}
                      />
                      <TextInput
                        name={`g${key}.email`}
                        label="Email"
                        type="email"
                        error={at('email')}
                      />
                    </Group>
                  </Stack>
                </Card>
              );
            })}
          </Stack>
        </Radio.Group>
        {guardians.length < MAX_GUARDIANS && (
          <Group>
            <Button variant="default" leftSection={<IconPlus size={16} />} onClick={addGuardian}>
              Add guardian
            </Button>
          </Group>
        )}

        <Group mt="md">
          <Button type="submit" loading={admit.isPending}>
            Admit
          </Button>
          <Button component={Link} to="/students" variant="default">
            Cancel
          </Button>
        </Group>
      </Stack>
    </form>
  );
}
