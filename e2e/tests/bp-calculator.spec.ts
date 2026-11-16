import { expect, test } from '@playwright/test';

const cases = [
  { systolic: 140, diastolic: 90, name: 'High Blood Pressure', cssClass: 'bp-cat-high' },
  { systolic: 139, diastolic: 89, name: 'Pre-High Blood Pressure', cssClass: 'bp-cat-prehigh' },
  { systolic: 120, diastolic: 80, name: 'Ideal Blood Pressure', cssClass: 'bp-cat-ideal' },
  { systolic: 85, diastolic: 55, name: 'Low Blood Pressure', cssClass: 'bp-cat-low' },
  { systolic: 120, diastolic: 90, name: 'High Blood Pressure', cssClass: 'bp-cat-high' },
  { systolic: 120, diastolic: 81, name: 'Pre-High Blood Pressure', cssClass: 'bp-cat-prehigh' },
];

test.describe('BP Calculator', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/');
  });

  test('renders the calculator form', async ({ page }) => {
    await expect(page).toHaveTitle(/BP Calculator/);
    await expect(page.getByRole('heading', { level: 1 })).toHaveText(
      'Check your blood pressure category',
    );
    await expect(page.locator('#systolic')).toBeVisible();
    await expect(page.locator('#diastolic')).toBeVisible();
    await expect(page.getByRole('button', { name: 'Check Category' })).toBeVisible();
  });

  test('shows the updated category ranges', async ({ page }) => {
    const legend = page.locator('table');
    await expect(legend).toContainText('below 90');
    await expect(legend).toContainText('121 – 139');
    await expect(legend).toContainText('140 – 190');
  });

  for (const bp of cases) {
    test(`${bp.systolic}/${bp.diastolic} -> ${bp.name}`, async ({ page }) => {
      await page.locator('#systolic').fill(String(bp.systolic));
      await page.locator('#diastolic').fill(String(bp.diastolic));
      await page.getByRole('button', { name: 'Check Category' }).click();

      await expect(page.locator('.bp-result-card')).toHaveClass(new RegExp(bp.cssClass));
      await expect(page.locator('.bp-result-name')).toHaveText(bp.name);
    });
  }

  test('rejects out-of-range readings', async ({ page }) => {
    await page.locator('#systolic').fill('200');
    await page.locator('#diastolic').fill('110');
    await page.getByRole('button', { name: 'Check Category' }).click();

    await expect(page.locator('#systolic')).toHaveClass(/is-invalid/);
    await expect(page.locator('#diastolic')).toHaveClass(/is-invalid/);
    await expect(page.locator('.bp-result-card')).toHaveCount(0);
  });

  test('privacy page is reachable', async ({ page }) => {
    await page.getByRole('link', { name: 'Privacy' }).first().click();

    await expect(page).toHaveURL(/\/privacy$/);
    await expect(page.getByRole('heading', { level: 1 })).toHaveText('Privacy Policy');
  });

  test('health endpoint reports UP', async ({ request }) => {
    const response = await request.get('/actuator/health');

    expect(response.status()).toBe(200);
    expect(await response.json()).toMatchObject({ status: 'UP' });
  });
});
