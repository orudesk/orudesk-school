import { useServices } from '@orudesk/web-core';
import createClient, { type Client } from 'openapi-fetch';
import { useMemo } from 'react';
import type { components, paths } from './generated/schema';

/** The school edition's JSON shapes by name: `SchoolSchemas['StudentDetails']`. */
export type SchoolSchemas = components['schemas'];

/**
 * A client typed from the school edition's contract (the core's endpoints plus admissions), sending through the
 * core's fetch: the access token, the org unit and the outage banner come with it.
 */
export function useSchoolApi(): Client<paths> {
  const { fetch } = useServices();
  return useMemo(
    () => createClient<paths>({ baseUrl: globalThis.location?.origin ?? '', fetch }),
    [fetch],
  );
}
