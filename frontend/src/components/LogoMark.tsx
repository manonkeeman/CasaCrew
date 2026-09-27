import type { SVGProps } from 'react';

export function LogoMark(props: SVGProps<SVGSVGElement>) {
  return (
    <svg viewBox="0 0 32 32" fill="none" {...props}>
      <path
        d="M4 28 L4 14 L9 14 L9 9 L13 9 L13 4 L19 4 L19 9 L23 9 L23 14 L28 14 L28 28 Z"
        fill="currentColor"
      />
    </svg>
  );
}
