import type { MaintenanceStatus, MaintenanceUrgency } from './types';

export const URGENCY_LABEL: Record<MaintenanceUrgency, string> = {
  LOW: 'Laag',
  MEDIUM: 'Gemiddeld',
  HIGH: 'Hoog',
};

export const URGENCY_STYLE: Record<MaintenanceUrgency, string> = {
  LOW: 'bg-stone-100 text-stone-600',
  MEDIUM: 'bg-amber-50 text-amber-700',
  HIGH: 'bg-red-50 text-red-700',
};

export const MAINTENANCE_STATUS_LABEL: Record<MaintenanceStatus, string> = {
  OPEN: 'Open',
  IN_PROGRESS: 'In behandeling',
  RESOLVED: 'Opgelost',
};

export const MAINTENANCE_STATUS_STYLE: Record<MaintenanceStatus, string> = {
  OPEN: 'bg-amber-50 text-amber-700',
  IN_PROGRESS: 'bg-sky-50 text-sky-700',
  RESOLVED: 'bg-emerald-50 text-emerald-700',
};
