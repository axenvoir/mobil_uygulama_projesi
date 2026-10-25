import {COLORS, SHADOWS, TYPOGRAPHY, SPACING} from '../src/theme';

describe('theme contract', () => {
  it('exposes soft/medium/heavy shadow levels with RN props', () => {
    expect(Object.keys(SHADOWS).sort()).toEqual(['heavy', 'medium', 'soft']);
    for (const level of ['soft', 'medium', 'heavy'] as const) {
      const shadow = SHADOWS[level];
      expect(typeof shadow.shadowColor).toBe('string');
      expect(shadow.shadowOffset).toEqual(
        expect.objectContaining({width: expect.any(Number), height: expect.any(Number)}),
      );
      expect(typeof shadow.shadowOpacity).toBe('number');
      expect(typeof shadow.shadowRadius).toBe('number');
      expect(typeof shadow.elevation).toBe('number');
    }
  });

  it('exposes required colors', () => {
    for (const key of ['primary', 'background', 'surface', 'text', 'fresh', 'expired']) {
      expect(typeof COLORS[key as keyof typeof COLORS]).toBe('string');
    }
  });

  it('exposes typography and spacing tokens', () => {
    expect(TYPOGRAPHY.h1).toBeGreaterThan(TYPOGRAPHY.body);
    expect(SPACING.md).toBeGreaterThan(SPACING.sm);
  });
});
