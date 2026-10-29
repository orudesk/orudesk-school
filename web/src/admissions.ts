import { saveError } from '@orudesk/web-core';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useSchoolApi, type SchoolSchemas } from './api';

export type Relationship = SchoolSchemas['GuardianRequest']['relationship'];

export const RELATIONSHIPS: { value: Relationship; label: string }[] = [
  { value: 'MOTHER', label: 'Mother' },
  { value: 'FATHER', label: 'Father' },
  { value: 'GRANDPARENT', label: 'Grandparent' },
  { value: 'SIBLING', label: 'Sibling' },
  { value: 'GUARDIAN', label: 'Guardian' },
  { value: 'OTHER', label: 'Other' },
];

export const relationshipLabel = (relationship: Relationship) =>
  RELATIONSHIPS.find((r) => r.value === relationship)?.label ?? relationship;

/** "5" or "5 A": how a school writes a class and its section. */
export const classLabel = (student: { className: string; section?: string | null }) =>
  student.section ? `${student.className} ${student.section}` : student.className;

/**
 * Students at every school where the user manages admissions (GET /api/v1/students), optionally one class.
 * Not paged: the API returns at most a few hundred, one school's worth, sorted by name.
 */
export function useStudents(className: string) {
  const api = useSchoolApi();
  return useQuery({
    queryKey: ['students', 'list', className],
    queryFn: async () => {
      const { data, error } = await api.GET('/api/v1/students', {
        params: { query: className ? { className } : {} },
      });
      if (error) throw new Error('Could not load the students');
      return data;
    },
  });
}

/** One student with their guardians; null when there is no such student here (404). */
export function useStudent(studentId: string) {
  const api = useSchoolApi();
  return useQuery({
    queryKey: ['students', 'one', studentId],
    queryFn: async () => {
      const { data, error, response } = await api.GET('/api/v1/students/{studentId}', {
        params: { path: { studentId } },
      });
      if (response.status === 404 || response.status === 400) return null;
      if (error) throw new Error('Could not load this student');
      return data;
    },
  });
}

/**
 * Admit at `schoolId`, sent as X-Org-Unit explicitly: the school is part of what the user confirmed on the form,
 * so switching units in another tab mustn't change where the child is admitted.
 */
export function useAdmitStudent(schoolId: string) {
  const api = useSchoolApi();
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (admission: SchoolSchemas['AdmitStudentRequest']) => {
      const { data, error, response } = await api.POST('/api/v1/admissions', {
        params: { header: { 'X-Org-Unit': schoolId } },
        body: admission,
      });
      if (data) return data.id;
      // A ValidationError's fields are keyed as the API names them: "admissionNumber", and for a guardian
      // "guardians[1].firstName" or "guardians[1].identified" (no name given)
      throw saveError(response, error, 'The student was not admitted.');
    },
    // The child and new guardians are people in the core's registry too
    onSuccess: () =>
      Promise.all([
        queryClient.invalidateQueries({ queryKey: ['students'] }),
        queryClient.invalidateQueries({ queryKey: ['people'] }),
      ]),
  });
}

/**
 * Saves a student's corrections (PUT, Day 49): the name and date of birth (the person's) and the class and
 * section (the school's), with both versions read. The core's EditConflictError if either was saved in between.
 */
export function useEditStudent(studentId: string) {
  const api = useSchoolApi();
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (student: SchoolSchemas['EditStudentRequest']) => {
      const { data, error, response } = await api.PUT('/api/v1/students/{studentId}', {
        params: { path: { studentId } },
        body: student,
      });
      if (data) return data;
      throw saveError(response, error, 'The changes were not saved.');
    },
    onSuccess: (student) => {
      queryClient.setQueryData(['students', 'one', studentId], student);
      return Promise.all([
        queryClient.invalidateQueries({ queryKey: ['students', 'list'] }),
        queryClient.invalidateQueries({ queryKey: ['people'] }),
        queryClient.invalidateQueries({ queryKey: ['audit'] }),
      ]);
    },
  });
}
