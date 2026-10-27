import { Anchor, Badge, Card, Group, Loader, SimpleGrid, Stack, Text, Title } from '@mantine/core';
import { formatDate, useMe, usePageTitle } from '@orudesk/web-core';
import { Link, useParams } from 'react-router';
import { classLabel, relationshipLabel, useStudent } from '../admissions';

export function StudentPage() {
  const { studentId = '' } = useParams();
  const student = useStudent(studentId);
  const isAdmin = useMe().data?.roles.includes('ADMIN') ?? false; // the audit log is for tenant admins
  const name = student.data ? `${student.data.firstName} ${student.data.lastName}` : 'Student';
  usePageTitle(student.data === null ? 'Student not found' : name);

  return (
    <Stack>
      <Anchor component={Link} to="/students" size="sm">
        ← Students
      </Anchor>
      {student.isPending && <Loader size="sm" aria-label="Loading" />}
      {student.isError && <Text role="alert">{student.error.message}</Text>}
      {student.data === null && (
        <>
          <Title order={1}>Student not found</Title>
          {/* The API says 404 for a student at a school that isn't yours, too: it doesn't confirm they exist */}
          <Text>There is no such student at your schools.</Text>
        </>
      )}
      {student.data && (
        <>
          <Title order={1}>{name}</Title>
          <Card withBorder maw={640}>
            <SimpleGrid cols={{ base: 1, xs: 2 }} spacing="md" component="dl" m={0}>
              <Field label="Admission no." value={student.data.admissionNumber} />
              <Field label="Class" value={classLabel(student.data)} />
              <Field label="Date of birth" value={formatDate(student.data.dateOfBirth)} />
              <Field label="Admitted on" value={formatDate(student.data.admittedOn)} />
            </SimpleGrid>
          </Card>

          <Title order={2} size="h3">
            Guardians
          </Title>
          <SimpleGrid cols={{ base: 1, sm: 2 }} maw={640}>
            {student.data.guardians.map((guardian) => (
              <Card withBorder key={guardian.personId}>
                <Group justify="space-between" align="flex-start">
                  <div>
                    <Anchor component={Link} to={`/people/${guardian.personId}`} fw={500}>
                      {guardian.firstName} {guardian.lastName}
                    </Anchor>
                    <Text size="sm" c="dimmed">
                      {relationshipLabel(guardian.relationship)}
                    </Text>
                  </div>
                  {guardian.primary && (
                    <Badge variant="light" tt="none">
                      Primary contact
                    </Badge>
                  )}
                </Group>
                <Text size="sm" mt="xs">
                  {guardian.phone ?? 'No phone'}
                </Text>
              </Card>
            ))}
          </SimpleGrid>
          <Group gap="lg">
            <Anchor component={Link} to={`/people/${student.data.personId}`} size="sm">
              Their entry in the People registry
            </Anchor>
            {isAdmin && (
              <Anchor component={Link} to={`/activity?subject=${student.data.id}`} size="sm">
                History
              </Anchor>
            )}
          </Group>
        </>
      )}
    </Stack>
  );
}

function Field({ label, value }: { label: string; value: string }) {
  return (
    <div>
      <Text component="dt" size="sm" c="dimmed">
        {label}
      </Text>
      <Text component="dd" m={0}>
        {value}
      </Text>
    </div>
  );
}
