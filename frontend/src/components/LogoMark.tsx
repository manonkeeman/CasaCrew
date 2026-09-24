import type { SVGProps } from 'react';

const BADGE = '#24483b';
const HOUSE = '#faf7f2';
const ACCENT = '#c96a4d';

export function LogoMark(props: SVGProps<SVGSVGElement>) {
  return (
    <svg viewBox="0 0 48 48" fill="none" {...props}>
      <rect width="48" height="48" rx="12" fill={BADGE} />
      <path
        d="M10 38 L10 20 L16 20 L16 14 L20 14 L20 8 L28 8 L28 14 L32 14 L32 20 L38 20 L38 38 Z"
        fill={HOUSE}
      />
      <rect x="21" y="10" width="6" height="5" fill={ACCENT} />
      <rect x="20" y="30" width="8" height="8" fill={BADGE} />
    </svg>
  );
}
