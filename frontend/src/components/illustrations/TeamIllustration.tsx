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
const SKIN_B = '#ad6a43';
const STONE = '#57534e';

export function TeamIllustration(props: SVGProps<SVGSVGElement>) {
  return (
    <svg viewBox="0 0 420 320" fill="none" {...props}>
      <circle cx="120" cy="130" r="110" fill={GREEN_LIGHT} opacity="0.25" />
      <circle cx="300" cy="90" r="70" fill={CLAY_LIGHT} opacity="0.25" />

      <line x1="20" y1="284" x2="400" y2="284" stroke={INK} strokeWidth="3" strokeLinecap="round" opacity="0.3" />

      {/* Grachtenpand */}
      <path
        d="M232 140 L232 120 L252 120 L252 105 L272 105 L272 90 L302 90 L302 75 L332 75 L332 90 L352 90 L352 105 L372 105 L372 120 L392 120 L392 140"
        stroke={INK}
        strokeWidth="3.5"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      <rect x="232" y="140" width="160" height="144" fill={SAND} stroke={INK} strokeWidth="3.5" strokeLinejoin="round" />
      <rect x="296" y="95" width="32" height="24" rx="2" fill={CLAY_LIGHT} opacity="0.5" stroke={INK} strokeWidth="2.5" />

      <rect x="252" y="160" width="30" height="34" rx="2" fill={CLAY_LIGHT} opacity="0.45" stroke={INK} strokeWidth="2.5" />
      <rect x="342" y="160" width="30" height="34" rx="2" fill={CLAY_LIGHT} opacity="0.45" stroke={INK} strokeWidth="2.5" />
      <rect x="252" y="222" width="28" height="30" rx="2" fill={CLAY_LIGHT} opacity="0.45" stroke={INK} strokeWidth="2.5" />
      <rect x="344" y="222" width="28" height="30" rx="2" fill={CLAY_LIGHT} opacity="0.45" stroke={INK} strokeWidth="2.5" />
      <rect x="298" y="212" width="32" height="72" rx="2" fill={GREEN_DARK} stroke={INK} strokeWidth="2.5" />
      <circle cx="322" cy="250" r="2.5" fill={SAND} />

      {/* Plant */}
      <path d="M46 284 L52 258 L60 284 Z" fill={CLAY} />
      <circle cx="53" cy="248" r="9" fill={GREEN_MID} />
      <circle cx="44" cy="256" r="7" fill={GREEN_LIGHT} />
      <circle cx="63" cy="256" r="7" fill={GREEN_LIGHT} />

      {/* Persoon 1: draagt een verhuisdoos */}
      <rect x="76" y="216" width="10" height="42" rx="3" fill={INK} opacity="0.85" />
      <rect x="96" y="216" width="10" height="42" rx="3" fill={INK} opacity="0.85" />
      <rect x="70" y="252" width="20" height="8" rx="3" fill={INK} />
      <rect x="92" y="252" width="20" height="8" rx="3" fill={INK} />
      <rect x="66" y="152" width="46" height="68" rx="18" fill={CLAY} />
      <rect x="54" y="168" width="46" height="38" rx="8" fill={GREEN_PALE} stroke={INK} strokeWidth="2.5" />
      <path d="M62 168 L70 152 M92 168 L84 152" stroke={INK} strokeWidth="2.5" strokeLinecap="round" />
      <circle cx="89" cy="130" r="18" fill={SKIN_A} />
      <path d="M69 132 A20 20 0 0 1 109 132 Z" fill={GREEN_DARK} />

      {/* Persoon 2: zit op een koffer, welkomstgebaar */}
      <rect x="148" y="240" width="56" height="30" rx="5" fill={GREEN_MID} stroke={INK} strokeWidth="2.5" />
      <rect x="168" y="232" width="16" height="10" rx="3" fill={GREEN_MID} stroke={INK} strokeWidth="2.5" />
      <rect x="156" y="222" width="14" height="34" rx="6" fill={STONE} />
      <rect x="182" y="222" width="14" height="34" rx="6" fill={STONE} />
      <rect x="150" y="170" width="52" height="60" rx="20" fill={GREEN_DARK} />
      <circle cx="147" cy="196" r="9" fill={SKIN_B} />
      <rect x="196" y="188" width="22" height="9" rx="4.5" fill={SKIN_B} transform="rotate(-18 196 188)" />
      <circle cx="176" cy="154" r="17" fill={SKIN_B} />
      <path d="M157 156 A19 19 0 0 1 195 156 Z" fill="#2b241c" />
      <circle cx="204" cy="180" r="6" fill={CLAY_LIGHT} />
    </svg>
  );
}
