import { SHADOWS } from '../src/theme';

// Regression test: the release build crashed at startup with
// "Cannot read property 'soft' of undefined" because Card.js
// destructured SHADOWS from useResponsive(), which never provides it.
// These tests pin the actual contracts the UI relies on.

describe('theme SHADOWS contract', () => {
  it('exposes soft/medium/heavy shadow levels', () => {
    expect(Object.keys(SHADOWS).sort()).toEqual(['heavy', 'medium', 'soft']);
  });

  it.each(['soft', 'medium', 'heavy'])('SHADOWS.%s contains RN shadow + elevation props', (level) => {
    const shadow = SHADOWS[level];
    expect(shadow).toBeTruthy();
    expect(typeof shadow.shadowColor).toBe('string');
    expect(shadow.shadowOffset).toEqual(expect.objectContaining({ width: expect.any(Number), height: expect.any(Number) }));
    expect(typeof shadow.shadowOpacity).toBe('number');
    expect(typeof shadow.shadowRadius).toBe('number');
    expect(typeof shadow.elevation).toBe('number');
  });
});

describe('useResponsive contract', () => {
  it('does NOT expose SHADOWS or COLORS from the responsive hook', async () => {
    const fs = require('fs');
    const path = require('path');
    const source = fs.readFileSync(path.join(__dirname, '..', 'src', 'hooks', 'useResponsive.js'), 'utf8');
    expect(source).not.toMatch(/SHADOWS/);
    expect(source).not.toMatch(/LIGHT_COLORS/);
    expect(source).not.toMatch(/DARK_COLORS/);
  });

  it('components do not destructure SHADOWS/COLORS from responsive', () => {
    const fs = require('fs');
    const path = require('path');
    const walk = (dir) => fs.readdirSync(dir).flatMap((f) => {
      const p = path.join(dir, f);
      return fs.statSync(p).isDirectory() ? walk(p) : [p];
    });
    const offenders = [];
    for (const file of walk(path.join(__dirname, '..', 'src')).filter((f) => f.endsWith('.js'))) {
      const lines = fs.readFileSync(file, 'utf8').split('\n');
      lines.forEach((line, i) => {
        const m = line.match(/(?:const|let)\s*\{([^}]+)\}\s*=\s*responsive/);
        if (m) {
          const keys = m[1].split(',').map((s) => s.trim()).filter(Boolean);
          if (keys.includes('SHADOWS') || keys.includes('LIGHT_COLORS') || keys.includes('DARK_COLORS')) {
            offenders.push(`${file}:${i + 1}`);
          }
        }
      });
    }
    expect(offenders).toEqual([]);
  });
});
