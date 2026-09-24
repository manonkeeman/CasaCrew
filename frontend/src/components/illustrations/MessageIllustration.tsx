import type { SVGProps } from 'react';

const INK = '#24483b';
const GREEN_DARK = '#2f5d4c';
const GREEN_MID = '#47805f';
const GREEN_LIGHT = '#99bfa6';
const GREEN_PALE = '#e0ebe4';
const SAND = '#faf7f2';
const CLAY = '#c96a4d';
const CLAY_LIGHT = '#e8a479';
const SKIN_A = '#e8b48a';
const STONE = '#a8a29e';

export function MessageIllustration(props: SVGProps<SVGSVGElement>) {
  return (
    <svg viewBox="0 0 420 320" fill="none" {...props}>
      <circle cx="330" cy="90" r="95" fill={GREEN_LIGHT} opacity="0.22" />
      <circle cx="80" cy="100" r="70" fill={CLAY_LIGHT} opacity="0.22" />

      {/* Huisje als bestemming */}
      <g>
        <path d="M336 76 L362 52 L388 76 Z" fill={SAND} stroke={INK} strokeWidth="2.5" strokeLinejoin="round" />
        <rect x="342" y="76" width="40" height="34" fill={SAND} stroke={INK} strokeWidth="2.5" />
        <rect x="356" y="90" width="12" height="20" fill={GREEN_DARK} />
      </g>

      {/* Vliegend berichtje */}
      <path
        d="M226 182 C 260 150, 300 130, 330 108"
        stroke={GREEN_MID}
        strokeWidth="2.5"
        strokeDasharray="2 8"
        strokeLinecap="round"
      />
      <g transform="translate(318,104) rotate(-24)">
        <path d="M0 0 L26 6 L2 14 L6 6 Z" fill={CLAY_LIGHT} stroke={INK} strokeWidth="2" strokeLinejoin="round" />
      </g>

      {/* Plant op het bureau */}
      <path d="M84 232 L92 210 L100 232 Z" fill={CLAY} />
      <circle cx="92" cy="202" r="10" fill={GREEN_MID} />
      <circle cx="82" cy="210" r="7" fill={GREEN_LIGHT} />
      <circle cx="102" cy="210" r="7" fill={GREEN_LIGHT} />

      {/* Koffie */}
      <path d="M268 200 Q272 190 268 182" stroke={GREEN_LIGHT} strokeWidth="2.5" strokeLinecap="round" />
      <path d="M280 200 Q284 190 280 182" stroke={GREEN_LIGHT} strokeWidth="2.5" strokeLinecap="round" />
      <rect x="262" y="208" width="24" height="22" rx="3" fill={SAND} stroke={INK} strokeWidth="2.5" />
      <path d="M286 214 q10 0 10 8 q0 8 -10 8" stroke={INK} strokeWidth="2.5" fill="none" />

      {/* Persoon */}
      <path d="M158 188 L134 226" stroke={SKIN_A} strokeWidth="16" strokeLinecap="round" />
      <path d="M222 188 L246 226" stroke={SKIN_A} strokeWidth="16" strokeLinecap="round" />
      <circle cx="134" cy="228" r="9" fill={SKIN_A} />
      <circle cx="246" cy="228" r="9" fill={SKIN_A} />
      <rect x="150" y="166" width="80" height="100" rx="28" fill={CLAY} />
      <circle cx="190" cy="138" r="20" fill={SKIN_A} />
      <path d="M168 140 A22 22 0 0 1 212 140 Z" fill={GREEN_DARK} />

      {/* Bureau */}
      <rect x="40" y="230" width="300" height="16" fill="#e7e5e4" stroke={INK} strokeWidth="2.5" />
      <rect x="40" y="246" width="300" height="52" fill={SAND} stroke={INK} strokeWidth="2.5" />
      <line x1="190" y1="246" x2="190" y2="298" stroke={INK} strokeOpacity="0.25" strokeWidth="2" />
      <rect x="176" y="266" width="28" height="6" rx="3" fill={STONE} />
      <rect x="126" y="266" width="28" height="6" rx="3" fill={STONE} />
      <rect x="226" y="266" width="28" height="6" rx="3" fill={STONE} />

      {/* Laptop met envelop-icoon */}
      <rect x="158" y="220" width="64" height="12" rx="3" fill={STONE} stroke={INK} strokeWidth="2" />
      <rect x="164" y="174" width="52" height="46" rx="3" fill={INK} />
      <rect x="168" y="178" width="44" height="38" rx="2" fill={GREEN_PALE} />
      <rect x="180" y="190" width="20" height="14" rx="1" fill={SAND} stroke={GREEN_DARK} strokeWidth="2" />
      <path d="M180 190 L190 200 L200 190" stroke={GREEN_DARK} strokeWidth="2" fill="none" strokeLinecap="round" />
    </svg>
  );
}
