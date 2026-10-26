import { coreModule, startOrudeskApp } from '@orudesk/web-core';
import { schoolModule } from './schoolModule';

// The school edition: everything the core has, plus admissions
startOrudeskApp([coreModule, schoolModule]);
