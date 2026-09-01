import { BuildingIcon, MegaphoneIcon, SparklesIcon } from '../components/icons';

export const ANNOUNCEMENT_TYPE_LABEL: Record<string, string> = {
  mededeling: 'Mededeling',
  onderhoud: 'Onderhoud',
  evenement: 'Evenement',
};

export const ANNOUNCEMENT_TYPE_ICON: Record<string, typeof MegaphoneIcon> = {
  mededeling: MegaphoneIcon,
  onderhoud: BuildingIcon,
  evenement: SparklesIcon,
};
