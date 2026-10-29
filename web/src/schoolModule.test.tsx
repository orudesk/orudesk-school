import { render, screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { App, buildRoutes, coreModule, createServices } from '@orudesk/web-core';
import { fakeApi, json, tokens } from '@orudesk/web-core/testing';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { schoolModule } from './schoolModule';

// An admissions officer at one school: the role is a grant at North, not tenant-wide
const officer = {
  id: 'u1',
  tenantId: 't1',
  email: 'meera@sunrise.edu',
  roles: ['USER'],
  grants: [{ role: 'USER' }, { role: 'ADMISSIONS_OFFICER', orgUnitId: 'north' }],
  enabled: true,
};
const tree = {
  id: 'group',
  name: 'Sunrise Group',
  code: 'SG',
  type: 'GROUP',
  children: [{ id: 'north', name: 'Sunrise North', code: 'N', type: 'SCHOOL', children: [] }],
};
const north = {
  id: 'north',
  name: 'Sunrise North',
  code: 'N',
  type: 'SCHOOL',
  roles: ['ADMISSIONS_OFFICER'],
};
const anika = {
  id: 's1',
  schoolId: 'north',
  firstName: 'Anika',
  lastName: 'Nair',
  admissionNumber: '2026/001',
  className: '5',
  section: 'A',
};

function openAt(path: string, handlers: Parameters<typeof fakeApi>[0]) {
  window.history.pushState({}, '', path);
  const api = fakeApi({
    'POST /api/v1/auth/refresh': () => tokens(),
    'GET /api/v1/users/me': () => json(officer),
    'GET /api/v1/org-units': () => json(tree),
    'GET /api/v1/org-units/current': () => json(north),
    ...handlers,
  });
  render(<App services={createServices()} modules={[coreModule, schoolModule]} />);
  return api;
}

describe('the school edition', () => {
  beforeEach(() => localStorage.clear());
  afterEach(() => vi.unstubAllGlobals());

  it("fits next to the core's screens", () => {
    expect(() => buildRoutes([coreModule, schoolModule])).not.toThrow();
  });

  it('shows an officer at one school the Students menu and their students, by class', async () => {
    const api = openAt('/students', {
      'GET /api/v1/students': () => json([anika]),
    });

    const link = await screen.findByRole('link', { name: 'Nair, Anika' });
    expect(link).toHaveAttribute('href', '/students/s1');
    expect(within(link.closest('tr')!).getByText('5 A')).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Students' })).toBeInTheDocument();
    expect(screen.queryByRole('link', { name: 'Users' })).not.toBeInTheDocument();

    await userEvent.type(screen.getByLabelText(/Class/), '5');
    await vi.waitFor(() =>
      expect(api.requests.some((r) => new URL(r.url).searchParams.get('className') === '5')).toBe(
        true,
      ),
    );
  });

  it('asks for a school when the current unit is not one', async () => {
    openAt('/students/admit', {
      'GET /api/v1/org-units/current': () =>
        json({ id: 'group', name: 'Sunrise Group', code: 'SG', type: 'GROUP', roles: [] }),
    });

    expect(await screen.findByRole('status')).toHaveTextContent(
      'Students are admitted at a school.',
    );
    expect(screen.queryByRole('button', { name: 'Admit' })).not.toBeInTheDocument();
  });

  it('admits a child with two guardians at the current school, showing the server’s messages', async () => {
    let attempt = 0;
    const api = openAt('/students/admit', {
      'POST /api/v1/admissions': () =>
        ++attempt === 1
          ? json(
              {
                title: 'Validation failed',
                errors: {
                  'guardians[1].identified': 'give existingPersonId, or firstName and lastName',
                },
              },
              400,
            )
          : json({ id: 's2' }, 201),
      'GET /api/v1/students/s2': () =>
        json({
          ...anika,
          id: 's2',
          personId: 'p-child',
          dateOfBirth: '2016-06-01',
          admittedOn: '2026-10-26',
          guardians: [
            {
              personId: 'p-m',
              firstName: 'Lakshmi',
              lastName: 'Nair',
              phone: null,
              relationship: 'MOTHER',
              primary: false,
            },
            {
              personId: 'p-f',
              firstName: 'Suresh',
              lastName: 'Nair',
              phone: '+91 98860 00002',
              relationship: 'FATHER',
              primary: true,
            },
          ],
        }),
    });

    expect(await screen.findByText('Sunrise North', { selector: 'strong' })).toBeInTheDocument();
    await userEvent.type(screen.getAllByLabelText(/First name/)[0]!, 'Anika'); // the child's comes first
    await userEvent.type(screen.getAllByLabelText(/Last name/)[0]!, 'Nair');
    await userEvent.type(screen.getByLabelText(/Date of birth/), '2016-06-01');
    await userEvent.type(screen.getByLabelText(/Admission no/), '2026/002');
    await userEvent.type(screen.getByLabelText(/^Class/), '5');
    const first = screen.getByRole('group', { name: 'Guardian 1' });
    await userEvent.type(within(first).getByLabelText(/First name/), 'Lakshmi');
    await userEvent.type(within(first).getByLabelText(/Last name/), 'Nair');
    await userEvent.click(screen.getByRole('button', { name: 'Add guardian' }));
    const second = screen.getByRole('group', { name: 'Guardian 2' });
    await userEvent.click(within(second).getByRole('radio'));

    await userEvent.click(screen.getByRole('button', { name: 'Admit' }));
    expect(await within(second).findByText('Give a first and last name')).toBeInTheDocument();

    await userEvent.type(within(second).getByLabelText(/First name/), 'Suresh');
    await userEvent.type(within(second).getByLabelText(/Last name/), 'Nair');
    await userEvent.click(screen.getByRole('button', { name: 'Admit' }));

    expect(await screen.findByRole('heading', { name: 'Anika Nair' })).toBeInTheDocument();
    expect(screen.getByText('Primary contact')).toBeInTheDocument();
    const sent = api.requests.filter((r) => r.method === 'POST').at(-1)!;
    expect(sent.headers.get('X-Org-Unit')).toBe('north');
    expect(await sent.json()).toMatchObject({
      admissionNumber: '2026/002',
      className: '5',
      section: null,
      guardians: [
        { firstName: 'Lakshmi', relationship: 'MOTHER', primary: false },
        { firstName: 'Suresh', relationship: 'FATHER', primary: true },
      ],
    });
  });

  describe('editing a student', () => {
    const details = {
      ...anika,
      personId: 'p-child',
      dateOfBirth: '2016-06-01',
      admittedOn: '2026-10-26',
      guardians: [],
      version: 1,
      personVersion: 4,
    };

    it('moves a student to another section, sending both versions', async () => {
      const api = openAt('/students/s1/edit', {
        'GET /api/v1/students/s1': () => json(details),
        'PUT /api/v1/students/s1': () => json({ ...details, section: 'B', version: 2 }),
      });

      const section = await screen.findByLabelText('Section');
      await userEvent.clear(section);
      await userEvent.type(section, 'B');
      await userEvent.click(screen.getByRole('button', { name: 'Save' }));

      expect(await screen.findByText('5 B')).toBeInTheDocument();
      expect(window.location.pathname).toBe('/students/s1');
      const sent = api.requests.find((r) => r.method === 'PUT')!;
      expect(await sent.json()).toEqual({
        version: 1,
        personVersion: 4,
        firstName: 'Anika',
        lastName: 'Nair',
        dateOfBirth: '2016-06-01',
        className: '5',
        section: 'B',
      });
    });

    it('offers a reload when someone else saved the student first', async () => {
      openAt('/students/s1/edit', {
        'GET /api/v1/students/s1': () => json(details),
        'PUT /api/v1/students/s1': () =>
          json(
            { title: 'Edit conflict', detail: 'student s1 was changed', currentVersion: 2 },
            409,
          ),
      });

      await userEvent.click(await screen.findByRole('button', { name: 'Save' }));

      expect(await screen.findByRole('alert')).toHaveTextContent('Changed by someone else');
    });
  });
});
