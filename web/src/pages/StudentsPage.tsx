import { Anchor, Button, Group, Loader, Stack, Table, Text, TextInput, Title } from '@mantine/core';
import { useDebouncedValue } from '@mantine/hooks';
import { usePageTitle } from '@orudesk/web-core';
import { IconPlus } from '@tabler/icons-react';
import { useState } from 'react';
import { Link } from 'react-router';
import { classLabel, useStudents } from '../admissions';

/** Students at the schools where the user manages admissions, optionally one class. */
export function StudentsPage() {
  usePageTitle('Students');
  const [className, setClassName] = useState('');
  const [debounced] = useDebouncedValue(className.trim(), 300);
  const students = useStudents(debounced);
  const rows = students.data ?? [];

  return (
    <Stack>
      <Group justify="space-between">
        <Title order={1}>Students</Title>
        <Button component={Link} to="/students/admit" leftSection={<IconPlus size={16} />}>
          Admit student
        </Button>
      </Group>
      <TextInput
        label="Class"
        description="Only this class, as the school writes it: 5, LKG, XII"
        value={className}
        onChange={(event) => setClassName(event.currentTarget.value)}
        maw={240}
      />

      {students.isPending && <Loader size="sm" aria-label="Loading" />}
      {students.isError && <Text role="alert">{students.error.message}</Text>}
      {students.isSuccess && rows.length === 0 && (
        <Text>{debounced ? `Nobody is in class ${debounced}.` : 'No students admitted yet.'}</Text>
      )}

      {rows.length > 0 && (
        <Table.ScrollContainer minWidth={480}>
          <Table striped highlightOnHover>
            <Table.Thead>
              <Table.Tr>
                <Table.Th>Name</Table.Th>
                <Table.Th>Admission no.</Table.Th>
                <Table.Th>Class</Table.Th>
              </Table.Tr>
            </Table.Thead>
            <Table.Tbody>
              {rows.map((student) => (
                <Table.Tr key={student.id}>
                  <Table.Td>
                    <Anchor component={Link} to={`/students/${student.id}`}>
                      {student.lastName}, {student.firstName}
                    </Anchor>
                  </Table.Td>
                  <Table.Td>{student.admissionNumber}</Table.Td>
                  <Table.Td>{classLabel(student)}</Table.Td>
                </Table.Tr>
              ))}
            </Table.Tbody>
          </Table>
        </Table.ScrollContainer>
      )}
    </Stack>
  );
}
