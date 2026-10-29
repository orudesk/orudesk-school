import type { OrudeskModule } from '@orudesk/web-core';
import { IconSchool } from '@tabler/icons-react';

/**
 * The school edition's screens. Who sees them matches the API's SchoolAccess: admins, and admissions officers,
 * who are given the role at their school, so the entry asks for `rolesAnywhere` rather than `roles`.
 */
export const schoolModule: OrudeskModule = {
  id: 'school',
  routes: [
    {
      path: '/students',
      lazy: async () => ({ Component: (await import('./pages/StudentsPage')).StudentsPage }),
    },
    {
      path: '/students/admit',
      lazy: async () => ({
        Component: (await import('./pages/AdmitStudentPage')).AdmitStudentPage,
      }),
    },
    {
      path: '/students/:studentId/edit',
      lazy: async () => ({ Component: (await import('./pages/EditStudentPage')).EditStudentPage }),
    },
    {
      path: '/students/:studentId',
      lazy: async () => ({ Component: (await import('./pages/StudentPage')).StudentPage }),
    },
  ],
  navigation: [
    {
      label: 'Students',
      path: '/students',
      icon: <IconSchool size={18} />,
      rolesAnywhere: ['ADMIN', 'ADMISSIONS_OFFICER'],
    },
  ],
};
