import type { SVGProps } from 'react';

export function HouseIllustration(props: SVGProps<SVGSVGElement>) {
  return (
    <svg viewBox="0 0 200 170" fill="none" {...props}>
      <line x1="15" y1="150" x2="185" y2="150" stroke="currentColor" strokeWidth="3" strokeLinecap="round" opacity="0.35" />

      {/* Stepped grachtenpand-gevel */}
      <path
        d="M40 65 L40 42 L58 42 L58 28 L78 28 L78 16 L100 16 L100 28 L120 28 L120 42 L138 42 L138 65"
        stroke="currentColor"
        strokeWidth="3.5"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      <rect x="40" y="65" width="98" height="85" stroke="currentColor" strokeWidth="3.5" strokeLinejoin="round" />

      {/* Deur */}
      <rect x="80" y="112" width="18" height="38" rx="2" fill="currentColor" opacity="0.12" stroke="currentColor" strokeWidth="2.5" />

      {/* Ramen */}
      <rect x="52" y="80" width="16" height="20" rx="1.5" fill="currentColor" opacity="0.12" stroke="currentColor" strokeWidth="2.5" />
      <rect x="110" y="80" width="16" height="20" rx="1.5" fill="currentColor" opacity="0.12" stroke="currentColor" strokeWidth="2.5" />
      <rect x="52" y="112" width="16" height="18" rx="1.5" fill="currentColor" opacity="0.12" stroke="currentColor" strokeWidth="2.5" />
      <rect x="110" y="112" width="16" height="18" rx="1.5" fill="currentColor" opacity="0.12" stroke="currentColor" strokeWidth="2.5" />

      {/* Gevelraam boven */}
      <rect x="90" y="34" width="20" height="16" rx="1.5" fill="currentColor" opacity="0.12" stroke="currentColor" strokeWidth="2.5" />
    </svg>
  );
}
